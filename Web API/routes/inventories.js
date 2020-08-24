module.exports = function(router, database, authMiddleware){

    router.all('/inventory/:id', authMiddleware('Bearer'), (req, res) => {
        let id = req.params.id;
        if(id && typeof id === 'string'){
            if(req.method === 'GET'){
                database.fetchInventory({ _id: id }, inv => {
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
                res.status(500).json({ success: false, err: 'api_unavailable', err_description: 'API Server is unavailable' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad Request' });
            return;
        }

        res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
    });

    router.all('/inventories', authMiddleware('Bearer'), (req, res) => {
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