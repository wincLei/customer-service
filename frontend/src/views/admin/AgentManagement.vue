<template>
  <div class="agent-management">
    <div class="page-header">
      <h2>{{ $t('agentMgmt.title') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          {{ $t('agentMgmt.newAgent') }}
        </el-button>
      </div>
    </div>

    <!-- 客服列表 -->
    <el-table :data="agents" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column :label="$t('agentMgmt.userAccount')" width="140">
        <template #default="{ row }">
          <span>{{ row.user?.username || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" :label="$t('agentMgmt.agentNickname')" width="140" />
      <el-table-column :label="$t('agentMgmt.workStatus')" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.workStatus)">
            {{ getStatusLabel(row.workStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('agentMgmt.receptionCount')" width="120">
        <template #default="{ row }">
          <span>{{ row.currentLoad }} / {{ row.maxLoad }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('agentMgmt.relatedProjects')" min-width="200">
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
          <span v-else class="text-gray">{{ $t('agentMgmt.noProject') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('agentMgmt.autoReply')" width="90">
        <template #default="{ row }">
          <el-tag :type="row.autoReplyEnabled ? 'success' : 'info'" size="small">
            {{ row.autoReplyEnabled ? $t('agentMgmt.autoReplyOn') : $t('agentMgmt.autoReplyOff') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">{{ $t('common.edit') }}</el-button>
          <el-button link type="warning" @click="showStatusDialog(row)">{{ $t('agentMgmt.statusLabel') }}</el-button>
          <el-button link type="danger" @click="deleteAgent(row)">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑客服对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('agentMgmt.editAgent') : $t('agentMgmt.newAgent')"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item v-if="!isEdit" :label="$t('agentMgmt.selectUser')" prop="userId">
          <el-select 
            v-model="form.userId" 
            :placeholder="$t('agentMgmt.selectUserPlaceholder')" 
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
          <div class="form-tip">{{ $t('agentMgmt.userTip') }}</div>
        </el-form-item>
        <el-form-item v-else :label="$t('agentMgmt.userAccount')">
          <el-input :value="editingAgent?.user?.username" disabled />
        </el-form-item>
        <el-form-item :label="$t('agentMgmt.agentNickname')" prop="nickname">
          <el-input v-model="form.nickname" :placeholder="$t('agentMgmt.nicknamePlaceholder')" maxlength="50" />
        </el-form-item>
        <el-form-item :label="$t('agentMgmt.maxLoad')" prop="maxLoad">
          <el-input-number v-model="form.maxLoad" :min="1" :max="50" />
        </el-form-item>
        <el-form-item :label="$t('agentMgmt.relatedProjects')" prop="projectIds">
          <el-select 
            v-model="form.projectIds" 
            :placeholder="$t('agentMgmt.selectProjectPlaceholder')" 
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
          <div class="form-tip">{{ $t('agentMgmt.projectTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('agentMgmt.welcomeMsg')">
          <el-input 
            v-model="form.welcomeMessage" 
            type="textarea" 
            :rows="3"
            :placeholder="$t('agentMgmt.welcomeMsgPlaceholder')" 
          />
        </el-form-item>
        <el-form-item :label="$t('agentMgmt.autoReply')">
          <el-switch v-model="form.autoReplyEnabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? $t('common.save') : $t('common.create') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改状态对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      :title="$t('agentMgmt.changeStatus')"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item :label="$t('agentMgmt.agentLabel')">
          {{ editingAgent?.nickname || editingAgent?.user?.username }}
        </el-form-item>
        <el-form-item :label="$t('agentMgmt.statusLabel')">
          <el-radio-group v-model="statusForm.workStatus">
            <el-radio value="online">{{ $t('agentMgmt.online') }}</el-radio>
            <el-radio value="busy">{{ $t('agentMgmt.busy') }}</el-radio>
            <el-radio value="offline">{{ $t('agentMgmt.offline') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitStatus" :loading="submitting">{{ $t('common.confirm') }}</el-button>
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
    { required: true, message: () => t('agentMgmt.selectUserRequired'), trigger: 'change' }
  ],
  nickname: [
    { required: true, message: () => t('agentMgmt.nicknameRequired'), trigger: 'blur' }
  ],
  maxLoad: [
    { required: true, message: () => t('agentMgmt.maxLoadRequired'), trigger: 'blur' }
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
      ElMessage.error(res.message || t('common.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('agentMgmt.loadListFailed'))
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
    logger.error('加载可用用户失败', error)
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
    logger.error('加载项目列表失败', error)
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
        ElMessage.success(isEdit.value ? t('common.updateSuccess') : t('common.createSuccess'))
        dialogVisible.value = false
        loadAgents()
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

// 提交状态修改
const submitStatus = async () => {
  submitting.value = true
  try {
    const res = await request.post(`/api/admin/agents/${editingId.value}/status`, {
      workStatus: statusForm.workStatus
    }) as any
    
    if (res.code === 0) {
      ElMessage.success(t('agentMgmt.statusUpdateSuccess'))
      statusDialogVisible.value = false
      loadAgents()
    } else {
      ElMessage.error(res.message || t('agentMgmt.statusUpdateFailed'))
    }
  } catch (error) {
    ElMessage.error(t('agentMgmt.statusUpdateFailed'))
  } finally {
    submitting.value = false
  }
}

// 删除客服
const deleteAgent = async (agent: Agent) => {
  try {
    await ElMessageBox.confirm(
      t('agentMgmt.deleteConfirm', { name: agent.nickname || agent.user?.username }),
      t('agentMgmt.deleteConfirmTitle'),
      { confirmButtonText: t('common.confirmDelete'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    
    const res = await request.delete(`/api/admin/agents/${agent.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadAgents()
    } else {
      ElMessage.error(res.message || t('common.deleteFailed'))
    }
  } catch (error) {
    // 用户取消
  }
}

// 获取项目名称
const getProjectName = (projectId: number) => {
  const project = projects.value.find(p => p.id === projectId)
  return project?.name || `${t('agentMgmt.projectPrefix')}${projectId}`
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
    online: t('agentMgmt.online'),
    busy: t('agentMgmt.busy'),
    offline: t('agentMgmt.offline')
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
