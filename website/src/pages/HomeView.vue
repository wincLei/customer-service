<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()

const features = [
  {
    title: 'home.features.realtime.title',
    description: 'home.features.realtime.description',
    icon: 'M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z'
  },
  {
    title: 'home.features.multichannel.title',
    description: 'home.features.multichannel.description',
    icon: 'M12 18h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z'
  },
  {
    title: 'home.features.management.title',
    description: 'home.features.management.description',
    icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2'
  }
]

const stats = [
  { label: 'home.stats.projects', value: '100+' },
  { label: 'home.stats.agents', value: '50+' },
  { label: 'home.stats.conversations', value: '1000+' },
  { label: 'home.stats.satisfaction', value: '98%' }
]

const handleCTAClick = () => {
  router.push('/demo')
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="gradient-bg">
      <div class="container-max section-padding">
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
            {{ t('home.hero.title') }}
          </h1>
          <p class="text-xl md:text-2xl text-gray-700 mb-4">
            {{ t('home.hero.subtitle') }}
          </p>
          <p class="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
            {{ t('home.hero.description') }}
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center">
            <button @click="handleCTAClick" class="btn-primary text-lg px-8 py-4">
              {{ t('home.hero.cta') }}
            </button>
            <a href="https://github.com" target="_blank" class="btn-secondary text-lg px-8 py-4">
              {{ t('home.hero.github') }}
            </a>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="section-padding bg-white">
      <div class="container-max">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            {{ t('home.features.title') }}
          </h2>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div
            v-for="(feature, index) in features"
            :key="index"
            class="card text-center"
          >
            <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="feature.icon" />
              </svg>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-3">
              {{ t(feature.title) }}
            </h3>
            <p class="text-gray-600">
              {{ t(feature.description) }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="section-padding gradient-bg">
      <div class="container-max">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div
            v-for="(stat, index) in stats"
            :key="index"
            class="text-center"
          >
            <div class="text-3xl md:text-4xl font-bold text-blue-600 mb-2">
              {{ stat.value }}
            </div>
            <div class="text-gray-700 font-medium">
              {{ t(stat.label) }}
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="section-padding bg-white">
      <div class="container-max text-center">
        <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-6">
          准备开始使用了吗？
        </h2>
        <p class="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
          立即体验极简客服系统，为您的企业提供专业高效的客户服务解决方案
        </p>
        <button @click="handleCTAClick" class="btn-primary text-lg px-8 py-4">
          {{ t('home.hero.cta') }}
        </button>
      </div>
    </section>
  </div>
</template>