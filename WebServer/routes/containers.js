const { mutate, requireScope } = require('./../utils');

module.exports = function(router, database, authMiddleware){

    const modifiableProps = {
        'content': function(s){
            return typeof s === 'string' && s.length >= 1 && s.length <= 22
        },
        'state': function(s){
            return typeof s === 'string' && s.length <= 16
        },
        'details': function(s){
            return typeof s === 'string' && s.length >= 0
        },
        'locations': function(s){
            return Array.isArray(s)
        },
        'items': function(s){
            let b = 0;
            s.forEach(item => {
                if(typeof item === 'string') b++;
            })
            return Array.isArray(s) && b === s.length;
        }
    };

    router.post('/container', authMiddleware('Bearer'), requireScope([ 'add.*', 'add.cnt' ]), (req, res) => {
        let body = req.body;
        let keys = Object.keys(req.body);
        let props = {}, p = 0;
        Object.keys(modifiableProps).forEach(prop => {
            if(keys.indexOf(prop) >= 0 && modifiableProps[prop](body[prop])){
                props[prop] = body[prop];
                p++;
            }
        });

        if(p > 0 && !!props['content']){
            database.pushContainer(props, result => {
                if(result){
                    result.id = result._id;
                    delete result._id;
                    res.status(200).json({ sucess: true, ...result });
                } else {
                    res.status(500).json({ sucess: false, error: 'internal_error', err_description: 'Could not push container' });
                }
            });
        } else {
            res.status(400).json({  sucess: false, error: 'bad_request', err_description: 'Fields are missing' });
        }
    });

    router.all('/container/:id', authMiddleware('Bearer'), (req, res) => {
        let id = req.params.id;
        if(id && typeof id === 'string'){
            const query = { _id : id };
            if(req.method === 'GET'){
                database.fetchContainer(query, cnt => {
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
                const body = req.body;
                let mutation = mutate(modifiableProps, body);

                if(mutation.length == 0){
                    res.status(204).end();
                    return;
                }
                console.log('mutation', mutation, 'from', body);

                database.fetchContainer(query, cnt => {
                    if(cnt != null){
                        let mutedCnt = Object.assign({}, cnt, mutation);
                        if(cnt != mutedCnt){
                            database.updateContainer(query, mutedCnt, cb => {
                                if(cb){
                                    mutedCnt.id = mutedCnt._id;
                                    delete mutedCnt._id;
                                    res.status(200).json(mutedCnt);
                                } else {
                                    res.status(500).json({ success: false, err: 'internal_error', err_description: 'Could not update the container' });
                                }
                            });
                        } else {
                            res.status(200).json(mutedInv);
                        }
                    } else {
                        res.status(404).json({ success: false, err: 'not_found', err_description: 'The provided ID does not refer to any container in the database.' })
                    }
                });

            } else {
                res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
            }
        } else {
            res.status(400).json({ success: false, err: 'bad_request', err_description: 'Bad Request' });
        }
    });

    router.all('/containers', authMiddleware('Bearer'), requireScope([ 'fetch.*', 'fetch.cnt' ]), (req, res) => {
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
        database.fetchContainers({}, offset, length, docs => {
            if(docs != null){
                let containers = docs.map(cnt => {
                    try {
                        let location = cnt.locations.length > 0 && cnt.locations.sort((a, b) => b-a)[0].location || '';
                        return { id: cnt._id, content: cnt.content, location, state: cnt.state };
                    } catch(e){
                        console.error(e, cnt);
                        return undefined;
                    }
                });
                res.json(containers);
            } else {
                res.status(500).json({ success: false, err: 'internal_error', err_description: 'Failed to query containers' });
            }
        });
    });

};