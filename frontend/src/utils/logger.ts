// 全局日志工具，支持日志等级控制
// 日志等级：error < warn < info < debug

const LOG_LEVELS = ['error', 'warn', 'info', 'debug'] as const;
type LogLevel = typeof LOG_LEVELS[number];

function getLogLevelFromEnv(): LogLevel {
  // VITE_LOG_LEVEL 需在 .env 文件中配置
  const level = import.meta.env.VITE_LOG_LEVEL as LogLevel | undefined;
  if (level && LOG_LEVELS.includes(level)) return level;
  return 'info'; // 默认 info
}

let currentLevel: LogLevel = getLogLevelFromEnv();

export function setLogLevel(level: LogLevel) {
  if (LOG_LEVELS.includes(level)) currentLevel = level;
}

function shouldLog(level: LogLevel) {
  return LOG_LEVELS.indexOf(level) <= LOG_LEVELS.indexOf(currentLevel);
}

export const logger = {
  error(...args: any[]) {
    if (shouldLog('error')) console.error('[ERROR]', ...args);
  },
  warn(...args: any[]) {
    if (shouldLog('warn')) console.warn('[WARN]', ...args);
  },
  info(...args: any[]) {
    if (shouldLog('info')) console.info('[INFO]', ...args);
  },
  debug(...args: any[]) {
    if (shouldLog('debug')) console.debug('[DEBUG]', ...args);
  },
};
