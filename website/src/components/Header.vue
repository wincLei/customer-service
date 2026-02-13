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
        <nav class="hidden md:flex items-center space-x-6">
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
          
          <!-- External Links -->
          <a
            href="https://github.com/wincLei/customer-service"
            target="_blank"
            rel="noopener noreferrer"
            class="font-medium text-gray-700 hover:text-blue-600 transition-colors duration-200 flex items-center space-x-1"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            <span>{{ t('nav.sourceCode') }}</span>
          </a>
          
          <a
            href="https://github.com/wincLei/customer-service/wiki"
            target="_blank"
            rel="noopener noreferrer"
            class="font-medium text-gray-700 hover:text-blue-600 transition-colors duration-200 flex items-center space-x-1"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-1 17l-5-5 1.41-1.41 3.59 3.59 7.59-7.59 1.41 1.41-9 9z"/>
            </svg>
            <span>{{ t('nav.documentation') }}</span>
          </a>
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