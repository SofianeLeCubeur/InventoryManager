const { generateToken } = require('./../utils');
const argon2 = require('argon2');
const allowedScopes = [
    'fetch.*', 'fetch.inv', 'fetch.itm', 'fetch.cnt', 
    'scan', 
    'add.inv', 'add.cnt', 'add.itm', 'add.*', 
    'edit.*', 'edit.inv', 'edit.cnt', 'edit.itm', 
    'delete.*', 'delete.inv', 'delete.cnt', 'delete.itm' ];

module.exports = function(router, database, authMiddleware){

    function filterScopes(scope, allowedScopes){
        let filteredScope = '';
        let scopes = [...scope.matchAll(/([a-zA-Z0-9.*]+)\s?/g)];
        for(let idx in scopes){
            let sc = scopes[idx][1];
            if(allowedScopes.indexOf(sc) >= 0){
                filteredScope += ' ' + sc;
            }
        }
        return filteredScope.substring(1);
    }

    async function assertPasswordEquals(input, target){
        try {
            let b = await argon2.verify(target, input);
            return b;
        } catch (err) {
            console.err('[Express] ERROR: Could not assert passwords: ' + err);
        }
        return false;
    }

    router.post('/user', async (req, res) => {
        const body = req.body;
        if(typeof body.username === 'string' && typeof body.password === 'string'){
            if(body.username.length >= 2 && body.username.length <= 24){
                if(body.password.length >= 4 && body.password.length <= 128){
                    const hash = await argon2.hash(body.password);
                    database.fetchUser({ username: body.username }, (u) => {
                        if(u){
                            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Username already taken' });
                            return;
                        }
                        database.insertUser({ username: body.username, password: hash }, (user) => {
                            if(user){
                                delete user.password;
                                user.id = user._id;
                                delete user._id;
                                res.status(200).json({ success: true, ...user });
                            } else {
                                res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not create user' });
                            }
                        });
                    });
                    
                } else {
                    res.status(400).json({ success: false, err: 'bad_request', err_description: 'Password must be at least 4 characters' });
                }
            } else {
                res.status(400).json({ success: false, err: 'bad_request', err_description: 'Username must be between 2 and 26 characters' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Missing username and password' });
        }
    });

    router.all('/user', authMiddleware('Bearer'), (req, res) => {
        let token = res.locals.token;
        database.fetchUser({ _id: token.uid.toString() }, (user) => {
            if(user){
                delete user.password;
                user.id = user._id;
                delete user._id;
                res.status(200).json({ success: true, ...user });
            } else {
                res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not fetch user info' });
            }
        })
    });

    router.all('/token', async (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
            return;
        }
        let grant_type = req.body.grant_type;
        let username = req.body.username;
        let password = req.body.password;
        let scope = req.body.scope;

        if(grant_type && typeof grant_type === 'string'){
            if(grant_type === 'password'){
                if(username && typeof username === 'string' && username.length >= 2 && username.length <= 24){
                    if(scope && typeof scope === 'string'){
                        let scopes = filterScopes(scope, allowedScopes);
                        if(scopes.length > 0){
                            database.fetchUser({ username: username }, (user) => {
                                if(user && user.password){
                                    let verified = assertPasswordEquals(password, user.password);
                                    if(verified){
                                        let token = { token: generateToken(), type: 'Bearer', uid: user._id, scope: scopes };
                                        database.storeToken(token, result => {
                                            if(result){
                                                res.status(200).json({ success: true, ...token });
                                            } else {
                                                res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not acquire token' });
                                            }
                                        });
                                    } else {
                                        res.status(403).json({ success: false, err: 'forbidden', err_description: 'Forbidden' });
                                    }
                                } else {
                                    res.status(403).json({ success: false, err: 'forbidden', err_description: 'Forbidden' });
                                }
                            });
                        } else {
                            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Invalid Scope' })
                        }
                    } else {
                        res.status(400).json({ success: false, err: 'bad_request', err_description: 'Missing Scope' })
                    }
                } else {
                    res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad Request' })
                }
            } else {
                res.status(400).json({ success: false, err: 'bad_request', err_description: 'Invalid Grant Type' })
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Missing Grant Type' })
        }
    })

};