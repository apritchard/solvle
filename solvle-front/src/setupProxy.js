const { createProxyMiddleware } = require("http-proxy-middleware");
module.exports = function(app) {
    app.use(createProxyMiddleware('/solvle',
        { target: 'http://server:8081',
            changeOrigin: true
        }
        ));
}