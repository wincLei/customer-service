<template>
  <div class="quick-reply-management">
    <div class="page-header">
      <h2>快捷回复管理</h2>
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
          新建快捷回复
        </el-button>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-bar" v-if="selectedProjectId">
      <el-radio-group v-model="selectedCategory" @change="filterByCategory">
        <el-radio-button label="">全部</el-radio-button>
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
      <el-table-column prop="title" label="标题" width="200">
        <template #default="{ row }">
          {{ row.title || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="300">
        <template #default="{ row }">
          <div class="content-preview">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.category" size="small">{{ row.category }}</el-tag>
          <span v-else class="text-muted">未分类</span>
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
          <el-button link type="danger" @click="deleteReply(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!selectedProjectId" description="请先选择项目" />

    <!-- 创建/编辑快捷回复对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑快捷回复' : '新建快捷回复'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题（可选）" maxlength="100" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="6"
            placeholder="请输入快捷回复内容" 
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select 
            v-model="form.category" 
            placeholder="选择或输入分类"
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/api'

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
    { required: true, message: '请输入快捷回复内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '内容长度在 1 到 1000 个字符', trigger: 'blur' }
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
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载快捷回复列表失败')
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
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadReplies()
      } else {
        ElMessage.error(res.message || '更新失败')
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
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadReplies()
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

// 删除快捷回复
const deleteReply = async (reply: QuickReply) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除快捷回复 "${reply.title || reply.content.substring(0, 20)}..." 吗？`,
      '确认删除',
      { type: 'warning' }
    )

    const res = await request.delete(`/api/admin/quick-replies/${reply.id}`) as any
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadReplies()
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
