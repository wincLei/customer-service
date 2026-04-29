<template>
  <div class="auto-reply-page">
    <div class="page-header">
      <h2>{{ $t('autoReplyMgmt.title') }}</h2>
    </div>

    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select
          v-model="selectedProjectId"
          :placeholder="$t('autoReplyMgmt.selectProject')"
          style="width: 200px"
          @change="loadRules"
        >
          <el-option
            v-for="proj in projects"
            :key="proj.id"
            :label="proj.name"
            :value="proj.id"
          />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          {{ $t('autoReplyMgmt.addRule') }}
        </el-button>
      </div>
    </div>

    <!-- 规则列表 -->
    <el-table
      v-loading="loading"
      :data="rules"
      style="width: 100%; margin-top: 16px"
      border
    >
      <el-table-column
        prop="ruleName"
        :label="$t('autoReplyMgmt.ruleName')"
        min-width="120"
      />
      <el-table-column
        prop="keywords"
        :label="$t('autoReplyMgmt.keywords')"
        min-width="180"
      >
        <template #default="{ row }">
          <div class="keywords-cell">
            <el-tag
              v-for="kw in parseKeywords(row.keywords)"
              :key="kw"
              size="small"
              style="margin: 2px"
            >{{ kw }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="replyContent"
        :label="$t('autoReplyMgmt.replyContent')"
        min-width="220"
        show-overflow-tooltip
      />
      <el-table-column
        prop="priority"
        :label="$t('autoReplyMgmt.priority')"
        width="90"
        align="center"
      />
      <el-table-column
        prop="enabled"
        :label="$t('autoReplyMgmt.enabled')"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            @change="toggleRule(row)"
          />
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('common.actions')"
        width="150"
        align="center"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openEditDialog(row)">
            {{ $t('common.edit') }}
          </el-button>
          <el-button type="danger" size="small" link @click="deleteRule(row)">
            {{ $t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? $t('autoReplyMgmt.editRule') : $t('autoReplyMgmt.addRule')"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item :label="$t('autoReplyMgmt.ruleName')" prop="ruleName">
          <el-input
            v-model="form.ruleName"
            :placeholder="$t('autoReplyMgmt.ruleNamePlaceholder')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item :label="$t('autoReplyMgmt.keywords')" prop="keywords">
          <el-input
            v-model="form.keywords"
            :placeholder="$t('autoReplyMgmt.keywordsPlaceholder')"
            maxlength="500"
            show-word-limit
          />
          <div class="form-hint">{{ $t('autoReplyMgmt.keywordsHint') }}</div>
        </el-form-item>

        <el-form-item :label="$t('autoReplyMgmt.replyContent')" prop="replyContent">
          <el-input
            v-model="form.replyContent"
            type="textarea"
            :rows="4"
            :placeholder="$t('autoReplyMgmt.replyContentPlaceholder')"
          />
        </el-form-item>

        <el-form-item :label="$t('autoReplyMgmt.priority')" prop="priority">
          <el-input-number
            v-model="form.priority"
            :min="1"
            :max="9999"
            style="width: 160px"
          />
          <span class="form-hint-inline">{{ $t('autoReplyMgmt.priorityHint') }}</span>
        </el-form-item>

        <el-form-item :label="$t('autoReplyMgmt.enabled')">
          <el-switch
            v-model="form.enabled"
            :active-text="$t('autoReplyMgmt.enabledOn')"
            :inactive-text="$t('autoReplyMgmt.enabledOff')"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveRule">
          {{ $t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import request from '@/api/index'

const { t } = useI18n()

interface Project {
  id: number
  name: string
}

interface AutoReplyRule {
  id: number
  projectId: number
  ruleName: string
  keywords: string
  replyContent: string
  priority: number
  enabled: boolean
}

// State
const projects = ref<Project[]>([])
const selectedProjectId = ref<number | null>(null)
const rules = ref<AutoReplyRule[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()

const form = reactive({
  ruleName: '',
  keywords: '',
  replyContent: '',
  priority: 100,
  enabled: true,
})

const formRules = {
  ruleName: [{ required: true, message: t('autoReplyMgmt.ruleNameRequired'), trigger: 'blur' }],
  keywords: [{ required: true, message: t('autoReplyMgmt.keywordsRequired'), trigger: 'blur' }],
  replyContent: [{ required: true, message: t('autoReplyMgmt.replyContentRequired'), trigger: 'blur' }],
}

// Methods
const parseKeywords = (keywords: string) => {
  return keywords.split(',').map(k => k.trim()).filter(k => k)
}

const loadProjects = async () => {
  try {
    const res = await request.get('/api/admin/projects/all') as any
    projects.value = res.data || res || []
    if (projects.value.length > 0 && !selectedProjectId.value) {
      selectedProjectId.value = projects.value[0].id
      await loadRules()
    }
  } catch {
    ElMessage.error(t('autoReplyMgmt.loadFailed'))
  }
}

const loadRules = async () => {
  if (!selectedProjectId.value) return
  loading.value = true
  try {
    const res = await request.get(`/api/admin/auto-reply/list/${selectedProjectId.value}`) as any
    rules.value = res.data || res || []
  } catch {
    ElMessage.error(t('autoReplyMgmt.loadFailed'))
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  if (!selectedProjectId.value) {
    ElMessage.warning(t('autoReplyMgmt.selectProjectFirst'))
    return
  }
  editingId.value = null
  Object.assign(form, { ruleName: '', keywords: '', replyContent: '', priority: 100, enabled: true })
  dialogVisible.value = true
}

const openEditDialog = (row: AutoReplyRule) => {
  editingId.value = row.id
  Object.assign(form, {
    ruleName: row.ruleName,
    keywords: row.keywords,
    replyContent: row.replyContent,
    priority: row.priority,
    enabled: row.enabled,
  })
  dialogVisible.value = true
}

const saveRule = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      projectId: selectedProjectId.value,
      ...form,
    }
    if (editingId.value) {
      await request.put(`/api/admin/auto-reply/${editingId.value}`, payload)
    } else {
      await request.post('/api/admin/auto-reply', payload)
    }
    dialogVisible.value = false
    await loadRules()
    ElMessage.success(t('common.saveSuccess'))
  } catch {
    ElMessage.error(t('common.saveFailed'))
  } finally {
    saving.value = false
  }
}

const toggleRule = async (row: AutoReplyRule) => {
  try {
    await request.patch(`/api/admin/auto-reply/${row.id}/toggle`)
  } catch {
    // 回滚
    row.enabled = !row.enabled
    ElMessage.error(t('common.operateFailed'))
  }
}

const deleteRule = async (row: AutoReplyRule) => {
  try {
    await ElMessageBox.confirm(
      t('autoReplyMgmt.deleteConfirm', { name: row.ruleName }),
      t('autoReplyMgmt.confirmDeleteTitle'),
      { type: 'warning' }
    )
    await request.delete(`/api/admin/auto-reply/${row.id}`)
    await loadRules()
    ElMessage.success(t('common.deleteSuccess'))
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(t('common.deleteFailed'))
    }
  }
}

onMounted(loadProjects)
</script>

<style scoped>
.auto-reply-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.keywords-cell {
  line-height: 1.8;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-hint-inline {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style>
