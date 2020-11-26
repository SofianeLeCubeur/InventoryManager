const { mutate, requireScope } = require('./../utils');
const { Content, Item, Error } = require('./../models');
const Webhook = require('./../webhooks');

module.exports = function(router, database, authMiddleware){

    const modifiableProps = {
        'name': function(s){
            return typeof s === 'string' && s.length >= 1 && s.length <= 22
        }, 
        'reference': function(s){
            return typeof s === 'string' && s.length >= 1
        },
        'serial_number': function(s){
            return typeof s === 'string' && s.length >= 1
        },
        'description': function(s){
            return typeof s === 'string' && s.length >= 0
        },
        'details': function(s){
            return typeof s === 'string' && s.length >= 0
        },
        'locations': function(s){
            return Array.isArray(s)
        },
        'tags': function(s){
            return Array.isArray(s)
        },
        'icon': function(s){
            if(typeof s !== 'string') return false;
            if(s.length == 0) return true;
            try {
                new URL(s);
                return true;
            } catch (_) {}
            return false;
        },
        'background': function(s){
            if(typeof s !== 'string') return false;
            if(s.length == 0) return true;
            try {
                new URL(s);
                return true;
            } catch (_) {}
            return false;
        },
        'state': function(s){
            console.log(typeof s, s.length);
            return typeof s === 'string' && s.length <= 16
        },
        'webhooks': function(s){
            if(!Array.isArray(s)) return false;
            let b = 0;
            s.forEach(wh => {
                if(typeof wh === 'object' && typeof wh.id === 'string' 
                && typeof wh.url === 'string' && wh.event === 'string'){
                    delete wh.last_delivery;
                    b++;
                }
            })
            return b === s.length;
        }
    };

    router.post('/item', authMiddleware('Bearer'), requireScope([ 'add.*', 'add.itm' ]), (req, res) => {
        let body = req.body;
        let keys = Object.keys(req.body);
        let props = {}, p = 0;
        Object.keys(modifiableProps).forEach(prop => {
            if(keys.indexOf(prop) >= 0 && modifiableProps[prop](body[prop])){
                props[prop] = body[prop];
                p++;
            }
        });

        if(p > 0 && !!props['name']){
            database.pushItem(props, result => {
                if(result){
                    res.status(200).json(Content(result));
                } else {
                    res.status(500).json(Error('internal_error', 'Could not push item'));
                }
            });
        } else {
            res.status(400).json(Error('bad_request', 'Fields are missing'));
        }
    });

    router.all('/item/:id', authMiddleware('Bearer'), (req, res) => {
        let token = res.locals.token;
        let id = req.params.id;
        if(id && typeof id === 'string'){
            const query = { _id: id };
            if(req.method === 'GET'){
                database.fetchItem(query, it => {
                    if(it != null){
                        res.json(Content(it));
                    } else {
                        res.status(404).json(Error('not_found', 'The provided ID does not refer to any item in the database.'))
                    }
                });
                return;
            } else if(req.method === 'POST' || req.method === 'UPDATE'){
                if(token.scope.indexOf('edit.*') >= 0 || token.scope.indexOf('edit.itm') >= 0){
                    const body = req.body;
                    let mutation = mutate(modifiableProps, body);

                    if(mutation.length == 0){
                        res.status(204).end();
                        return;
                    }
                    //console.log('mutation', mutation, 'from', body);

                    database.fetchItem(query, itm => {
                        if(itm != null){
                            let mutedItm = Object.assign({}, itm, mutation);
                            if(itm != mutedItm){
                                database.updateItem(query, mutedItm, cb => {
                                    if(cb){
                                        res.status(200).json(Content(mutedItm));
                                        Webhook.call(database, 'update', 'item', token.uid, mutedItm);
                                    } else {
                                        res.status(500).json(Error('internal_error', 'Could not update the item'));
                                    }
                                });
                            } else {
                                res.status(200).json(Content(mutedItm));
                            }
                        } else {
                            res.status(404).json(Error('not_found', 'The provided ID does not refer to any inventory in the database.'))
                        }
                    });
                } else {
                    res.status(403).json(Error('forbidden', 'Not Authorized'));
                }
            } else if(req.method === 'DELETE'){
                if(token.scope.indexOf('delete.*') >= 0 || token.scope.indexOf('delete.itm') >= 0){
                    database.deleteItem(query, (success) => {
                        if(success){
                            res.status(200).json(Message('Item successfully deleted', 'server'));
                        } else {
                            res.status(500).json(Error('internal_error', 'Could not delete the item'));
                        }
                    });
                } else {
                    res.status(403).json(Error('forbidden', 'Not Authorized'));
                }
            } else {
                res.status(405).json(Error('method_not_allowed', 'This request method is not allowed'));
            }
        } else {
            res.status(400).json(Error('bad_request', 'Bad Request'));
        }
    });

    router.all('/items/', authMiddleware('Bearer'), requireScope([ 'fetch.*', 'fetch.itm' ]), (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json(Error('method_not_allowed', 'This request method is not allowed'));
            return;
        }
        const body = req.body;
        let itemIds = body.itemIds;
        if(!itemIds){
            res.status(400).json(Error('bad_request', 'Missing itemIds field'));
            return;
        }
        let offset = Math.round(Math.abs(parseInt(body.offset)));
        if(!isFinite(offset)){
            offset = 0;
        }
        let length = Math.round(Math.abs(parseInt(body.length)));
        if(!isFinite(length)){
            length = 0;
        }
        database.fetchItems({ _id: { $in: itemIds } }, offset, length, docs => {
            if(docs !== null){
                let items = docs.map(Item);
                res.json(items);
            } else {
                res.status(404).json({ err: 'not_found', err_description: 'Find error: ' + docs });
            }
        });
    })

};