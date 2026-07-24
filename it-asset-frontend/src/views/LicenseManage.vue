<template>
  <div class="page-container">
    <div class="header-title">
      <span>软件许可证管理</span>
      <el-button type="primary" size="small" @click="showAdd" v-if="userRole === 2"><el-icon><Plus /></el-icon> 新增许可证</el-button>
    </div>

    <el-alert v-if="expiringList.length > 0" type="warning" :closable="false" style="margin-bottom:16px">
      <b>即将到期（30天内）：</b>
      <span v-for="(l,i) in expiringList" :key="i">{{ l.softwareName }}({{ l.expireDate }})<template v-if="i < expiringList.length-1">，</template></span>
    </el-alert>

    <div class="table-section">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="licenseId" label="ID" width="60" align="center" />
        <el-table-column prop="softwareName" label="软件名称" min-width="160" />
        <el-table-column prop="vendor" label="厂商" width="100" />
        <el-table-column prop="licenseKey" label="授权号" width="140" />
        <el-table-column label="用量" width="100" align="center">
          <template #default="{row}">{{ row.usedCount }}/{{ row.totalCount }}</template>
        </el-table-column>
        <el-table-column label="到期日期" width="110" align="center">
          <template #default="{row}">
            <span :style="{color: isExpired(row) ? '#F56C6C' : ''}">{{ row.expireDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="80" align="center" />
        <el-table-column prop="responsiblePerson" label="负责人" width="90" />
        <el-table-column label="操作" width="160" align="center" v-if="userRole === 2">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无许可证数据" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑许可证':'新增许可证'" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="软件名称" prop="softwareName"><el-input v-model="form.softwareName" /></el-form-item>
        <el-form-item label="厂商"><el-input v-model="form.vendor" /></el-form-item>
        <el-form-item label="授权号"><el-input v-model="form.licenseKey" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="授权总数"><el-input-number v-model="form.totalCount" :min="1" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="已使用"><el-input-number v-model="form.usedCount" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="到期日期"><el-date-picker v-model="form.expireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%"/></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="form.unitPrice" :precision="2" style="width:100%"/></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.responsiblePerson" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userRole = computed(() => parseInt(localStorage.getItem('role')||'1'))
const loading=ref(false),tableData=ref([]),expiringList=ref([]),dialogVisible=ref(false),isEdit=ref(false),formRef=ref(null)
const form=reactive({licenseId:null,softwareName:'',vendor:'',licenseKey:'',totalCount:1,usedCount:0,expireDate:'',unitPrice:null,responsiblePerson:''})
const rules=reactive({softwareName:[{required:true,message:'请输入名称',trigger:'blur'}]})

const isExpired=(row)=>row.expireDate && new Date(row.expireDate) <= new Date()

const load=async()=>{loading.value=true;try{const r=await request.get('/license/list');tableData.value=r.data||[]}catch(e){/* ignore */}finally{loading.value=false}}
const loadExpiring=async()=>{try{const r=await request.get('/license/expiring');expiringList.value=r.data||[]}catch(e){/* ignore */}}
const showAdd=()=>{isEdit.value=false;Object.assign(form,{licenseId:null,softwareName:'',vendor:'',licenseKey:'',totalCount:1,usedCount:0,expireDate:'',unitPrice:null,responsiblePerson:''});formRef.value?.clearValidate();dialogVisible.value=true}
const showEdit=(row)=>{isEdit.value=true;Object.assign(form,{licenseId:row.licenseId,softwareName:row.softwareName,vendor:row.vendor,licenseKey:row.licenseKey,totalCount:row.totalCount,usedCount:row.usedCount,expireDate:row.expireDate,unitPrice:row.unitPrice,responsiblePerson:row.responsiblePerson});formRef.value?.clearValidate();dialogVisible.value=true}
const submit=async()=>{if(!formRef.value)return;try{await formRef.value.validate();const u=isEdit.value?'/license/update':'/license/save';const r=await request.post(u,form);if(r.code===200){ElMessage.success(isEdit.value?'更新成功':'添加成功');dialogVisible.value=false;load();loadExpiring()}}catch(e){/* validation */}}
const handleDelete=(row)=>{ElMessageBox.confirm(`删除「${row.softwareName}」？`,'确认',{type:'warning'}).then(async()=>{const r=await request.post('/license/delete',null,{params:{licenseId:row.licenseId}});if(r.code===200){ElMessage.success('已删除');load();loadExpiring()}}).catch(()=>{})}

onMounted(()=>{load();loadExpiring()})
</script>

<style scoped>
.page-container{width:95%;margin:0 auto;padding:20px}
.header-title{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:16px 24px;font-size:18px;font-weight:bold;border-radius:8px;margin-bottom:20px;display:flex;align-items:center;justify-content:space-between;box-shadow:0 2px 12px rgba(102,126,234,0.3)}
.table-section{background:white;padding:20px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.05)}
</style>
