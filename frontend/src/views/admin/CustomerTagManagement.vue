<template>
  <div class="customer-tag-management">
    <div class="page-header">
      <h2>标签管理</h2>
      <div class="header-actions">
        <el-select 
          v-model="selectedProjectId" 
          placeholder="请选择项目" 
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
          新建标签
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
      <el-table-column label="标签名称" width="180">
        <template #default="{ row }">
          <el-tag :color="row.color" style="color: #fff;">{{ row.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="颜色" width="120">
        <template #default="{ row }">
          <div class="color-preview" :style="{ backgroundColor: row.color }"></div>
          <span style="margin-left: 8px;">{{ row.color }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200">
        <template #default="{ row }">
          {{ row.description || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="用户数" width="100">
        <template #default="{ row }">
          {{ row.userCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="deleteTag(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!selectedProjectId" description="请先选择项目" />

    <!-- 创建/编辑标签对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签' : '新建标签'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="form.color" />
          <span style="margin-left: 12px;">{{ form.color }}</span>
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入标签描述（可选）" 
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          <span style="margin-left: 12px; color: #909399;">数值越小越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/api'

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
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 50, message: '标签名称长度在 1 到 50 个字符', trigger: 'blur' }
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
    console.error('加载项目失败', error)
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
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载标签列表失败')
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
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadTags()
      } else {
        ElMessage.error(res.message || '更新失败')
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
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadTags()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除标签
const deleteTag = async (tag: CustomerTag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签 "${tag.name}" 吗？删除后，所有关联此标签的用户将失去该标签。`,
      '确认删除',
      { type: 'warning' }
    )

    const res = await request.delete(`/api/admin/customer-tags/${tag.id}`) as any
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadTags()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
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
