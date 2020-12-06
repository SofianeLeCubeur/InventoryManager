const redis = require("redis");

module.exports = class RedisDatabase {

    constructor(config, id){
        const password = config.auth || undefined;
        delete config.auth;
        const client = redis.createClient('redis://' + config.host + ':' + config.port, { password });
        client.on("error", (error) => {
            console.error('[DB][Redis]', error);
        });
        client.on('connect', function(err){
            if(err){
                console.log('[DB][Redis] Failed to connect:', err);
            } else {
                console.log('[DB][Redis] Successfully connected');
                client.send_command('CLIENT', ['SETNAME', id]);
            }
        });
        this.client = client;
    }

    store(col, key, val, callback){
        this.client.hmset(col, key, JSON.stringify(val), (err, value) => {
            callback(value);
        });
    }

    fetch(col, key, callback){
        this.client.hget(col, key, (err, value) => {
            if(err){
                return callback(undefined);
            }
            try {
                callback(JSON.parse(value));
            } catch(e){
                console.err('[DB][Redis] Failed to fetch data:', e);
            }
        })
    }

    del(col, key, callback){
        this.client.hdel(col, key, callback);
    }

}