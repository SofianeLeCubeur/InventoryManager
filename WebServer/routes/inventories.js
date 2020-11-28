const { mutate, requireScope } = require('./../utils');
const { Content, Inventory, Error } = require('./../models');
const Webhook = require('./../webhooks');

module.exports = function(router, database, authMiddleware){

    const modifiableProps = {
        'name': function(s){
            return typeof s === 'string' && s.length >= 1 && s.length <= 22
        }, 
        'icon': function(s){
            if(typeof s !== 'string') return false;
            if(s.length == 0) return true;
            if(s.length > 4) return false;
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
        'location': function(s){
            return typeof s === 'string' && s.length >= 3 && s.length <= 40
        },
        'marker': function(s){
            return false; // DISABLED FOR NOW, Enable with latitude and longitude location control
        }, 
        'state': function(s){
            return typeof s === 'string' && s.length <= 16
        }, 
        'items': function(s){
            if(!Array.isArray(s)) return false;
            let b = 0;
            s.forEach(item => {
                if(typeof item === 'string') b++;
            })
            return b === s.length;
        },
        'webhooks': function(s){
            if(!Array.isArray(s)) return false;
            let b = 0;
            s = s.filter(wh => {
                return typeof wh === 'object' && typeof wh.id === 'string' 
                && typeof wh.url === 'string' && wh.event === 'string' }).map(wh => {
                    return { id: wh.id, url: wh.url, event: wh.event };
                })
            return b === s.length;
        }
    };

    router.post('/inventory', authMiddleware('Bearer'), requireScope([ 'add.* ', 'add.inv' ]), (req, res) => {
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
            database.pushInventory(props, result => {
                if(result){
                    res.status(200).json(Content(result));
                } else {
                    res.status(500).json(Error('internal_error', 'Could not push inventory'));
                }
            });
        } else {
            res.status(400).json(Error('bad_request', 'Fields are missing'));
        }
    });

    router.all('/inventory/:id', authMiddleware('Bearer'), (req, res) => {
        const token = res.locals.token;
        const id = req.params.id;
        const query = { _id: id };
        if(id && typeof id === 'string'){
            if(req.method === 'GET'){
                database.fetchInventory(query, inv => {
                    if(inv != null){
                        res.json(Content(inv));
                    } else {
                        res.status(404).json(Error('not_found', 'The provided ID does not refer to any inventory in the database.'))
                    }
                });
                return;
            } else if(req.method === 'POST' || req.method === 'UPDATE'){
                if(token.scope.indexOf('edit.*') >= 0 || token.scope.indexOf('edit.inv') >= 0){
                    const body = req.body;
                    let mutation = mutate(modifiableProps, body);

                    if(mutation.length == 0){
                        res.status(204).end();
                        return;
                    }

                    database.fetchInventory(query, inv => {
                        if(inv != null){
                            let mutedInv = Object.assign({}, inv, mutation);
                            if(inv != mutedInv){
                                database.updateInventory(query, mutedInv, cb => {
                                    if(cb){
                                        res.status(200).json(Content(mutedInv));
                                        Webhook.call(database, 'update', 'inventory', token.uid, mutedInv);
                                    } else {
                                        res.status(500).json(Error('internal_error', 'Could not update the inventory'));
                                    }
                                });
                            } else {
                                res.status(200).json(Content(mutedInv));
                            }
                        } else {
                            res.status(404).json(Error('not_found', 'The provided ID does not refer to any inventory in the database.'))
                        }
                    });
                } else {
                    res.status(403).json(Error('forbidden', 'Not Authorized'));
                }
            } else if(req.method === 'DELETE'){
                if(token.scope.indexOf('delete.*') >= 0 || token.scope.indexOf('delete.inv') >= 0){
                    Webhook.trigger('delete', token.uid, mutedInv);
                    database.deleteInventory(query, (success) => {
                        if(success){
                            res.status(200).json(Message('Inventory successfully deleted', 'server'));
                        } else {
                            res.status(500).json(Error('internal_error', 'Could not delete the inventory'));
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

    router.all('/inventories', authMiddleware('Bearer'), requireScope([ 'fetch.*', 'fetch.inv' ]), (req, res) => {
        if(req.method !== 'GET'){
            res.status(405).json(Error('method_not_allowed', 'This request method is not allowed'));
            return;
        }
        let body = req.query;
        let offset = Math.round(Math.abs(parseInt(body.offset)));
        if(!isFinite(offset)){
            offset = 0;
        }
        let length = Math.round(Math.abs(parseInt(body.length)));
        if(!isFinite(length)){
            length = 0;
        }
        database.fetchInventories({}, offset, length, docs => {
            if(docs != null){
                res.status(200).json(docs.map(Inventory));
            } else {
                res.status(500).json(Error('internal_error', 'Failed to query inventories'));
            }
        });
    });

};