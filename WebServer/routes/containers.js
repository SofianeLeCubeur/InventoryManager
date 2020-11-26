const { mutate, requireScope } = require('./../utils');
const { Content, Container, Error } = require('./../models');
const Webhook = require('./../webhooks');

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
        },
        'webhooks': function(s){
            if(!Array.isArray(s)) return false;
            let b = 0;
            s.forEach(wh => {
                if(typeof wh === 'object' && typeof wh.id === 'string' 
                && typeof wh.url === 'string' && wh.event === 'string'){
                    b++;
                }
            })
            return b === s.length;
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
                    res.status(200).json(Content(result));
                } else {
                    res.status(500).json(Error('internal_error', 'Could not push container'));
                }
            });
        } else {
            res.status(400).json(Error('bad_request', 'Fields are missing'));
        }
    });

    router.all('/container/:id', authMiddleware('Bearer'), (req, res) => {
        let token = res.locals.token;
        let id = req.params.id;
        if(id && typeof id === 'string'){
            const query = { _id : id };
            if(req.method === 'GET'){
                database.fetchContainer(query, cnt => {
                    if(cnt != null){
                        res.status(200).json(Content(cnt));
                    } else {
                        res.status(404).json(Error('not_found', 'The provided ID does not refer to any container in the database.'))
                    }
                });
                return;
            } else if(req.method === 'POST' || req.method === 'UPDATE'){
                if(token.scope.indexOf('edit.*') >= 0 || token.scope.indexOf('edit.cnt') >= 0){
                    const body = req.body;
                    let mutation = mutate(modifiableProps, body);

                    if(mutation.length == 0){
                        res.status(204).end();
                        return;
                    }
                    //console.log('mutation', mutation, 'from', body);

                    database.fetchContainer(query, cnt => {
                        if(cnt != null){
                            let mutedCnt = Object.assign({}, cnt, mutation);
                            if(cnt != mutedCnt){
                                database.updateContainer(query, mutedCnt, cb => {
                                    if(cb){
                                        res.status(200).json(Content(mutedCnt));
                                        Webhook.call(database, 'update', 'container', token.uid, mutedCnt);
                                    } else {
                                        res.status(500).json(Error('internal_error', 'Could not update the container'));
                                    }
                                });
                            } else {
                                res.status(200).json(Content(mutedCnt));
                            }
                        } else {
                            res.status(404).json(Error('not_found', 'The provided ID does not refer to any container in the database.'))
                        }
                    });
                } else {
                    res.status(403).json(Error('forbidden', 'Not Authorized'));
                }
            } else if(req.method === 'DELETE'){
                if(token.scope.indexOf('delete.*') >= 0 || token.scope.indexOf('delete.cnt') >= 0){
                    database.deleteContainer(query, (success) => {
                        if(success){
                            res.status(200).json(Message('Container successfully deleted', 'server'));
                        } else {
                            res.status(500).json(Error('internal_error', 'Could not delete the container'));
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

    router.all('/containers', authMiddleware('Bearer'), requireScope([ 'fetch.*', 'fetch.cnt' ]), (req, res) => {
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
        database.fetchContainers({}, offset, length, docs => {
            if(docs != null){
                let containers = docs.map(Container);
                res.json(containers);
            } else {
                res.status(500).json(Error('internal_error', 'Failed to query containers'));
            }
        });
    });

};