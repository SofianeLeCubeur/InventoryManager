module.exports = function(router, database, authMiddleware){

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
                res.status(500).json({ success: false, err: 'api_unavailable', err_description: 'API Server is unavailable' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad Request' });
            return;
        }

        res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
    });

    router.all('/items/', authMiddleware('Bearer'), (req, res) => {
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