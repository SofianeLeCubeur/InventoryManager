const MongoClient = require('mongodb').MongoClient;

function assertQueryMatch(query, match){
    let valid = true;
    Object.keys(query).forEach(targetKey => {
        let targetValue = query[targetKey];
        if(match[targetKey] !== targetValue){
            valid = false;
        }
    });
    return valid;
}

module.exports = class MongoDatabase {

    constructor(config, id){
        MongoClient.connect('mongodb://' + config.host + ':' + config.port, 
        { auth: { user: config.username, password: config.password }, useUnifiedTopology: true, appname: id }, (err, c) => {
            if(err){
                console.error('[DB][MongoDB] Could not connect:', err);
            } else {
                console.log('[DB][MongoDB] Connected successfully to MongoDB');
                this.client = c;
                this.db = c.db(config.databaseName);
                this.db.listCollections().toArray(function(err, cols){
                    if(!err){
                        let needed = ['users', 'inventories', 'containers', 'items', 'scan_log']
                        cols.forEach(col => {
                            needed.forEach((n,i) => {
                                if(col.name === n){
                                    needed.splice(i, 1);
                                }
                            })
                        })
                        if(needed.length == 0) return;
                        if(config.rebuild || config.rebuild === undefined){
                            console.warn('[DB][MongoDB] Warning: Missing', needed.join(', '), 'collections! Trying to rebuild...');
                            needed.forEach(col => {
                                this.db.createCollection(col, function(err, result) {
                                    if (err){
                                        console.error('[DB][MongoDB] Could not create', col, ':', err);
                                    } else {
                                        console.log('[DB][MongoDB] Collection', col, 'successfully created');
                                    }
                                });
                            });
                        } else {
                            console.warn('[DB][MongoDB] Warning: Missing', needed.join(', '), 'collections!');
                        }
                    }
                }.bind(this))
            }
        });
    }

    /* Actions */
    fetch(col, query, offset, length, callback){
        const collection = this.db.collection(col);
        collection.find(query)
            .limit(length).skip(offset).toArray((err, docs) => {
                callback(err ? null : docs);
            });
    }

    findOne(col, query, callback){
        const collection = this.db.collection(col);
        collection.findOne(query, function(err, result) {
            if(!err && result && assertQueryMatch(query, result)){
                callback(result);
            } else {
                callback(null);
            }
        });
    }

    insertOne(col, data, validator, callback){
        const collection = this.db.collection(col);
        collection.insertOne(data, function(err, result) {
            if(!err && validator(result, data) && callback){
                callback(result.ops[0]);
            } else {
                callback(null);
            }
        });
    }

    updateOne(col, query, data, callback){
        const collection = this.db.collection(col);
        collection.updateOne(query, { $set: data }, function(err, result) {
            callback(!err);
        });
    }

    deleteOne(col, query, callback){
        const collection = this.db.collection(col);
        collection.deleteOne(query, function(err, result){
            callback(!err);
        })
    }
}