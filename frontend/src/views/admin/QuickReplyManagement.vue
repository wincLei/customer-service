<template>
  <div class="quick-reply-management">
    <div class="page-header">
      <h2>{{ $t('quickReplyMgmt.title') }}</h2>
      <div class="header-actions">
        <el-select 
          v-model="selectedProjectId" 
          :placeholder="$t('quickReplyMgmt.selectProject')"
          style="width: 200px; margin-right: 16px;"
          @change="onProjectChange"
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
        <el-button type="primary" @click="showCreateDialog" :disabled="!selectedProjectId">
          <el-icon><Plus /></el-icon>
          {{ $t('quickReplyMgmt.newReply') }}
        </el-button>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-bar" v-if="selectedProjectId">
      <el-radio-group v-model="selectedCategory" @change="filterByCategory">
        <el-radio-button label="">{{ $t('quickReplyMgmt.all') }}</el-radio-button>
        <el-radio-button 
          v-for="cat in categories" 
          :key="cat" 
          :label="cat"
        >
          {{ cat }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 快捷回复列表 -->
    <el-table 
      v-if="selectedProjectId"
      :data="filteredReplies" 
      style="width: 100%" 
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" :label="$t('quickReplyMgmt.replyTitle')" width="200">
        <template #default="{ row }">
          {{ row.title || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="content" :label="$t('quickReplyMgmt.content')" min-width="300">
        <template #default="{ row }">
          <div class="content-preview">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="category" :label="$t('quickReplyMgmt.category')" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.category" size="small">{{ row.category }}</el-tag>
          <span v-else class="text-muted">{{ $t('quickReplyMgmt.uncategorized') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.createdAt')" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">{{ $t('common.edit') }}</el-button>
          <el-button link type="danger" @click="deleteReply(row)">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!selectedProjectId" :description="$t('quickReplyMgmt.selectProjectFirst')" />

    <!-- 创建/编辑快捷回复对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('quickReplyMgmt.editReply') : $t('quickReplyMgmt.newReply')"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item :label="$t('quickReplyMgmt.replyTitle')">
          <el-input v-model="form.title" :placeholder="$t('quickReplyMgmt.titlePlaceholder')" maxlength="100" />
        </el-form-item>
        <el-form-item :label="$t('quickReplyMgmt.content')" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="6"
            :placeholder="$t('quickReplyMgmt.contentPlaceholder')" 
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('quickReplyMgmt.category')">
          <el-select 
            v-model="form.category" 
            :placeholder="$t('quickReplyMgmt.categoryPlaceholder')"
            filterable
            allow-create
            clearable
            style="width: 100%;"
          >
            <el-option
              v-for="cat in categories"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? $t('common.save') : $t('common.create') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/api'

const { t } = useI18n()

interface Project {
  id: number
  name: string
}

interface QuickReply {
  id: number
  projectId: number
  title: string
  content: string
  category: string
  creatorId: number
  createdAt: string
}

const loading = ref(false)
const submitting = ref(false)
const selectedProjectId = ref<number | null>(null)
const selectedCategory = ref('')
const projects = ref<Project[]>([])
const replies = ref<QuickReply[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  content: '',
  category: ''
})

const rules: FormRules = {
  content: [
    { required: true, message: () => t('quickReplyMgmt.contentRequired'), trigger: 'blur' },
    { min: 1, max: 1000, message: () => t('quickReplyMgmt.contentLength'), trigger: 'blur' }
  ]
}

// 获取所有分类
const categories = computed(() => {
  const cats = new Set<string>()
  replies.value.forEach(r => {
    if (r.category) cats.add(r.category)
  })
  return Array.from(cats).sort()
})

// 根据分类筛选
const filteredReplies = computed(() => {
  if (!selectedCategory.value) {
    return replies.value
  }
  return replies.value.filter(r => r.category === selectedCategory.value)
})

// 加载项目列表
const loadProjects = async () => {
  try {
    const res = await request.get('/api/admin/projects/all') as any
    if (res.code === 0) {
      projects.value = res.data
      // 默认选择第一个项目
      if (projects.value.length > 0 && !selectedProjectId.value) {
        selectedProjectId.value = projects.value[0].id
        onProjectChange()
      }
    }
  } catch (error) {
    console.error('加载项目失败', error)
  }
}

// 项目切换
const onProjectChange = () => {
  selectedCategory.value = ''
  loadReplies()
}

// 分类筛选
const filterByCategory = () => {
  // 分类变化时，filteredReplies 会自动更新
}

// 加载快捷回复列表
const loadReplies = async () => {
  if (!selectedProjectId.value) return

  loading.value = true
  try {
    const res = await request.get('/api/admin/quick-replies', {
      params: { projectIds: selectedProjectId.value }
    }) as any
    if (res.code === 0) {
      replies.value = res.data
    } else {
      ElMessage.error(res.message || t('common.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('quickReplyMgmt.loadFailed'))
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  form.title = ''
  form.content = ''
  form.category = ''
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (reply: QuickReply) => {
  isEdit.value = true
  editingId.value = reply.id
  form.title = reply.title || ''
  form.content = reply.content
  form.category = reply.category || ''
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      // 更新
      const res = await request.put(`/api/admin/quick-replies/${editingId.value}`, {
        title: form.title,
        content: form.content,
        category: form.category
      }) as any
      if (res.code === 0) {
        ElMessage.success(t('common.updateSuccess'))
        dialogVisible.value = false
        loadReplies()
      } else {
        ElMessage.error(res.message || t('common.updateFailed'))
      }
    } else {
      // 创建
      const res = await request.post('/api/admin/quick-replies', {
        projectId: selectedProjectId.value,
        title: form.title,
        content: form.content,
        category: form.category
      }) as any
      if (res.code === 0) {
        ElMessage.success(t('common.createSuccess'))
        dialogVisible.value = false
        loadReplies()
      } else {
        ElMessage.error(res.message || t('common.createFailed'))
      }
    }
  } catch (error) {
    ElMessage.error(t('common.operateFailed'))
  } finally {
    submitting.value = false
  }
}

// 删除快捷回复
const deleteReply = async (reply: QuickReply) => {
  try {
    await ElMessageBox.confirm(
      t('quickReplyMgmt.deleteConfirm', { name: reply.title || reply.content.substring(0, 20) }),
      t('quickReplyMgmt.confirmDeleteTitle'),
      { type: 'warning' }
    )

    const res = await request.delete(`/api/admin/quick-replies/${reply.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadReplies()
    } else {
      ElMessage.error(res.message || t('common.deleteFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.deleteFailed'))
    }
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.quick-reply-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.filter-bar {
  margin-bottom: 16px;
}

.content-preview {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted {
  color: #909399;
}
</style>
