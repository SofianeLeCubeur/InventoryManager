const crypto = require('crypto');
const { Error } = require('./models');
const colors = [ '#5326A1', '#FF9A76', '#76BBFF', '#ff414d', '#28df99' ];

module.exports = {
    escapeRegExp(s){
        return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    },
    generateToken(){
        return crypto.randomBytes(32).toString('base64').replace(/\//g, '.')
    },
    mutate(props, body){
        const propKeys = Object.keys(body).filter(k => !!props[k]);
        let mutation = {};
        for(let i in propKeys){
            let key = propKeys[i];
            let validator = props[key];
            let val = body[key];
            if(validator(val)){
                mutation[key] = val;
            }
        }
        return mutation;
    },
    generateId(key=0){
        return Number(key).toString(16).padStart(2, '0') + crypto.randomBytes(12).toString('hex').substring(2)
    },
    generateHash(){
        return crypto.randomBytes(12).toString('hex');
    },
    randomColor(){
        let idx = Math.floor(Math.random() * colors.length);
        return colors[idx];
    },
    requireScope(scopes){
        return function(req, res, next){
            let token = res.locals.token;
            if(token){
                let contains = false;
                scopes.forEach(scope => {
                    contains = contains || token.scope.indexOf(scope) >= 0
                });
                if(contains){
                    next();
                    return;
                }
            }
            res.status(403).json(Error('forbidden', 'Forbidden: missing scope in authorization'));
        }
    }
};