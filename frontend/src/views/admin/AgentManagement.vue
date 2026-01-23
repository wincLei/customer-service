<template>
  <div class="agent-management">
    <div class="page-header">
      <h2>客服管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新建客服
        </el-button>
      </div>
    </div>

    <!-- 客服列表 -->
    <el-table :data="agents" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="用户账号" width="140">
        <template #default="{ row }">
          <span>{{ row.user?.username || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="客服昵称" width="140" />
      <el-table-column label="工作状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.workStatus)">
            {{ getStatusLabel(row.workStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接待量" width="120">
        <template #default="{ row }">
          <span>{{ row.currentLoad }} / {{ row.maxLoad }}</span>
        </template>
      </el-table-column>
      <el-table-column label="关联项目" min-width="200">
        <template #default="{ row }">
          <span v-if="row.projectIds && row.projectIds.length > 0" class="project-tags">
            <el-tag 
              v-for="pid in row.projectIds.slice(0, 3)" 
              :key="pid" 
              size="small" 
              type="info"
              style="margin-right: 4px; margin-bottom: 4px;"
            >
              {{ getProjectName(pid) }}
            </el-tag>
            <el-tooltip v-if="row.projectIds.length > 3" :content="getProjectNames(row.projectIds).join(', ')">
              <el-tag size="small" type="info">+{{ row.projectIds.length - 3 }}</el-tag>
            </el-tooltip>
          </span>
          <span v-else class="text-gray">未关联项目</span>
        </template>
      </el-table-column>
      <el-table-column label="自动回复" width="90">
        <template #default="{ row }">
          <el-tag :type="row.autoReplyEnabled ? 'success' : 'info'" size="small">
            {{ row.autoReplyEnabled ? '已开启' : '未开启' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
          <el-button link type="warning" @click="showStatusDialog(row)">状态</el-button>
          <el-button link type="danger" @click="deleteAgent(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑客服对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑客服' : '新建客服'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item v-if="!isEdit" label="选择用户" prop="userId">
          <el-select 
            v-model="form.userId" 
            placeholder="请选择用户（仅显示客服角色用户）" 
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            >
              <span>{{ user.username }}</span>
              <span v-if="user.email" style="color: #909399; margin-left: 8px;">{{ user.email }}</span>
            </el-option>
          </el-select>
          <div class="form-tip">仅显示角色为"客服"且尚未创建客服记录的用户</div>
        </el-form-item>
        <el-form-item v-else label="用户账号">
          <el-input :value="editingAgent?.user?.username" disabled />
        </el-form-item>
        <el-form-item label="客服昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入客服昵称" maxlength="50" />
        </el-form-item>
        <el-form-item label="最大接待量" prop="maxLoad">
          <el-input-number v-model="form.maxLoad" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="关联项目" prop="projectIds">
          <el-select 
            v-model="form.projectIds" 
            placeholder="请选择客服负责的项目（可多选）" 
            style="width: 100%"
            multiple
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
          <div class="form-tip">客服只能查看和处理关联项目的会话</div>
        </el-form-item>
        <el-form-item label="欢迎语">
          <el-input 
            v-model="form.welcomeMessage" 
            type="textarea" 
            :rows="3"
            placeholder="客服的欢迎语（可选）" 
          />
        </el-form-item>
        <el-form-item label="自动回复">
          <el-switch v-model="form.autoReplyEnabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改状态对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="修改工作状态"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="客服">
          {{ editingAgent?.nickname || editingAgent?.user?.username }}
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="statusForm.workStatus">
            <el-radio value="online">在线</el-radio>
            <el-radio value="busy">忙碌</el-radio>
            <el-radio value="offline">离线</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/api'

interface User {
  id: number
  username: string
  email: string
  avatar: string
}

interface Project {
  id: number
  name: string
}

interface Agent {
  id: number
  userId: number
  nickname: string
  workStatus: string
  maxLoad: number
  currentLoad: number
  welcomeMessage: string
  autoReplyEnabled: boolean
  projectIds: number[]
  user: User | null
  createdAt: string
}

const loading = ref(false)
const submitting = ref(false)
const agents = ref<Agent[]>([])
const availableUsers = ref<User[]>([])
const projects = ref<Project[]>([])
const dialogVisible = ref(false)
const statusDialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const editingAgent = ref<Agent | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  userId: null as number | null,
  nickname: '',
  maxLoad: 5,
  welcomeMessage: '',
  autoReplyEnabled: false,
  projectIds: [] as number[]
})

const statusForm = reactive({
  workStatus: 'online'
})

const rules: FormRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  nickname: [
    { required: true, message: '请输入客服昵称', trigger: 'blur' }
  ],
  maxLoad: [
    { required: true, message: '请设置最大接待量', trigger: 'blur' }
  ]
}

// 加载客服列表
const loadAgents = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/agents') as any
    if (res.code === 0) {
      agents.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载客服列表失败')
  } finally {
    loading.value = false
  }
}

// 加载可用用户列表
const loadAvailableUsers = async () => {
  try {
    const res = await request.get('/api/admin/agents/available-users') as any
    if (res.code === 0) {
      availableUsers.value = res.data
    }
  } catch (error) {
    console.error('加载可用用户失败', error)
  }
}

// 加载项目列表
const loadProjects = async () => {
  try {
    const res = await request.get('/api/admin/projects/all') as any
    if (res.code === 0) {
      projects.value = res.data
    }
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  editingAgent.value = null
  form.userId = null
  form.nickname = ''
  form.maxLoad = 5
  form.welcomeMessage = ''
  form.autoReplyEnabled = false
  form.projectIds = []
  loadAvailableUsers()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (agent: Agent) => {
  isEdit.value = true
  editingId.value = agent.id
  editingAgent.value = agent
  form.userId = agent.userId
  form.nickname = agent.nickname || ''
  form.maxLoad = agent.maxLoad
  form.welcomeMessage = agent.welcomeMessage || ''
  form.autoReplyEnabled = agent.autoReplyEnabled
  form.projectIds = agent.projectIds || []
  dialogVisible.value = true
}

// 显示状态对话框
const showStatusDialog = (agent: Agent) => {
  editingId.value = agent.id
  editingAgent.value = agent
  statusForm.workStatus = agent.workStatus
  statusDialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const url = isEdit.value ? `/api/admin/agents/${editingId.value}` : '/api/admin/agents'
      const method = isEdit.value ? 'put' : 'post'
      
      const data: any = {
        nickname: form.nickname,
        maxLoad: form.maxLoad,
        welcomeMessage: form.welcomeMessage,
        autoReplyEnabled: form.autoReplyEnabled,
        projectIds: form.projectIds
      }
      
      if (!isEdit.value) {
        data.userId = form.userId
      }
      
      const res = await request[method](url, data) as any
      
      if (res.code === 0) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadAgents()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 提交状态修改
const submitStatus = async () => {
  submitting.value = true
  try {
    const res = await request.post(`/api/admin/agents/${editingId.value}/status`, {
      workStatus: statusForm.workStatus
    }) as any
    
    if (res.code === 0) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      loadAgents()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    submitting.value = false
  }
}

// 删除客服
const deleteAgent = async (agent: Agent) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除客服 "${agent.nickname || agent.user?.username}" 吗？删除后该用户将无法处理客服业务！`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    
    const res = await request.delete(`/api/admin/agents/${agent.id}`) as any
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadAgents()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    // 用户取消
  }
}

// 获取项目名称
const getProjectName = (projectId: number) => {
  const project = projects.value.find(p => p.id === projectId)
  return project?.name || `项目${projectId}`
}

// 获取项目名称列表
const getProjectNames = (projectIds: number[]) => {
  return projectIds.map(id => getProjectName(id))
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    online: 'success',
    busy: 'warning',
    offline: 'info'
  }
  return map[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    online: '在线',
    busy: '忙碌',
    offline: '离线'
  }
  return map[status] || status
}

onMounted(() => {
  loadAgents()
  loadProjects()
})
</script>

<style scoped>
.agent-management {
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

.text-gray {
  color: #909399;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
</style>
