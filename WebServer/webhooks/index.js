const needle = require('needle');
const { WebhookBody } = require('./../models');

module.exports = {
    send(url, content){
        delete content.webhooks;
        needle('post', url, content, { json: true }, function(err, resp) {
            if (!err) {
                console.log(resp.body);
            }
 
            if (err) {
                console.log('neddle error');
            }
        })
    },
    trigger(event, initiator, object){
        if(object.webhooks && Array.isArray(object.webhooks)){
            let webhooks = object.webhooks.filter(wh => wh.event_type === event);
            webhooks.forEach(wh => {
                this.send(wh.url, WebhookBody(initiator, object));
            });
        }
    }
}