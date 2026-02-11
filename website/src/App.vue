<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'

const { locale } = useI18n()
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Header />
    <RouterView />
    <Footer />
  </div>
</template>

<style scoped>
</style>
