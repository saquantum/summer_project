<script setup></script>
<template>
  <el-row>
    <el-col :span="12">
      <el-card style="max-width: 480px">
        <template #header>
          <div class="card-header">
            <span>Map</span>
          </div>
        </template>
        <p v-for="o in 4" :key="o" class="text item">{{ 'List item ' + o }}</p>
        <template #footer>Footer content</template>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-form>
        <el-form-item>
          <el-dropdown>
            <span class="el-dropdown-link">
              Dropdown List
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>Action 1</el-dropdown-item>
                <el-dropdown-item>Action 2</el-dropdown-item>
                <el-dropdown-item>Action 3</el-dropdown-item>
                <el-dropdown-item disabled>Action 4</el-dropdown-item>
                <el-dropdown-item divided>Action 5</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="input"
            style="width: 240px"
            placeholder="Please input"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="input"
            style="width: 240px"
            placeholder="Please input"
          />
        </el-form-item>
        <el-form-item>
          <el-button>Default</el-button>
          <el-button>Default</el-button>
        </el-form-item>
        <el-form-item>
          <el-button>Default</el-button>
          <el-button>Default</el-button>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
</template>
