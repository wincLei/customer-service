<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted } from 'vue'

const { t, locale } = useI18n()

const features = [
  {
    title: 'features.list.0.title',
    description: 'features.list.0.description',
    icon: 'M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z'
  },
  {
    title: 'features.list.1.title',
    description: 'features.list.1.description',
    icon: 'M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z'
  },
  {
    title: 'features.list.2.title',
    description: 'features.list.2.description',
    icon: 'M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z'
  },
  {
    title: 'features.list.3.title',
    description: 'features.list.3.description',
    icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'
  }
]

const techStack = [
  { 
    key: locale.value === 'zh' ? 'features.frontendApp' : 'features.techStack.frontendAppShort', 
    descriptionKey: 'features.architecture.frontendDesc' 
  },
  { key: 'common.typescript', descriptionKey: 'features.techStack.typescriptDesc' },
  { 
    key: locale.value === 'zh' ? 'features.im' : 'features.techStack.imShort', 
    descriptionKey: 'features.architecture.imDesc' 
  },
  { 
    key: locale.value === 'zh' ? 'features.backend' : 'features.techStack.backendShort', 
    descriptionKey: 'features.architecture.backendDesc' 
  },
  { key: 'features.postgresql', descriptionKey: 'features.postgresqlDesc' },
  { key: 'features.docker', descriptionKey: 'features.dockerDesc' }
]
</script>

<template>
  <div class="min-h-screen">
    <!-- SEO Semantic Headings -->
    <h1 class="sr-only">极简客服系统：为效率而生的开源客服方案</h1>
    <h2 class="sr-only">功能特性: 多端支持、智能分配、多渠道接入、数据统计</h2>
    <h3 class="sr-only">安装指南: 容器化部署、负载均衡、缓存服务</h3>
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
                {{ t(feature.title) }}
              </h3>
              <p class="text-gray-600">
                {{ t(feature.description) }}
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
            {{ t('features.techStack.title') }}
          </h2>
          <p class="text-gray-700 max-w-2xl mx-auto">
            {{ t('features.techStack.description') }}
          </p>
        </div>
        
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
          <div
            v-for="(tech, index) in techStack"
            :key="index"
            class="bg-white rounded-lg p-6 text-center shadow-sm hover:shadow-md transition-shadow"
          >
            <h3 class="font-semibold text-gray-900 mb-2">{{ t(tech.key) }}</h3>
            <p class="text-sm text-gray-600">{{ t(tech.descriptionKey) }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Architecture Diagram -->
    <section class="section-padding bg-white">
      <div class="container-max">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">
            {{ t('features.architecture.title') }}
          </h2>
          <p class="text-gray-700 max-w-2xl mx-auto">
            {{ t('features.architecture.description') }}
          </p>
        </div>
        
        <div class="bg-gray-50 rounded-xl p-8">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
            <div>
              <div class="bg-blue-500 text-white rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4">
                <span class="text-xl font-bold whitespace-normal text-center">
                  {{ locale === 'zh' ? t('features.frontendApp') : t('features.architecture.frontendAppShort') }}
                </span>
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">{{ t('features.frontendApp') }}</h3>
              <p class="text-gray-600 text-sm">{{ t('features.architecture.frontendDesc') }}</p>
            </div>
            
            <div>
              <div class="bg-green-500 text-white rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4">
                <span class="text-xl font-bold whitespace-normal text-center">
                  {{ locale === 'zh' ? t('features.backend') : t('features.architecture.backendShort') }}
                </span>
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">{{ t('features.backend') }}</h3>
              <p class="text-gray-600 text-sm">{{ t('features.architecture.backendDesc') }}</p>
            </div>
            
            <div>
              <div class="bg-purple-500 text-white rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4">
                <span class="text-xl font-bold whitespace-normal text-center">
                  {{ locale === 'zh' ? t('features.im') : t('features.architecture.imShort') }}
                </span>
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">{{ t('features.im') }}</h3>
              <p class="text-gray-600 text-sm">{{ t('features.architecture.imDesc') }}</p>
            </div>
          </div>
          
          <div class="mt-8 pt-8 border-t border-gray-200">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div>
                <h4 class="font-semibold text-gray-900 mb-3">{{ t('features.architecture.infrastructure') }}</h4>
                <ul class="text-gray-600 space-y-1">
                  <li>• {{ t('features.infrastructureItems.docker') }}</li>
                  <li>• {{ t('features.infrastructureItems.nginx') }}</li>
                  <li>• {{ t('features.infrastructureItems.redis') }}</li>
                  <li>• {{ t('features.infrastructureItems.postgresql') }}</li>
                </ul>
              </div>
              <div>
                <h4 class="font-semibold text-gray-900 mb-3">{{ t('features.monitoring') }}</h4>
                <ul class="text-gray-600 space-y-1">
                  <li>• {{ t('features.monitoringItems.logs') }}</li>
                  <li>• {{ t('features.monitoringItems.performance') }}</li>
                  <li>• {{ t('features.monitoringItems.deploy') }}</li>
                  <li>• {{ t('features.monitoringItems.recovery') }}</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>

  <!-- FAQ Section -->
  <FAQSection />

</template>