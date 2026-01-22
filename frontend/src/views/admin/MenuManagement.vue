<template>
  <div class="menu-management">
    <div class="page-header">
      <h2>菜单管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog(null)">
          <el-icon><Plus /></el-icon>
          新建菜单
        </el-button>
        <el-button @click="loadMenuTree">
          <el-icon><Refresh /></el-icon>
          刷新
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
      <el-table-column prop="name" label="菜单名称" min-width="180">
        <template #default="{ row }">
          <span>
            <el-icon v-if="row.icon" style="margin-right: 5px">
              <component :is="row.icon" />
            </el-icon>
            {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="菜单编码" width="150" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'menu' ? 'primary' : 'success'" size="small">
            {{ row.type === 'menu' ? '菜单' : '按钮' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径" width="150" />
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.isEnabled"
            @change="toggleStatus(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="showCreateDialog(row.id)">
            添加子级
          </el-button>
          <el-button link type="primary" size="small" @click="showEditDialog(row)">
            编辑
          </el-button>
          <el-button link type="danger" size="small" @click="deleteMenu(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑菜单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新建菜单'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="父级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentMenuOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="无（顶级菜单）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="menu">菜单</el-radio>
            <el-radio value="button">按钮/操作</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单编码" prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="如：dashboard、user:manage"
            maxlength="50"
          />
          <div class="form-tip">唯一标识，用于权限判断</div>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.type === 'menu'">
          <el-input v-model="form.path" placeholder="如：/admin/dashboard" maxlength="200" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.type === 'menu'">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          <span class="form-tip" style="margin-left: 10px">数值越小越靠前</span>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.isEnabled" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="2"
            placeholder="菜单功能描述" 
            maxlength="255" 
          />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api'

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
    { required: true, message: '请输入菜单编码', trigger: 'blur' },
    { min: 2, max: 50, message: '编码长度在2-50个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_:]*$/, message: '编码只能包含字母、数字、下划线和冒号，且以字母开头', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 1, max: 100, message: '名称长度在1-100个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
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
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载菜单列表失败')
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
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadMenuTree()
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

// 切换状态
const toggleStatus = async (menu: MenuItem) => {
  try {
    const res = await request.post(`/api/admin/menus/${menu.id}/toggle`) as any
    if (res.code === 0) {
      ElMessage.success(res.message)
    } else {
      // 恢复状态
      menu.isEnabled = !menu.isEnabled
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    menu.isEnabled = !menu.isEnabled
    ElMessage.error('操作失败')
  }
}

// 删除菜单
const deleteMenu = async (menu: MenuItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单 "${menu.name}" 吗？`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    
    const res = await request.delete(`/api/admin/menus/${menu.id}`) as any
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadMenuTree()
    } else {
      ElMessage.error(res.message || '删除失败')
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
