<template>
  <div class="knowledge-base">
    <div class="page-header">
      <h2>{{ $t('kbMgmt.title') }}</h2>
      <div class="header-actions">
        <el-select
          v-model="selectedProjectId"
          :placeholder="$t('kbMgmt.selectProject')"
          style="width: 200px; margin-right: 12px"
          @change="handleProjectChange"
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧分类树 -->
      <div class="category-panel">
        <div class="panel-header">
          <span>{{ $t('kbMgmt.categoryDir') }}</span>
          <el-button type="primary" link @click="showCreateCategoryDialog(null)" :disabled="!selectedProjectId">
            <el-icon><Plus /></el-icon> {{ $t('common.add') }}
          </el-button>
        </div>
        <div class="category-tree" v-loading="categoryLoading">
          <el-tree
            ref="categoryTreeRef"
            :data="categoryTree"
            :props="treeProps"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleCategoryClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span class="node-label">{{ node.label }}</span>
                <span class="node-count">({{ data.articleCount || 0 }})</span>
                <span class="node-actions">
                  <el-icon @click.stop="showCreateCategoryDialog(data)"><Plus /></el-icon>
                  <el-icon @click.stop="showEditCategoryDialog(data)"><Edit /></el-icon>
                  <el-icon @click.stop="deleteCategory(data)"><Delete /></el-icon>
                </span>
              </div>
            </template>
          </el-tree>
          <el-empty v-if="!categoryLoading && categoryTree.length === 0" :description="$t('kbMgmt.noCategories')" />
        </div>
      </div>

      <!-- 右侧文章列表 -->
      <div class="article-panel">
        <div class="panel-header">
          <div class="left">
            <span>{{ currentCategoryName || $t('kbMgmt.allArticles') }}</span>
            <el-input
              v-model="searchKeyword"
              :placeholder="$t('kbMgmt.searchPlaceholder')"
              style="width: 200px; margin-left: 16px"
              clearable
              @keyup.enter="loadArticles"
              @clear="loadArticles"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <el-button type="primary" @click="showCreateArticleDialog" :disabled="!selectedProjectId">
            <el-icon><Plus /></el-icon> {{ $t('kbMgmt.newArticle') }}
          </el-button>
        </div>

        <el-table :data="articles" style="width: 100%" v-loading="articleLoading">
          <el-table-column prop="title" :label="$t('kbMgmt.articleTitle')" min-width="200">
            <template #default="{ row }">
              <el-link type="primary" @click="showArticleDetail(row)">{{ row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column :label="$t('kbMgmt.category')" width="120">
            <template #default="{ row }">
              {{ getCategoryName(row.categoryId) }}
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" :label="$t('kbMgmt.viewCount')" width="80" />
          <el-table-column prop="hitCount" :label="$t('kbMgmt.hitCount')" width="90" />
          <el-table-column :label="$t('common.status')" width="80">
            <template #default="{ row }">
              <el-tag :type="row.isPublished ? 'success' : 'info'" size="small">
                {{ row.isPublished ? $t('kbMgmt.published') : $t('kbMgmt.draft') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="$t('common.updatedAt')" width="160">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt || row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('common.operation')" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="showEditArticleDialog(row)">{{ $t('common.edit') }}</el-button>
              <el-button 
                link 
                :type="row.isPublished ? 'warning' : 'success'"
                @click="togglePublish(row)"
              >
                {{ row.isPublished ? $t('kbMgmt.unpublish') : $t('kbMgmt.publish') }}
              </el-button>
              <el-button link type="danger" @click="deleteArticle(row)">{{ $t('common.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <Pagination
          v-model:page="pagination.page"
          v-model:size="pagination.size"
          :total="pagination.total"
          @change="loadArticles"
        />
      </div>
    </div>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="isCategoryEdit ? $t('kbMgmt.editCategory') : $t('kbMgmt.newCategory')"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="80px">
        <el-form-item :label="$t('kbMgmt.categoryName')" prop="name">
          <el-input v-model="categoryForm.name" :placeholder="$t('kbMgmt.categoryNamePlaceholder')" maxlength="50" />
        </el-form-item>
        <el-form-item :label="$t('kbMgmt.sort')" prop="sortOrder">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item :label="$t('kbMgmt.parentCategory')">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryTree"
            :props="treeSelectProps"
            :placeholder="$t('kbMgmt.parentCategoryPlaceholder')"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitCategory" :loading="submitting">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 文章编辑对话框 -->
    <el-dialog
      v-model="articleDialogVisible"
      :title="isArticleEdit ? $t('kbMgmt.editArticle') : $t('kbMgmt.newArticle')"
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="articleForm" :rules="articleRules" ref="articleFormRef" label-width="80px">
        <el-form-item :label="$t('kbMgmt.articleTitle')" prop="title">
          <el-input v-model="articleForm.title" :placeholder="$t('kbMgmt.titlePlaceholder')" maxlength="200" />
        </el-form-item>
        <el-form-item :label="$t('kbMgmt.belongCategory')" prop="categoryId">
          <el-tree-select
            v-model="articleForm.categoryId"
            :data="categoryTree"
            :props="treeSelectProps"
            :placeholder="$t('kbMgmt.selectCategory')"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('kbMgmt.articleContent')" prop="content">
          <el-input 
            v-model="articleForm.content" 
            type="textarea" 
            :rows="12" 
            :placeholder="$t('kbMgmt.contentPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="$t('kbMgmt.publishNow')">
          <el-switch v-model="articleForm.isPublished" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="articleDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitArticle" :loading="submitting">
          {{ isArticleEdit ? $t('common.save') : $t('common.create') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 文章详情对话框 -->
    <el-dialog
      v-model="articleDetailVisible"
      :title="currentArticle?.title"
      width="800px"
    >
      <div class="article-detail">
        <div class="article-meta">
          <el-tag :type="currentArticle?.isPublished ? 'success' : 'info'" size="small">
            {{ currentArticle?.isPublished ? $t('kbMgmt.published') : $t('kbMgmt.draft') }}
          </el-tag>
          <span>{{ $t('kbMgmt.category') }}{{ $t('common.colon') }}{{ getCategoryName(currentArticle?.categoryId) }}</span>
          <span>{{ $t('kbMgmt.views') }}{{ $t('common.colon') }}{{ currentArticle?.viewCount || 0 }}</span>
          <span>{{ $t('kbMgmt.hits') }}{{ $t('common.colon') }}{{ currentArticle?.hitCount || 0 }}</span>
        </div>
        <div class="article-content" v-html="renderedContent"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import request from '@/api'
import Pagination from '@/components/Pagination.vue'
import { marked } from 'marked'
import { logger } from '@/utils/logger'

const { t } = useI18n()

interface Project {
  id: number
  name: string
}

interface Category {
  id: number
  projectId: number
  parentId: number | null
  name: string
  sortOrder: number
  articleCount: number
  children?: Category[]
}

interface Article {
  id: number
  projectId: number
  categoryId: number
  title: string
  content: string
  viewCount: number
  hitCount: number
  isPublished: boolean
  createdAt: string
  updatedAt: string
}

const categoryLoading = ref(false)
const articleLoading = ref(false)
const submitting = ref(false)

const projects = ref<Project[]>([])
const selectedProjectId = ref<number | null>(null)
const categoryTree = ref<Category[]>([])
const flatCategories = ref<Category[]>([])
const articles = ref<Article[]>([])
const searchKeyword = ref('')
const currentCategoryId = ref<number | null>(null)
const currentCategoryName = ref('')

const categoryDialogVisible = ref(false)
const articleDialogVisible = ref(false)
const articleDetailVisible = ref(false)
const isCategoryEdit = ref(false)
const isArticleEdit = ref(false)
const editingCategoryId = ref<number | null>(null)
const editingArticleId = ref<number | null>(null)
const currentArticle = ref<Article | null>(null)

const categoryFormRef = ref<FormInstance>()
const articleFormRef = ref<FormInstance>()
const categoryTreeRef = ref()

const treeProps = {
  label: 'name',
  children: 'children'
}

const treeSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id'
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const categoryForm = reactive({
  name: '',
  sortOrder: 0,
  parentId: null as number | null
})

const articleForm = reactive({
  title: '',
  categoryId: null as number | null,
  content: '',
  isPublished: false
})

const categoryRules: FormRules = {
  name: [
    { required: true, message: () => t('kbMgmt.categoryNameRequired'), trigger: 'blur' },
    { max: 50, message: () => t('kbMgmt.categoryNameMaxLength'), trigger: 'blur' }
  ]
}

const articleRules: FormRules = {
  title: [
    { required: true, message: () => t('kbMgmt.titleRequired'), trigger: 'blur' },
    { max: 200, message: () => t('kbMgmt.titleMaxLength'), trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: () => t('kbMgmt.selectCategoryRequired'), trigger: 'change' }
  ],
  content: [
    { required: true, message: () => t('kbMgmt.contentRequired'), trigger: 'blur' }
  ]
}

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!currentArticle.value?.content) return ''
  return marked(currentArticle.value.content)
})

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取分类名称
const getCategoryName = (categoryId: number | undefined | null) => {
  if (!categoryId) return t('kbMgmt.uncategorized')
  const category = flatCategories.value.find(c => c.id === categoryId)
  return category?.name || t('kbMgmt.unknownCategory')
}

// 将树形结构扁平化
const flattenTree = (tree: Category[]): Category[] => {
  const result: Category[] = []
  const flatten = (nodes: Category[]) => {
    nodes.forEach(node => {
      result.push(node)
      if (node.children && node.children.length > 0) {
        flatten(node.children)
      }
    })
  }
  flatten(tree)
  return result
}

// 加载项目列表
const loadProjects = async () => {
  try {
    const res = await request.get('/api/admin/projects/all') as any
    if (res.code === 0) {
      projects.value = res.data
      if (projects.value.length > 0) {
        selectedProjectId.value = projects.value[0].id
        loadCategories()
        loadArticles()
      }
    }
  } catch (error) {
    logger.error('加载项目列表失败', error)
  }
}

// 处理项目切换
const handleProjectChange = () => {
  currentCategoryId.value = null
  currentCategoryName.value = ''
  loadCategories()
  loadArticles()
}

// 加载分类列表
const loadCategories = async () => {
  if (!selectedProjectId.value) return
  categoryLoading.value = true
  try {
    const res = await request.get('/api/admin/kb/categories/tree', {
      params: { projectId: selectedProjectId.value }
    }) as any
    if (res.code === 0) {
      categoryTree.value = res.data
      flatCategories.value = flattenTree(res.data)
    }
  } catch (error) {
    logger.error('加载分类失败', error)
  } finally {
    categoryLoading.value = false
  }
}

// 加载文章列表
const loadArticles = async () => {
  if (!selectedProjectId.value) return
  articleLoading.value = true
  try {
    const res = await request.get('/api/admin/kb/articles/page', {
      params: {
        projectId: selectedProjectId.value,
        categoryId: currentCategoryId.value || undefined,
        keyword: searchKeyword.value || undefined,
        page: pagination.page,
        size: pagination.size
      }
    }) as any
    if (res.code === 0) {
      articles.value = res.data.content
      pagination.total = res.data.totalElements
    }
  } catch (error) {
    logger.error('加载文章失败', error)
  } finally {
    articleLoading.value = false
  }
}

// 点击分类
const handleCategoryClick = (data: Category) => {
  currentCategoryId.value = data.id
  currentCategoryName.value = data.name
  pagination.page = 1
  loadArticles()
}

// 显示创建分类对话框
const showCreateCategoryDialog = (parentCategory: Category | null) => {
  if (!selectedProjectId.value) {
    ElMessage.warning(t('kbMgmt.selectProjectFirst'))
    return
  }
  isCategoryEdit.value = false
  editingCategoryId.value = null
  categoryForm.name = ''
  categoryForm.sortOrder = 0
  categoryForm.parentId = parentCategory?.id || null
  categoryDialogVisible.value = true
}

// 显示编辑分类对话框
const showEditCategoryDialog = (category: Category) => {
  isCategoryEdit.value = true
  editingCategoryId.value = category.id
  categoryForm.name = category.name
  categoryForm.sortOrder = category.sortOrder
  categoryForm.parentId = category.parentId
  categoryDialogVisible.value = true
}

// 提交分类
const submitCategory = async () => {
  if (!categoryFormRef.value) return
  if (!selectedProjectId.value) {
    ElMessage.warning(t('kbMgmt.selectProjectFirst'))
    return
  }
  await categoryFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const data = {
        projectId: selectedProjectId.value,
        name: categoryForm.name,
        sortOrder: categoryForm.sortOrder,
        parentId: categoryForm.parentId
      }
      
      if (isCategoryEdit.value) {
        const res = await request.put(`/api/admin/kb/categories/${editingCategoryId.value}`, data) as any
        if (res.code === 0) {
          ElMessage.success(t('kbMgmt.categoryUpdateSuccess'))
          categoryDialogVisible.value = false
          loadCategories()
        } else {
          ElMessage.error(res.message || t('common.updateFailed'))
        }
      } else {
        const res = await request.post('/api/admin/kb/categories', data) as any
        if (res.code === 0) {
          ElMessage.success(t('kbMgmt.categoryCreateSuccess'))
          categoryDialogVisible.value = false
          loadCategories()
        } else {
          ElMessage.error(res.message || t('common.createFailed'))
        }
      }
    } catch (error) {
      ElMessage.error(t('common.operateFailed'))
    } finally {
      submitting.value = false
    }
  })
}

// 删除分类
const deleteCategory = async (category: Category) => {
  try {
    await ElMessageBox.confirm(
      t('kbMgmt.deleteCategoryConfirm', { name: category.name }),
      t('kbMgmt.confirmDelete'),
      { type: 'warning' }
    )
    const res = await request.delete(`/api/admin/kb/categories/${category.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadCategories()
      loadArticles()
    } else {
      ElMessage.error(res.message || t('common.deleteFailed'))
    }
  } catch {
    // 用户取消
  }
}

// 显示创建文章对话框
const showCreateArticleDialog = () => {
  isArticleEdit.value = false
  editingArticleId.value = null
  articleForm.title = ''
  articleForm.categoryId = currentCategoryId.value
  articleForm.content = ''
  articleForm.isPublished = false
  articleDialogVisible.value = true
}

// 显示编辑文章对话框
const showEditArticleDialog = (article: Article) => {
  isArticleEdit.value = true
  editingArticleId.value = article.id
  articleForm.title = article.title
  articleForm.categoryId = article.categoryId
  articleForm.content = article.content
  articleForm.isPublished = article.isPublished
  articleDialogVisible.value = true
}

// 显示文章详情
const showArticleDetail = async (article: Article) => {
  currentArticle.value = article
  articleDetailVisible.value = true
  // 增加浏览次数
  try {
    await request.post(`/api/admin/kb/articles/${article.id}/view`)
  } catch {
    // 忽略错误
  }
}

// 提交文章
const submitArticle = async () => {
  if (!articleFormRef.value) return
  await articleFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const data = {
        projectId: selectedProjectId.value,
        categoryId: articleForm.categoryId,
        title: articleForm.title,
        content: articleForm.content,
        isPublished: articleForm.isPublished
      }
      
      if (isArticleEdit.value) {
        const res = await request.put(`/api/admin/kb/articles/${editingArticleId.value}`, data) as any
        if (res.code === 0) {
          ElMessage.success(t('kbMgmt.articleUpdateSuccess'))
          articleDialogVisible.value = false
          loadArticles()
          loadCategories()
        } else {
          ElMessage.error(res.message || t('common.updateFailed'))
        }
      } else {
        const res = await request.post('/api/admin/kb/articles', data) as any
        if (res.code === 0) {
          ElMessage.success(t('kbMgmt.articleCreateSuccess'))
          articleDialogVisible.value = false
          loadArticles()
          loadCategories()
        } else {
          ElMessage.error(res.message || t('common.createFailed'))
        }
      }
    } catch (error) {
      ElMessage.error(t('common.operateFailed'))
    } finally {
      submitting.value = false
    }
  })
}

// 切换发布状态
const togglePublish = async (article: Article) => {
  try {
    const action = article.isPublished ? t('kbMgmt.unpublish') : t('kbMgmt.publish')
    await ElMessageBox.confirm(t('kbMgmt.togglePublishConfirm', { action, title: article.title }), t('kbMgmt.confirmAction', { action }))
    const res = await request.post(`/api/admin/kb/articles/${article.id}/toggle-publish`) as any
    if (res.code === 0) {
      ElMessage.success(t('kbMgmt.actionSuccess', { action }))
      loadArticles()
    } else {
      ElMessage.error(res.message || t('common.operateFailed'))
    }
  } catch {
    // 用户取消
  }
}

// 删除文章
const deleteArticle = async (article: Article) => {
  try {
    await ElMessageBox.confirm(t('kbMgmt.deleteArticleConfirm', { title: article.title }), t('kbMgmt.confirmDelete'), { type: 'warning' })
    const res = await request.delete(`/api/admin/kb/articles/${article.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadArticles()
      loadCategories()
    } else {
      ElMessage.error(res.message || t('common.deleteFailed'))
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped lang="css">
.knowledge-base {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 8px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  white-space: nowrap;
}

.content-wrapper {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.category-panel {
  width: 280px;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

.article-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 500;
}

.panel-header .left {
  display: flex;
  align-items: center;
}

.category-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  padding-right: 8px;
}

.node-label {
  flex: 1;
}

.node-count {
  color: #909399;
  margin-left: 4px;
  font-size: 12px;
}

.node-actions {
  display: none;
  gap: 4px;
  margin-left: 8px;
}

.tree-node:hover .node-actions {
  display: flex;
}

.node-actions .el-icon {
  font-size: 14px;
  color: #909399;
  cursor: pointer;
}

.node-actions .el-icon:hover {
  color: #409eff;
}

.node-actions .el-icon:last-child:hover {
  color: #f56c6c;
}

.article-detail {
  padding: 0 16px;
}

.article-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  color: #909399;
  font-size: 13px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.article-content {
  line-height: 1.8;
  font-size: 14px;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 12px;
}

.article-content :deep(p) {
  margin-bottom: 12px;
}

.article-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.article-content :deep(pre) {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
}

.article-content :deep(pre code) {
  padding: 0;
  background: none;
}
</style>
