<template>
  <div class="page-container">
    <div class="header-title">
      <span>耗材管理</span>
      <el-button type="primary" size="small" @click="showAdd" v-if="userRole === 2">
        <el-icon><Plus /></el-icon> 新增耗材
      </el-button>
      <el-button size="small" type="warning" @click="showUseRecords"><el-icon><Tickets /></el-icon> 申请记录</el-button>
      <el-button size="small" @click="exportConsumable"><el-icon><Download /></el-icon> 导出</el-button>
    </div>

    <!-- 低库存预警 -->
    <el-alert v-if="lowStockList.length > 0" type="warning" :closable="false" style="margin-bottom:16px">
      <b>低库存预警：</b>
      <span v-for="(c,i) in lowStockList" :key="i">
        {{ c.consumableName }}({{ c.currentStock }}/{{ c.minStock }}{{ c.unit }}，建议补充 {{ c.suggestedReplenishment || (c.minStock - c.currentStock) }}{{ c.unit }})
        <template v-if="i < lowStockList.length - 1">，</template>
      </span>
    </el-alert>

    <div class="table-section">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="consumableId" label="ID" width="70" align="center" />
        <el-table-column prop="consumableName" label="耗材名称" min-width="160" />
        <el-table-column prop="category" label="分类" width="80">
          <template #default="{row}"><el-tag size="small">{{row.category||'-'}}</el-tag></template>
        </el-table-column>
        <el-table-column label="库存" width="100" align="center">
          <template #default="{row}">
            <span :style="{color: row.currentStock <= row.minStock ? '#F56C6C' : ''}">
              <b>{{ row.currentStock }}</b> {{ row.unit }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="80" align="center" />
        <el-table-column prop="price" label="单价(元)" width="90" align="center" />
        <el-table-column prop="supplier" label="供应商" width="100" />
        <el-table-column label="操作" width="300" align="center" v-if="userRole === 2">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="showStockIn(row)">入库</el-button>
            <el-button size="small" type="warning" @click="showStockOut(row)">出库</el-button>
            <el-button size="small" type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column label="领用" width="140" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="showUse(row)">申请</el-button>
            <el-button size="small" type="info" @click="showRecords(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无耗材数据" />

    <!-- 领用申请 -->
    <el-dialog v-model="useVisible" title="耗材领用申请" width="420px">
      <el-form :model="useForm" label-width="80px">
        <el-form-item label="耗材">{{ useTarget?.consumableName || '' }}</el-form-item>
        <el-form-item label="库存">{{ useTarget?.currentStock || 0 }}</el-form-item>
        <el-form-item label="数量"><el-input-number v-model="useForm.quantity" :min="1" :max="useTarget?.currentStock||1" /></el-form-item>
        <el-form-item label="申请人"><el-input v-model="useForm.applicant" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="useForm.department" /></el-form-item>
        <el-form-item label="用途"><el-input v-model="useForm.usePurpose" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="useVisible=false">取消</el-button><el-button type="primary" @click="submitUse">提交领用</el-button></template>
    </el-dialog>
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑耗材':'新增耗材'" width="450px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="consumableName"><el-input v-model="form.consumableName" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" placeholder="墨盒/纸张/配件" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item>
        <el-form-item label="当前库存"><el-input-number v-model="form.currentStock" :min="0" /></el-form-item>
        <el-form-item label="最低库存"><el-input-number v-model="form.minStock" :min="1" /></el-form-item>
        <el-form-item label="单价(元)"><el-input-number v-model="form.price" :precision="2" :min="0" style="width:100%"/></el-form-item>
        <el-form-item label="供应商"><el-input v-model="form.supplier" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">确定</el-button></template>
    </el-dialog>

    <!-- 出入库 -->
    <el-dialog v-model="stockVisible" :title="stockInMode?'入库':'出库'" width="360px">
      <p>耗材：<b>{{ stockTarget?.consumableName }}</b>（当前：{{ stockTarget?.currentStock }}{{ stockTarget?.unit }}）</p>
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="数量"><el-input-number v-model="stockForm.quantity" :min="1" :max="stockInMode?9999:stockTarget?.currentStock" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="stockForm.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="stockVisible=false">取消</el-button><el-button type="primary" @click="submitStock">确定</el-button></template>
    </el-dialog>

    <!-- 领用记录 -->
    <el-dialog v-model="recordVisible" :title="'操作记录 - ' + recordTarget?.consumableName" width="580px">
      <el-table :data="recordList" v-loading="recordLoading" border max-height="350" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{row}"><el-tag :type="row.type===1?'success':row.type===2?'warning':'info'" size="small">{{ typeText(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
      <el-empty v-if="!recordLoading && recordList.length===0" description="暂无操作记录" />
    </el-dialog>

    <!-- 领用申请记录 -->
    <el-dialog v-model="useRecordVisible" title="领用申请记录" width="760px">
      <el-table :data="useRecordList" v-loading="useRecordLoading" border max-height="400" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="consumableName" label="耗材名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column prop="usePurpose" label="用途" min-width="140" show-overflow-tooltip />
        <el-table-column label="审批状态" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="approveType(row.approveStatus)" size="small">{{ approveText(row.approveStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveUser" label="审批人" width="90" />
        <el-table-column prop="createTime" label="申请时间" width="160" />
      </el-table>
      <el-empty v-if="!useRecordLoading && useRecordList.length===0" description="暂无申请记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Tickets } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { exportCSV } from '@/utils/export'

const userRole = computed(() => parseInt(localStorage.getItem('role')||'1'))
const loading=ref(false),tableData=ref([]),lowStockList=ref([]),dialogVisible=ref(false),isEdit=ref(false),formRef=ref(null)
const stockVisible=ref(false),stockInMode=ref(true),stockTarget=ref(null)
const useVisible=ref(false),useTarget=ref(null),useForm=reactive({quantity:1,applicant:localStorage.getItem('username')||'',department:'',usePurpose:''})
const recordVisible=ref(false),recordTarget=ref(null),recordLoading=ref(false),recordList=ref([])
const useRecordVisible=ref(false),useRecordLoading=ref(false),useRecordList=ref([])
const form=reactive({consumableId:null,consumableName:'',category:'',unit:'个',currentStock:0,minStock:5,price:null,supplier:''})
const rules=reactive({consumableName:[{required:true,message:'请输入名称',trigger:'blur'}]})
const stockForm=reactive({quantity:1,remark:''})

const loadData=async()=>{
  loading.value=true
  try{const r=await request.get('/consumable/list');if(r.code===200)tableData.value=r.data||[]}
  catch{ElMessage.error('加载失败')}finally{loading.value=false}
}
const loadLowStock=async()=>{
  try{const r=await request.get('/consumable/alerts');if(r.code===200)lowStockList.value=r.data||[]}catch(e){/* ignore */}
}
const showAdd=()=>{isEdit.value=false;Object.assign(form,{consumableId:null,consumableName:'',category:'',unit:'个',currentStock:0,minStock:5,price:null,supplier:''});formRef.value?.clearValidate();dialogVisible.value=true}
const showEdit=(row)=>{isEdit.value=true;Object.assign(form,{consumableId:row.consumableId,consumableName:row.consumableName,category:row.category,unit:row.unit,currentStock:row.currentStock,minStock:row.minStock,price:row.price,supplier:row.supplier});formRef.value?.clearValidate();dialogVisible.value=true}
const submit=async()=>{if(!formRef.value)return;try{await formRef.value.validate();const u=isEdit.value?'/consumable/update':'/consumable/save';const r=await request.post(u,form);if(r.code===200){ElMessage.success(isEdit.value?'更新成功':'添加成功');dialogVisible.value=false;loadData()}}catch(e){/* validation */}}
const handleDelete=(row)=>{ElMessageBox.confirm(`删除「${row.consumableName}」？`,'确认',{type:'warning'}).then(async()=>{const r=await request.post('/consumable/delete',null,{params:{consumableId:row.consumableId}});if(r.code===200){ElMessage.success('已删除');loadData()}}).catch(()=>{})}

const showStockIn=(row)=>{stockInMode.value=true;stockTarget.value=row;Object.assign(stockForm,{quantity:1,remark:''});stockVisible.value=true}
const showStockOut=(row)=>{stockInMode.value=false;stockTarget.value=row;Object.assign(stockForm,{quantity:1,remark:''});stockVisible.value=true}
const submitStock=async()=>{
  try {
    const u=stockInMode.value?'/consumable/stock-in':'/consumable/stock-out'
    const r=await request.post(u,{consumableId:stockTarget.value.consumableId,quantity:stockForm.quantity,operator:localStorage.getItem('username')||'admin'})
    if(r.code===200){ElMessage.success(stockInMode.value?'入库成功':'出库成功');stockVisible.value=false;loadData();loadLowStock()}else ElMessage.error(r.msg)
  } catch(e) { ElMessage.error('操作失败') }
}

const showUse=(row)=>{useTarget.value=row;Object.assign(useForm,{quantity:1,applicant:localStorage.getItem('username')||'',department:'',usePurpose:''});useVisible.value=true}
const submitUse=async()=>{
  try {
    const r=await request.post('/consumable-use/apply',{consumableId:useTarget.value.consumableId,quantity:useForm.quantity,applicant:useForm.applicant,department:useForm.department,usePurpose:useForm.usePurpose})
    if(r.code===200){ElMessage.success('提交成功');useVisible.value=false;loadData()}else ElMessage.error(r.msg)
  } catch(e) { ElMessage.error('提交失败') }
}

const typeText = (t) => ({ 1: '入库', 2: '出库' }[t] || '其他')

const showRecords = async (row) => {
  recordTarget.value = row
  recordVisible.value = true
  recordLoading.value = true
  try {
    const r = await request.get('/consumable/records', { params: { consumableId: row.consumableId } })
    recordList.value = r.data || []
  } catch { recordList.value = [] }
  recordLoading.value = false
}

// 领用申请记录
const approveText = (s) => ({ 0: '待审批', 1: '已通过', 2: '已拒绝' }[s] || '待审批')
const approveType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')

const showUseRecords = async () => {
  useRecordVisible.value = true
  useRecordLoading.value = true
  try {
    const r = await request.get('/consumable-use/page', { params: { current: 1, size: 100 } })
    useRecordList.value = (r.data && r.data.records) || r.data || []
  } catch { useRecordList.value = [] }
  useRecordLoading.value = false
}

const exportConsumable = async () => {
  try {
    const r = await request.get('/consumable/list', { params: { size: 10000 } })
    if (r.code === 200) {
      const cols = [
        { label:'ID', key:'consumableId' }, { label:'名称', key:'consumableName' },
        { label:'分类', key:'category' }, { label:'单位', key:'unit' },
        { label:'库存', key:'currentStock' }, { label:'最低库存', key:'minStock' },
        { label:'单价', key:'price' }, { label:'供应商', key:'supplier' },
        { label:'备注', key:'remark' }, { label:'站点', key:'site' }
      ]
      exportCSV('耗材清单', cols, r.data || [])
      ElMessage.success('导出完成')
    }
  } catch (e) { ElMessage.error('导出失败') }
}

onMounted(()=>{loadData();loadLowStock()})
</script>

<style scoped>
</style>
