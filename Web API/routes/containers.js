module.exports = function(router, database, authMiddleware){

    router.all('/container/:id', authMiddleware('Bearer'), (req, res) => {
        let id = req.params.id;
        if(id && typeof id === 'string'){
            if(req.method === 'GET'){
                database.fetchContainer({ _id: id }, cnt => {
                    if(cnt != null){
                        cnt.id = cnt._id;
                        delete cnt._id;
                        res.json(cnt);
                    } else {
                        res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any container in the database.' })
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

    router.all('/containers', authMiddleware('Bearer'), (req, res) => {
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
        database.fetchContainers({  }, offset, length, docs => {
            if(docs != null){
                let containers = docs.map(cnt => {
                    let location = cnt.locations.sort((a, b) => b-a)[0].location;
                    return { id: cnt._id, content: cnt.content, location, state: cnt.state };
                });
                res.json(containers);
            } else {
                res.status(500).json({ success: false, err: 'internal_error', err_description: 'Failed to query containers' });
            }
        });
    });

};