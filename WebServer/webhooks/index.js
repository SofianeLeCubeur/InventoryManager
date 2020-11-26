const needle = require('needle');
const { WebhookBody } = require('./../models');

module.exports = {
    deliver(url, content, callback){
        let body = { ...content };
        delete body.webhooks;
        needle.post(url, body, { json: true }, callback)
    },
    trigger(event, initiator, type, object, callback){
        if(object.webhooks && Array.isArray(object.webhooks)){
            let webhooks = object.webhooks.filter(wh => wh.event.startsWith(event));
            let completed = 0, failed = [], success = [];
            webhooks.forEach(wh => {
                this.deliver(wh.url, WebhookBody(event, type, initiator, object), (err) => {
                    completed++;
                    (err ? failed : success).push(wh.id);
                    if(completed == webhooks.length){
                        callback(completed, success, failed);
                    }
                });
            });
        }
    },
    call(database, event, type, uid, content){
        database.fetchUser({ _id: uid }, ((cb) => {
            if(cb){
                this.trigger(event, type, { username: cb.username, id: cb._id }, content, 
                    (completed, success, failed) => {
                        let webhooks = content.webhooks;
                        webhooks.forEach(wh => {
                            let status = 0;
                            if(success.indexOf(wh.id) >= 0){
                                status = 1;
                            } else if(failed.indexOf(wh.id) >= 0){
                                status = 2;
                            }
                            wh.last_delivery = { timestamp: Date.now(), status };
                        });
                        content.webhooks = webhooks;
                        console.log('[Express][Webhooks] Webhook delivered to', success, 'of', completed);
                        this.updateStatus(database, type, content);
                });
            } else {
                console.log('[Express][Webhooks] Failed to deliver webhook: could not retrieve user data');
            }
        }).bind(this))
    },
    updateStatus(database, type, content){
        let callback = cb => {
            if(!cb){
                console.log('[Express][Webhooks] Warning: Could not update the item webhook history');
            }
        };
        if(type === 'inventory'){
            database.updateInventory({ _id: content._id }, content, callback);
        } else if(type === 'container'){
            database.updateContainer({ _id: content._id }, content, callback);
        } else if(type === 'item'){
            database.updateItem({ _id: content._id }, content, callback);
        }
    }
}