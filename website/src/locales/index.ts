import { createI18n } from 'vue-i18n'

// 中文语言包
const zh = {
  // 导航
  nav: {
    home: '首页',
    features: '功能特性',
    docs: '文档',
    demo: '演示',
    contact: '联系我们'
  },
  
  // 首页
  home: {
    hero: {
      title: '极简客服系统',
      subtitle: '为企业提供开箱即用的全渠道客服解决方案',
      description: '支持Web/H5多端接入，集成WuKongIM实现实时通讯，界面现代化，操作简单易用',
      cta: '立即体验',
      github: '查看源码'
    },
    features: {
      title: '核心功能',
      realtime: {
        title: '实时通讯',
        description: '基于WuKongIM实现毫秒级消息传递，支持文本、图片等多种消息类型'
      },
      multichannel: {
        title: '多渠道接入',
        description: '支持Web、H5移动端等多种接入方式，统一管理客户会话'
      },
      management: {
        title: '智能管理',
        description: '完善的客服工作台，支持会话分配、标签管理、数据分析等功能'
      }
    },
    stats: {
      title: '系统数据',
      projects: '服务项目',
      agents: '在线客服',
      conversations: '日均会话',
      satisfaction: '客户满意度'
    }
  },
  
  // 功能页面
  features: {
    title: '功能特性',
    description: '全面的客服系统功能，满足企业各种需求',
    list: [
      {
        title: '多端支持',
        description: '支持PC端管理后台、Web访客端、H5移动端，覆盖全场景使用'
      },
      {
        title: '实时通讯',
        description: '集成WuKongIM消息中间件，实现低延迟、高可靠的实时消息传递'
      },
      {
        title: '智能分配',
        description: '支持轮询、最少会话、技能匹配等多种分配策略，提升服务效率'
      },
      {
        title: '数据统计',
        description: '丰富的数据报表和统计分析，帮助企业优化客服运营'
      }
    ]
  },
  
  // 文档页面
  docs: {
    title: '技术文档',
    gettingStarted: '快速开始',
    apiReference: 'API参考',
    deployment: '部署指南',
    troubleshooting: '故障排除'
  },
  
  // 联系我们
  contact: {
    title: '联系我们',
    email: '邮箱',
    phone: '电话',
    address: '地址',
    wechat: '微信',
    form: {
      name: '姓名',
      email: '邮箱',
      message: '留言',
      submit: '发送消息'
    }
  },
  
  // 通用
  common: {
    learnMore: '了解更多',
    tryDemo: '体验演示',
    backToTop: '回到顶部',
    copyright: '© 2024 极简客服系统. 保留所有权利.'
  }
}

// 英文语言包
const en = {
  // Navigation
  nav: {
    home: 'Home',
    features: 'Features',
    docs: 'Documentation',
    demo: 'Demo',
    contact: 'Contact'
  },
  
  // Home
  home: {
    hero: {
      title: 'Mini Customer Service',
      subtitle: 'Out-of-the-box omnichannel customer service solution for enterprises',
      description: 'Supports Web/H5 multi-channel access, integrates WuKongIM for real-time communication, modern interface, simple and easy to use',
      cta: 'Try Now',
      github: 'View Source'
    },
    features: {
      title: 'Core Features',
      realtime: {
        title: 'Real-time Communication',
        description: 'Millisecond-level message delivery based on WuKongIM, supporting various message types including text and images'
      },
      multichannel: {
        title: 'Multi-channel Access',
        description: 'Supports Web, H5 mobile and other access methods, unified customer session management'
      },
      management: {
        title: 'Smart Management',
        description: 'Complete agent workbench with session allocation, tag management, data analysis and other functions'
      }
    },
    stats: {
      title: 'System Statistics',
      projects: 'Projects Served',
      agents: 'Online Agents',
      conversations: 'Daily Conversations',
      satisfaction: 'Customer Satisfaction'
    }
  },
  
  // Features
  features: {
    title: 'Features',
    description: 'Comprehensive customer service system features to meet various enterprise needs',
    list: [
      {
        title: 'Multi-platform Support',
        description: 'Supports PC admin panel, Web visitor portal, H5 mobile, covering all usage scenarios'
      },
      {
        title: 'Real-time Communication',
        description: 'Integrates WuKongIM messaging middleware for low-latency, reliable real-time message delivery'
      },
      {
        title: 'Intelligent Allocation',
        description: 'Supports round-robin, least sessions, skill matching and other allocation strategies to improve service efficiency'
      },
      {
        title: 'Data Analytics',
        description: 'Rich data reports and statistical analysis to help enterprises optimize customer service operations'
      }
    ]
  },
  
  // Documentation
  docs: {
    title: 'Documentation',
    gettingStarted: 'Getting Started',
    apiReference: 'API Reference',
    deployment: 'Deployment Guide',
    troubleshooting: 'Troubleshooting'
  },
  
  // Contact
  contact: {
    title: 'Contact Us',
    email: 'Email',
    phone: 'Phone',
    address: 'Address',
    wechat: 'WeChat',
    form: {
      name: 'Name',
      email: 'Email',
      message: 'Message',
      submit: 'Send Message'
    }
  },
  
  // Common
  common: {
    learnMore: 'Learn More',
    tryDemo: 'Try Demo',
    backToTop: 'Back to Top',
    copyright: '© 2024 Mini Customer Service. All rights reserved.'
  }
}

// 检测浏览器语言
function getDefaultLocale(): string {
  const browserLang = navigator.language || (navigator as any).userLanguage || 'en'
  return browserLang.toLowerCase().startsWith('zh') ? 'zh' : 'en'
}

const i18n = createI18n({
  legacy: false,
  locale: getDefaultLocale(),
  fallbackLocale: 'en',
  messages: {
    zh,
    en
  }
})

export default i18n