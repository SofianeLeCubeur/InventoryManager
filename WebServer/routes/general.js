const { escapeRegExp, requireScope } = require('./../utils');

module.exports = function(router, database, authMiddleware){

    function searchInvs(query, callback){
        database.fetchInventories(query, 0, 0, docs => {
            if(docs !== null){
                let items = docs.map(inv => {
                    return { id: inv._id, name: inv.name, icon: inv.icon, location: inv.location, state: inv.state, items: inv.items };
                });
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
        console.log('cnt', queryR);
        database.fetchContainers(query, 0, 0, docs => {
            console.log(docs);
            if(docs != null){
                let containers = docs.map(cnt => {
                    let location = cnt.locations.sort((a, b) => b-a)[0].location;
                    return { id: cnt._id, content: cnt.content, location, state: cnt.state };
                });
                callback(containers);
            }
        })
    }

    function searchItems(query, callback){
        database.fetchItems(query, 0, 0, docs => {
            if(docs !== null){
                let items = docs.map(it => {
                    return { id: it._id, name: it.name, description: it.description, reference: it.reference, serial_number: it.serial_number, icon: it.icon }
                });
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
            res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
            return;
        }
        let token = res.locals.token;
        let id = req.params.id;
        let device = req.body.device;
        let marker = req.body.marker && req.body.marker.lat && req.body.marker.lon ? req.body.marker : undefined;
        let location = req.body.location || req.ip;
        if(id && typeof id === 'string'){
            if(typeof device === 'string' && typeof location === 'string'){
                let type = parseInt(id.substring(0,2), 16);
                database.pushScanLog({ uid: token.uid, marker, device, location, timestamp: Date.now(), recipient: { id } }, result => {
                    if(result){
                        if(type == 0){
                            database.fetchInventory({ _id: id }, inv => {
                                if(inv != null){
                                    res.json({ success: true, type: 'inventory', id: inv._id, name: inv.name, icon: inv.icon, location: inv.location, state: inv.state, items: inv.items });
                                } else {
                                    res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any inventory in the database.' })
                                }
                            });
                        } else if(type == 1){
                            database.fetchItem({ _id: id }, it => {
                                if(it != null){
                                    res.json({ success: true, type: 'item', id: it._id, name: it.name, description: it.description, icon: it.icon });
                                } else {
                                    res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any item in the database.' })
                                }
                            });
                        } else if(type == 2){
                            database.fetchContainer({ _id: id }, cnt => {
                                if(cnt != null){
                                    let location = cnt.locations.sort((a, b) => b-a)[0].location;
                                    res.json({ success: true, type: 'container', id: cnt._id, content: cnt.content, location, state: cnt.state });
                                } else {
                                    res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any container in the database.' })
                                }
                            });
                        } else {
                            res.status(404).json({ success: false, err: 'not_found', err_description: 'The id provided does not have a valid key' });
                        }
                    } else {
                        res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not push scan log' });
                    }
                });
            } else {
                res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad parameters' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Missing id field' });
        }
    })

    router.all('/search', authMiddleware('Bearer'), (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
            return;
        }

        const body = req.body;
        let query;
        try {
            query = prepareParams(body);
        } catch(e){
            console.error('Could not prepare query params:', body, '->', e);
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad filters' });
            return;
        }

        let results = null;

        function validate(){
            res.json({ success: true, ...results });
        }

        if(Object.keys(query).length > 0 && !!body.type){
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
                res.status(404).json({ success: false, err: 'not_found', err_description: 'Query does not have any result' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'No filter was provided' });
        }
    })

};