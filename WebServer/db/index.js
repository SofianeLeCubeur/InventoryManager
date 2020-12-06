const MongoDatabase = require('./mongodb');
const RedisDatabase = require('./redis');
const { generateId, generateHash, randomColor } = require('./../utils');

module.exports = class Database {

    constructor(config){
        console.log('[DB] Initializing databases');
        this.mongodb = new MongoDatabase(config.mongodb, config.id);
        this.redis = new RedisDatabase(config.redis, config.id);
    }

    isConnected(){
        return !!this.mongodb.db;
    }
    
    insertUser(user, callback){
        const uid = generateHash();
        this.mongodb.insertOne('users', { _id: uid, group_id: uid, ...user }, (result, data) => result.ops[0].password === user.password, callback);
    }

    insertInventory(inventory, callback){
        this.mongodb.insertOne('inventories', inventory, (result, data) => result.ops[0] === inventory, callback);
    }

    insertContainer(container, callback){
        this.mongodb.insertOne('containers', container, (result, data) => result.ops[0] === container, callback);
    }

    insertItem(item, callback){
        this.mongodb.insertOne('items', item, (result, data) => result.ops[0] === item, callback);
    }
    
    storeToken(token, callback){
        /*this.mongodb.insertOne('tokens', { _id: token.token, type: token.type, uid: token.uid, scope: token.scope, timestamp }, 
            (result, data) => result.ops[0]._id === token.token, callback);*/
        this.redis.store('tokens', token.token, token, callback);
    }

    fetchUser(query, callback){
        this.mongodb.findOne('users', query, callback);
    }
    
    fetchInventory(query, callback){
        this.mongodb.findOne('inventories', query, callback);
    }

    fetchContainer(query, callback){
        this.mongodb.findOne('containers', query, callback);
    }

    fetchItem(query, callback){
        this.mongodb.findOne('items', query, callback);
    }

    fetchToken(token, callback){
        //this.mongodb.findOne('tokens', { _id: token }, callback);
        this.redis.fetch('tokens', token, (result) => {
            if(result){
                if(result.expire > Date.now()){
                    return callback(result);
                } else {
                    this.redis.del('tokens', token);
                }
            }
            callback(false);
        });
    }

    fetchInventories(query, offset, length, callback){
        this.mongodb.fetch('inventories', query, offset, length, callback);
    }

    fetchContainers(query, offset, length, callback){
        this.mongodb.fetch('containers', query, offset, length, callback);
    }    

    fetchItems(query, offset, length, callback){
        this.mongodb.fetch('items', query, offset, length, callback);
    }

    updateInventory(query, inv, callback){
        this.mongodb.updateOne('inventories', query, inv, callback);
    }

    updateContainer(query, cnt, callback){
        this.mongodb.updateOne('containers', query, cnt, callback);
    }

    updateItem(query, item, callback){
        this.mongodb.updateOne('items', query, item, callback);
    }

    updateUser(query, user, callback){
        this.mongodb.updateOne('users', query, user, callback);
    }

    pushInventory(inventory, callback){
        const id = generateId();
        if(!inventory['icon']){
            inventory['icon'] = inventory.name.substring(0,1).toUpperCase() + ':' + randomColor();
        }
        let inv = Object.assign({ _id: id, background: null, state: '', location: '', items: [], }, inventory);
        this.mongodb.insertOne('inventories', inv, (result, data) => result.ops[0]._id === id, callback);
    }
    
    pushContainer(container, callback){
        const id = generateId();
        let cnt = Object.assign({ _id: id, state: '', details: '', locations: [], items: [] }, container);
        this.mongodb.insertOne('containers', cnt, (result, data) => result.ops[0]._id === id, callback);
    }
    
    pushItem(item, callback){
        const id = generateId();
        if(!item['icon']){
            item['icon'] = item.name.substring(0,1).toUpperCase() + ':' + randomColor();
        }
        let itm = Object.assign({ _id: id, background: null, reference: '', serial_number: '', description: '', details: '', locations: [], tags: [], state: '' }, item);
        this.mongodb.insertOne('items', itm, (result, data) => result.ops[0]._id === id, callback);
    }

    pushScanLog(log, callback){
        this.mongodb.insertOne('scan_log', log, (result, data) => result.ops[0].uid === data.uid, callback);
    }

    deleteInventory(id, callback){
        this.mongodb.deleteOne('inventories', { _id: id }, callback);
    }

    deleteContainer(id, callback){
        this.mongodb.deleteOne('containers', { _id: id }, callback);
    }

    deleteItem(id, callback){
        this.mongodb.deleteOne('items', { _id: id }, callback);
    }
}