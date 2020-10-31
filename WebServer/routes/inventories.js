const { mutate, requireScope } = require('./../utils');

module.exports = function(router, database, authMiddleware){

    const modifiableProps = {
        'name': function(s){
            return typeof s === 'string' && s.length >= 1 && s.length <= 22
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
            let b = 0;
            s.forEach(item => {
                if(typeof item === 'string') b++;
            })
            return Array.isArray(s) && b === s.length;
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
                    result.id = result._id;
                    delete result._id;
                    res.status(200).json({ sucess: true, ...result });
                } else {
                    res.status(500).json({ sucess: false, error: 'internal_error', err_description: 'Could not push inventory' });
                }
            });
        } else {
            res.status(400).json({  sucess: false, error: 'bad_request', err_description: 'Fields are missing' });
        }
    });

    router.all('/inventory/:id', authMiddleware('Bearer'), (req, res) => {
        const id = req.params.id;
        const query = { _id: id };
        if(id && typeof id === 'string'){
            if(req.method === 'GET'){
                database.fetchInventory(query, inv => {
                    if(inv != null){
                        inv.id = inv._id;
                        delete inv._id;
                        res.json({ success: true, ...inv });
                    } else {
                        res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any inventory in the database.' })
                    }
                });
                return;
            } else if(req.method === 'POST' || req.method === 'UPDATE'){
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
                                    mutedInv.id = mutedInv._id;
                                    delete mutedInv._id;
                                    res.status(200).json(mutedInv);
                                } else {
                                    res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not update the inventory' });
                                }
                            });
                        } else {
                            res.status(200).json(mutedInv);
                        }
                    } else {
                        res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any inventory in the database.' })
                    }
                });
            } else if(req.method === 'DELETE'){
                if(token.scope.indexOf('delete.*') >= 0 || token.scope.indexOf('delete.inv') >= 0){
                    database.deleteInventory(query, (success) => {
                        if(success){
                            res.status(200).json({ success: true, message: 'Inventory successfully deleted' });
                        } else {
                            res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not delete the inventory' });
                        }
                    });
                } else {
                    res.status(403).json({ success: false, err: 'forbidden', err_description: 'Not Authorized' });
                }
            } else {
                res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad Request' });
        }
    });

    router.all('/inventories', authMiddleware('Bearer'), requireScope([ 'fetch.*', 'fetch.inv' ]), (req, res) => {
        if(req.method !== 'GET'){
            res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
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
                res.status(200).json(docs.map(inv => {
                    inv.id = inv._id;
                    delete inv._id;
                    return { id: inv.id, name: inv.name, icon: inv.icon, location: inv.location, state: inv.state, items: inv.items };
                }));
            } else {
                res.status(500).json({ success: false, err: 'internal_error', err_description: 'Failed to query inventories' });
            }
        });
    });

};