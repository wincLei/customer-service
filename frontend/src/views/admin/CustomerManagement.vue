<template>
  <div class="customer-management">
    <div class="page-header">
      <h2>用户管理</h2>
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
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-bar" v-if="selectedProjectId">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="游客" name="guest" />
        <el-tab-pane label="用户" name="registered" />
      </el-tabs>
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索昵称、手机号、UID"
          clearable
          style="width: 240px; margin-right: 12px;"
          @keyup.enter="loadCustomers"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadCustomers">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-bar" v-if="selectedProjectId && stats">
      <el-tag type="info">总用户: {{ stats.total }}</el-tag>
      <el-tag type="warning" style="margin-left: 8px;">游客: {{ stats.guests }}</el-tag>
      <el-tag type="success" style="margin-left: 8px;">注册用户: {{ stats.registered }}</el-tag>
    </div>

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedUsers.length > 0">
      <span class="selected-count">已选择 {{ selectedUsers.length }} 项</span>
      <el-button size="small" @click="showBatchTagDialog">批量设置标签</el-button>
    </div>

    <!-- 用户列表 -->
    <el-table 
      v-if="selectedProjectId"
      :data="customers" 
      style="width: 100%" 
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" min-width="120">
        <template #default="{ row }">
          <span>{{ row.nickname || row.uid }}</span>
          <el-tag v-if="row.isGuest" size="small" type="warning" style="margin-left: 4px;">游客</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130">
        <template #default="{ row }">
          {{ row.phone || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="deviceType" label="设备" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="getDeviceTagType(row.deviceType)">
            {{ row.deviceType || 'Unknown' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="city" label="城市" width="100">
        <template #default="{ row }">
          {{ row.city || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="180">
        <template #default="{ row }">
          <div class="tag-list" v-if="row.tags && row.tags.length > 0">
            <el-tag 
              v-for="tag in row.tags.slice(0, 3)" 
              :key="tag.id" 
              size="small"
              :color="tag.color"
              style="margin-right: 4px; color: #fff;"
            >
              {{ tag.name }}
            </el-tag>
            <el-tag v-if="row.tags.length > 3" size="small" type="info">
              +{{ row.tags.length - 3 }}
            </el-tag>
          </div>
          <span v-else class="text-gray">-</span>
        </template>
      </el-table-column>
      <el-table-column label="最后活跃" width="160">
        <template #default="{ row }">
          {{ formatTime(row.lastActiveAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
          <el-button link type="primary" @click="showTagDialog(row)">标签</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!selectedProjectId" description="请先选择项目" />

    <!-- 分页 -->
    <div class="pagination-container" v-if="selectedProjectId && total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadCustomers"
        @size-change="loadCustomers"
      />
    </div>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="UID">
          <el-input :value="editingCustomer?.uid" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设置标签对话框 -->
    <el-dialog
      v-model="tagDialogVisible"
      title="设置标签"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="tag-dialog-content">
        <p class="dialog-tip">为用户 "{{ editingCustomer?.nickname || editingCustomer?.uid }}" 设置标签</p>
        <el-checkbox-group v-model="selectedTagIds">
          <div v-for="tag in projectTags" :key="tag.id" class="tag-checkbox-item">
            <el-checkbox :label="tag.id" :value="tag.id">
              <el-tag :color="tag.color" style="color: #fff;">{{ tag.name }}</el-tag>
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <el-empty v-if="projectTags.length === 0" description="暂无标签，请先在标签管理中创建" />
      </div>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTags" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量设置标签对话框 -->
    <el-dialog
      v-model="batchTagDialogVisible"
      title="批量设置标签"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="tag-dialog-content">
        <p class="dialog-tip">为 {{ selectedUsers.length }} 个用户添加标签</p>
        <el-select v-model="batchTagId" placeholder="请选择要添加的标签" style="width: 100%;">
          <el-option
            v-for="tag in projectTags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          >
            <el-tag :color="tag.color" style="color: #fff; margin-right: 8px;">{{ tag.name }}</el-tag>
          </el-option>
        </el-select>
      </div>
      <template #footer>
        <el-button @click="batchTagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchTag" :loading="submitting">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, User } from '@element-plus/icons-vue'
import request from '@/api'
import { DEFAULT_PAGE_SIZE } from '@/constants'

interface Project {
  id: number
  name: string
}

interface CustomerTag {
  id: number
  name: string
  color: string
}

interface Customer {
  id: number
  uid: string
  externalUid: string | null
  isGuest: boolean
  nickname: string
  avatar: string
  phone: string
  email: string
  deviceType: string
  city: string
  lastActiveAt: string
  createdAt: string
  tags?: CustomerTag[]
}

const loading = ref(false)
const submitting = ref(false)
const selectedProjectId = ref<number | null>(null)
const projects = ref<Project[]>([])
const customers = ref<Customer[]>([])
const projectTags = ref<CustomerTag[]>([])
const keyword = ref('')
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(DEFAULT_PAGE_SIZE)
const total = ref(0)
const stats = ref<{ total: number; guests: number; registered: number } | null>(null)

const selectedUsers = ref<Customer[]>([])
const editDialogVisible = ref(false)
const tagDialogVisible = ref(false)
const batchTagDialogVisible = ref(false)
const editingCustomer = ref<Customer | null>(null)
const selectedTagIds = ref<number[]>([])
const batchTagId = ref<number | null>(null)

const editForm = reactive({
  nickname: '',
  phone: '',
  email: ''
})

// 计算筛选条件
const isGuestFilter = computed(() => {
  if (activeTab.value === 'guest') return true
  if (activeTab.value === 'registered') return false
  return undefined
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
  currentPage.value = 1
  loadCustomers()
  loadProjectTags()
  loadStats()
}

// 标签切换
const onTabChange = () => {
  currentPage.value = 1
  loadCustomers()
}

// 加载用户列表
const loadCustomers = async () => {
  if (!selectedProjectId.value) return

  loading.value = true
  try {
    const params: any = {
      projectId: selectedProjectId.value,
      page: currentPage.value,
      size: pageSize.value
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    if (isGuestFilter.value !== undefined) {
      params.isGuest = isGuestFilter.value
    }

    const res = await request.get('/api/admin/customers', { params }) as any
    if (res.code === 0) {
      customers.value = res.data.list
      total.value = res.data.total

      // 加载每个用户的标签
      await loadUsersTags()
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载用户标签
const loadUsersTags = async () => {
  for (const customer of customers.value) {
    try {
      const res = await request.get(`/api/admin/customers/${customer.id}/tags`) as any
      if (res.code === 0) {
        customer.tags = res.data
      }
    } catch (error) {
      // 忽略单个用户标签加载失败
    }
  }
}

// 加载项目标签
const loadProjectTags = async () => {
  if (!selectedProjectId.value) return

  try {
    const res = await request.get('/api/admin/customer-tags', {
      params: { projectId: selectedProjectId.value }
    }) as any
    if (res.code === 0) {
      projectTags.value = res.data
    }
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

// 加载统计
const loadStats = async () => {
  if (!selectedProjectId.value) return

  try {
    const res = await request.get('/api/admin/customers/stats', {
      params: { projectId: selectedProjectId.value }
    }) as any
    if (res.code === 0) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 重置搜索
const resetSearch = () => {
  keyword.value = ''
  activeTab.value = 'all'
  currentPage.value = 1
  loadCustomers()
}

// 选择变化
const handleSelectionChange = (selection: Customer[]) => {
  selectedUsers.value = selection
}

// 显示编辑对话框
const showEditDialog = (customer: Customer) => {
  editingCustomer.value = customer
  editForm.nickname = customer.nickname || ''
  editForm.phone = customer.phone || ''
  editForm.email = customer.email || ''
  editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!editingCustomer.value) return

  submitting.value = true
  try {
    const res = await request.put(`/api/admin/customers/${editingCustomer.value.id}`, editForm) as any
    if (res.code === 0) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      loadCustomers()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 显示标签对话框
const showTagDialog = async (customer: Customer) => {
  editingCustomer.value = customer
  // 获取用户当前标签
  try {
    const res = await request.get(`/api/admin/customers/${customer.id}/tags`) as any
    if (res.code === 0) {
      selectedTagIds.value = res.data.map((t: CustomerTag) => t.id)
    } else {
      selectedTagIds.value = []
    }
  } catch (error) {
    selectedTagIds.value = []
  }
  tagDialogVisible.value = true
}

// 提交标签
const submitTags = async () => {
  if (!editingCustomer.value) return

  submitting.value = true
  try {
    const res = await request.put(`/api/admin/customers/${editingCustomer.value.id}/tags`, {
      tagIds: selectedTagIds.value
    }) as any
    if (res.code === 0) {
      ElMessage.success('标签设置成功')
      tagDialogVisible.value = false
      loadCustomers()
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch (error) {
    ElMessage.error('设置失败')
  } finally {
    submitting.value = false
  }
}

// 显示批量标签对话框
const showBatchTagDialog = () => {
  batchTagId.value = null
  batchTagDialogVisible.value = true
}

// 提交批量标签
const submitBatchTag = async () => {
  if (!batchTagId.value) {
    ElMessage.warning('请选择标签')
    return
  }

  submitting.value = true
  try {
    const res = await request.post('/api/admin/customers/batch/tags', {
      userIds: selectedUsers.value.map(u => u.id),
      tagId: batchTagId.value
    }) as any
    if (res.code === 0) {
      ElMessage.success('批量添加成功')
      batchTagDialogVisible.value = false
      selectedUsers.value = []
      loadCustomers()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 获取设备类型标签样式
const getDeviceTagType = (deviceType: string) => {
  if (deviceType === 'pc') return 'primary'
  if (deviceType === 'mobile') return 'success'
  if (deviceType === 'tablet') return 'warning'
  return 'info'
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
.customer-management {
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

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  align-items: center;
}

.stats-bar {
  margin-bottom: 16px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.selected-count {
  color: #409eff;
  font-weight: 500;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.text-gray {
  color: #909399;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-dialog-content {
  padding: 10px 0;
}

.dialog-tip {
  color: #606266;
  margin-bottom: 16px;
}

.tag-checkbox-item {
  margin-bottom: 12px;
}
</style>
