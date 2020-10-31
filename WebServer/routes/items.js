const { mutate, requireScope } = require('./../utils');

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
                    result.id = result._id;
                    delete result._id;
                    res.status(200).json({ sucess: true, ...result });
                } else {
                    res.status(500).json({ sucess: false, error: 'internal_error', err_description: 'Could not push item' });
                }
            });
        } else {
            res.status(400).json({  sucess: false, error: 'bad_request', err_description: 'Fields are missing' });
        }
    });

    router.all('/item/:id', authMiddleware('Bearer'), (req, res) => {
        let id = req.params.id;
        if(id && typeof id === 'string'){
            if(req.method === 'GET'){
                database.fetchItem({ _id: id }, it => {
                    if(it != null){
                        it.id = it._id;
                        delete it._id;
                        res.json({ success: true, ...it });
                    } else {
                        res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any item in the database.' })
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
                console.log('mutation', mutation, 'from', body);

                database.fetchItem(query, itm => {
                    if(itm != null){
                        let mutedItm = Object.assign({}, itm, mutation);
                        if(itm != mutedItm){
                            database.updateItem(query, mutedItm, cb => {
                                if(cb){
                                    mutedItm.id = mutedItm._id;
                                    delete mutedItm._id;
                                    res.status(200).json(mutedItm);
                                } else {
                                    res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not update the item' });
                                }
                            });
                        } else {
                            res.status(200).json(mutedItm);
                        }
                    } else {
                        res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any inventory in the database.' })
                    }
                });

                res.status(500).json({ success: false, err: 'api_unavailable', err_description: 'API Server is unavailable' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad Request' });
            return;
        }

        res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
    });

    router.all('/items/', authMiddleware('Bearer'), requireScope([ 'fetch.*', 'fetch.itm' ]), (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
            return;
        }
        const body = req.body;
        let itemIds = body.itemIds;
        if(!itemIds){
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Missing itemIds field' });
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
                let items = docs.map(it => {
                    return { id: it._id, name: it.name, description: it.description, reference: it.reference, serial_number: it.serial_number, icon: it.icon }
                });
                res.json(items);
            } else {
                res.status(404).json({ err: 'find_error', err_description: 'Find error: ' + docs });
            }
        });
    })

};