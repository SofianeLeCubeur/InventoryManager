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

module.exports = class Database {

    constructor(config){
        MongoClient.connect('mongodb://' + config.mongodb.host + ':' + config.mongodb.port, 
        { auth: { user: config.mongodb.username, password: config.mongodb.password }, useUnifiedTopology: true }, (err, c) => {
            if(err){
                console.error('[DB] Could not connect to MongoDB');
            } else {
                console.log("[DB] Connected successfully to MongoDB");
                this.client = c;
                this.db = c.db(config.mongodb.databaseName);
                this.db.listCollections().toArray(function(err, cols){
                    if(!err){
                        let needed = ['containers', 'inventories', 'items', 'scan_log']
                        cols.forEach(col => {
                            needed.forEach((n,i) => {
                                if(col.name === n){
                                    needed.splice(i, 1);
                                }
                            })
                        })
                        if(needed.length == 0) return;
                    }
                    console.warn('[DB][MongoDB] Warning: Missing', needed.join(', '), 'collections: Rebuilding');
                }.bind(this))
            }
        });
    }

    insertItem(item, callback){
        const collection = this.db.collection('items');
        collection.insertOne(item, function(err, result) {
            if(!err && result.ops[0] === item && callback){
                callback(data);
            } else {
                callback(null);
            }
        });
    }

    insertContainer(container, callback){
        const collection = this.db.collection('containers');
        collection.insertOne(container, function(err, result) {
            if(!err && result.ops[0] === container && callback){
                callback(data);
            } else {
                callback(null);
            }
        });
    }

    insertInventory(inventory, callback){
        const collection = this.db.collection('inventories');
        collection.insertOne(inventory, function(err, result) {
            if(!err && result.ops[0] === inventory && callback){
                callback(data);
            } else {
                callback(null);
            }
        });
    }

    fetchItem(query, callback){
        const collection = this.db.collection('items');
        collection.findOne(query, function(err, result) {
            if(!err && result && assertQueryMatch(query, result)){
                callback(result);
            } else {
                callback(null);
            }
        });
    }

    fetchContainer(query, callback){
        const collection = this.db.collection('containers');
        collection.findOne(query, function(err, result) {
            if(!err && result && assertQueryMatch(query, result)){
                callback(result);
            } else {
                callback(null);
            }
        });
    }

    fetchInventory(query, callback){
        const collection = this.db.collection('inventories');
        collection.findOne(query, function(err, result) {
            if(!err && result && assertQueryMatch(query, result)){
                callback(result);
            } else {
                callback(null);
            }
        });
    }

    fetchItems(query, offset, length, callback){
        const collection = this.db.collection('items');
        collection.find(query)
            .limit(length).skip(offset).toArray((err, docs) => {
                if(!err){
                    callback(docs);
                } else {
                    callback(null);
                }
            });
    }
    
    fetchContainers(query, offset, length, callback){
        const collection = this.db.collection('containers');
        collection.find(query)
            .limit(length).skip(offset).toArray((err, docs) => {
                if(!err){
                    callback(docs);
                } else {
                    callback(null);
                }
            });
    }    

    fetchInventories(query, offset, length, callback){
        const collection = this.db.collection('inventories');
        collection.find(query)
            .limit(length).skip(offset).toArray((err, docs) => {
                if(!err){
                    callback(docs);
                } else {
                    callback(null);
                }
            });
    }
}