<template>
  <div class="portal-home">
    <!-- 加载中 -->
    <div v-if="loading" class="loading-section">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <template v-else>
      <!-- FAQ搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="搜索常见问题..."
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
        <h3>搜索结果 <span v-if="searchResults.length > 0" class="result-count">({{ searchResults.length }}条)</span></h3>
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
          <p>😔 没有找到相关问题</p>
          <p class="empty-hint">换个关键词试试</p>
        </div>
      </div>

      <template v-else>
        <!-- 热门问题 -->
        <div class="hot-section" v-if="hotFAQs.length > 0">
          <h3>🔥 热门问题</h3>
          <div class="faq-grid">
            <div
              v-for="faq in hotFAQs"
              :key="faq.id"
              class="faq-card"
              @click="viewFAQ(faq)"
            >
              <div class="faq-title">{{ faq.title }}</div>
              <div class="faq-views">{{ faq.viewCount || 0 }}次查看</div>
            </div>
          </div>
        </div>

        <!-- FAQ分类 -->
        <div class="category-section" v-if="categories.length > 0">
          <h3>📂 问题分类</h3>
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
          <p>📭 暂无常见问题</p>
        </div>
      </template>
    </template>

    <!-- FAQ详情对话框 -->
    <el-dialog v-model="showFAQDetail" :title="selectedFAQ?.title" width="90%" top="5vh">
      <div class="faq-detail-content" v-html="selectedFAQ?.content"></div>
      <div v-if="selectedFAQ && !selectedFAQ.content" class="loading-detail">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import portalApi from '@/api/portal'

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

const route = useRoute()
const loading = ref(true)
const searchQuery = ref('')
const showFAQDetail = ref(false)
const selectedFAQ = ref<FAQ | null>(null)
const isSearchMode = ref(false)
const searchResults = ref<FAQ[]>([])

const hotFAQs = ref<FAQ[]>([])
const categories = ref<CategoryGroup[]>([])

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
    console.error('Failed to fetch KB data:', error)
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
      console.error('Failed to search articles:', error)
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
    console.error('Failed to fetch article detail:', error)
    selectedFAQ.value = { ...faq, content: '<p>加载失败，请重试</p>' }
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
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
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
