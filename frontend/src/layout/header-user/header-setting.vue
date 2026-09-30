<template>
  <el-drawer :title="t('setting.title')" direction="rtl" v-model="visible" @close="close">
    <el-form label-position="left" label-width="80px">
      <el-form-item :label="t('layout.language')">
        <el-select v-model="configStore.language" @change="applyLanguage">
          <el-option :label="t('layout.chinese')" value="zh-cn"/>
          <el-option :label="t('layout.english')" value="en"/>
        </el-select>
      </el-form-item>
      <el-form-item :label="t('setting.themeColor')">
        <div class="color-container">
          <template v-for="(item, index) in themeColors" :key="index">
            <div v-if="item.primaryColor === useAdminConfigStore().themeColor" class="color">
              <el-icon :style="{ color: item.primaryColor, fontSize: '22px' }">
                <CircleCheckFilled/>
              </el-icon>
            </div>
            <div v-else @click=" useAdminConfigStore().themeColor = item.primaryColor" class="color">
              <svg
                  class="icon"
                  viewBox="0 0 1024 1024"
                  :fill="item.primaryColor"
                  xmlns="http://www.w3.org/2000/svg"
                  width="26"
                  height="26"
              >
                <path
                    d="M128 160.01219c0-17.67619 14.336-32.01219 32.01219-32.01219h704c17.65181 0 31.98781 14.336 31.98781 32.01219v704c0 17.65181-14.336 31.98781-32.01219 31.98781H160.036571a31.98781 31.98781 0 0 1-32.01219-32.01219V160.036571z"
                />
              </svg>
            </div>
          </template>
        </div>
      </el-form-item>
      <el-form-item :label="t('setting.menuWidth')">
        <el-input v-model="useAdminConfigStore().sideMenuWidth"/>
      </el-form-item>
      <el-form-item :label="t('setting.breadcrumb')">
        <el-switch v-model="useAdminConfigStore().breadCrumbShow" :active-text="t('common.show')" :inactive-text="t('common.hide')"/>
      </el-form-item>
      <el-form-item :label="t('setting.pageTab')">
        <el-switch v-model="useAdminConfigStore().pageTabShow" :active-text="t('common.show')" :inactive-text="t('common.hide')"/>
      </el-form-item>
      <el-form-item :label="t('setting.footer')">
        <el-switch v-model="useAdminConfigStore().footerShow" :active-text="t('common.show')" :inactive-text="t('common.hide')"/>
      </el-form-item>
      <br/>
      <br/>
    </el-form>

    <div class="footer">
      <el-button type="danger" block @click="useAdminConfigStore().reset()">{{ t('setting.reset') }}</el-button>
    </div>
  </el-drawer>
</template>

<script setup>
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {themeColors, useAdminConfigStore} from "@/stores/admin-config.js";
import {applyLanguage} from '@/i18n/index.js'

const {t} = useI18n()
const configStore = useAdminConfigStore()
// ----------------- 显示与隐藏 -----------------
const visible = ref(false)
defineExpose({
  show
})

function close() {
  visible.value = false
}

function show() {
  visible.value = true
}

// ------------------------------------------
</script>
<style lang="scss" scoped>
.footer {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 100%;
  border-top: 1px solid #e9e9e9;
  padding: 10px 16px;
  background: #fff;
  text-align: left;
  z-index: 1;
}

.color-container {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.color {
  margin-left: 8px;
  height: 26px;
  width: 26px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}
</style>
