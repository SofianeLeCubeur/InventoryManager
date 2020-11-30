const MongoClient = require('mongodb').MongoClient;
const { generateId, generateHash, randomColor } = require('./../utils');

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
        MongoClient.connect('mongodb://' + config.host + ':' + config.port, 
        { auth: { user: config.username, password: config.password }, useUnifiedTopology: true, appname: 'im-api-server' }, (err, c) => {
            if(err){
                console.error('[DB] Could not connect to MongoDB');
            } else {
                console.log("[DB] Connected successfully to MongoDB");
                this.client = c;
                this.db = c.db(config.databaseName);
                this.db.listCollections().toArray(function(err, cols){
                    if(!err){
                        let needed = ['users', 'inventories', 'containers', 'items', 'scan_log', 'tokens']
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

    isConnected(){
        return !!this.db;
    }
    
    insertUser(user, callback){
        const uid = generateHash();
        this.insertOne('users', { _id: uid, group_id: uid, ...user }, (result, data) => result.ops[0].password === user.password, callback);
    }

    insertInventory(inventory, callback){
        this.insertOne('inventories', inventory, (result, data) => result.ops[0] === inventory, callback);
    }

    insertContainer(container, callback){
        this.insertOne('containers', container, (result, data) => result.ops[0] === container, callback);
    }

    insertItem(item, callback){
        this.insertOne('items', item, (result, data) => result.ops[0] === item, callback);
    }
    
    storeToken(token, callback){
        let timestamp = Date.now(); 
        this.insertOne('tokens', { _id: token.token, type: token.type, uid: token.uid, scope: token.scope, timestamp }, 
            (result, data) => result.ops[0]._id === token.token, callback);
    }

    fetchUser(query, callback){
        this.findOne('users', query, callback);
    }
    
    fetchInventory(query, callback){
        this.findOne('inventories', query, callback);
    }

    fetchContainer(query, callback){
        this.findOne('containers', query, callback);
    }

    fetchItem(query, callback){
        this.findOne('items', query, callback);
    }

    fetchToken(token, callback){
        this.findOne('tokens', { _id: token }, callback);
    }

    fetchInventories(query, offset, length, callback){
        this.fetch('inventories', query, offset, length, callback);
    }

    fetchContainers(query, offset, length, callback){
        this.fetch('containers', query, offset, length, callback);
    }    

    fetchItems(query, offset, length, callback){
        this.fetch('items', query, offset, length, callback);
    }

    updateInventory(query, inv, callback){
        this.updateOne('inventories', query, inv, callback);
    }

    updateContainer(query, cnt, callback){
        this.updateOne('containers', query, cnt, callback);
    }

    updateItem(query, item, callback){
        this.updateOne('items', query, item, callback);
    }

    updateUser(query, user, callback){
        this.updateOne('users', query, user, callback);
    }

    pushInventory(inventory, callback){
        const id = generateId();
        if(!inventory['icon']){
            inventory['icon'] = inventory.name.substring(0,1).toUpperCase() + ':' + randomColor();
        }
        let inv = Object.assign({ _id: id, background: null, state: '', location: '', items: [], }, inventory);
        this.insertOne('inventories', inv, (result, data) => result.ops[0]._id === id, callback);
    }
    
    pushContainer(container, callback){
        const id = generateId();
        let cnt = Object.assign({ _id: id, state: '', details: '', locations: [], items: [] }, container);
        this.insertOne('containers', cnt, (result, data) => result.ops[0]._id === id, callback);
    }
    
    pushItem(item, callback){
        const id = generateId();
        if(!item['icon']){
            item['icon'] = item.name.substring(0,1).toUpperCase() + ':' + randomColor();
        }
        let itm = Object.assign({ _id: id, background: null, reference: '', serial_number: '', description: '', details: '', locations: [], tags: [], state: '' }, item);
        this.insertOne('items', itm, (result, data) => result.ops[0]._id === id, callback);
    }

    pushScanLog(log, callback){
        this.insertOne('scan_log', log, (result, data) => result.ops[0].uid === data.uid, callback);
    }

    deleteInventory(id, callback){
        this.deleteOne('inventories', { _id: id }, callback);
    }

    deleteContainer(id, callback){
        this.deleteOne('containers', { _id: id }, callback);
    }

    deleteItem(id, callback){
        this.deleteOne('items', { _id: id }, callback);
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