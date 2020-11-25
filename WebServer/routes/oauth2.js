const { generateToken } = require('./../utils');
const { User, Token, Error } = require('./../models');
const argon2 = require('argon2');
const rateLimit = require('express-rate-limit');
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

    const createAccountLimiter = rateLimit({
        windowMs: 60 * 60 * 1000,
        max: 4,
        message: 'Too many accounts created from this IP, please try again after an hour'
    });

    router.post('/user', createAccountLimiter, async (req, res) => {
        const body = req.body;
        if(typeof body.username === 'string' && typeof body.password === 'string'){
            if(body.username.length >= 2 && body.username.length <= 24){
                if(body.password.length >= 4 && body.password.length <= 128){
                    const hash = await argon2.hash(body.password);
                    database.fetchUser({ username: body.username }, (u) => {
                        if(u){
                            res.status(400).json(Error('bad_request', 'Username already taken'));
                            return;
                        }
                        database.insertUser({ username: body.username, password: hash }, (user) => {
                            if(user){
                                res.status(200).json(User(user));
                            } else {
                                res.status(500).json(Error('internal_error', 'Could not create user'));
                            }
                        });
                    });
                    
                } else {
                    res.status(400).json(Error('bad_request', 'Password must be at least 4 characters'));
                }
            } else {
                res.status(400).json(Error('bad_request', 'Username must be between 2 and 26 characters'));
            }
        } else {
            res.status(400).json(Error('bad_request', 'Missing username and password'));
        }
    });

    router.all('/user', authMiddleware('Bearer'), (req, res) => {
        let token = res.locals.token;
        database.fetchUser({ _id: token.uid.toString() }, (user) => {
            if(user){
                res.status(200).json(User(user));
            } else {
                res.status(500).json(Error('internal_error', 'Could not fetch user info'));
            }
        })
    });

    router.all('/token', async (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json(Error('method_not_allowed', 'This request method is not allowed'));
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
                                                res.status(200).json(Token(token));
                                            } else {
                                                res.status(500).json(Error('internal_error', 'Could not acquire token'));
                                            }
                                        });
                                    } else {
                                        res.status(403).json(Error('forbidden', 'Forbidden'));
                                    }
                                } else {
                                    res.status(403).json(Error('forbidden', 'Forbidden'));
                                }
                            });
                        } else {
                            res.status(400).json(Error('bad_request', 'Invalid Scope'))
                        }
                    } else {
                        res.status(400).json(Error('bad_request', 'Missing Scope'))
                    }
                } else {
                    res.status(400).json(Error('bad_request', 'Bad Request'))
                }
            } else {
                res.status(400).json(Error('bad_request', 'Invalid Grant Type'))
            }
        } else {
            res.status(400).json(Error('bad_request', 'Missing Grant Type'))
        }
    })

};