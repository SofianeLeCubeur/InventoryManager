const { generateToken } = require('./../utils');
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
        console.log(scope, scopes);
        for(let idx in scopes){
            let sc = scopes[idx][1];
            if(allowedScopes.indexOf(sc) >= 0){
                filteredScope += ' ' + sc;
            }
        }
        return filteredScope.substring(1);
    }

    router.all('/token', (req, res) => {
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
                        let token = { token: generateToken(), type: 'Bearer', uid: username, scope: scopes };
                        database.storeToken(token, result => {
                            if(result){
                                res.status(200).json({ success: true, ...token });
                            } else {
                                res.status(403).json({ success: false, err: 'forbidden', err_description: 'Could not acquire token' });
                            }
                        });
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