<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const docs = [
  {
    title: '快速开始',
    description: '从零开始搭建客服系统环境',
    icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253',
    items: [
      '环境准备',
      '项目初始化',
      '数据库配置',
      '启动服务'
    ]
  },
  {
    title: 'API参考',
    description: '完整的接口文档和使用说明',
    icon: 'M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4',
    items: [
      '认证接口',
      '会话管理',
      '消息接口',
      '用户管理'
    ]
  },
  {
    title: '部署指南',
    description: '生产环境部署和运维说明',
    icon: 'M5 12.5v-8.5a1 1 0 011-1h12a1 1 0 011 1v8.5M5 12.5v8.5a1 1 0 001 1h12a1 1 0 001-1v-8.5M9 8h6M9 12h6m-6 4h6',
    items: [
      'Docker部署',
      '集群配置',
      '负载均衡',
      '监控告警'
    ]
  },
  {
    title: '故障排除',
    description: '常见问题和解决方案',
    icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
    items: [
      '连接问题',
      '性能优化',
      '数据迁移',
      '安全配置'
    ]
  }
]
</script>

<template>
  <div class="min-h-screen">
    <!-- Hero Section -->
    <section class="gradient-bg">
      <div class="container-max section-padding">
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
            {{ t('docs.title') }}
          </h1>
          <p class="text-xl text-gray-700">
            详细的开发文档和技术指南，帮助您快速上手和深入使用
          </p>
        </div>
      </div>
    </section>

    <!-- Documentation Sections -->
    <section class="section-padding bg-white">
      <div class="container-max">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div
            v-for="(doc, index) in docs"
            :key="index"
            class="card hover:shadow-lg transition-shadow duration-300"
          >
            <div class="flex items-start space-x-4">
              <div class="flex-shrink-0">
                <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="doc.icon" />
                  </svg>
                </div>
              </div>
              <div class="flex-1">
                <h3 class="text-xl font-semibold text-gray-900 mb-2">
                  {{ doc.title }}
                </h3>
                <p class="text-gray-600 mb-4">
                  {{ doc.description }}
                </p>
                <ul class="space-y-1">
                  <li
                    v-for="(item, itemIndex) in doc.items"
                    :key="itemIndex"
                    class="text-sm text-gray-500 flex items-center"
                  >
                    <svg class="h-4 w-4 text-green-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    {{ item }}
                  </li>
                </ul>
                <button class="mt-4 text-blue-600 hover:text-blue-800 font-medium flex items-center">
                  查看详情
                  <svg class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Quick Start Guide -->
    <section class="section-padding gradient-bg">
      <div class="container-max">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-12">
            <h2 class="text-3xl font-bold text-gray-900 mb-4">
              快速开始
            </h2>
            <p class="text-gray-700">
              简单几步即可运行您的客服系统
            </p>
          </div>
          
          <div class="bg-white rounded-xl shadow-sm p-8">
            <div class="space-y-6">
              <div class="flex items-start">
                <div class="flex-shrink-0 w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                  1
                </div>
                <div class="ml-4">
                  <h3 class="font-semibold text-gray-900 mb-2">克隆项目</h3>
                  <div class="bg-gray-100 rounded-lg p-4 font-mono text-sm">
                    git clone https://github.com/your-repo/customer-service.git
                  </div>
                </div>
              </div>
              
              <div class="flex items-start">
                <div class="flex-shrink-0 w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                  2
                </div>
                <div class="ml-4">
                  <h3 class="font-semibold text-gray-900 mb-2">安装依赖</h3>
                  <div class="bg-gray-100 rounded-lg p-4 font-mono text-sm">
                    cd customer-service && npm install
                  </div>
                </div>
              </div>
              
              <div class="flex items-start">
                <div class="flex-shrink-0 w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                  3
                </div>
                <div class="ml-4">
                  <h3 class="font-semibold text-gray-900 mb-2">启动服务</h3>
                  <div class="bg-gray-100 rounded-lg p-4 font-mono text-sm">
                    docker-compose up -d
                  </div>
                </div>
              </div>
              
              <div class="flex items-start">
                <div class="flex-shrink-0 w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                  4
                </div>
                <div class="ml-4">
                  <h3 class="font-semibold text-gray-900 mb-2">访问系统</h3>
                  <p class="text-gray-600">
                    打开浏览器访问 <a href="http://localhost:3000" class="text-blue-600 hover:underline">http://localhost:3000</a>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Support Section -->
    <section class="section-padding bg-white">
      <div class="container-max text-center">
        <h2 class="text-3xl font-bold text-gray-900 mb-4">
          需要帮助？
        </h2>
        <p class="text-gray-600 mb-8 max-w-2xl mx-auto">
          如果您在使用过程中遇到任何问题，可以通过以下方式获得帮助
        </p>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
          <div class="card">
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
              </svg>
            </div>
            <h3 class="font-semibold text-gray-900 mb-2">社区讨论</h3>
            <p class="text-gray-600 text-sm">
              在GitHub Discussions中提问和交流
            </p>
          </div>
          
          <div class="card">
            <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
            </div>
            <h3 class="font-semibold text-gray-900 mb-2">技术支持</h3>
            <p class="text-gray-600 text-sm">
              通过工单系统获得专业技术支持
            </p>
          </div>
          
          <div class="card">
            <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="h-6 w-6 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 class="font-semibold text-gray-900 mb-2">FAQ</h3>
            <p class="text-gray-600 text-sm">
              查看常见问题解答
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>