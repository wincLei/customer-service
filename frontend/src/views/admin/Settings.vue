<template>
  <div class="settings">
    <el-row>
      <el-col :md="20">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ t('route.systemSettings') }}</span>
            </div>
          </template>

          <el-form :model="form" label-width="120px">
            <el-form-item :label="t('projectMgmt.projectName')">
              <el-input v-model="form.projectName" :placeholder="t('projectMgmt.namePlaceholder')"></el-input>
            </el-form-item>

            <el-form-item :label="t('projectMgmt.welcomeMessage')">
              <el-input
                v-model="form.welcomeMessage"
                type="textarea"
                :placeholder="t('projectMgmt.welcomeMsgPlaceholder')"
              ></el-input>
              <div class="form-tip">{{ t('projectMgmt.welcomeMsgTip') }}</div>
            </el-form-item>

            <el-form-item :label="t('systemSettings.themeColor')">
              <el-color-picker v-model="form.themeColor"></el-color-picker>
            </el-form-item>

            <el-form-item :label="t('systemSettings.workingHours')">
              <el-time-picker
                v-model="form.workingHours"
                is-range
                :range-separator="t('dashboard.rangeTo')"
                :start-placeholder="t('dashboard.startDate')"
                :end-placeholder="t('dashboard.endDate')"
              ></el-time-picker>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSettings">{{ t('systemSettings.saveSettings') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>


      </el-col>
    </el-row>


  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const form = ref({
  projectName: t('auth.title'),
  welcomeMessage: t('projectMgmt.defaultWelcome'),
  themeColor: '#1890ff',
  workingHours: null,
})

const saveSettings = () => {
  ElMessage.success(t('common.saveSuccess'))
  // TODO: 调用 API 保存设置
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
