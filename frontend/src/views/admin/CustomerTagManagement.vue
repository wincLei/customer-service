<template>
  <div class="customer-tag-management">
    <div class="page-header">
      <h2>{{ $t('tagMgmt.title') }}</h2>
      <div class="header-actions">
        <el-select 
          v-model="selectedProjectId" 
          :placeholder="$t('tagMgmt.selectProject')" 
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
          {{ $t('tagMgmt.newTag') }}
        </el-button>
      </div>
    </div>

    <!-- 标签列表 -->
    <el-table 
      v-if="selectedProjectId"
      :data="tags" 
      style="width: 100%" 
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column :label="$t('tagMgmt.tagName')" width="180">
        <template #default="{ row }">
          <el-tag :color="row.color" style="color: #fff;">{{ row.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('tagMgmt.color')" width="120">
        <template #default="{ row }">
          <div class="color-preview" :style="{ backgroundColor: row.color }"></div>
          <span style="margin-left: 8px;">{{ row.color }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" :label="$t('common.description')" min-width="200">
        <template #default="{ row }">
          {{ row.description || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" :label="$t('tagMgmt.sort')" width="80" />
      <el-table-column :label="$t('tagMgmt.userCount')" width="100">
        <template #default="{ row }">
          {{ row.userCount || 0 }}
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
          <el-button link type="danger" @click="deleteTag(row)">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!selectedProjectId" :description="$t('tagMgmt.selectProjectFirst')" />

    <!-- 创建/编辑标签对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('tagMgmt.editTag') : $t('tagMgmt.newTag')"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item :label="$t('tagMgmt.tagName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('tagMgmt.tagNamePlaceholder')" maxlength="50" />
        </el-form-item>
        <el-form-item :label="$t('tagMgmt.color')" prop="color">
          <el-color-picker v-model="form.color" />
          <span style="margin-left: 12px;">{{ form.color }}</span>
        </el-form-item>
        <el-form-item :label="$t('common.description')">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            :placeholder="$t('tagMgmt.descPlaceholder')" 
            maxlength="200"
          />
        </el-form-item>
        <el-form-item :label="$t('tagMgmt.sort')">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          <span style="margin-left: 12px; color: #909399;">{{ $t('tagMgmt.sortTip') }}</span>
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/api'
import { logger } from '@/utils/logger'

const { t } = useI18n()

interface Project {
  id: number
  name: string
}

interface CustomerTag {
  id: number
  projectId: number
  name: string
  color: string
  description: string
  sortOrder: number
  userCount?: number
  createdAt: string
  updatedAt: string
}

const loading = ref(false)
const submitting = ref(false)
const selectedProjectId = ref<number | null>(null)
const projects = ref<Project[]>([])
const tags = ref<CustomerTag[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  color: '#409EFF',
  description: '',
  sortOrder: 0
})

const rules: FormRules = {
  name: [
    { required: true, message: () => t('tagMgmt.tagNameRequired'), trigger: 'blur' },
    { min: 1, max: 50, message: () => t('tagMgmt.tagNameLength'), trigger: 'blur' }
  ]
}

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
    logger.error('加载项目失败', error)
  }
}

// 项目切换
const onProjectChange = () => {
  loadTags()
}

// 加载标签列表
const loadTags = async () => {
  if (!selectedProjectId.value) return

  loading.value = true
  try {
    const res = await request.get('/api/admin/customer-tags', {
      params: { projectId: selectedProjectId.value }
    }) as any
    if (res.code === 0) {
      tags.value = res.data
      // 加载每个标签的用户数
      await loadTagUserCounts()
    } else {
      ElMessage.error(res.message || t('common.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('tagMgmt.loadFailed'))
  } finally {
    loading.value = false
  }
}

// 加载标签用户数
const loadTagUserCounts = async () => {
  for (const tag of tags.value) {
    try {
      const res = await request.get(`/api/admin/customer-tags/${tag.id}`) as any
      if (res.code === 0) {
        tag.userCount = res.data.userCount
      }
    } catch (error) {
      // 忽略单个标签加载失败
    }
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  form.name = ''
  form.color = '#409EFF'
  form.description = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (tag: CustomerTag) => {
  isEdit.value = true
  editingId.value = tag.id
  form.name = tag.name
  form.color = tag.color
  form.description = tag.description || ''
  form.sortOrder = tag.sortOrder
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
      const res = await request.put(`/api/admin/customer-tags/${editingId.value}`, {
        name: form.name,
        color: form.color,
        description: form.description,
        sortOrder: form.sortOrder
      }) as any
      if (res.code === 0) {
        ElMessage.success(t('common.updateSuccess'))
        dialogVisible.value = false
        loadTags()
      } else {
        ElMessage.error(res.message || t('common.updateFailed'))
      }
    } else {
      // 创建
      const res = await request.post('/api/admin/customer-tags', {
        projectId: selectedProjectId.value,
        name: form.name,
        color: form.color,
        description: form.description,
        sortOrder: form.sortOrder
      }) as any
      if (res.code === 0) {
        ElMessage.success(t('common.createSuccess'))
        dialogVisible.value = false
        loadTags()
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

// 删除标签
const deleteTag = async (tag: CustomerTag) => {
  try {
    await ElMessageBox.confirm(
      t('tagMgmt.deleteConfirm', { name: tag.name }),
      t('tagMgmt.confirmDeleteTitle'),
      { type: 'warning' }
    )

    const res = await request.delete(`/api/admin/customer-tags/${tag.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadTags()
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
.customer-tag-management {
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

.color-preview {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  vertical-align: middle;
  border: 1px solid #dcdfe6;
}
</style>
