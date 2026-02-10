<template>
  <div class="role-management">
    <div class="page-header">
      <h2>{{ $t('roleMgmt.title') }}</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          :placeholder="$t('roleMgmt.searchPlaceholder')"
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
          {{ $t('roleMgmt.newRole') }}
        </el-button>
      </div>
    </div>

    <!-- 角色列表 -->
    <el-table :data="roles" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" :label="$t('roleMgmt.roleCode')" width="150" />
      <el-table-column prop="name" :label="$t('roleMgmt.roleName')" width="150" />
      <el-table-column prop="description" :label="$t('common.description')" min-width="200" />
      <el-table-column :label="$t('roleMgmt.type')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isSystem ? 'warning' : 'info'">
            {{ row.isSystem ? $t('roleMgmt.system') : $t('roleMgmt.custom') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.createdAt')" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showPermissionDialog(row)">{{ $t('roleMgmt.authorize') }}</el-button>
          <el-button link type="primary" @click="showEditDialog(row)">{{ $t('common.edit') }}</el-button>
          <el-button 
            link 
            type="danger" 
            @click="deleteRole(row)"
            :disabled="row.isSystem"
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

    <!-- 创建/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('roleMgmt.editRole') : $t('roleMgmt.newRole')"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('roleMgmt.roleCode')" prop="code">
          <el-input 
            v-model="form.code" 
            :placeholder="$t('roleMgmt.codePlaceholder')" 
            :disabled="isEdit"
            maxlength="50" 
          />
          <div class="form-tip" v-if="!isEdit">{{ $t('roleMgmt.codeTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('roleMgmt.roleName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('roleMgmt.namePlaceholder')" maxlength="100" />
        </el-form-item>
        <el-form-item :label="$t('common.description')" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            :placeholder="$t('roleMgmt.descPlaceholder')" 
            maxlength="255" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? $t('common.save') : $t('common.create') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 权限配置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="$t('roleMgmt.configPermissions')"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="permission-dialog-content">
        <div class="role-info">
          <span class="role-label">{{ $t('roleMgmt.currentRole') }}</span>
          <el-tag>{{ currentRole?.name }} ({{ currentRole?.code }})</el-tag>
        </div>

        <el-divider content-position="left">{{ $t('roleMgmt.selectMenuPerms') }}</el-divider>

        <div class="tree-container" v-loading="menuLoading">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="code"
            show-checkbox
            default-expand-all
            :default-checked-keys="checkedMenuCodes"
            @check="handleMenuCheck"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-tag size="small" :type="data.type === 'menu' ? 'primary' : 'success'" style="margin-right: 8px">
                  {{ data.type === 'menu' ? $t('roleMgmt.menuLabel') : $t('roleMgmt.buttonLabel') }}
                </el-tag>
                <span>{{ node.label }}</span>
                <span class="tree-node-code">({{ data.code }})</span>
              </span>
            </template>
          </el-tree>
        </div>

        <div class="selected-permissions">
          <div class="selected-title">{{ $t('roleMgmt.selectedPerms') }}</div>
          <div class="selected-tags">
            <el-tag
              v-for="code in selectedMenuCodes"
              :key="code"
              closable
              size="small"
              style="margin: 2px"
              @close="removeSelectedMenu(code)"
            >
              {{ code }}
            </el-tag>
            <span v-if="selectedMenuCodes.length === 0" class="no-selection">{{ $t('roleMgmt.noSelection') }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="savePermissions" :loading="savingPermissions">
          {{ $t('roleMgmt.savePerms') }}
        </el-button>
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
import type { ElTree } from 'element-plus'

const { t } = useI18n()

interface Role {
  id: number
  code: string
  name: string
  description: string
  permissions: any
  isSystem: boolean
  createdAt: string
}

interface MenuItem {
  id: number
  code: string
  name: string
  type: string
  children?: MenuItem[]
}

const loading = ref(false)
const submitting = ref(false)
const roles = ref<Role[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const searchKeyword = ref('')

// 权限配置相关
const permissionDialogVisible = ref(false)
const currentRole = ref<Role | null>(null)
const menuTree = ref<MenuItem[]>([])
const menuLoading = ref(false)
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const checkedMenuCodes = ref<string[]>([])
const selectedMenuCodes = ref<string[]>([])
const savingPermissions = ref(false)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  code: '',
  name: '',
  description: ''
})

const rules: FormRules = {
  code: [
    { required: true, message: () => t('roleMgmt.codeRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: () => t('roleMgmt.codeLength'), trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: () => t('roleMgmt.codePattern'), trigger: 'blur' }
  ],
  name: [
    { required: true, message: () => t('roleMgmt.nameRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: () => t('roleMgmt.nameLength'), trigger: 'blur' }
  ]
}

// 加载角色列表
const loadRoles = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/roles', {
      params: {
        keyword: searchKeyword.value,
        page: pagination.page,
        size: pagination.size
      }
    }) as any
    if (res.code === 0) {
      roles.value = res.data.list
      pagination.total = res.data.total
    } else {
      ElMessage.error(res.message || t('common.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('roleMgmt.loadListFailed'))
  } finally {
    loading.value = false
  }
}

// 加载菜单树
const loadMenuTree = async () => {
  menuLoading.value = true
  try {
    const res = await request.get('/api/admin/menus/enabled-tree') as any
    if (res.code === 0) {
      menuTree.value = res.data
    } else {
      ElMessage.error(res.message || t('roleMgmt.loadMenuFailed'))
    }
  } catch (error) {
    ElMessage.error(t('roleMgmt.loadMenuFailed'))
  } finally {
    menuLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRoles()
}

// 分页变化
const handlePageChange = () => {
  loadRoles()
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  form.code = ''
  form.name = ''
  form.description = ''
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (role: Role) => {
  isEdit.value = true
  editingId.value = role.id
  form.code = role.code
  form.name = role.name
  form.description = role.description || ''
  dialogVisible.value = true
}

// 显示权限配置对话框
const showPermissionDialog = async (role: Role) => {
  currentRole.value = role
  
  // 解析当前角色的权限
  let menus: string[] = []
  let actions: string[] = []
  
  if (role.permissions) {
    if (typeof role.permissions === 'string') {
      try {
        const parsed = JSON.parse(role.permissions)
        menus = parsed.menus || []
        actions = parsed.actions || []
      } catch (e) {
        console.error('解析权限失败', e)
      }
    } else if (typeof role.permissions === 'object') {
      menus = role.permissions.menus || []
      actions = role.permissions.actions || []
    }
  }
  
  // 合并菜单和操作权限
  checkedMenuCodes.value = [...menus, ...actions]
  selectedMenuCodes.value = [...checkedMenuCodes.value]
  
  permissionDialogVisible.value = true
  await loadMenuTree()
  
  // 等待树渲染完成后设置选中状态
  setTimeout(() => {
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(checkedMenuCodes.value)
    }
  }, 100)
}

// 处理菜单选中
const handleMenuCheck = () => {
  if (menuTreeRef.value) {
    selectedMenuCodes.value = menuTreeRef.value.getCheckedKeys(false) as string[]
  }
}

// 移除已选菜单
const removeSelectedMenu = (code: string) => {
  selectedMenuCodes.value = selectedMenuCodes.value.filter(c => c !== code)
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys(selectedMenuCodes.value)
  }
}

// 保存权限
const savePermissions = async () => {
  if (!currentRole.value) return
  
  savingPermissions.value = true
  try {
    // 将选中的权限分类为菜单和操作
    const menus: string[] = []
    const actions: string[] = []
    
    // 遍历菜单树，根据类型分类
    const categorize = (items: MenuItem[]) => {
      for (const item of items) {
        if (selectedMenuCodes.value.includes(item.code)) {
          if (item.type === 'menu') {
            menus.push(item.code)
          } else {
            actions.push(item.code)
          }
        }
        if (item.children) {
          categorize(item.children)
        }
      }
    }
    categorize(menuTree.value)
    
    const permissions = { menus, actions }
    
    const res = await request.put(`/api/admin/roles/${currentRole.value.id}`, {
      name: currentRole.value.name,
      description: currentRole.value.description,
      permissions
    }) as any
    
    if (res.code === 0) {
      ElMessage.success(t('roleMgmt.permSaveSuccess'))
      permissionDialogVisible.value = false
      loadRoles()
    } else {
      ElMessage.error(res.message || t('roleMgmt.permSaveFailed'))
    }
  } catch (error) {
    ElMessage.error(t('roleMgmt.permSaveFailed'))
  } finally {
    savingPermissions.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const url = isEdit.value ? `/api/admin/roles/${editingId.value}` : '/api/admin/roles'
      const method = isEdit.value ? 'put' : 'post'
      
      const data: any = {
        name: form.name,
        description: form.description
      }
      
      if (!isEdit.value) {
        data.code = form.code
        // 新建时设置空权限
        data.permissions = { menus: [], actions: [] }
      }
      
      const res = await request[method](url, data) as any
      
      if (res.code === 0) {
        ElMessage.success(isEdit.value ? t('common.updateSuccess') : t('common.createSuccess'))
        dialogVisible.value = false
        loadRoles()
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

// 删除角色
const deleteRole = async (role: Role) => {
  try {
    await ElMessageBox.confirm(
      t('roleMgmt.deleteConfirm', { name: role.name }),
      t('roleMgmt.deleteConfirmTitle'),
      { confirmButtonText: t('common.confirmDelete'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    
    const res = await request.delete(`/api/admin/roles/${role.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadRoles()
    } else {
      ElMessage.error(res.message || t('common.deleteFailed'))
    }
  } catch (error) {
    // 用户取消
  }
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
})
</script>

<style scoped>
.role-management {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.permission-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.role-info {
  margin-bottom: 15px;
}

.role-label {
  color: #606266;
  margin-right: 10px;
}

.tree-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  max-height: 300px;
  overflow-y: auto;
  background: #fafafa;
}

.tree-node {
  display: flex;
  align-items: center;
}

.tree-node-code {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.selected-permissions {
  margin-top: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.selected-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.selected-tags {
  min-height: 32px;
}

.no-selection {
  color: #909399;
  font-size: 13px;
}
</style>
