const { createProxyMiddleware } = require("http-proxy-middleware");
module.exports = function(app) {
    app.use(createProxyMiddleware('/solvle',
        { target: 'http://localhost:8081',
            changeOrigin: true
        }
        ));
    app.use(createProxyMiddleware('/solvescape',
        { target: 'http://localhost:8081',
            changeOrigin: true
        }
        ));
}