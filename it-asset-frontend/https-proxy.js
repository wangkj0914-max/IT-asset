const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');

const key = fs.readFileSync(path.resolve(__dirname, 'cert/key.pem'));
const cert = fs.readFileSync(path.resolve(__dirname, 'cert/cert.pem'));

const server = https.createServer({ key, cert }, (req, res) => {
  const opts = {
    hostname: 'localhost',
    port: 8081,
    path: req.url,
    method: req.method,
    headers: { ...req.headers, host: 'localhost:8081' },
    timeout: 30000
  };
  const proxyReq = http.request(opts, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers);
    proxyRes.pipe(res);
  });
  proxyReq.on('error', (e) => { res.writeHead(502); res.end('Proxy Error'); });
  proxyReq.on('timeout', () => { proxyReq.destroy(); res.writeHead(504); res.end('Timeout'); });
  req.pipe(proxyReq);
});

server.listen(8082, '0.0.0.0', () => console.log('HTTPS proxy :8082 → :8081'));
