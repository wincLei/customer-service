import { createI18n } from 'vue-i18n'

// 中文语言包
const zh = {
  app: {
    title: '极简客服'
  },
  // 导航
  nav: {
    home: '首页',
    features: '功能特性',
    contact: '联系我们',
    sourceCode: '源码',
    documentation: '文档'
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
    },
    ctaSection: {
      title: '准备开始使用了吗？',
      description: '立即体验极简客服系统，为您的企业提供专业高效的客户服务解决方案'
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
    ],
    // 新增的键结构用于动态翻译（已修复逗号问题）
    'list.0.title': '多端支持',
    'list.0.description': '支持PC端管理后台、Web访客端、H5移动端，覆盖全场景使用',
    'list.1.title': '实时通讯',
    'list.1.description': '集成WuKongIM消息中间件，实现低延迟、高可靠的实时消息传递',
    'list.2.title': '智能分配',
    'list.2.description': '支持轮询、最少会话、技能匹配等多种分配策略，提升服务效率',
    'list.3.title': '数据统计',
    'list.3.description': '丰富的数据报表和统计分析，帮助企业优化客服运营',
    techStack: {
      title: '技术架构',
      description: '基于现代化技术栈构建，确保系统的高性能和可扩展性',
      typescriptDesc: '强类型的JavaScript超集'
    },
    architecture: {
      title: '系统架构',
      description: '采用微服务架构设计，支持高并发和水平扩展',
      frontend: '前端',
      frontendApp: '应用',  // 简化为"应用"
      frontendDesc: '管理后台 + 访客端 + 移动端',
      api: 'API',
      backend: '服务',  // 简化为"服务"
      backendDesc: 'Spring Boot 微服务',
      im: '通讯',  // 简化为"通讯"
      imDesc: 'WuKongIM 实时通讯',
      infrastructure: '基础设施',
      docker: 'Docker',
      postgresql: 'PostgreSQL',
      postgresqlDesc: '关系型数据库',
      dockerDesc: '容器化平台'
    },
    infrastructureItems: {
      docker: 'Docker 容器化部署',
      nginx: 'Nginx 负载均衡',
      redis: 'Redis 缓存服务',
      postgresql: 'PostgreSQL 数据库'
    },
    monitoring: '监控运维',
    monitoringItems: {
      logs: '日志收集分析',
      performance: '性能监控告警',
      deploy: '自动化部署',
      recovery: '故障自动恢复'
    },
    // 确保这些关键键存在
    frontendApp: '应用',
    im: '通讯',
    backend: '服务',
    docker: 'Docker',
    postgresql: 'PostgreSQL',
    frontendDesc: '管理后台 + 访客端 + 移动端',
    backendDesc: 'Spring Boot 微服务',
    imDesc: 'WuKongIM 实时通讯',
    dockerDesc: '容器化平台',
    postgresqlDesc: '关系型数据库'
  },

  // 文档页面
  docs: {
    title: '技术文档',
    gettingStarted: '快速开始',
    apiReference: 'API参考',
    deployment: '部署指南',
    troubleshooting: '故障排除',
    subtitle: '详细的开发文档和技术指南，帮助您快速上手和深入使用',
    quickStart: {
      title: '快速开始',
      description: '简单几步即可运行您的客服系统',
      steps: {
        clone: '克隆项目',
        install: '安装依赖',
        start: '启动服务',
        access: '访问系统',
        accessDesc: '打开浏览器访问',
        githubUrl: 'https://github.com/your-repo/customer-service.git',
        installCmd: 'cd customer-service && npm install',
        startCmd: 'docker-compose up -d',
        localUrl: 'http://localhost:3000'
      }
    },
    support: {
      title: '需要帮助？',
      description: '如果您在使用过程中遇到任何问题，可以通过以下方式获得帮助',
      community: '社区讨论',
      communityDesc: '在GitHub Discussions中提问和交流',
      techSupport: '技术支持',
      techSupportDesc: '通过工单系统获得专业技术支持',
      faq: 'FAQ',
      faqDesc: '查看常见问题解答'
    },
    viewDetails: '查看详情'
  },

  // 联系我们
  contact: {
    title: '联系我们',
    email: 'leijiang@fulitoutiao.cn',
    emailTitle: '邮箱',
    phone: '电话',
    address: '地址',
    wechat: '微信',
    form: {
      name: '姓名',
      email: '邮箱',
      message: '留言',
      submit: '发送消息'
    },
    subtitle: '我们期待与您合作，为您提供最优质的客服解决方案',
    sendMessage: '发送消息',
    contactInfo: '联系信息',
    businessHours: '工作时间',
    weekdays: {
      mondayToFriday: '周一 - 周五',
      saturday: '周六',
      sunday: '周日',
      rest: '休息'
    },
    companyLocation: '公司位置',
    locationDescription: '欢迎实地考察我们的办公环境，了解我们的团队和文化',
    faqTitle: '常见问题',
    faqDescription: '解答您可能关心的问题',
    faq: {
      q1: '如何开始使用客服系统？',
      a1: '您可以通过我们的快速开始指南，在几分钟内完成系统部署和基本配置。',
      q2: '系统支持哪些部署方式？',
      a2: '我们支持Docker容器化部署、传统服务器部署以及云平台部署等多种方式。',
      q3: '是否提供技术支持？',
      a3: '是的，我们提供7×24小时的技术支持服务，包括在线客服、邮件支持和电话支持。'
    },
    submitSuccess: '感谢您的留言！我们会尽快回复您。'
  },

  // 通用
  common: {
    learnMore: '了解更多',
    tryDemo: '体验演示',
    backToTop: '回到顶部',
    copyright: '© 2026 极简客服系统. 保留所有权利.',
    frontend: '前端',
    typescript: 'TypeScript'
  }
}

