const needle = require('needle');
const { WebhookBody } = require('./../models');

module.exports = {
    deliver(url, content, callback){
        delete content.webhooks;
        needle.post(url, content, { json: true }, callback)
    },
    trigger(event, initiator, type, object, callback){
        if(object.webhooks && Array.isArray(object.webhooks)){
            let webhooks = object.webhooks.filter(wh => wh.event === event);
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
    }
}