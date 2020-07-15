const express = require('express');
const app = express();
const bodyParser = require('body-parser');

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.use('/', (req, res, next) => {
    res.set('Access-Control-Allow-Origin', '*')
    res.set('Access-Control-Allow-Headers', '*')
    res.set('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS')
    if (req.method === 'OPTIONS') {
        res.sendStatus(204);
        return;
    }
    console.log('[Express]', req.method, req.originalUrl);
    next();
})

let server = app.listen(80, () => {
    console.log('[Express] Listening on localhost:80');
})

