<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const contactForm = ref({
  name: '',
  email: '',
  message: ''
})

const contactInfo = [
  {
    title: 'emailTitle',
    value: 'leijiang@fulitoutiao.cn',
    icon: 'M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z'
  },
  {
    title: 'phone',
    value: '+86 13386232717',
    icon: 'M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z'
  },
  {
    title: 'address',
    value: '上海市闵行区华一实业大厦 ',
    icon: 'M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z'
  }
]

const handleSubmit = () => {
  // 这里可以添加表单提交逻辑
  console.log('Form submitted:', contactForm.value)
  alert(t('contact.submitSuccess'))
  contactForm.value = { name: '', email: '', message: '' }
}
</script>

<template>
  <div class="min-h-screen">
    <!-- Hero Section -->
    <section class="gradient-bg">
      <div class="container-max section-padding">
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
            {{ t('contact.title') }}
          </h1>
          <p class="text-xl text-gray-700">
            {{ t('contact.subtitle') }}
          </p>
        </div>
      </div>
    </section>

    <!-- Contact Content -->
    <section class="section-padding bg-white">
      <div class="container-max">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12">
          <!-- Contact Form -->
          <div class="card">
            <h2 class="text-2xl font-bold text-gray-900 mb-6">
              {{ t('contact.sendMessage') }}
            </h2>
            <form @submit.prevent="handleSubmit" class="space-y-6">
              <div>
                <label for="name" class="block text-sm font-medium text-gray-700 mb-2">
                  {{ t('contact.form.name') }}
                </label>
                <input
                  id="name"
                  v-model="contactForm.name"
                  type="text"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  :placeholder="t('contact.form.name')"
                />
              </div>

              <div>
                <label for="email" class="block text-sm font-medium text-gray-700 mb-2">
                  {{ t('contact.form.email') }}
                </label>
                <input
                  id="email"
                  v-model="contactForm.email"
                  type="email"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  :placeholder="t('contact.form.email')"
                />
              </div>

              <div>
                <label for="message" class="block text-sm font-medium text-gray-700 mb-2">
                  {{ t('contact.form.message') }}
                </label>
                <textarea
                  id="message"
                  v-model="contactForm.message"
                  rows="5"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  :placeholder="t('contact.form.message')"
                ></textarea>
              </div>

              <button type="submit" class="btn-primary w-full">
                {{ t('contact.form.submit') }}
              </button>
            </form>
          </div>

          <!-- Contact Information -->
          <div>
            <div class="mb-12">
              <h2 class="text-2xl font-bold text-gray-900 mb-6">
                {{ t('contact.contactInfo') }}
              </h2>
              <div class="space-y-6">
                <div
                  v-for="(info, index) in contactInfo"
                  :key="index"
                  class="flex items-start space-x-4"
                >
                  <div class="flex-shrink-0">
                    <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                      <svg class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="info.icon" />
                      </svg>
                    </div>
                  </div>
                  <div>
                    <h3 class="font-semibold text-gray-900 mb-1">
                      {{ t(`contact.${info.title}`) }}
                    </h3>
                    <p class="text-gray-600">
                      {{ info.value }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Business Hours -->
            <div class="card">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">
                {{ t('contact.businessHours') }}
              </h3>
              <div class="space-y-2 text-gray-600">
                <div class="flex justify-between">
                  <span>{{ t('contact.weekdays.mondayToFriday') }}</span>
                  <span>9:00 - 18:00</span>
                </div>
                <div class="flex justify-between">
                  <span>{{ t('contact.weekdays.saturday') }}</span>
                  <span>9:00 - 12:00</span>
                </div>
                <div class="flex justify-between">
                  <span>{{ t('contact.weekdays.sunday') }}</span>
                  <span>{{ t('contact.weekdays.rest') }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Map Section -->
    <section class="section-padding gradient-bg">
      <div class="container-max">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">
            {{ t('contact.companyLocation') }}
          </h2>
          <p class="text-gray-700 max-w-2xl mx-auto">
            {{ t('contact.locationDescription') }}
          </p>
        </div>

        <div class="bg-white rounded-xl shadow-sm overflow-hidden">
          <div class="h-96 bg-gray-200 flex items-center justify-center">
            <div class="text-center">
              <svg class="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <p class="text-gray-500">地图位置展示区域</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- FAQ Section -->
    <section class="section-padding bg-white">
      <div class="container-max">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">
            {{ t('contact.faqTitle') }}
          </h2>
          <p class="text-gray-700 max-w-2xl mx-auto">
            {{ t('contact.faqDescription') }}
          </p>
        </div>

        <div class="max-w-3xl mx-auto space-y-6">
          <div class="card">
            <h3 class="font-semibold text-gray-900 mb-2">
              {{ t('contact.faq.q1') }}
            </h3>
            <p class="text-gray-600">
              {{ t('contact.faq.a1') }}
            </p>
          </div>

          <div class="card">
            <h3 class="font-semibold text-gray-900 mb-2">
              {{ t('contact.faq.q2') }}
            </h3>
            <p class="text-gray-600">
              {{ t('contact.faq.a2') }}
            </p>
          </div>

          <div class="card">
            <h3 class="font-semibold text-gray-900 mb-2">
              {{ t('contact.faq.q3') }}
            </h3>
            <p class="text-gray-600">
              {{ t('contact.faq.a3') }}
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
