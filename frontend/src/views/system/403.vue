<template>
  <el-result
    status="403"
    :title="$t('error.forbiddenTitle')"
    :subTitle="$t('error.forbiddenSub')"
  >
    <template #extra>
      <el-button type="primary" @click="goHome">{{ $t('error.backHome') }}</el-button>
    </template>
  </el-result>
</template>
<script setup>
import { useRouter } from 'vue-router'
import {HOME_PAGE} from "@/constants/index.js";

const router = useRouter()

function goHome() {
  router.push({ name: HOME_PAGE })
}
</script>
