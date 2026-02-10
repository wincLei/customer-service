<template>
  <div class="menu-management">
    <div class="page-header">
      <h2>{{ $t('menuMgmt.title') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog(null)">
          <el-icon><Plus /></el-icon>
          {{ $t('menuMgmt.newMenu') }}
        </el-button>
        <el-button @click="loadMenuTree">
          <el-icon><Refresh /></el-icon>
          {{ $t('common.refresh') }}
        </el-button>
      </div>
    </div>

    <!-- 菜单树表格 -->
    <el-table
      :data="menuTree"
      style="width: 100%"
      row-key="id"
      :tree-props="{ children: 'children' }"
      v-loading="loading"
      default-expand-all
    >
      <el-table-column prop="name" :label="$t('menuMgmt.menuName')" min-width="180">
        <template #default="{ row }">
          <span>
            <el-icon v-if="row.icon" style="margin-right: 5px">
              <component :is="row.icon" />
            </el-icon>
            {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="code" :label="$t('menuMgmt.menuCode')" width="150" />
      <el-table-column prop="type" :label="$t('menuMgmt.type')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'menu' ? 'primary' : 'success'" size="small">
            {{ row.type === 'menu' ? $t('menuMgmt.menu') : $t('menuMgmt.button') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" :label="$t('menuMgmt.path')" width="150" />
      <el-table-column prop="sortOrder" :label="$t('menuMgmt.sort')" width="80" align="center" />
      <el-table-column :label="$t('common.status')" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.isEnabled"
            @change="toggleStatus(row)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="showCreateDialog(row.id)">
            {{ $t('menuMgmt.addChild') }}
          </el-button>
          <el-button link type="primary" size="small" @click="showEditDialog(row)">
            {{ $t('common.edit') }}
          </el-button>
          <el-button link type="danger" size="small" @click="deleteMenu(row)">
            {{ $t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑菜单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('menuMgmt.editMenu') : $t('menuMgmt.newMenu')"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('menuMgmt.parentMenu')">
          <el-tree-select
            v-model="form.parentId"
            :data="parentMenuOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            :placeholder="$t('menuMgmt.noParent')"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('menuMgmt.menuType')" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="menu">{{ $t('menuMgmt.menu') }}</el-radio>
            <el-radio value="button">{{ $t('menuMgmt.buttonAction') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('menuMgmt.menuCode')" prop="code">
          <el-input 
            v-model="form.code" 
            :placeholder="$t('menuMgmt.codePlaceholder')"
            maxlength="50"
          />
          <div class="form-tip">{{ $t('menuMgmt.codeTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('menuMgmt.menuName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('menuMgmt.namePlaceholder')" maxlength="100" />
        </el-form-item>
        <el-form-item :label="$t('menuMgmt.routePath')" v-if="form.type === 'menu'">
          <el-input v-model="form.path" :placeholder="$t('menuMgmt.pathPlaceholder')" maxlength="200" />
        </el-form-item>
        <el-form-item :label="$t('menuMgmt.icon')" v-if="form.type === 'menu'">
          <el-input v-model="form.icon" :placeholder="$t('menuMgmt.iconPlaceholder')" maxlength="100" />
        </el-form-item>
        <el-form-item :label="$t('menuMgmt.sortOrder')" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          <span class="form-tip" style="margin-left: 10px">{{ $t('menuMgmt.sortTip') }}</span>
        </el-form-item>
        <el-form-item :label="$t('menuMgmt.enabled')">
          <el-switch v-model="form.isEnabled" />
        </el-form-item>
        <el-form-item :label="$t('common.description')">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="2"
            :placeholder="$t('menuMgmt.descPlaceholder')" 
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api'

const { t } = useI18n()

interface MenuItem {
  id: number
  code: string
  name: string
  type: string
  parentId: number | null
  path: string
  icon: string
  sortOrder: number
  isEnabled: boolean
  description: string
  children?: MenuItem[]
}

const loading = ref(false)
const submitting = ref(false)
const menuTree = ref<MenuItem[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  code: '',
  name: '',
  type: 'menu',
  parentId: null as number | null,
  path: '',
  icon: '',
  sortOrder: 0,
  isEnabled: true,
  description: ''
})

const rules: FormRules = {
  code: [
    { required: true, message: () => t('menuMgmt.codeRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: () => t('menuMgmt.codeLength'), trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_:]*$/, message: () => t('menuMgmt.codePattern'), trigger: 'blur' }
  ],
  name: [
    { required: true, message: () => t('menuMgmt.nameRequired'), trigger: 'blur' },
    { min: 1, max: 100, message: () => t('menuMgmt.nameLength'), trigger: 'blur' }
  ],
  type: [
    { required: true, message: () => t('menuMgmt.typeRequired'), trigger: 'change' }
  ]
}

// 计算父级菜单选项（排除当前编辑的菜单及其子菜单）
const parentMenuOptions = computed(() => {
  if (!isEdit.value) return menuTree.value
  return filterMenuTree(menuTree.value, editingId.value)
})

// 过滤掉指定ID及其子菜单
const filterMenuTree = (menus: MenuItem[], excludeId: number | null): MenuItem[] => {
  return menus
    .filter(menu => menu.id !== excludeId)
    .map(menu => ({
      ...menu,
      children: menu.children ? filterMenuTree(menu.children, excludeId) : undefined
    }))
}

// 加载菜单树
const loadMenuTree = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/menus/tree') as any
    if (res.code === 0) {
      menuTree.value = res.data
    } else {
      ElMessage.error(res.message || t('common.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('menuMgmt.loadFailed'))
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = (parentId: number | null) => {
  isEdit.value = false
  editingId.value = null
  form.code = ''
  form.name = ''
  form.type = 'menu'
  form.parentId = parentId
  form.path = ''
  form.icon = ''
  form.sortOrder = 0
  form.isEnabled = true
  form.description = ''
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (menu: MenuItem) => {
  isEdit.value = true
  editingId.value = menu.id
  form.code = menu.code
  form.name = menu.name
  form.type = menu.type || 'menu'
  form.parentId = menu.parentId
  form.path = menu.path || ''
  form.icon = menu.icon || ''
  form.sortOrder = menu.sortOrder || 0
  form.isEnabled = menu.isEnabled !== false
  form.description = menu.description || ''
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const url = isEdit.value ? `/api/admin/menus/${editingId.value}` : '/api/admin/menus'
      const method = isEdit.value ? 'put' : 'post'
      
      const data = {
        code: form.code,
        name: form.name,
        type: form.type,
        parentId: form.parentId,
        path: form.path,
        icon: form.icon,
        sortOrder: form.sortOrder,
        isEnabled: form.isEnabled,
        description: form.description
      }
      
      const res = await request[method](url, data) as any
      
      if (res.code === 0) {
        ElMessage.success(isEdit.value ? t('common.updateSuccess') : t('common.createSuccess'))
        dialogVisible.value = false
        loadMenuTree()
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

// 切换状态
const toggleStatus = async (menu: MenuItem) => {
  try {
    const res = await request.post(`/api/admin/menus/${menu.id}/toggle`) as any
    if (res.code === 0) {
      ElMessage.success(res.message)
    } else {
      // 恢复状态
      menu.isEnabled = !menu.isEnabled
      ElMessage.error(res.message || t('common.operateFailed'))
    }
  } catch (error) {
    menu.isEnabled = !menu.isEnabled
    ElMessage.error(t('common.operateFailed'))
  }
}

// 删除菜单
const deleteMenu = async (menu: MenuItem) => {
  try {
    await ElMessageBox.confirm(
      t('menuMgmt.deleteConfirm', { name: menu.name }),
      t('menuMgmt.deleteConfirmTitle'),
      { confirmButtonText: t('common.confirmDelete'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    
    const res = await request.delete(`/api/admin/menus/${menu.id}`) as any
    if (res.code === 0) {
      ElMessage.success(t('common.deleteSuccess'))
      loadMenuTree()
    } else {
      ElMessage.error(res.message || t('common.deleteFailed'))
    }
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadMenuTree()
})
</script>

<style scoped>
.menu-management {
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
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
