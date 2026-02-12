<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t, locale } = useI18n()
const router = useRouter()
const isMenuOpen = ref(false)

const toggleLanguage = () => {
  locale.value = locale.value === 'zh' ? 'en' : 'zh'
}

const navigation = [
  { name: 'nav.home', path: '/' },
  { name: 'nav.features', path: '/features' },
  { name: 'nav.docs', path: '/docs' },
  { name: 'nav.contact', path: '/contact' }
]

const isActive = (path: string) => {
  return router.currentRoute.value.path === path
}
</script>

<template>
  <header class="bg-white shadow-sm sticky top-0 z-50">
    <div class="container-max">
      <div class="flex justify-between items-center h-16">
        <!-- Logo -->
        <div class="flex items-center">
          <router-link to="/" class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
              <span class="text-white font-bold text-lg">CS</span>
            </div>
            <span class="text-xl font-bold text-gray-900">{{ t('app.title') }}</span>
          </router-link>
        </div>

        <!-- Desktop Navigation -->
        <nav class="hidden md:flex items-center space-x-8">
          <router-link
            v-for="item in navigation"
            :key="item.path"
            :to="item.path"
            :class="[
              'font-medium transition-colors duration-200',
              isActive(item.path)
                ? 'text-blue-600'
                : 'text-gray-700 hover:text-blue-600'
            ]"
          >
            {{ t(item.name) }}
          </router-link>
        </nav>

        <!-- Language Toggle & Mobile Menu Button -->
        <div class="flex items-center space-x-4">
          <button
            @click="toggleLanguage"
            class="px-3 py-1 text-sm font-medium text-gray-700 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors"
          >
            {{ locale === 'zh' ? 'EN' : '中文' }}
          </button>
          
          <button
            @click="isMenuOpen = !isMenuOpen"
            class="md:hidden p-2 rounded-md text-gray-700 hover:text-blue-600 hover:bg-gray-100"
          >
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path v-if="!isMenuOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Mobile Navigation -->
      <div v-show="isMenuOpen" class="md:hidden py-4 border-t">
        <div class="flex flex-col space-y-3">
          <router-link
            v-for="item in navigation"
            :key="item.path"
            :to="item.path"
            @click="isMenuOpen = false"
            :class="[
              'font-medium py-2 px-4 rounded-lg transition-colors duration-200',
              isActive(item.path)
                ? 'text-blue-600 bg-blue-50'
                : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
            ]"
          >
            {{ t(item.name) }}
          </router-link>
        </div>
      </div>
    </div>
  </header>
</template>