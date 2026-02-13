<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const faqItems = t('faq.questions')
</script>

<template>
  <section class="section-padding bg-white">
    <div class="container-max">
      <div class="text-center mb-12">
        <h2 class="text-3xl font-bold text-gray-900 mb-4">
          {{ t('faq.title') }}
        </h2>
        <p class="text-gray-700 max-w-2xl mx-auto">
          {{ t('faq.description') }}
        </p>
      </div>
      
      <div class="max-w-4xl mx-auto">
        <div v-for="(item, index) in faqItems" :key="index" class="mb-6 border-b border-gray-200 pb-6">
          <h3 class="text-xl font-semibold text-gray-900 mb-3">
            {{ item.question }}
          </h3>
          <p class="text-gray-600 leading-relaxed">
            {{ item.answer }}
          </p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* FAQ section styling */
</style>