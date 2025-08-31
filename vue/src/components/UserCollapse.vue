<script setup lang="ts">
import { ref } from 'vue'
const props = defineProps({
  users: Object
})
const activeNames = ref<(number | string)[]>([])

const handleChange = (val: number) => {
  console.log(val)
}
</script>

<template>
  <el-collapse
    v-if="props.users"
    v-model="activeNames"
    class="mobile-collapse"
    @change="handleChange"
  >
    <el-collapse-item
      v-for="(item, index) in props.users"
      :key="index"
      :name="index"
      class="mobile-user-card"
    >
      <template #title>
        <div class="title-wrapper">
          <div class="title-name">{{ item.username }}</div>
          <div class="title-meta">{{ item.count }}</div>
        </div>
      </template>

      <div class="content grid-kv">
        <div class="k">Asset Holder Id</div>
        <div class="v">{{ item.assets }}</div>
        <div class="k">Permission</div>
        <div class="v">
          <div class="perm-row">
            <PermissionIndicator :status="true"></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
          </div>
        </div>

        <div class="k">Action</div>
        <div class="v actions-row">
          <el-button text type="primary" size="small" class="btn-edit"
            >Edit</el-button
          >
          <el-button text type="danger" size="small" class="btn-del"
            >Delete</el-button
          >
        </div>
      </div>
    </el-collapse-item>
  </el-collapse>
</template>

<style scoped>
.mobile-collapse {
  background: transparent;
  border: 0;
  max-width: 700px;
}

.btn-del {
  padding: 8px 10px;
  border-radius: 8px;
  color: rgba(104, 5, 5, 0.72);
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background: rgba(234, 215, 184, 0.5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
}

.btn-edit {
  padding: 8px 10px;
  border-radius: 8px;
  color: #ffffff;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background: rgba(122, 164, 194, 0.78);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
}

.mobile-user-card {
  margin: 10px 0;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #eef2f7;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
  background: #fff;
}

.title-wrapper {
  padding-left: 44px;
  padding-right: 16px;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 8px;
}
.title-name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
  word-break: break-word;
}
.title-meta {
  color: #475569;
  font-size: 12px;
  padding-left: 20px;
}

:deep(.mobile-user-card .el-collapse-item__header) {
  position: relative;
  padding: 14px 0;
}
:deep(.mobile-user-card .el-collapse-item__arrow) {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  margin: 0;
}

:deep(.mobile-user-card .el-collapse-item__wrap) {
  border-top: 1px solid #f1f5f9;
}
:deep(.mobile-user-card .el-collapse-item__content) {
  padding: 12px 16px 14px;
}

.grid-kv {
  display: grid;
  grid-template-columns: 130px 1fr;
  column-gap: 5px;
  row-gap: 8px;
  align-items: start;
}
.k {
  color: #64748b;
}
.v {
  color: #334155;
  word-break: break-word;
}

.perm-row {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: wrap;
}
.actions-row {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}
</style>
