const MongoClient = require('mongodb').MongoClient;
const { generateId, randomColor } = require('./../utils');

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
                        let needed = ['containers', 'inventories', 'items', 'scan_log', 'tokens']
                        cols.forEach(col => {
                            needed.forEach((n,i) => {
                                if(col.name === n){
                                    needed.splice(i, 1);
                                }
                            })
                        })
                        if(needed.length == 0) return;
                        console.warn('[DB][MongoDB] Warning: Missing', needed.join(', '), 'collections!');
                    }
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

    pushItem(item, callback){
        const id = generateId();
        const collection = this.db.collection('items');
        if(!item['icon']){
            item['icon'] = item.name.substring(0,1).toUpperCase() + ':' + randomColor();
        }
        let itm = Object.assign({ _id: id, background: null, reference: '', serial_number: '', description: '', details: '', locations: [], tags: [], state: '' }, item);
        collection.insertOne(itm, function(err, result) {
            if(!err && result.ops[0]._id === id && callback){
                callback(result.ops[0]);
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
    
    updateContainer(query, cnt, callback){
        const collection = this.db.collection('containers');
        collection.updateOne(query, { $set: cnt }, function(err, result) {
            if(!err){
                callback(true);
            } else {
                callback(false);
            }
        });
    }

    pushContainer(container, callback){
        const id = generateId();
        const collection = this.db.collection('containers');
        let cnt = Object.assign({ _id: id, state: '', details: '', locations: [], items: [] }, container);
        collection.insertOne(cnt, function(err, result) {
            if(!err && result.ops[0]._id === id && callback){
                callback(result.ops[0]);
            } else {
                callback(null);
            }
        });
    }

    pushInventory(inventory, callback){
        const id = generateId();
        const collection = this.db.collection('inventories');
        if(!inventory['icon']){
            inventory['icon'] = inventory.name.substring(0,1).toUpperCase() + ':' + randomColor();
        }
        let inv = Object.assign({ _id: id, background: null, state: '', location: '', items: [], }, inventory);
        collection.insertOne(inv, function(err, result) {
            if(!err && result.ops[0]._id === id && callback){
                callback(result.ops[0]);
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
    
    updateInventory(query, inv, callback){
        const collection = this.db.collection('inventories');
        collection.updateOne(query, { $set: inv }, function(err, result) {
            if(!err){
                callback(true);
            } else {
                callback(false);
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

    pushScanLog(log, callback){
        console.log('pushScanLog->', log);
        callback(true);
    }

    storeToken(token, callback){
        const collection = this.db.collection('tokens');
        collection.insertOne({ _id: token.token, type: token.type, uid: token.uid, scope: token.scope }, function(err, result) {
            if(!err && result.ops[0]._id === token.token && callback){
                callback(true);
            } else {
                callback(null);
            }
        });
    }

    getToken(token, callback){
        const collection = this.db.collection('tokens');
        collection.findOne({ _id: token }, function(err, result) {
            if(!err && result){
                callback(result);
            } else {
                callback(null);
            }
        });
    }
}