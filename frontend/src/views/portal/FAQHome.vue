<template>
  <div class="portal-home">
    <!-- 加载中 -->
    <div v-if="loading" class="loading-section">
      <div class="loading-spinner"></div>
      <span>{{ $t('common.loading') }}</span>
    </div>

    <template v-else>
      <!-- FAQ搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('faq.searchPlaceholder')"
          clearable
          @input="handleSearch"
          @clear="handleClearSearch"
        >
          <template #prefix>
            <i class="el-icon-search"></i>
          </template>
        </el-input>
      </div>

      <!-- 搜索结果 -->
      <div v-if="isSearchMode" class="search-results-section">
        <h3>{{ $t('faq.searchResults') }} <span v-if="searchResults.length > 0" class="result-count">{{ $t('faq.resultCount', { count: searchResults.length }) }}</span></h3>
        <div class="faq-list" v-if="searchResults.length > 0">
          <div
            v-for="article in searchResults"
            :key="article.id"
            class="faq-item"
            @click="viewFAQ(article)"
          >
            <div class="faq-title">{{ article.title }}</div>
            <div class="faq-excerpt">{{ article.excerpt }}</div>
          </div>
        </div>
        <div v-else class="empty-results">
          <p>{{ $t('faq.noResults') }}</p>
          <p class="empty-hint">{{ $t('faq.tryOtherKeywords') }}</p>
        </div>
      </div>

      <template v-else>
        <!-- 热门问题 -->
        <div class="hot-section" v-if="hotFAQs.length > 0">
          <h3>{{ $t('faq.hotQuestions') }}</h3>
          <div class="faq-grid">
            <div
              v-for="faq in hotFAQs"
              :key="faq.id"
              class="faq-card"
              @click="viewFAQ(faq)"
            >
              <div class="faq-title">{{ faq.title }}</div>
              <div class="faq-views">{{ $t('faq.viewCount', { count: faq.viewCount || 0 }) }}</div>
            </div>
          </div>
        </div>

        <!-- FAQ分类 -->
        <div class="category-section" v-if="categories.length > 0">
          <h3>{{ $t('faq.categories') }}</h3>
          <el-collapse>
            <el-collapse-item
              v-for="category in categories"
              :key="category.id"
              :title="`${category.name} (${category.articles.length})`"
              :name="category.id"
            >
              <div class="faq-list">
                <div
                  v-for="article in category.articles"
                  :key="article.id"
                  class="faq-item"
                  @click="viewFAQ(article)"
                >
                  <div class="faq-title">{{ article.title }}</div>
                  <div class="faq-excerpt">{{ article.excerpt }}</div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- 无数据 -->
        <div v-if="hotFAQs.length === 0 && categories.length === 0" class="empty-section">
          <p>{{ $t('faq.noFaq') }}</p>
        </div>
      </template>
    </template>

    <!-- FAQ详情对话框 -->
    <el-dialog v-model="showFAQDetail" :title="selectedFAQ?.title" width="90%" top="5vh">
      <div class="faq-detail-content markdown-body" v-html="renderedContent"></div>
      <div v-if="selectedFAQ && !selectedFAQ.content" class="loading-detail">
        <div class="loading-spinner"></div>
        <span>{{ $t('common.loading') }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import portalApi from '@/api/portal'
import { logger } from '@/utils/logger'

interface FAQ {
  id: number
  title: string
  content?: string
  excerpt?: string
  viewCount?: number
}

interface CategoryGroup {
  id: number
  name: string
  articles: FAQ[]
}

const { t } = useI18n()
const route = useRoute()
const loading = ref(true)
const searchQuery = ref('')
const showFAQDetail = ref(false)
const selectedFAQ = ref<FAQ | null>(null)
const isSearchMode = ref(false)
const searchResults = ref<FAQ[]>([])

const hotFAQs = ref<FAQ[]>([])
const categories = ref<CategoryGroup[]>([])

// 配置 marked 选项
marked.setOptions({
  breaks: true,
  gfm: true,
})

// 将 Markdown 内容渲染为 HTML
const renderedContent = computed(() => {
  if (!selectedFAQ.value?.content) return ''
  try {
    return marked(selectedFAQ.value.content) as string
  } catch (e) {
    logger.error('Markdown parse error:', e)
    return selectedFAQ.value.content
  }
})

// 从 URL 获取 project_id
const getProjectId = (): string => {
  return (route.query.project_id as string) || (route.query.projectId as string) || '1'
}

// 加载知识库数据
const fetchKbData = async () => {
  loading.value = true
  try {
    const pid = getProjectId()
    const response = await portalApi.get(`/pub/kb/articles?projectId=${pid}`) as any
    if (response.code === 0 && response.data) {
      hotFAQs.value = response.data.hotArticles || []
      categories.value = response.data.categories || []
    }
  } catch (error) {
    logger.error('Failed to fetch KB data:', error)
  } finally {
    loading.value = false
  }
}

// 搜索防抖
let searchTimer: ReturnType<typeof setTimeout> | null = null

const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)

  const keyword = searchQuery.value.trim()
  if (!keyword) {
    isSearchMode.value = false
    searchResults.value = []
    return
  }

  searchTimer = setTimeout(async () => {
    isSearchMode.value = true
    try {
      const pid = getProjectId()
      const response = await portalApi.get(`/pub/kb/articles/search?projectId=${pid}&keyword=${encodeURIComponent(keyword)}`) as any
      if (response.code === 0 && response.data) {
        searchResults.value = response.data
      }
    } catch (error) {
      logger.error('Failed to search articles:', error)
      searchResults.value = []
    }
  }, 300)
}

