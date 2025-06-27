<script setup></script>

<template>
  <div class="search-dropdown">
    <el-input
      placeholder="Search something..."
      v-model="searchText"
      style="max-width: 300px"
    >
      <template #append>
        <el-popover
          v-model:visible="popoverVisible"
          placement="bottom"
          width="260"
          trigger="manual"
        >
          <div>detail</div>
          <div>detail</div>
          <div>detail</div>
          <div>detail</div>
          <div>detail</div>
          <div>detail</div>
          <template #reference>
            <!-- <el-icon><Filter /></el-icon> -->
            <el-button
              :icon="Filter"
              @click="popoverVisible = !popoverVisible"
            />
          </template>
        </el-popover>
      </template>
    </el-input>
  </div>
</template>
