<template>
  <div class="settings">
    <el-row>
      <el-col :md="20">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统设置</span>
            </div>
          </template>

          <el-form :model="form" label-width="120px">
            <el-form-item label="项目名称">
              <el-input v-model="form.projectName"></el-input>
            </el-form-item>

            <el-form-item label="欢迎语">
              <el-input
                v-model="form.welcomeMessage"
                type="textarea"
                placeholder="输入欢迎消息"
              ></el-input>
            </el-form-item>

            <el-form-item label="主题色">
              <el-color-picker v-model="form.themeColor"></el-color-picker>
            </el-form-item>

            <el-form-item label="工作时间">
              <el-time-picker
                v-model="form.workingHours"
                is-range
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
              ></el-time-picker>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>客服管理</span>
            </div>
          </template>

          <el-button type="primary" @click="showAddAgentDialog = true">添加客服</el-button>

          <el-table :data="agents" style="margin-top: 16px">
            <el-table-column prop="username" label="用户名"></el-table-column>
            <el-table-column prop="nickname" label="昵称"></el-table-column>
            <el-table-column prop="role" label="角色"></el-table-column>
            <el-table-column prop="status" label="状态"></el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="editAgent(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteAgent(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加客服对话框 -->
    <el-dialog v-model="showAddAgentDialog" title="添加客服">
      <el-form :model="agentForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="agentForm.username"></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="agentForm.nickname"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="agentForm.role">
            <el-option label="客服" value="agent"></el-option>
            <el-option label="主管" value="supervisor"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddAgentDialog = false">取消</el-button>
        <el-button type="primary" @click="addAgent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const form = ref({
  projectName: '客服系统',
  welcomeMessage: '欢迎咨询我们的客服系统',
  themeColor: '#1890ff',
  workingHours: null,
})

const agents = ref([
  { username: 'agent1', nickname: '李客服', role: 'agent', status: '在线' },
  { username: 'agent2', nickname: '王客服', role: 'agent', status: '离线' },
])

const showAddAgentDialog = ref(false)
const agentForm = ref({
  username: '',
  nickname: '',
  role: 'agent',
})

const saveSettings = () => {
  ElMessage.success('设置保存成功')
  // TODO: 调用 API 保存设置
}

const addAgent = () => {
  agents.value.push({
    ...agentForm.value,
    status: '离线',
  })
  showAddAgentDialog.value = false
  agentForm.value = { username: '', nickname: '', role: 'agent' }
  ElMessage.success('客服添加成功')
}

const editAgent = (_agent: any) => {
  ElMessage.info('编辑功能开发中')
}

const deleteAgent = (_agent: any) => {
  agents.value = agents.value.filter(a => a.username !== _agent.username)
  ElMessage.success('客服删除成功')
}
</script>

<style scoped lang="css">
.settings {
  padding: 16px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
