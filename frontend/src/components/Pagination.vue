<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :background="background"
      :small="small"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DEFAULT_PAGE_SIZE } from '@/constants'

interface Props {
  total: number
  page?: number
  size?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
  small?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  total: 0,
  page: 1,
  size: DEFAULT_PAGE_SIZE,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  small: false
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'change', params: { page: number; size: number }): void
}>()

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val)
})

const pageSize = computed({
  get: () => props.size,
  set: (val) => emit('update:size', val)
})

const handleSizeChange = (size: number) => {
  emit('update:size', size)
  emit('update:page', 1) // 切换每页条数时重置到第一页
  emit('change', { page: 1, size })
}

const handleCurrentChange = (page: number) => {
  emit('update:page', page)
  emit('change', { page, size: props.size })
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