const handleClearSearch = () => {
  isSearchMode.value = false
  searchResults.value = []
}

const viewFAQ = async (faq: FAQ) => {
  selectedFAQ.value = { ...faq, content: undefined }
  showFAQDetail.value = true

  // 加载文章详情
  try {
    const response = await portalApi.get(`/pub/kb/articles/${faq.id}`) as any
    if (response.code === 0 && response.data) {
      selectedFAQ.value = {
        ...faq,
        content: response.data.content,
        viewCount: response.data.viewCount
      }
    }
  } catch (error) {
    logger.error('Failed to fetch article detail:', error)
    selectedFAQ.value = { ...faq, content: `<p>${t('faq.loadFailed')}</p>` }
  }
}

onMounted(() => {
  fetchKbData()
})
</script>

<style scoped lang="css">
.portal-home {
  padding: 20px 0;
}

.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
  gap: 12px;
}

.loading-spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 3px solid #eee;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.search-section {
  margin-bottom: 30px;
}

.search-results-section {
  margin-bottom: 30px;
}

.search-results-section h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

.result-count {
  font-size: 14px;
  color: #999;
  font-weight: normal;
}

.empty-results {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-results p:first-child {
  font-size: 18px;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: #ccc;
}

.hot-section {
  margin-bottom: 40px;
}

.hot-section h3,
.category-section h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

.faq-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.faq-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.faq-card:hover {
  transform: translateY(-4px);
}

.faq-card .faq-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 8px;
  color: white;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.faq-views {
  font-size: 12px;
  opacity: 0.8;
}

.category-section {
  margin-bottom: 30px;
}

.faq-list {
  padding: 16px 0;
}

.faq-item {
  padding: 12px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.faq-item:hover {
  background-color: #f5f7fa;
  border-color: #1890ff;
}

.faq-item .faq-title {
  color: #1890ff;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
}

.faq-excerpt {
  font-size: 12px;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-section {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.faq-detail-content {
  line-height: 1.8;
  font-size: 14px;
  color: #333;
  word-break: break-word;
}

.faq-detail-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}

.faq-detail-content :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid #e1e4e8;
}

.faq-detail-content :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
}

.faq-detail-content :deep(:not(pre) > code) {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 3px;
  color: #c7254e;
}

.faq-detail-content :deep(h1),
.faq-detail-content :deep(h2),
.faq-detail-content :deep(h3),
.faq-detail-content :deep(h4) {
  margin-top: 20px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
}

.faq-detail-content :deep(h1) { font-size: 22px; }
.faq-detail-content :deep(h2) { font-size: 19px; }
.faq-detail-content :deep(h3) { font-size: 16px; }
.faq-detail-content :deep(h4) { font-size: 14px; }

.faq-detail-content :deep(p) {
  margin-bottom: 12px;
}

.faq-detail-content :deep(ul),
.faq-detail-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 12px;
}

.faq-detail-content :deep(li) {
  margin-bottom: 6px;
}

.faq-detail-content :deep(blockquote) {
  border-left: 4px solid #1890ff;
  padding: 10px 16px;
  margin: 12px 0;
  background: #f5f7fa;
  color: #666;
  border-radius: 0 4px 4px 0;
}

.faq-detail-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.faq-detail-content :deep(th),
.faq-detail-content :deep(td) {
  border: 1px solid #e1e4e8;
  padding: 8px 12px;
  text-align: left;
}

.faq-detail-content :deep(th) {
  background: #f6f8fa;
  font-weight: 600;
}

.faq-detail-content :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.faq-detail-content :deep(a:hover) {
  text-decoration: underline;
}

.faq-detail-content :deep(hr) {
  border: none;
  border-top: 1px solid #e1e4e8;
  margin: 20px 0;
}

.loading-detail {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
  gap: 8px;
}
</style>
