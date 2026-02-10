<template>
  <div class="user-management">
    <div class="page-header">
      <h2>{{ $t('userMgmt.title') }}</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          :placeholder="$t('userMgmt.searchPlaceholder')"
          style="width: 250px"
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
          {{ $t('userMgmt.newUser') }}
        </el-button>
      </div>
    </div>

    <!-- 用户列表 -->
    <el-table :data="users" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" :label="$t('userMgmt.username')" width="120" />
      <el-table-column prop="email" :label="$t('userMgmt.email')" min-width="180" />
      <el-table-column prop="phone" :label="$t('userMgmt.phone')" width="130" />
      <el-table-column :label="$t('userMgmt.role')" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.role" :type="getRoleTagType(row.role.code)">
            {{ row.role.name }}
          </el-tag>
          <span v-else class="text-gray">{{ $t('userMgmt.unassigned') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
            {{ row.status === 'active' ? $t('userMgmt.active') : $t('userMgmt.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('userMgmt.lastLogin')" width="180">
        <template #default="{ row }">
          {{ row.lastLoginAt ? formatDate(row.lastLoginAt) : $t('userMgmt.neverLogin') }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">{{ $t('common.edit') }}</el-button>
          <el-button link type="warning" @click="showPasswordDialog(row)">{{ $t('userMgmt.changePassword') }}</el-button>
          <el-button 
            link 
            :type="row.status === 'active' ? 'danger' : 'success'"
            @click="toggleStatus(row)"
            :disabled="row.username === 'admin'"
          >
            {{ row.status === 'active' ? $t('userMgmt.disable') : $t('userMgmt.enable') }}
          </el-button>
          <el-button 
            link 
            type="danger" 
            @click="deleteUser(row)"
            :disabled="row.username === 'admin'"
          >
            {{ $t('common.delete') }}
          </el-button>
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

    <!-- 创建/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('userMgmt.editUser') : $t('userMgmt.newUser')"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item :label="$t('userMgmt.username')" prop="username">
          <el-input 
            v-model="form.username" 
            :placeholder="$t('userMgmt.usernamePlaceholder')" 
            :disabled="isEdit"
            maxlength="50" 
          />
        </el-form-item>
        <el-form-item v-if="!isEdit" :label="$t('auth.password')" prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            :placeholder="$t('userMgmt.passwordPlaceholder')" 
            show-password
          />
        </el-form-item>
        <el-form-item :label="$t('userMgmt.email')" prop="email">
          <el-input v-model="form.email" :placeholder="$t('userMgmt.emailPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('userMgmt.phone')" prop="phone">
          <el-input v-model="form.phone" :placeholder="$t('userMgmt.phonePlaceholder')" maxlength="20" />
        </el-form-item>
        <el-form-item :label="$t('userMgmt.role')" prop="roleId">
          <el-select v-model="form.roleId" :placeholder="$t('userMgmt.selectRole')" style="width: 100%">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isEdit" :label="$t('common.status')" prop="status">
          <el-select v-model="form.status" :placeholder="$t('userMgmt.selectStatus')" style="width: 100%">
            <el-option :label="$t('userMgmt.active')" value="active" />
            <el-option :label="$t('userMgmt.disabled')" value="disabled" />
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

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      :title="$t('userMgmt.changePasswordTitle')"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item :label="$t('userMgmt.newPassword')" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            :placeholder="$t('userMgmt.newPasswordPlaceholder')"
            show-password
          />
        </el-form-item>
        <el-form-item :label="$t('userMgmt.confirmPassword')" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            :placeholder="$t('userMgmt.confirmPasswordPlaceholder')"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitPassword" :loading="submitting">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/api'
import Pagination from '@/components/Pagination.vue'

const { t } = useI18n()

interface Role {
  id: number
  code: string
  name: string
}

interface User {
  id: number
  username: string
  email: string
  phone: string
  avatar: string
  status: string
  role: Role | null
  lastLoginAt: string
  createdAt: string
}

const loading = ref(false)
const submitting = ref(false)
const users = ref<User[]>([])
const roles = ref<Role[]>([])
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const searchKeyword = ref('')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  roleId: null as number | null,
  status: 'active'
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: () => t('userMgmt.usernameRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: () => t('userMgmt.usernameLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: () => t('userMgmt.passwordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('userMgmt.passwordMinLength'), trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: () => t('userMgmt.emailFormat'), trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: () => t('userMgmt.selectRole'), trigger: 'change' }
  ]
}

const passwordRules: FormRules = {
  newPassword: [
    { required: true, message: () => t('userMgmt.newPasswordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('userMgmt.passwordMinLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: () => t('userMgmt.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(t('userMgmt.passwordMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await request.get('/api/admin/roles/all') as any
    if (res.code === 0) {
      roles.value = res.data
    }
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/users', {
      params: {
        keyword: searchKeyword.value,
        page: pagination.page,
        size: pagination.size
      }
    }) as any
    if (res.code === 0) {
      users.value = res.data.list
      pagination.total = res.data.total
    } else {
      ElMessage.error(res.message || t('common.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('userMgmt.loadListFailed'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

// 分页变化
const handlePageChange = () => {
  loadUsers()
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  form.username = ''
  form.password = ''
  form.email = ''
  form.phone = ''
  form.roleId = null
  form.status = 'active'
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (user: User) => {
  isEdit.value = true
  editingId.value = user.id
  form.username = user.username
  form.password = ''
  form.email = user.email || ''
  form.phone = user.phone || ''
  form.roleId = user.role?.id || null
  form.status = user.status
  dialogVisible.value = true
}

// 显示修改密码对话框
const showPasswordDialog = (user: User) => {
  editingId.value = user.id
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const url = isEdit.value ? `/api/admin/users/${editingId.value}` : '/api/admin/users'
      const method = isEdit.value ? 'put' : 'post'
      
      const data: any = {
        email: form.email,
        phone: form.phone,
        roleId: form.roleId
      }
      
      if (!isEdit.value) {
        data.username = form.username
        data.password = form.password
      } else {
        data.status = form.status
      }
      
      const res = await request[method](url, data) as any
      
      if (res.code === 0) {
        ElMessage.success(isEdit.value ? t('common.updateSuccess') : t('common.createSuccess'))
        dialogVisible.value = false
        loadUsers()
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

// 提交密码修改
const submitPassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const res = await request.post(`/api/admin/users/${editingId.value}/password`, {
        newPassword: passwordForm.newPassword
      }) as any
      
      if (res.code === 0) {
        ElMessage.success(t('userMgmt.passwordChangeSuccess'))
        passwordDialogVisible.value = false
      } else {
        ElMessage.error(res.message || t('userMgmt.passwordChangeFailed'))
      }
    } catch (error) {
      ElMessage.error(t('userMgmt.passwordChangeFailed'))
    } finally {
      submitting.value = false
    }
  })
}

// 切换用户状态
const toggleStatus = async (user: User) => {
  const action = user.status === 'active' ? t('userMgmt.disable') : t('userMgmt.enable')
  try {
    await ElMessageBox.confirm(
      t('userMgmt.disableConfirm', { action, username: user.username }),
      t('userMgmt.actionConfirmTitle', { action }),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    
    const res = await request.post(`/api/admin/users/${user.id}/toggle-status`) as any
    if (res.code === 0) {
      ElMessage.success(t('userMgmt.actionSuccess', { action }))
      loadUsers()
    } else {
      ElMessage.error(res.message || t('userMgmt.actionFailed', { action }))
    }
  } catch (error) {
    // 用户取消
  }
}

// 删除用户
const deleteUser = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      t('userMgmt.deleteConfirm', { username: user.username }),
      t('userMgmt.deleteConfirmTitle'),
      { confirmButtonText: t('common.confirmDelete'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    
    const res = await request.delete(`/api/admin/users/${user.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadUsers()
    } else {
      ElMessage.error(res.message || t('common.deleteFailed'))
    }
  } catch (error) {
    // 用户取消
  }
}

// 获取角色标签类型
const getRoleTagType = (code: string) => {
  const map: Record<string, string> = {
    super_admin: 'danger',
    admin: 'warning',
    agent: 'primary',
    viewer: 'info'
  }
  return map[code] || 'info'
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

onMounted(() => {
  loadRoles()
  loadUsers()
})
</script>

<style scoped>
.user-management {
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
</style>
