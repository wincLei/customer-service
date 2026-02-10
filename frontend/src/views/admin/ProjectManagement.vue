<template>
  <div class="project-management">
    <div class="page-header">
      <h2>{{ $t('projectMgmt.title') }}</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          :placeholder="$t('projectMgmt.searchPlaceholder')"
          style="width: 280px"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          {{ $t('projectMgmt.newProject') }}
        </el-button>
      </div>
    </div>

    <!-- 项目列表 -->
    <el-table :data="projects" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" :label="$t('projectMgmt.projectName')" min-width="150" />
      <el-table-column prop="description" :label="$t('common.description')" min-width="200" show-overflow-tooltip />
      <el-table-column label="AppKey" min-width="280">
        <template #default="{ row }">
          <div class="key-cell">
            <code>{{ row.appKey }}</code>
            <el-button link type="primary" @click="copyToClipboard(row.appKey)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="AppSecret" min-width="320">
        <template #default="{ row }">
          <div class="key-cell">
            <code v-if="visibleSecrets[row.id]">{{ row.appSecret }}</code>
            <code v-else>{{ maskSecret(row.appSecret) }}</code>
            <el-button link type="primary" @click="toggleSecretVisibility(row.id)">
              <el-icon><View v-if="!visibleSecrets[row.id]" /><Hide v-else /></el-icon>
            </el-button>
            <el-button link type="primary" @click="copyToClipboard(row.appSecret)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" :label="$t('common.createdAt')" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">{{ $t('common.edit') }}</el-button>
          <el-button link type="warning" @click="regenerateSecret(row)">{{ $t('projectMgmt.resetSecret') }}</el-button>
          <el-button link type="danger" @click="deleteProject(row)">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <Pagination
      v-model:page="pagination.page"
      v-model:size="pagination.size"
      :total="pagination.total"
      @change="handlePageChange"
    />

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('projectMgmt.editProject') : $t('projectMgmt.newProject')"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item :label="$t('projectMgmt.projectName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('projectMgmt.namePlaceholder')" maxlength="100" />
        </el-form-item>
        <el-form-item :label="$t('common.description')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            :placeholder="$t('projectMgmt.descPlaceholder')"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item :label="$t('projectMgmt.welcomeMessage')" prop="welcomeMessage">
          <el-input
            v-model="form.welcomeMessage"
            type="textarea"
            :rows="3"
            :placeholder="$t('projectMgmt.welcomeMsgPlaceholder')"
            maxlength="500"
            show-word-limit
          />
          <div class="form-tip">{{ $t('projectMgmt.welcomeMsgTip') }}</div>
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
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, CopyDocument, View, Hide, Search } from '@element-plus/icons-vue'
import request from '@/api'
import Pagination from '@/components/Pagination.vue'

const { t } = useI18n()

interface Project {
  id: number
  name: string
  description: string
  appKey: string
  appSecret: string
  welcomeMessage: string
  createdAt: string
  updatedAt: string
}

const loading = ref(false)
const submitting = ref(false)
const projects = ref<Project[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const visibleSecrets = ref<Record<number, boolean>>({})
const searchKeyword = ref('')

// 分页状态
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  name: '',
  description: '',
  welcomeMessage: ''
})

const rules: FormRules = {
  name: [
    { required: true, message: () => t('projectMgmt.nameRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: () => t('projectMgmt.nameLength'), trigger: 'blur' }
  ]
}

// 加载项目列表
const loadProjects = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/projects', {
      params: {
        keyword: searchKeyword.value,
        page: pagination.page,
        size: pagination.size
      }
    }) as any
    if (res.code === 0) {
      projects.value = res.data.list
      pagination.total = res.data.total
    } else {
      ElMessage.error(res.message || t('common.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('projectMgmt.loadListFailed'))
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  form.name = ''
  form.description = ''
  form.welcomeMessage = t('projectMgmt.defaultWelcome')
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (project: Project) => {
  isEdit.value = true
  editingId.value = project.id
  form.name = project.name
  form.description = project.description || ''
  form.welcomeMessage = project.welcomeMessage || ''
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const url = isEdit.value ? `/api/admin/projects/${editingId.value}` : '/api/admin/projects'
      const method = isEdit.value ? 'put' : 'post'
      
      const res = await request[method](url, {
        name: form.name,
        description: form.description,
        welcomeMessage: form.welcomeMessage
      }) as any
      
      if (res.code === 0) {
        ElMessage.success(isEdit.value ? t('projectMgmt.updateSuccess') : t('projectMgmt.createSuccess'))
        dialogVisible.value = false
        loadProjects()
      } else {
        ElMessage.error(res.message || t('common.operateFailed'))
      }
    } catch (error) {
      ElMessage.error(t('common.operateFailed'))
    } finally {
      submitting.value = false
    }
  })
}

// 删除项目
const deleteProject = async (project: Project) => {
  try {
    await ElMessageBox.confirm(
      t('projectMgmt.deleteConfirm', { name: project.name }),
      t('projectMgmt.deleteConfirmTitle'),
      {
        confirmButtonText: t('projectMgmt.confirmDelete'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    const res = await request.delete(`/api/admin/projects/${project.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('projectMgmt.deleteSuccess'))
      loadProjects()
    } else {
      ElMessage.error(res.message || t('projectMgmt.deleteFailed'))
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 重新生成密钥
const regenerateSecret = async (project: Project) => {
  try {
    await ElMessageBox.confirm(
      t('projectMgmt.secretConfirm', { name: project.name }),
      t('projectMgmt.secretConfirmTitle'),
      {
        confirmButtonText: t('projectMgmt.confirmReset'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    const res = await request.post(`/api/admin/projects/${project.id}/regenerate-secret`) as any
    if (res.code === 0) {
      ElMessage.success(t('projectMgmt.secretRegenSuccess'))
      loadProjects()
    } else {
      ElMessage.error(res.message || t('projectMgmt.resetFailed'))
    }
  } catch (error) {
    // 用户取消
  }
}

// 复制到剪贴板
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('projectMgmt.copied'))
  } catch (error) {
    ElMessage.error(t('projectMgmt.copyFailed'))
  }
}

// 切换密钥可见性
const toggleSecretVisibility = (id: number) => {
  visibleSecrets.value[id] = !visibleSecrets.value[id]
}

// 遮蔽密钥
const maskSecret = (secret: string) => {
  if (!secret) return ''
  return secret.substring(0, 8) + '****' + secret.substring(secret.length - 8)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1 // 搜索时重置到第一页
  loadProjects()
}

// 分页变化处理
const handlePageChange = ({ page, size }: { page: number; size: number }) => {
  pagination.page = page
  pagination.size = size
  loadProjects()
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-management {
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
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.key-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.key-cell code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  color: #606266;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
