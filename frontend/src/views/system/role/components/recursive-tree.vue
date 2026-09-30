<template>
  <div v-for="module in props.tree" :key="module.menuId">
    <div class="menu" :style="{ marginLeft: `${ props.index * 4}%` }">
      <div class="menu-item">
        <el-checkbox
            @change="selectCheckbox(module)"
            size="default"
            :value="module.menuId"
            :label="translateMenuName(module.menuName, module.uri)"
        />
      </div>
      <div v-if="module.children && module.children.some((e) => e.type === 3)">
        <div class="point">
          <div class="each-point" v-for="point in module.children" :key="point.menuId">
            <el-checkbox @change="selectCheckbox(point)" :value="point.menuId">
              {{ translateMenuName(point.menuName) }}
            </el-checkbox>
          </div>
        </div>
      </div>
    </div>

    <template v-if="module.children && !module.children.some((e) => e.type === 3)">
      <RecursiveTree :tree="module.children" :index="props.index + 1"/>
    </template>
  </div>
</template>

<script setup>
import {ref, watch} from 'vue';
import {useMenuSettingStore} from "@/stores/menu-setting.js";
import {translateMenuName} from '@/i18n/menu.js';

const props = defineProps({
  tree: {
    type: Array,
    default: () => [],
  },
  index: {
    type: Number,
    default: 0,
  },
});

let menuSettingStore = useMenuSettingStore();

function selectCheckbox(module) {
  if (!module.menuId) return;
  // 是否勾选
  const checkedData = menuSettingStore.checkedData;
  const findIndex = checkedData.indexOf(module.menuId);

  if (findIndex === -1) {
    // 选中本级以及子级
    menuSettingStore.addCheckedDataAndChildren(module);
    // 选中上级
    menuSettingStore.selectUpperLevel(module);
  } else {
    // 取消选中本级以及子级
    menuSettingStore.deleteCheckedDataAndChildren(module);
  }
}
</script>

<style scoped lang="scss">
.menu {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 10px;

  .menu-item {
    padding-right: 30px;
  }

  .point {
    display: flex;
    border-left: 1px solid #f0f0f0;
    margin-left: 10px;
    padding-left: 10px;

    .each-point {
      margin-left: 10px;
    }
  }
}
</style>