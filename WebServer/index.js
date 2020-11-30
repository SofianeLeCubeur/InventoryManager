const express = require('express');
const Fingerprint = require('express-fingerprint');
const bodyParser = require('body-parser');
const Database = require('./db/mongodb');
const app = express();

const path = require('path');
const { Error } = require('./models');
const { escapeRegExp } = require('./utils');

const config = require('./config.json');
if(!config){
    console.error('No configuration provided. A config is required to start the server properly.');
    process.exit(1);
}
const expressConfig = config.express;
if(!expressConfig.cors){
    expressConfig.cors = {};
}

const database = new Database(config.mongodb);

app.enable('trust proxy');
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(Fingerprint({ parameters: [ Fingerprint.useragent ] }))

function authMiddleware(authorization_type){
    return function(req, res, next){
        let header = req.headers.authorization;
        let validator;
        let error = () => res.status(401).json(Error('unauthorized', 'Unauthorized'));
        if(header && header.indexOf(authorization_type) === 0){
            validator = new RegExp(`(${escapeRegExp(authorization_type)})\\s([A-Za-z0-9+.=]+)`);
            if(validator.test(header)){
                const stk = header.substring(authorization_type.length+1);
                database.fetchToken(stk, token => {
                    if(token && token.type === authorization_type){
                        res.locals.authorization_type = authorization_type;
                        res.locals.token = token;
                        next();
                    } else error();
                });
            } else error();
        } else error();
    }
}

const history = [];
app.use('/', (req, res, next) => {
    res.set('Access-Control-Allow-Origin', expressConfig.cors.origin || '*')
    res.set('Access-Control-Allow-Headers', expressConfig.cors.headers || '*')
    res.set('Access-Control-Allow-Methods', expressConfig.cors.methods || 'GET,PUT,POST,DELETE,OPTIONS')
    if (req.method === 'OPTIONS') {
        res.sendStatus(204);
        return;
    }
    const fingerprint = req.fingerprint.hash;
    console.log('[Express]', 'F{' + fingerprint + '}', req.method, req.originalUrl);
    if(!history[fingerprint]){
        history[fingerprint] = req.fingerprint;
        console.log('[Express] New device detected on the network: ', JSON.stringify(req.fingerprint));
    }
    next();
})

let router = express.Router()

app.use(function(req, res, next){
    if(!database.isConnected()){
        res.status(500).sendFile(path.resolve('./errors/500.html'));
        return;
    }
    next();
});

require('./routes/oauth2')(router, database, authMiddleware)
require('./routes/inventories')(router, database, authMiddleware)
require('./routes/containers')(router, database, authMiddleware)
require('./routes/items')(router, database, authMiddleware)
require('./routes/general')(router, database, authMiddleware)

app.use(expressConfig.baseUrl, router);
app.use(function(req, res) {
    res.sendFile(path.resolve('./errors/404.html'));
});

let server = app.listen(80, () => {
    console.log(`[Express] Listening on port 80`);
})

