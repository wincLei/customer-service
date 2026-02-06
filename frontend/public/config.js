// 运行时配置（由 Docker 启动时动态注入）
// 这个文件会在容器启动时被 docker-entrypoint.sh 替换
window.__RUNTIME_CONFIG__ = {
  WUKONGIM_WS_URL: "__WUKONGIM_WS_URL__"
};
