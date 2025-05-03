import { createProxyMiddleware } from 'http-proxy-middleware'

export default createProxyMiddleware({
    target: 'http://localhost:8080',
    changeOrigin: true,
    pathRewrite: { '^/api': '' },
    logLevel: 'debug'
})