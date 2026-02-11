<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const features = [
  {
    title: '多端支持',
    description: '支持PC端管理后台、Web访客端、H5移动端，覆盖全场景使用',
    icon: 'M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z'
  },
  {
    title: '实时通讯',
    description: '集成WuKongIM消息中间件，实现低延迟、高可靠的实时消息传递',
    icon: 'M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z'
  },
  {
    title: '智能分配',
    description: '支持轮询、最少会话、技能匹配等多种分配策略，提升服务效率',
    icon: 'M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z'
  },
  {
    title: '数据统计',
    description: '丰富的数据报表和统计分析，帮助企业优化客服运营',
    icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'
  }
]

const techStack = [
  { name: 'Vue 3', description: '现代前端框架' },
  { name: 'TypeScript', description: '类型安全' },
  { name: 'WuKongIM', description: '实时通讯' },
  { name: 'Spring Boot', description: '后端服务' },
  { name: 'PostgreSQL', description: '数据存储' },
  { name: 'Docker', description: '容器化部署' }
]
</script>

<template>
  <div class="min-h-screen">
    <!-- Hero Section -->
    <section class="gradient-bg">
      <div class="container-max section-padding">
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
            {{ t('features.title') }}
          </h1>
          <p class="text-xl text-gray-700 max-w-2xl mx-auto">
            {{ t('features.description') }}
          </p>
        </div>
      </div>
    </section>

    <!-- Main Features -->
    <section class="section-padding bg-white">
      <div class="container-max">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12">
          <div
            v-for="(feature, index) in features"
            :key="index"
            class="flex items-start space-x-6"
          >
            <div class="flex-shrink-0">
              <div class="w-16 h-16 bg-blue-100 rounded-xl flex items-center justify-center">
                <svg class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="feature.icon" />
                </svg>
              </div>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-gray-900 mb-3">
                {{ feature.title }}
              </h3>
              <p class="text-gray-600">
                {{ feature.description }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Technical Stack -->
    <section class="section-padding gradient-bg">
      <div class="container-max">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">
            技术架构
          </h2>
          <p class="text-gray-700 max-w-2xl mx-auto">
            基于现代化技术栈构建，确保系统的高性能和可扩展性
          </p>
        </div>
        
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
          <div
            v-for="(tech, index) in techStack"
            :key="index"
            class="bg-white rounded-lg p-6 text-center shadow-sm hover:shadow-md transition-shadow"
          >
            <h3 class="font-semibold text-gray-900 mb-2">{{ tech.name }}</h3>
            <p class="text-sm text-gray-600">{{ tech.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Architecture Diagram -->
    <section class="section-padding bg-white">
      <div class="container-max">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">
            系统架构
          </h2>
          <p class="text-gray-700 max-w-2xl mx-auto">
            采用微服务架构设计，支持高并发和水平扩展
          </p>
        </div>
        
        <div class="bg-gray-50 rounded-xl p-8">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
            <div>
              <div class="bg-blue-500 text-white rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <span class="text-xl font-bold">前端</span>
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">Vue 3 应用</h3>
              <p class="text-gray-600 text-sm">管理后台 + 访客端 + 移动端</p>
            </div>
            
            <div>
              <div class="bg-green-500 text-white rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <span class="text-xl font-bold">API</span>
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">后端服务</h3>
              <p class="text-gray-600 text-sm">Spring Boot 微服务</p>
            </div>
            
            <div>
              <div class="bg-purple-500 text-white rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <span class="text-xl font-bold">IM</span>
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">消息服务</h3>
              <p class="text-gray-600 text-sm">WuKongIM 实时通讯</p>
            </div>
          </div>
          
          <div class="mt-8 pt-8 border-t border-gray-200">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div>
                <h4 class="font-semibold text-gray-900 mb-3">基础设施</h4>
                <ul class="text-gray-600 space-y-1">
                  <li>• Docker 容器化部署</li>
                  <li>• Nginx 负载均衡</li>
                  <li>• Redis 缓存服务</li>
                  <li>• PostgreSQL 数据库</li>
                </ul>
              </div>
              <div>
                <h4 class="font-semibold text-gray-900 mb-3">监控运维</h4>
                <ul class="text-gray-600 space-y-1">
                  <li>• 日志收集分析</li>
                  <li>• 性能监控告警</li>
                  <li>• 自动化部署</li>
                  <li>• 故障自动恢复</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>