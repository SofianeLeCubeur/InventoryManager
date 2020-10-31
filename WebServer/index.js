const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const Database = require('./db/mongodb');
const path = require('path');
const { escapeRegExp } = require('./utils');

let database = new Database(require('./config.json'));

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

function validateAccessToken(str){
    return true
}

function authMiddleware(authorization_type){
    return function(req, res, next){
        let header = req.headers.authorization;
        let validator;
        let error = () => res.status(401).json({ success: false, err: 'unauthorized', err_description: 'Unauthorized' });
        if(header && header.indexOf(authorization_type) === 0){
            validator = new RegExp(`(${escapeRegExp(authorization_type)})\\s([A-Za-z0-9+.=]+)`);
            if(validator.test(header)){
                const stk = header.substring(authorization_type.length+1);
                if(validateAccessToken(stk)){
                    database.fetchToken(stk, token => {
                        if(token && token.type === authorization_type){
                            res.locals.authorization_type = authorization_type;
                            res.locals.token = token;
                            next();
                        } else error();
                    });
                } else error();
            } else error();
        } else error();
    }
}

app.use('/', (req, res, next) => {
    res.set('Access-Control-Allow-Origin', '*')
    res.set('Access-Control-Allow-Headers', '*')
    res.set('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS')
    if (req.method === 'OPTIONS') {
        res.sendStatus(204);
        return;
    }
    console.log('[Express]', req.method, req.originalUrl);
    next();
})

let router = express.Router()

app.use(function(req, res, next){
    if(!database.isConnected()){
        res.sendFile(path.resolve('./errors/database_not_connected.html'));
        return;
    }
    next();
});

require('./routes/oauth2')(router, database, authMiddleware)
require('./routes/inventories')(router, database, authMiddleware)
require('./routes/containers')(router, database, authMiddleware)
require('./routes/items')(router, database, authMiddleware)
require('./routes/general')(router, database, authMiddleware)

app.use(router);
app.use(function(req, res) {
    res.sendFile(path.resolve('./errors/404.html'));
});

let server = app.listen(80, () => {
    console.log('[Express] Listening on localhost:80');
})

