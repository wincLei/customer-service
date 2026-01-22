# 角色区分修复测试

## 修复内容

### 问题1: 刷新页面404错误 ✅

**原因**: nginx配置中有`location /admin/`将前端路由代理到后端，导致刷新时访问`/admin/dashboard`被转发到后端API

**修复**: 删除nginx中的`location /admin/`配置，保留前端路由

**测试方法**:

1. 登录系统（admin或agent1）
2. 在`http://localhost/admin/dashboard`或`http://localhost/admin/workbench`页面
3. 按F5刷新或点击浏览器刷新按钮
4. ✅ 应该正常显示页面，不再出现404错误

### 问题2: 角色导航显示错误 ✅

**原因**: AdminLayout虽然定义了`userRole`，但可能在渲染时机上有问题

**修复**:

1. 添加`loadUserInfo()`函数并在`onMounted`时调用
2. 添加console.log调试信息查看角色加载
3. 添加`watch`监听路由变化

**测试方法**:

#### 测试admin账号

```bash
# 1. 登录admin账号
用户名: admin
密码: admin123

# 2. 预期界面
✅ 左侧导航只显示两个图标：
   - 数据 (data-analysis图标)
   - 设置 (setting图标)
✅ 不显示"工作台"图标
✅ 默认进入 /admin/dashboard 页面
```

#### 测试agent1账号

```bash
# 1. 登录agent1账号
用户名: agent1
密码: admin123

# 2. 预期界面
✅ 左侧导航只显示两个图标：
   - 工作台 (chat-line-square图标)
   - 设置 (setting图标)
✅ 不显示"数据"图标
✅ 默认进入 /admin/workbench 页面
```

## 快速验证脚本

```bash
# 在浏览器console中运行，查看当前用户角色
const userInfo = JSON.parse(localStorage.getItem('user_info'))
console.log('用户角色:', userInfo.role)
console.log('用户ID:', userInfo.id)
console.log('用户名:', userInfo.username)
```

## 调试方法

如果仍然看到错误的导航，请：

1. **清除浏览器缓存和localStorage**

```bash
# 在浏览器console运行
localStorage.clear()
# 然后刷新页面重新登录
```

2. **查看浏览器console日志**
   应该看到类似信息：

```
加载用户信息: {username: "admin", role: "admin"}
```

或

```
加载用户信息: {username: "agent1", role: "agent"}
```

3. **检查登录返回的数据**
   在Network面板查看`/api/admin/auth/login`的响应，确认返回的role字段正确

## 预期结果对比

### Admin账号看到的界面

```
┌─────┐  ┌──────────────────────────────┐
│ [D] │  │  数据概览                     │
│     │  │  ┌────┬────┬────┬────┐      │
│ [S] │  │  │排队│进行│今日│消息│      │
│     │  │  └────┴────┴────┴────┘      │
└─────┘  │  满意度统计 | 客服状态       │
         └──────────────────────────────┘
D = 数据图标 (仅admin可见)
S = 设置图标 (所有人可见)
```

### Agent账号看到的界面

```
┌─────┐  ┌──────┬────────┬──────┐
│ [W] │  │会话列│聊天窗口│用户面│
│     │  │表    │        │板    │
│ [S] │  │      │        │      │
│     │  │      │        │      │
└─────┘  └──────┴────────┴──────┘
W = 工作台图标 (仅agent可见)
S = 设置图标 (所有人可见)
```

## 常见问题

**Q: 刷新后还是404**
A: 确认frontend容器已重启，nginx配置已生效

```bash
docker-compose ps frontend
# 应该显示Up状态
```

**Q: 角色导航还是不对**
A:

1. 清除localStorage重新登录
2. 检查浏览器console是否有错误
3. 确认frontend代码已重新构建

**Q: 如何确认我的修改已生效**
A:

```bash
# 检查nginx配置
docker exec mini-customer-service-frontend cat /etc/nginx/conf.d/default.conf | grep -A 5 "location /api/"

# 不应该看到 "location /admin/" 这一段
```
