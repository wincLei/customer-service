// 简单的 Mock 服务器用于测试登录功能
import http from 'http';
import url from 'url';

const PORT = 8080;

const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // 设置 CORS 头
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }

  // 获取验证码
  if (pathname === '/api/admin/auth/captcha' && req.method === 'GET') {
    const captchaSvg = `data:image/svg+xml;base64,${Buffer.from(`
      <svg width="120" height="40" xmlns="http://www.w3.org/2000/svg">
        <rect width="120" height="40" fill="#f5f5f5"/>
        <text x="50%" y="50%" font-size="20" fill="#667eea" text-anchor="middle" dominant-baseline="middle" font-weight="bold">1234</text>
      </svg>
    `).toString('base64')}`;

    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      code: 200,
      message: '成功',
      data: {
        image: captchaSvg,
        key: 'test-captcha-key-' + Date.now()
      }
    }));
    return;
  }

  // 登录接口
  if (pathname === '/api/admin/auth/login' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });

    req.on('end', () => {
      try {
        const data = JSON.parse(body);
        console.log('收到登录请求:', data);

        // 简单验证
        if (data.username === 'admin' && data.password === 'admin123') {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            code: 200,
            message: '登录成功',
            data: {
              token: 'mock-token-' + Date.now(),
              user: {
                id: '1',
                username: 'admin',
                email: 'admin@example.com',
                role: 'admin',
                avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'
              }
            }
          }));
        } else {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            code: 401,
            message: '用户名或密码错误',
            data: null
          }));
        }
      } catch (error) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          code: 500,
          message: '服务器错误',
          data: null
        }));
      }
    });
    return;
  }

  // 获取用户信息
  if (pathname === '/api/admin/auth/me' && req.method === 'GET') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      code: 200,
      message: '成功',
      data: {
        id: '1',
        username: 'admin',
        email: 'admin@example.com',
        role: 'admin'
      }
    }));
    return;
  }

  // 404
  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({
    code: 404,
    message: '接口不存在',
    data: null
  }));
});

server.listen(PORT, () => {
  console.log('🚀 Mock 服务器启动成功！');
  console.log(`📍 地址: http://localhost:${PORT}`);
  console.log('');
  console.log('可用接口:');
  console.log('  GET  /api/admin/auth/captcha - 获取验证码');
  console.log('  POST /api/admin/auth/login   - 登录 (admin/admin123)');
  console.log('  GET  /api/admin/auth/me      - 获取用户信息');
  console.log('');
  console.log('按 Ctrl+C 停止服务器');
});
