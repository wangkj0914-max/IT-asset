<template>
  <div class="notice-container">
    <el-card class="header-card">
      <el-row justify="space-between" align="middle">
        <span style="font-size:16px;font-weight:bold">公告信息</span>
        <el-button type="primary" @click="showAdd">发布公告</el-button>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-form inline style="margin-bottom:16px">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="搜索标题" clearable @keyup.enter="loadData" style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="noticeId" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="noticeType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === 2 ? 'warning' : 'info'" size="small">{{ row.noticeType === 2 ? '公告' : '通知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="发布人" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">查看</el-button>
            <el-button size="small" type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无公告" />
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end"
        @size-change="loadData" @current-change="loadData"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.noticeType">
            <el-radio :value="1">通知</el-radio>
            <el-radio :value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="公告详情" width="600px">
      <h2 style="text-align:center;margin-bottom:10px">{{ currentRow?.title }}</h2>
      <p style="text-align:center;color:#909399;font-size:13px;margin-bottom:20px">
        {{ currentRow?.createUserName }} · {{ currentRow?.createTime }}
        <el-tag :type="currentRow?.noticeType === 2 ? 'warning' : 'info'" size="small" style="margin-left:8px">{{ currentRow?.noticeType === 2 ? '公告' : '通知' }}</el-tag>
      </p>
      <div style="line-height:1.8;white-space:pre-wrap">{{ currentRow?.content }}</div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false), submitting = ref(false), tableData = ref([])
const pageNum = ref(1), pageSize = ref(10), total = ref(0), searchKeyword = ref('')
const dialogVisible = ref(false), detailVisible = ref(false), isEdit = ref(false)
const currentRow = ref(null), formRef = ref(null)

const form = reactive({ noticeId: null, title: '', content: '', noticeType: 1, status: 1 })
const rules = reactive({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/page', { params: { pageNum: pageNum.value, pageSize: pageSize.value, keyword: searchKeyword.value } })
    if (res.code === 200) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const resetForm = () => Object.assign(form, { noticeId: null, title: '', content: '', noticeType: 1, status: 1 })

const showAdd = () => { isEdit.value = false; resetForm(); formRef.value?.clearValidate(); dialogVisible.value = true }
const showEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { noticeId: row.noticeId, title: row.title, content: row.content, noticeType: row.noticeType, status: row.status })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}
const showDetail = (row) => { currentRow.value = row; detailVisible.value = true }

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const url = isEdit.value ? '/notice/update' : '/notice/save'
    const res = await request.post(url, form)
    if (res.code === 200) { ElMessage.success(isEdit.value ? '更新成功' : '发布成功'); dialogVisible.value = false; loadData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.msg || '操作失败') } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」？`, '确认', { type: 'warning' })
    const res = await request.post('/notice/delete', null, { params: { noticeId: row.noticeId } })
    if (res.code === 200) { ElMessage.success('已删除'); loadData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

onMounted(loadData)
</script>

<style scoped>
.notice-container { padding: 20px; }
.header-card { margin-bottom: 20px; }
</style>
