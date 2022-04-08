const { createProxyMiddleware } = require("http-proxy-middleware");
module.exports = function(app) {
    app.use(createProxyMiddleware('/solvle',
        { target: 'http://localhost:8080',
            changeOrigin: true
        }
        ));
}