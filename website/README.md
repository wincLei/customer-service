# 客服系统官网

基于Vue 3 + TypeScript + Tailwind CSS构建的企业级客服系统官方网站，支持中英文多语言切换。

## 功能特性

- 🌐 多语言支持（中文/英文）
- 📱 响应式设计，适配各种设备
- ⚡ 基于Vite的快速开发体验
- 🎨 使用Tailwind CSS的现代化UI
- 🚀 Docker容器化部署

## 技术栈

- **前端框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **路由管理**: Vue Router 4
- **状态管理**: Pinia
- **国际化**: Vue I18n
- **样式框架**: Tailwind CSS
- **部署**: Docker + Nginx

## 快速开始

### 开发环境

```bash
# 克隆项目
git clone <repository-url>
cd website

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 [http://localhost:3000](http://localhost:3000) 查看效果。

### 生产环境部署

```bash
# 构建项目
npm run build

# 使用Docker部署
docker-compose up -d
```

访问 [http://localhost:8080](http://localhost:8080) 查看生产环境。

## 项目结构

```
website/
├── src/
│   ├── assets/          # 静态资源
│   ├── components/      # 公共组件
│   ├── layouts/         # 布局组件
│   ├── locales/         # 多语言文件
│   ├── pages/           # 页面组件
│   ├── router/          # 路由配置
│   ├── stores/          # 状态管理
│   ├── styles/          # 全局样式
│   ├── utils/           # 工具函数
│   ├── App.vue          # 根组件
│   └── main.ts          # 入口文件
├── public/              # 公共文件
├── index.html           # HTML模板
├── Dockerfile           # Docker配置
├── docker-compose.yml   # Docker Compose配置
├── nginx.conf           # Nginx配置
├── tailwind.config.js   # Tailwind配置
├── vite.config.ts       # Vite配置
└── package.json         # 项目配置
```

## 多语言支持

项目支持中英文切换，语言文件位于 `src/locales/index.ts`：

```typescript
// 切换语言
locale.value = 'zh' // 中文
locale.value = 'en' // 英文
```

## 自定义配置

### 修改主题色

在 `tailwind.config.js` 中修改颜色配置：

```javascript
theme: {
  extend: {
    colors: {
      primary: {
        // 自定义主色调
      }
    }
  }
}
```

### 添加新页面

1. 在 `src/pages/` 目录下创建新页面组件
2. 在 `src/router/index.ts` 中添加路由配置
3. 在 `src/components/Header.vue` 中添加导航链接

## 部署说明

### 环境要求

- Node.js >= 16
- Docker (可选)
- Nginx (可选)

### 构建优化

项目已配置以下优化：

- 代码分割
- 资源压缩
- 静态资源缓存
- gzip压缩

### 监控和日志

生产环境建议配置：

- 应用性能监控(APM)
- 错误日志收集
- 访问日志分析
- 健康检查端点

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

MIT License

## 联系方式

如有问题，请联系：contact@customerservice.com