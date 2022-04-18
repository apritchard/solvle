const { createProxyMiddleware } = require("http-proxy-middleware");
module.exports = function(app) {
    app.use(createProxyMiddleware('/solvle',
        { target: 'http://solvleback:8081',
            changeOrigin: true
        }
        ));
}