// 英文语言包
const en = {
  app: {
    title: 'Mini-Customer-Service'
  },
  // Navigation
  nav: {
    home: 'Home',
    features: 'Features',
    contact: 'Contact',
    sourceCode: 'Source Code',
    documentation: 'Documentation'
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
    },
    ctaSection: {
      title: 'Ready to get started?',
      description: 'Try Mini Customer Service now to provide professional and efficient customer service solutions for your business'
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
    ],
    // 新增的键结构用于动态翻译
    'list.0.title': 'Multi-platform Support',
    'list.0.description': 'Supports PC admin panel, Web visitor portal, H5 mobile, covering all usage scenarios',
    'list.1.title': 'Real-time Communication',
    'list.1.description': 'Integrates WuKongIM messaging middleware for low-latency, reliable real-time message delivery',
    'list.2.title': 'Intelligent Allocation',
    'list.2.description': 'Supports round-robin, least sessions, skill matching and other allocation strategies to improve service efficiency',
    'list.3.title': 'Data Analytics',
    'list.3.description': 'Rich data reports and statistical analysis to help enterprises optimize customer service operations',
    techStack: {
      title: 'Technical Architecture',
      description: 'Built on modern technology stack to ensure high performance and scalability',
      typescriptDesc: 'Strongly typed JavaScript superset'
    },
    architecture: {
      title: 'System Architecture',
      description: 'Microservice architecture design',
      frontend: 'Frontend',
      frontendApp: 'Application',
      frontendAppShort: 'A',
      frontendDesc: 'Admin + Visitor + Mobile',
      api: 'API',
      backend: 'Service',
      backendShort: 'S',
      backendDesc: 'Spring Boot Services',
      im: 'Communication',
      imShort: 'C',
      imDesc: 'WuKongIM Real-time',
      infrastructure: 'Infrastructure',
      docker: 'Docker',
      postgresql: 'PostgreSQL',
      postgresqlDesc: 'Relational Database',
      dockerDesc: 'Containerization Platform'
    },
    infrastructureItems: {
      docker: 'Docker Containerization',
      nginx: 'Nginx Load Balancing',
      redis: 'Redis Cache Service',
      postgresql: 'PostgreSQL Database'
    },
    frontendApp: 'Application',
    im: 'Communication',
    backend: 'Service',
    docker: 'Docker',
    postgresql: 'PostgreSQL',
    monitoring: 'Monitoring & Operations',
    monitoringItems: {
      logs: 'Log Collection & Analysis',
      performance: 'Performance Monitoring & Alerts',
      deploy: 'Automated Deployment',
      recovery: 'Automatic Failure Recovery'
    }
  },

  // Documentation
  docs: {
    title: 'Documentation',
    gettingStarted: 'Getting Started',
    apiReference: 'API Reference',
    deployment: 'Deployment Guide',
    troubleshooting: 'Troubleshooting',
    subtitle: 'Detailed development documentation and technical guides to help you get started and dive deep',
    quickStart: {
      title: 'Quick Start',
      description: 'Run your customer service system in just a few steps',
      steps: {
        clone: 'Clone Project',
        install: 'Install Dependencies',
        start: 'Start Service',
        access: 'Access System',
        accessDesc: 'Open browser to access',
        githubUrl: 'https://github.com/wincLei/customer-service',
        installCmd: 'cd customer-service && npm install',
        startCmd: 'docker-compose up -d',
        localUrl: 'http://localhost:3000'
      }
    },
    support: {
      title: 'Need Help?',
      description: 'If you encounter any issues during use, you can get help through the following ways',
      community: 'Community Discussion',
      communityDesc: 'Ask questions and communicate in GitHub Discussions',
      techSupport: 'Technical Support',
      techSupportDesc: 'Get professional technical support through the ticket system',
      faq: 'FAQ',
      faqDesc: 'View common questions and answers'
    },
    viewDetails: 'View Details'
  },

  // FAQ
  faq: {
    title: 'Frequently Asked Questions',
    description: 'Common questions about Mini-Customer-Service',
    questions: [
      {
        question: 'How to install the open-source customer service system?',
        answer: 'You can follow our quick start guide to complete system deployment and basic configuration in minutes.'
      },
      {
        question: 'Which databases does Mini-Customer-Service support?',
        answer: 'We support PostgreSQL as the main database, with Redis for caching.'
      },
      {
        question: 'Does Mini-Customer-Service support multi-channel access?',
        answer: 'Yes, it supports Web, H5 mobile, and PC management backend access.'
      },
      {
        question: 'Is there technical support available?',
        answer: 'Yes, we provide 7×24 technical support service, including online customer service, email support, and phone support.'
      }
    ]
  },



  // Contact
  contact: {
    title: 'Contact Us',
    email: 'leijiang@fulitoutiao.cn',
    emailTitle: 'Email',
    phone: 'Phone',
    address: 'Address',
    wechat: 'WeChat',
    form: {
      name: 'Name',
      email: 'Email',
      message: 'Message',
      submit: 'Send Message'
    },
    subtitle: 'We look forward to working with you to provide the best customer service solutions',
    sendMessage: 'Send Message',
    contactInfo: 'Contact Information',
    businessHours: 'Business Hours',
    weekdays: {
      mondayToFriday: 'Monday - Friday',
      saturday: 'Saturday',
      sunday: 'Sunday',
      rest: 'Closed'
    },
    companyLocation: 'Company Location',
    locationDescription: 'Welcome to visit our office to learn about our team and culture',
    faqTitle: 'FAQ',
    faqDescription: 'Answers to questions you may care about',
    faq: {
      q1: 'How to get started with the customer service system?',
      a1: 'You can complete system deployment and basic configuration in minutes through our quick start guide.',
      q2: 'What deployment methods are supported?',
      a2: 'We support various deployment methods including Docker containerization, traditional server deployment, and cloud platform deployment.',
      q3: 'Is technical support provided?',
      a3: 'Yes, we provide 7×24 technical support service, including online customer service, email support, and phone support.'
    },
    submitSuccess: 'Thank you for your message! We will reply to you as soon as possible.'
  },

  // Common
  common: {
    learnMore: 'Learn More',
    tryDemo: 'Try Demo',
    backToTop: 'Back to Top',
    copyright: '© 2026 Mini-Customer-Service. All rights reserved.',
    frontend: 'Frontend',
    typescript: 'TypeScript'
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
