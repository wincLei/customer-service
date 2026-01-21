<template>
  <div class="portal-home">
    <!-- FAQ搜索框 -->
    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索常见问题..."
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <i class="el-icon-search"></i>
        </template>
      </el-input>
    </div>

    <!-- 热门问题 -->
    <div class="hot-section">
      <h3>热门问题</h3>
      <div class="faq-grid">
        <div
          v-for="faq in hotFAQs"
          :key="faq.id"
          class="faq-card"
          @click="viewFAQ(faq)"
        >
          <div class="faq-title">{{ faq.title }}</div>
          <div class="faq-views">{{ faq.views }}次查看</div>
        </div>
      </div>
    </div>

    <!-- FAQ分类 -->
    <div class="category-section">
      <h3>问题分类</h3>
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

    <!-- 联系客服按钮 -->
    <div class="action-section">
      <el-button type="primary" size="large" @click="contactService">
        联系客服
      </el-button>
    </div>

    <!-- FAQ详情对话框 -->
    <el-dialog v-model="showFAQDetail" :title="selectedFAQ?.title">
      <div v-html="selectedFAQ?.content"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

interface FAQ {
  id: number
  title: string
  content?: string
  excerpt?: string
  views?: number
}

const router = useRouter()
const searchQuery = ref('')
const showFAQDetail = ref(false)
const selectedFAQ = ref<FAQ | null>(null)

const hotFAQs = ref([
  { id: 1, title: '如何联系客服？', views: 1250, excerpt: '...' },
  { id: 2, title: '如何使用优惠券？', views: 980, excerpt: '...' },
  { id: 3, title: '退货流程说明', views: 850, excerpt: '...' },
  { id: 4, title: '发票开具指南', views: 720, excerpt: '...' },
])

const categories = ref([
  {
    id: 'account',
    name: '账户相关',
    articles: [
      { id: 1, title: '如何注册账号？', excerpt: '点击注册按钮...', content: '<p>详细内容...</p>' },
      { id: 2, title: '如何修改密码？', excerpt: '进入账户设置...', content: '<p>详细内容...</p>' },
    ],
  },
  {
    id: 'payment',
    name: '支付相关',
    articles: [
      { id: 3, title: '支持的支付方式？', excerpt: '我们支持...', content: '<p>详细内容...</p>' },
      { id: 4, title: '如何申请发票？', excerpt: '您可以在...', content: '<p>详细内容...</p>' },
    ],
  },
  {
    id: 'delivery',
    name: '订单与物流',
    articles: [
      { id: 5, title: '订单如何查询？', excerpt: '进入我的订单...', content: '<p>详细内容...</p>' },
      { id: 6, title: '退货流程说明', excerpt: '申请退货后...', content: '<p>详细内容...</p>' },
    ],
  },
])

const handleSearch = () => {
  // TODO: 实现搜索功能
}

const viewFAQ = (faq: any) => {
  selectedFAQ.value = faq
  showFAQDetail.value = true
}

const contactService = () => {
  router.push('/portal/chat')
}
</script>

<style scoped lang="css">
.portal-home {
  padding: 20px 0;
}

.search-section {
  margin-bottom: 30px;
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

.faq-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
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
}

.faq-excerpt {
  font-size: 12px;
  color: #666;
}

.action-section {
  text-align: center;
  margin-top: 40px;
}
</style>
