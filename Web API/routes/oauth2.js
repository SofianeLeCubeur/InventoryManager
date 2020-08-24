module.exports = function(router, database, authMiddleware){

    router.all('/token', (req, res) => {
        if(req.method !== 'POST'){
            res.status(405).json({ success: false, err: 'method_not_allowed', err_description: 'This request method is not allowed' });
            return;
        }
        res.status(200).json({ success: true, token: 'aaBBccDD', token_type: 'Bearer' }); // TODO: Do real oauth2 stuff
    })

};