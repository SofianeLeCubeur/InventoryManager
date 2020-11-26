const needle = require('needle');
const { WebhookBody } = require('./../models');

module.exports = {
    deliver(url, content, callback){
        delete content.webhooks;
        needle.post(url, content, { json: true }, callback)
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
                    console.log('[Express][Webhooks] Webhook delivery done for ', success, 'of', completed);
                });
            } else {
                console.log('[Express][Webhooks] Failed to deliver webhook: could not retrieve user data');
            }
        }).bind(this))
    }
}