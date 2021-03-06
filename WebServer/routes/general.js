const { escapeRegExp, requireScope } = require('./../utils');
const { Content, Inventory, Container, Item, SmallItem, Error } = require('./../models');

module.exports = function(router, database, authMiddleware){

    function searchInvs(query, callback){
        database.fetchInventories(query, 0, 0, docs => {
            if(docs !== null){
                let items = docs.map(Inventory);
                callback(items);
            }
        })
    }

    function searchCnts(query, callback){
        let queryR = { ...query };
        if(queryR['name']){
            queryR['content'] = queryR['name'];
            delete queryR['name'];
        }
        database.fetchContainers(queryR, 0, 0, docs => {
            if(docs != null){
                let containers = docs.map(Container);
                callback(containers);
            }
        })
    }

    function searchItems(query, callback){
        database.fetchItems(query, 0, 0, docs => {
            if(docs !== null){
                let items = docs.map(Item);
                callback(items);
            }
        });
    }

    function prepareParams(query){
        let parameters = {
            'id': {
                key: '_id',
                val: function(s){
                    return new RegExp('^' + escapeRegExp(s));
                }
            },
            'name': {
                key: 'name',
                val: function(s){
                    return new RegExp(s);
                }
            },
            'ref': {
                key: 'reference',
                val: function(s){
                    return new RegExp(escapeRegExp(s));
                }
            },
            'sn': {
                key: 'serial_number',
                val: function(s){
                    return new RegExp('^' + escapeRegExp(s));
                }
            },
            'state': {
                key: 'state',
                val: function(s){
                    return new RegExp(escapeRegExp(s));
                }
            },
            'desc': {
                key: 'description',
                val: function(s){
                    return new RegExp(escapeRegExp(s));
                }
            },
            'desc': {
                key: 'description',
                val: function(s){
                    return new RegExp(escapeRegExp(s));
                }
            },
            'details': {
                key: 'details',
                val: function(s){
                    return new RegExp(escapeRegExp(s));
                }
            }
        };
        let out = {};
        Object.keys(parameters).forEach(param => {
            let l = parameters[param];
            if(query[param]){
                out[l.key] = l.val(query[param]);
            }
        })
        return out;
    }

    router.all('/scan/:id', authMiddleware('Bearer'), requireScope([ 'scan' ]), (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json(Error('method_not_allowed', 'This request method is not allowed'));
            return;
        }
        const token = res.locals.token;
        const id = req.params.id;
        let device = req.body.device;
        let marker = req.body.marker && req.body.marker.lat && req.body.marker.lon ? req.body.marker : undefined;
        let location = req.body.location || req.ip;
        if(id && typeof id === 'string'){
            if(typeof device === 'string' && typeof location === 'string'){
                let type = parseInt(id.substring(0,2), 16);
                database.fetchUser({ _id: token.uid }, (user) => {
                    if(user){
                        delete user.password;
                        const query = { _id: id, owner: user.group_id };
                        database.pushScanLog({ uid: token.uid, marker, device, location, timestamp: Date.now(), recipient: { id } }, result => {
                            if(result){
                                if(type == 0){
                                    database.fetchInventory(query, inv => {
                                        if(inv != null){
                                            res.json({ type: 'inventory', ...Inventory(inv) });
                                        } else {
                                            res.status(404).json(Error('not_found', 'The provided ID does not refer to any inventory in the database.'))
                                        }
                                    });
                                } else if(type == 1){
                                    database.fetchItem(query, it => {
                                        if(it != null){
                                            res.json({ type: 'item', ...SmallItem(it)  });
                                        } else {
                                            res.status(404).json(Error('not_found', 'The provided ID does not refer to any item in the database.'))
                                        }
                                    });
                                } else if(type == 2){
                                    database.fetchContainer(query, cnt => {
                                        if(cnt != null){
                                            res.json({ type: 'container', ...Container(cnt) });
                                        } else {
                                            res.status(404).json(Errro('not_found', 'The provided ID does not refer to any container in the database.'))
                                        }
                                    });
                                } else {
                                    res.status(404).json(Error('not_found', 'The id provided does not have a valid key'));
                                }
                            } else {
                                res.status(500).json(Error('internal_error', 'Could not push scan log'));
                            }
                        });
                    } else {
                        res.status(403).json(Error('forbidden', 'Invalid Session'));
                    }
                });
            } else {
                res.status(400).json(Error('bad_request', 'Bad request'));
            }
        } else {
            res.status(400).json(Error('bad_request', 'Bad request'));
        }
    })

    router.all('/search', authMiddleware('Bearer'), (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json(Error('method_not_allowed', 'This request method is not allowed'));
            return;
        }

        const token = res.locals.token;
        const body = req.body;
        let query;
        try {
            query = prepareParams(body);
        } catch(e){
            console.error('Could not prepare query params:', body, '->', e);
            res.status(400).json(Error('bad_request', 'Bad filters'));
            return;
        }

        let results = null;

        function validate(){
            res.json({ success: true, ...results });
        }

        if(Object.keys(query).length > 0 && !!body.type){
            database.fetchUser({ _id: token.uid }, (user) => {
                if(user){
                    delete user.password;
                    query.owner = user.group_id;
                    switch(body.type){
                        case '*':
                        case 'all':
                            results = {};
                            searchInvs(query, invs => {
                                results.inventories = invs;
            
                                searchItems(query, items => {
                                    results.items = items;
            
                                    searchCnts(query, cnts => {
                                        results.containers = cnts;
                                        validate();
                                    });
                                });
                            });
                            break;
                        case 'inventories':
                            results = {};
                            searchInvs(query, cb => {
                                results.inventories = cb;
                                validate();
                            });
                            break;
                        case 'items':
                            results = {};
                            searchItems(query, cb => {
                                results.items = cb;
                                validate();
                            });
                            break;
                        case 'containers':
                            results = {};
                            searchCnts(query, cb => {
                                results.containers = cb;
                                validate();
                            });
                            break;
                    }
            
                    if(!results){
                        res.status(404).json(Error('not_found', 'Query does not have any result'));
                    }
                } else {
                    res.status(403).json(Error('forbidden', 'Invalid Session'));
                }
            });
        } else {
            res.status(400).json(Error('bad_request', 'No filter was provided'));
        }
    })

}