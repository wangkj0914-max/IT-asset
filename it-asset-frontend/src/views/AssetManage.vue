<template>
  <div class="page-container">
    <!-- 顶部标题栏 -->
    <div class="header-title">IT 固定资产管理</div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入资产名称或编号"
            clearable
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择资产分类"
            clearable
            style="width: 160px;"
          >
            <el-option
              v-for="cat in categoryList"
              :key="cat.categoryId"
              :label="cat.categoryName"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="searchForm.tagNo"
            placeholder="请输入资产标签号"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="状态" clearable style="width:140px">
            <el-option label="未领用" :value="0" />
            <el-option label="已领用" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="已报废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.department" placeholder="使用部门" clearable filterable style="width:160px">
            <el-option v-for="d in departmentList" :key="d.deptId" :label="d.deptName" :value="d.deptName" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.storageLocation" placeholder="存放地点" clearable filterable style="width:160px">
            <el-option v-for="s in storageLocationList" :key="s.locationId" :label="s.locationName" :value="s.locationName" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="searchForm.responsiblePerson" placeholder="责任人" clearable style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.statusLabelId" placeholder="状态标签" clearable style="width:130px">
            <el-option v-for="s in statusLabelList" :key="s.statusLabelId" :label="s.statusName" :value="s.statusLabelId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-section">
      <el-button type="primary" @click="showAddDialog" class="btn-add">
        <el-icon><Plus /></el-icon> 新增
      </el-button>
      <el-button type="warning" @click="handleRegenCode">
        <el-icon><RefreshRight /></el-icon> 重新制码
      </el-button>
      <el-button type="success" @click="exportLabels">
        <el-icon><Printer /></el-icon> 标签导出
      </el-button>
      <el-button type="info" @click="handleBatchImport">
        <el-icon><Upload /></el-icon> 批量导入
      </el-button>
      <el-button type="warning" @click="showBatchEditDialog" :disabled="selectedRows.length === 0">
        <el-icon><Edit /></el-icon> 批量修改 ({{ selectedRows.length }})
      </el-button>
    </div>

    <!-- 资产列表表格 -->
    <div class="table-section">
      <el-table
        ref="assetTableRef"
        :data="assetList"
        style="width: 100%;"
        v-loading="loading"
        element-loading-text="正在加载..."
        border
        stripe
        @selection-change="onSelectionChange"
        @sort-change="onSortChange"
      >
        <el-table-column type="selection" width="40" />
        <!-- 序号 -->
        <el-table-column type="index" label="序号" width="55" align="center" />

        <!-- 资产名称 -->
        <el-table-column prop="assetName" label="资产名称" min-width="120" show-overflow-tooltip sortable="custom" />

        <!-- 资产分类 -->
        <el-table-column label="资产分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small" v-if="getCategoryName(row.categoryId)">
              {{ getCategoryName(row.categoryId) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 资产编号 -->
        <el-table-column prop="assetCode" label="资产编号" width="130" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="asset-code">{{ row.assetCode || '-' }}</span>
          </template>
        </el-table-column>

        <!-- 资产图片 -->
        <el-table-column label="资产图片" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.assetImage"
              :src="row.assetImage"
              :preview-src-list="[row.assetImage]"
              style="width: 50px; height: 50px; border-radius: 4px;"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 资产品牌 -->
        <el-table-column prop="brand" label="资产品牌" width="120" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.brand || '-' }}
          </template>
        </el-table-column>

        <!-- 资产数量 -->
        <el-table-column prop="quantity" label="资产数量" width="80" align="center">
          <template #default="{ row }">
            {{ row.quantity || 1 }}
          </template>
        </el-table-column>

        <!-- 购置日期 -->
        <el-table-column label="购置日期" width="110" align="center">
          <template #default="{ row }">
            {{ row.purchaseDate || '-' }}
          </template>
        </el-table-column>

        <!-- 原始价值 -->
        <el-table-column label="原始价值" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.purchasePrice" class="price-text">
              ¥{{ Number(row.purchasePrice).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- P0: 当前价值 -->
        <el-table-column label="当前价值" width="110" align="right" sortable="custom" prop="currentValue">
          <template #default="{ row }">
            <span v-if="row.currentValue != null" :class="['price-text', { 'text-danger': row.currentValue <= 0 }]">
              ¥{{ Number(row.currentValue).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- P0: EOL日期 -->
        <el-table-column label="EOL日期" width="110" align="center" sortable="custom" prop="eolDate">
          <template #default="{ row }">
            <span v-if="row.eolDate" :style="{ color: isEolNear(row.eolDate) ? '#F56C6C' : '' }">
              {{ row.eolDate }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 保修到期 -->
        <el-table-column label="保修到期" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.warrantyExpireDate" :style="{ color: isExpireNear(row.warrantyExpireDate) ? '#F56C6C' : '' }">
              {{ row.warrantyExpireDate }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <!-- 下次维护 -->
        <el-table-column label="下次维护" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.nextMaintenanceDate" :style="{ color: isExpireNear(row.nextMaintenanceDate) ? '#E6A23C' : '' }">
              {{ row.nextMaintenanceDate }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 折旧方法 -->
        <el-table-column label="折旧方法" width="90" align="center">
          <template #default="{ row }">
            {{ getDepText(row.depreciationMethod) }}
          </template>
        </el-table-column>

        <!-- 使用部门 -->
        <el-table-column label="使用部门" width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.department || parseDepartment(row.remark) || '-' }}
          </template>
        </el-table-column>

        <!-- 责任人 -->
        <el-table-column prop="responsiblePerson" label="责任人" width="90" align="center">
          <template #default="{ row }">
            {{ row.responsiblePerson || row.userName || (row.userId ? getUserName(row.userId) : '-') }}
          </template>
        </el-table-column>

        <!-- 存放地点 -->
        <el-table-column prop="storageLocation" label="存放地点" width="100" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.storageLocation || '-' }}
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>

        <!-- P0: 状态标签 -->
        <el-table-column label="状态标签" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.statusLabelId" :type="getStatusLabelColor(row.statusLabelId)" size="small">
              {{ getStatusLabelName(row.statusLabelId) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 备注 -->
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ (row.remark || '').replace(/部门[:：][^,，\s]*[，,]?\s*/g, '').trim() || '-' }}
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="210" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="printSingleLabel(row)" style="color:#67C23A;border-color:#67C23A">标签</el-button>
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && assetList.length === 0" description="暂无资产数据" />

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑资产对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑资产' : '新增资产'"
      width="700px"
      @close="resetForm"
    >
      <el-form
        :model="assetForm"
        :rules="formRules"
        ref="assetFormRef"
        label-width="90px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="assetForm.assetName" placeholder="请输入资产名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产分类" prop="categoryId">
              <el-select v-model="assetForm.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option v-for="cat in categoryList" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产编号">
              <el-input v-model="assetForm.assetCode" placeholder="留空自动生成" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="assetForm.brand" placeholder="请输入品牌" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="assetForm.model" placeholder="请输入型号" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产模型">
              <el-select v-model="assetForm.modelId" placeholder="选择模型(自动继承折旧)" clearable filterable style="width:100%" @change="onModelChange">
                <el-option v-for="m in modelList" :key="m.modelId" :label="m.modelName" :value="m.modelId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态标签">
              <el-select v-model="assetForm.statusLabelId" placeholder="选择状态标签" clearable filterable style="width:100%">
                <el-option v-for="s in statusLabelList" :key="s.statusLabelId" :label="s.statusName" :value="s.statusLabelId">
                  <el-tag :type="s.color" size="small">{{ s.statusName }}</el-tag>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="序列号" prop="serialNumber">
              <el-input v-model="assetForm.serialNumber" placeholder="请输入序列号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购价格" prop="purchasePrice">
              <el-input v-model="assetForm.purchasePrice" placeholder="请输入采购价格" type="number" step="0.01" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存放位置" prop="storageLocation">
              <el-select v-model="assetForm.storageLocation" filterable placeholder="请选择存放位置" style="width:100%">
                <el-option v-for="s in storageLocationList" :key="s.locationId" :label="s.locationName" :value="s.locationName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产状态" prop="status">
              <el-select v-model="assetForm.status" placeholder="请选择状态" style="width: 100%;">
                <el-option label="未领用" :value="0" />
                <el-option label="已领用" :value="1" />
                <el-option label="维修中" :value="2" />
                <el-option label="已报废" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采购日期">
              <el-date-picker
                v-model="assetForm.purchaseDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商">
              <el-input v-model="assetForm.supplier" placeholder="请输入供应商" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="维保信息">
              <el-input v-model="assetForm.warrantyInfo" placeholder="如: 3年质保, 到期2027-06" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-select v-model="assetForm.department" placeholder="请选择部门" style="width: 100%;" clearable filterable>
                <el-option
                  v-for="dept in departmentList"
                  :key="dept.deptId"
                  :label="dept.deptName"
                  :value="dept.deptName"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 维护信息 -->
        <el-divider content-position="left">维护信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="保修到期日">
              <el-date-picker v-model="assetForm.warrantyExpireDate" type="date" placeholder="保修到期"
                value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="维护周期(天)">
              <el-input-number v-model="assetForm.maintenanceCycleDays" :min="1" :max="3650" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="下次维护日期">
              <el-date-picker v-model="assetForm.nextMaintenanceDate" type="date" placeholder="自动推算或手动设"
                value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- P0: 财务与折旧 -->
        <el-divider content-position="left">财务与折旧</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="采购成本">
              <el-input v-model="assetForm.purchaseCost" placeholder="采购成本" type="number" step="0.01" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折旧年限">
              <el-input-number v-model="assetForm.depreciationYears" :min="1" :max="20" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折旧方法">
              <el-select v-model="assetForm.depreciationMethod" style="width:100%">
                <el-option label="直线折旧" value="straight_line" />
                <el-option label="余额递减" value="declining_balance" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="EOL日期">
              <el-date-picker
                v-model="assetForm.eolDate"
                type="date"
                placeholder="EOL日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="当前价值">
              <span v-if="assetForm.currentValue != null" class="price-text">
                ¥{{ Number(assetForm.currentValue).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}
              </span>
              <span v-else style="color:#999">保存后自动计算</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折旧率">
              <span v-if="assetForm.depreciationRate != null">{{ Number(assetForm.depreciationRate).toFixed(2) }}% / 年</span>
              <span v-else style="color:#999">保存后自动计算</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="使用人">
              <el-select v-model="assetForm.userId" placeholder="请选择使用人" style="width: 100%;" clearable filterable>
                <el-option
                  v-for="user in userList"
                  :key="user.userId"
                  :label="user.realName || user.username"
                  :value="user.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="assetForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 自定义字段 -->
        <template v-if="customFieldDefs.length > 0">
          <el-divider content-position="left">自定义字段</el-divider>
          <el-row :gutter="20">
            <el-col :span="12" v-for="def in customFieldDefs" :key="def.fieldId">
              <el-form-item :label="def.fieldName" :required="def.isRequired === 1">
                <!-- 文本 -->
                <el-input v-if="def.fieldType === 'text'" v-model="customFieldValues[def.fieldId]"
                  :placeholder="'请输入' + def.fieldName" clearable />
                <!-- 数字 -->
                <el-input-number v-if="def.fieldType === 'number'" v-model="customFieldValues[def.fieldId]"
                  :precision="2" style="width:100%" />
                <!-- 日期 -->
                <el-date-picker v-if="def.fieldType === 'date'" v-model="customFieldValues[def.fieldId]"
                  type="date" value-format="YYYY-MM-DD" style="width:100%" />
                <!-- 下拉选择 -->
                <el-select v-if="def.fieldType === 'select'" v-model="customFieldValues[def.fieldId]"
                  :placeholder="'请选择' + def.fieldName" clearable style="width:100%">
                  <el-option v-for="opt in parseOptions(def.fieldOptions)" :key="opt" :label="opt" :value="opt" />
                </el-select>
                <!-- 多行文本 -->
                <el-input v-if="def.fieldType === 'textarea'" v-model="customFieldValues[def.fieldId]"
                  type="textarea" :rows="2" :placeholder="'请输入' + def.fieldName" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 领用申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请领用" width="500px" @close="resetApplyForm">
      <div class="apply-asset-info" v-if="applyAsset">
        <div class="apply-asset-title">领用资产：{{ applyAsset.assetName }}</div>
        <div class="apply-asset-detail">
          <span>编号：{{ applyAsset.assetCode || '-' }}</span>
          <span>型号：{{ applyAsset.model || '-' }}</span>
          <span>位置：{{ applyAsset.storageLocation || '-' }}</span>
        </div>
      </div>
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="90px">
        <el-form-item label="领用部门" prop="department">
          <el-select v-model="applyForm.department" placeholder="请选择部门" style="width: 100%;" filterable>
            <el-option
              v-for="dept in departmentList"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="applyForm.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="applyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 领用记录对话框 -->
    <el-dialog v-model="useRecordDialogVisible" title="领用记录" width="750px">
      <div class="use-record-header" v-if="useRecordAsset">
        <strong>{{ useRecordAsset.assetName }}</strong>
        <span class="use-record-code">（{{ useRecordAsset.assetCode || '-' }}）</span>
        <el-button type="primary" size="small" style="float: right;" @click="goToAssetUse" v-if="useRecordAsset.status === 0">
          前往申请领用
        </el-button>
      </div>
      <el-table :data="useRecordList" v-loading="useRecordLoading" border stripe style="width: 100%;" max-height="400">
        <el-table-column label="类型" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.useType === 1 ? '' : 'warning'" size="small">{{ getUseTypeText(row.useType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="80" align="center">
          <template #default="{ row }">{{ row.contactPerson || '-' }}</template>
        </el-table-column>
        <el-table-column label="部门" width="100" align="center" prop="department" />
        <el-table-column label="联系电话" width="120" align="center" prop="contactPhone" />
        <el-table-column label="领用时间" width="110" align="center">
          <template #default="{ row }">{{ row.useDate ? formatDateTime(row.useDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="归还时间" width="110" align="center">
          <template #default="{ row }">{{ row.returnDate ? formatDateTime(row.returnDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="审批状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getApproveStatusClass(row.approveStatus)]">
              {{ getApproveStatusText(row.approveStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="90" align="center">
          <template #default="{ row }">{{ row.approveUser || '-' }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 批量修改对话框 -->
    <el-dialog v-model="batchEditVisible" title="批量修改资产" width="500px">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        已选择 <b>{{ selectedRows.length }}</b> 条资产，以下字段将覆盖到所选资产。
      </el-alert>
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="所属部门">
          <el-select v-model="batchForm.department" clearable filterable placeholder="留空表示不修改" style="width:100%">
            <el-option v-for="d in departmentList" :key="d.deptId" :label="d.deptName" :value="d.deptName" />
          </el-select>
        </el-form-item>
        <el-form-item label="存放位置">
          <el-select v-model="batchForm.storageLocation" clearable filterable placeholder="留空表示不修改" style="width:100%">
            <el-option v-for="s in storageLocationList" :key="s.locationId" :label="s.locationName" :value="s.locationName" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用人">
          <el-input v-model="batchForm.userName" placeholder="留空表示不修改" clearable />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="batchForm.responsiblePerson" placeholder="留空表示不修改" clearable />
        </el-form-item>
        <el-form-item label="资产分类">
          <el-select v-model="batchForm.categoryId" clearable placeholder="留空表示不修改" style="width:100%">
            <el-option v-for="c in categoryList" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="batchForm.status" clearable placeholder="留空表示不修改" style="width:100%">
            <el-option label="未领用" :value="0" />
            <el-option label="已领用" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchEditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchEdit" :loading="batchSubmitting">确定修改</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入资产" width="780px" @close="resetImport">
      <div class="import-steps">
        <!-- 步骤一：下载模板 -->
        <div class="import-step">
          <div class="step-header"><span class="step-num">1</span> 下载导入模板</div>
          <div class="step-body">
            <el-button type="primary" plain size="small" @click="downloadTemplate">
              <el-icon><Download /></el-icon> 下载Excel模板
            </el-button>
            <span class="step-tip">模板包含必填字段说明，请严格按照模板格式填写</span>
          </div>
        </div>

        <!-- 步骤二：上传文件 -->
        <div class="import-step">
          <div class="step-header"><span class="step-num">2</span> 上传文件（支持 .xlsx / .xls / .csv）</div>
          <div class="step-body">
            <input
              ref="fileInputRef"
              type="file"
              accept=".xlsx,.xls,.csv"
              style="display:none"
              @change="handleFileChange"
            />
            <el-button type="success" plain size="small" @click="() => fileInputRef.click()">
              <el-icon><Upload /></el-icon> 选择文件
            </el-button>
            <span class="step-tip" v-if="importFileName">已选：{{ importFileName }}</span>
            <span class="step-tip" v-else>未选择文件</span>
          </div>
        </div>

        <!-- 步骤三：预览数据 -->
        <div class="import-step" v-if="importPreviewData.length > 0">
          <div class="step-header">
            <span class="step-num">3</span> 数据预览
            <span style="margin-left:10px;font-size:13px;color:#666;">共 {{ importPreviewData.length }} 条，前 5 条预览</span>
          </div>
          <div class="step-body">
            <el-table :data="importPreviewData.slice(0, 5)" border size="small" style="width:100%;">
              <el-table-column prop="assetName" label="资产名称" width="120" show-overflow-tooltip />
              <el-table-column prop="categoryName" label="资产分类" width="90" />
              <el-table-column prop="brand" label="品牌" width="90" />
              <el-table-column prop="model" label="型号" width="100" show-overflow-tooltip />
              <el-table-column prop="quantity" label="数量" width="60" />
              <el-table-column prop="purchasePrice" label="原始价值" width="90" />
              <el-table-column prop="purchaseDate" label="购置日期" width="100" />
              <el-table-column label="折旧方法" width="80">
                <template #default="{ row }">{{ getDepText(row.depreciationMethod) }}</template>
              </el-table-column>
              <el-table-column prop="department" label="使用部门" width="90" show-overflow-tooltip />
              <el-table-column prop="responsiblePerson" label="责任人" width="80" />
              <el-table-column prop="storageLocation" label="存放地点" width="90" show-overflow-tooltip />
              <el-table-column prop="remark" label="备注" min-width="80" show-overflow-tooltip />
            </el-table>
            <!-- 错误提示 -->
            <div v-if="importErrors.length > 0" class="import-errors">
              <div class="error-title">⚠️ 以下行存在问题（将跳过）：</div>
              <div v-for="err in importErrors" :key="err.row" class="error-item">第 {{ err.row }} 行：{{ err.msg }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="importPreviewData.length === 0"
          :loading="importLoading"
          @click="submitImport"
        >
          确认导入（{{ importPreviewData.length }} 条）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, RefreshRight, Printer, Upload, Edit } from '@element-plus/icons-vue'
import request from '@/utils/request'
import QRCode from 'qrcode'

const router = useRouter()
const route = useRoute()

// 表单引用
const assetFormRef = ref(null)

// 加载状态
const loading = ref(false)
const sortColumn = ref('')
const sortOrder = ref('')

const onSortChange = ({ prop, order }) => {
  sortColumn.value = prop || ''
  sortOrder.value = order ? (order === 'ascending' ? 'asc' : 'desc') : ''
  pagination.current = 1
  getAssetList()
}

// 资产列表数据
const assetList = ref([])

// 分类列表
const categoryList = ref([])

// 资产模型列表 (P0)
const modelList = ref([])

// 状态标签列表 (P0)
const statusLabelList = ref([])

// 用户列表（用于映射使用人）
const userList = ref([])
const userMap = ref({})

// 部门列表
const departmentList = ref([])

// 存放位置列表
const storageLocationList = ref([])

// 存放位置列表
const locationList = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',      // 资产名称或编号
  categoryId: null, // 资产分类
  tagNo: '',        // 资产标签号
  status: null,     // 状态
  department: '',   // 使用部门
  storageLocation: '', // 存放地点
  responsiblePerson: '', // 责任人
  statusLabelId: null // P0: 状态标签
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const isEditMode = ref(false)

// 资产表单数据
const assetForm = reactive({
  assetId: null,
  assetName: '',
  categoryId: null,
  brand: '',
  model: '',
  modelId: null,
  serialNumber: '',
  purchasePrice: '',
  purchaseCost: '',
  purchaseDate: '',
  supplier: '',
  storageLocation: '',
  status: 0,
  statusLabelId: null,
  userId: null,
  department: '',
  warrantyInfo: '',
  depreciationMethod: 'straight_line',
  depreciationYears: null,
  depreciationRate: null,
  eolDate: '',
  currentValue: null,
  warrantyExpireDate: '',
  maintenanceCycleDays: null,
  nextMaintenanceDate: '',
  remark: ''
})

// 领用申请相关
const applyDialogVisible = ref(false)
const applyAsset = ref(null)

// 自定义字段
const customFieldDefs = ref([])
const customFieldValues = reactive({})

const parseOptions = (options) => {
  if (!options) return []
  try { return JSON.parse(options) } catch { return [] }
}

const loadCustomFieldDefs = async () => {
  try {
    const r = await request.get('/custom-field/def-list', { params: { targetEntity: 'asset' } })
    if (r.code === 200) customFieldDefs.value = r.data || []
  } catch { customFieldDefs.value = [] }
}

const loadCustomFieldValues = async (assetId) => {
  // 重置
  Object.keys(customFieldValues).forEach(k => delete customFieldValues[k])
  if (!assetId) return
  try {
    const r = await request.get('/custom-field/values', { params: { entityType: 'asset', entityId: assetId } })
    if (r.code === 200) {
      const vals = r.data || []
      vals.forEach(v => {
        let val = v.fieldValue
        if (val) {
          // 找到对应定义，判断类型
          const def = customFieldDefs.value.find(d => d.fieldId === v.fieldId)
          if (def && def.fieldType === 'number') val = Number(val)
        }
        customFieldValues[v.fieldId] = val
      })
    }
  } catch { /* ignore */ }
}

const saveCustomFieldValues = async (assetId) => {
  if (!assetId) return
  const data = []
  for (const def of customFieldDefs.value) {
    const v = customFieldValues[def.fieldId]
    if (v !== undefined && v !== null && v !== '') {
      data.push({ entityType: 'asset', entityId: assetId, fieldId: def.fieldId, fieldValue: String(v) })
    }
  }
  if (data.length > 0) {
    await request.post('/custom-field/save-values', data)
  }
}
const applyFormRef = ref(null)
const applyForm = reactive({
  assetId: null,
  department: '',
  contactPerson: '',
  contactPhone: '',
  remark: ''
})
const applyRules = reactive({
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
})

// 领用记录相关
const useRecordDialogVisible = ref(false)
const useRecordAsset = ref(null)
const useRecordList = ref([])
const useRecordLoading = ref(false)

// 表单校验规则
const formRules = reactive({
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
  storageLocation: [{ required: true, message: '请输入存放位置', trigger: 'blur' }],
  status: [{ required: true, message: '请选择资产状态', trigger: 'change' }]
})

// 页面加载时查询数据
onMounted(() => {
  getCategoryList()
  loadModelList()
  loadStatusLabelList()
  loadUserList()
  loadDepartmentList()
  loadLocationList()

  // 检测URL参数，支持从领用页面跳转过来定位资产
  if (route.query.code) {
    searchForm.keyword = route.query.code
  }

  getAssetList()
})

// 加载用户列表，建立 userId → realName 映射
const loadUserList = async () => {
  try {
    const res = await request.get('/user/list', { params: { size: 999 } })
    const records = res.data.records || []
    userList.value = records
    const map = {}
    records.forEach(u => {
      map[u.userId] = u.realName || u.username
    })
    userMap.value = map
  } catch (error) {
    // Silently handle error
  }
}

// 根据userId获取用户名
const getUserName = (userId) => {
  return userMap.value[userId] || '-'
}

// 从remark中解析部门信息
const parseDepartment = (remark) => {
  if (!remark) return '-'
  const match = remark.match(/部门[:：]([^,，\s]+)/)
  return match ? match[1] : '-'
}

// 加载部门列表（返回 {deptId, deptName} 对象数组，与下拉模板兼容）
const loadDepartmentList = async (assetDept) => {
  try {
    const res = await request.get('/department/list')
    const serverList = res.data || []
    const merged = []
    serverList.forEach(item => {
      const name = typeof item === 'string' ? item : (item.deptName || item.departmentName || '')
      const id = typeof item === 'string' ? name : (item.deptId || item.departmentId || name)
      if (name) merged.push({ deptId: id, deptName: name })
    })
    // 确保资产原部门在下拉中存在
    if (assetDept && !merged.some(d => d.deptName === assetDept)) {
      merged.unshift({ deptId: assetDept, deptName: assetDept })
    }
    departmentList.value = merged
  } catch (error) {
    departmentList.value = []
  }
}

// 加载存放位置列表
const loadLocationList = async () => {
  try {
    const res = await request.get('/storage-location/list')
    storageLocationList.value = (res.data || []).map(s => ({ locationId: s.locationId, locationName: s.locationName }))
  } catch (error) {
    storageLocationList.value = []
  }
}

// 查询分类列表
const getCategoryList = async () => {
  try {
    const res = await request.get('/category/list')
    categoryList.value = res.data || []
  } catch (error) {
    // Silently handle error
  }
}

// P0: 加载资产模型列表
const loadModelList = async () => {
  try {
    const res = await request.get('/assetModel/list')
    modelList.value = res.data || []
  } catch (error) {
    modelList.value = []
  }
}

// P0: 加载状态标签列表
const loadStatusLabelList = async () => {
  try {
    const res = await request.get('/statusLabel/list')
    statusLabelList.value = res.data || []
  } catch (error) {
    statusLabelList.value = []
  }
}

// P0: 选择模型时自动填充折旧参数
const onModelChange = (modelId) => {
  if (!modelId) return
  const m = modelList.value.find(m => m.modelId === modelId)
  if (!m) return
  if (m.depreciationYears) assetForm.depreciationYears = m.depreciationYears
  if (m.depreciationMethod) assetForm.depreciationMethod = m.depreciationMethod
  if (!assetForm.model) assetForm.model = m.modelName
}

// P0: 获取状态标签名
const getStatusLabelName = (id) => {
  const s = statusLabelList.value.find(s => s.statusLabelId === id)
  return s ? s.statusName : ''
}

// P0: 获取状态标签颜色
const getStatusLabelColor = (id) => {
  const s = statusLabelList.value.find(s => s.statusLabelId === id)
  return s ? s.color : 'info'
}

// P0: EOL临近判断（3个月内）
const isEolNear = (eolDate) => {
  if (!eolDate) return false
  const d = new Date(eolDate)
  const now = new Date()
  const diff = (d - now) / (1000 * 60 * 60 * 24)
  return diff >= 0 && diff <= 90
}

// 维护日期临近判断（30天内）
const isExpireNear = (date) => {
  if (!date) return false
  const d = new Date(date)
  const now = new Date()
  const diff = (d - now) / (1000 * 60 * 60 * 24)
  return diff >= 0 && diff <= 30
}

// 查询资产列表（分页）
const getAssetList = async () => {
  try {
    loading.value = true
    const res = await request.get('/assetInfo/page', {
      params: {
        current: pagination.current,
        size: pagination.size,
        keyword: searchForm.keyword || undefined,
        categoryId: searchForm.categoryId || undefined,
        tagNo: searchForm.tagNo || undefined,
        status: searchForm.status === '' || searchForm.status === null ? undefined : searchForm.status,
        department: searchForm.department || undefined,
        storageLocation: searchForm.storageLocation || undefined,
        responsiblePerson: searchForm.responsiblePerson || undefined,
        statusLabelId: searchForm.statusLabelId || undefined,
        sortColumn: sortColumn.value || undefined,
        sortOrder: sortOrder.value || undefined
      }
    })

    assetList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('查询资产列表失败：' + (error.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getAssetList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.department = ''
  searchForm.tagNo = ''
  searchForm.storageLocation = ''
  searchForm.responsiblePerson = ''
  searchForm.statusLabelId = null
  pagination.current = 1
  getAssetList()
}

// 显示新增对话框
const showAddDialog = async () => {
  await loadDepartmentList()
  await loadLocationList()
  await loadUserList()
  await loadModelList()
  await loadStatusLabelList()
  await loadCustomFieldDefs()
  isEditMode.value = false
  // 重置自定义字段值
  Object.keys(customFieldValues).forEach(k => delete customFieldValues[k])
  Object.assign(assetForm, {
    assetId: null,
    assetName: '',
    categoryId: null,
    brand: '',
    model: '',
    modelId: null,
    serialNumber: '',
    purchasePrice: '',
    purchaseCost: '',
    purchaseDate: '',
    supplier: '',
    storageLocation: '',
    status: 0,
    statusLabelId: null,
    userId: null,
    department: '',
    warrantyInfo: '',
    depreciationMethod: 'straight_line',
    depreciationYears: null,
    depreciationRate: null,
    eolDate: '',
    currentValue: null,
    warrantyExpireDate: '',
    maintenanceCycleDays: null,
    nextMaintenanceDate: '',
    remark: ''
  })
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = async (row) => {
  isEditMode.value = true
  await loadDepartmentList(row.department)
  await loadLocationList()
  await loadUserList()
  await loadModelList()
  await loadStatusLabelList()
  await loadCustomFieldDefs()
  await loadCustomFieldValues(row.assetId)
  Object.assign(assetForm, {
    assetId: row.assetId,
    assetName: row.assetName,
    categoryId: row.categoryId,
    brand: row.brand || '',
    model: row.model || '',
    modelId: row.modelId || null,
    serialNumber: row.serialNumber || '',
    purchasePrice: row.purchasePrice || '',
    purchaseCost: row.purchaseCost || '',
    purchaseDate: row.purchaseDate || '',
    supplier: row.supplier || '',
    storageLocation: row.storageLocation || '',
    status: row.status,
    statusLabelId: row.statusLabelId || null,
    userId: row.userId || null,
    department: parseDepartment(row.remark) !== '-' ? parseDepartment(row.remark) : '',
    warrantyInfo: row.warrantyInfo || '',
    depreciationMethod: row.depreciationMethod || 'straight_line',
    depreciationYears: row.depreciationYears || null,
    depreciationRate: row.depreciationRate || null,
    eolDate: row.eolDate || '',
    currentValue: row.currentValue || null,
    warrantyExpireDate: row.warrantyExpireDate || '',
    maintenanceCycleDays: row.maintenanceCycleDays || null,
    nextMaintenanceDate: row.nextMaintenanceDate || '',
    remark: (() => {
      let r = row.remark || ''
      return r.replace(/部门[:：][^,，\s]*[，,]?\s*/g, '').trim()
    })() || ''
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!assetFormRef.value) return
  try {
    await assetFormRef.value.validate()

    const submitData = {
      ...assetForm,
      purchasePrice: assetForm.purchasePrice ? Number(assetForm.purchasePrice) : null,
      purchaseCost: assetForm.purchaseCost ? Number(assetForm.purchaseCost) : null
    }

    // 将部门信息写入remark
    let remark = assetForm.remark || ''
    if (assetForm.department) {
      // 移除旧部门信息
      remark = remark.replace(/部门[:：][^,，\s]+/g, '').trim()
      // 插入新部门信息
      remark = remark ? `部门:${assetForm.department}，${remark}` : `部门:${assetForm.department}`
    }
    submitData.remark = remark

    if (isEditMode.value) {
      await request.post('/assetInfo/update', submitData)
      ElMessage.success('更新成功')
      // 保存自定义字段值（编辑时assetId已知）
      await saveCustomFieldValues(assetForm.assetId)
    } else {
      const r = await request.post('/assetInfo/save', submitData)
      ElMessage.success('资产入库成功')
      // 保存自定义字段值（新增时从返回值获取assetId）
      const assetId = r.data?.assetId
      if (assetId) {
        await saveCustomFieldValues(assetId)
      }
    }

    dialogVisible.value = false
    getAssetList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEditMode.value ? '更新失败' : '入库失败')
    }
  }
}

// 删除资产
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认要删除资产"${row.assetName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/assetInfo/delete', null, {
        params: { assetId: row.assetId }
      })
      ElMessage.success('删除成功')
      getAssetList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 重置表单
const resetForm = () => {
  if (assetFormRef.value) {
    assetFormRef.value.resetFields()
  }
}

// 分页事件
const handleSizeChange = () => getAssetList()
const handleCurrentChange = () => getAssetList()

// 导出数据
const exportData = () => {
  if (assetList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  const headers = ['资产编号', '名称', '型号', '类别', '使用人', '部门', '存放位置', '采购日期', '原值', '资产状态', '维保信息', '备注']

  const rows = assetList.value.map(row => [
    row.assetCode || '',
    row.assetName,
    row.model || '',
    getCategoryName(row.categoryId),
    row.userId ? getUserName(row.userId) : '',
    parseDepartment(row.remark),
    row.storageLocation || '',
    row.purchaseDate || '',
    row.purchasePrice ? Number(row.purchasePrice).toFixed(2) : '',
    getStatusText(row.status),
    row.warrantyInfo || '',
    row.remark || ''
  ])

  const csvContent = [
    '\uFEFF',
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `资产列表_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()

  ElMessage.success('导出成功')
}

// 重新制码（重新生成资产编码）
const handleRegenCode = () => {
  ElMessage.info('重新制码功能开发中')
}

// 标签打印（NAI 苏州 37.8mm × 29.6mm 标签纸）
const exportLabels = async () => {
  if (importFileName.value) return
  const list = selectedRows.value.length > 0 ? selectedRows.value : assetList.value
  if (list.length === 0) { ElMessage.warning('没有可打印的资产'); return }
  if (list.length > 100 && selectedRows.value.length === 0) { ElMessage.warning('请先勾选要打印的资产（最多100条）'); return }

  // QR 码本地生成（离线可用）
  let qrCache = {}
  const buildQr = async (text) => {
    if (qrCache[text]) return qrCache[text]
    try {
      qrCache[text] = await QRCode.toDataURL(text, { width: 80, margin: 0, scale: 4 })
      return qrCache[text]
    } catch(e) {
      return ''
    }
  }

  // 格式化日期 → "2024年05月" 形式
  const fmtDate = (d) => {
    if (!d) return '2024年12月'
    const m = String(d).match(/(\d{4})[-/](\d{1,2})/)
    return m ? `${m[1]}年${m[2].padStart(2,'0')}月` : '2024年12月'
  }

  // 批量标签打印
  const labels = await Promise.all(list.map(async a => {
    const code = a.assetCode || 'N/A'
    const meCode = a.model || a.serialNumber || 'N/A'
    const desc = a.assetName || 'N/A'
    const date = fmtDate(a.purchaseDate)
    const qr = await buildQr(`${code}|${a.assetName}`)
    return `
      <div class="label">
        <div class="qr"><img src="${qr}" alt="QR" /></div>
        <div class="row"><span class="lbl">FA Code:</span><span class="val">${code}</span></div>
        <div class="row"><span class="lbl">ME Code:</span><span class="val">${meCode}</span></div>
        <div class="row"><span class="lbl">Description:</span><span class="val">${desc}</span></div>
        <div class="row"><span class="lbl">Date:</span><span class="val">${date}</span></div>
      </div>`
  }));

  const w = window.open('', '_blank', 'width=900,height=600')
  if (!w) return
  w.document.write(`<!DOCTYPE html><html><head><meta charset="utf-8"><title>资产标签 - NAI Suzhou</title>
<style>
  @page { size: A4; margin: 5mm; }
  * { box-sizing: border-box; }
  body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 0; padding: 0; }
  .toolbar { padding: 10px; text-align: center; background: #f5f5f5; border-bottom: 1px solid #ddd; }
  .toolbar button { padding: 8px 24px; font-size: 14px; cursor: pointer; background: #409EFF; color: white; border: 0; border-radius: 4px; }
  .toolbar span { margin-left: 12px; color: #666; font-size: 13px; }
  .page { width: 200mm; min-height: 290mm; padding: 2mm; margin: 10px auto; background: white; }
  .grid { display: grid; grid-template-columns: repeat(5, 37.8mm); gap: 0; justify-content: start; }
  .label {
    width: 37.8mm; height: 29.6mm;
    border: 1px dashed #aaa;
    padding: 1.2mm 1.8mm;
    position: relative;
    overflow: hidden;
    background: #fff;
  }
  .label .qr {
    position: absolute; top: 1.2mm; right: 1.2mm;
    width: 9mm; height: 9mm;
  }
  .label .qr img { width: 100%; height: 100%; display: block; }
  .label .row {
    font-size: 6.5pt; line-height: 1.35;
    display: flex; gap: 1mm; margin-bottom: 0.4mm;
    align-items: baseline;
  }
  .label .lbl { color: #1a8a3a; font-weight: bold; flex-shrink: 0; }
  .label .val { color: #000; word-break: break-all; flex: 1; font-size: 6.5pt; }
  @media print {
    .toolbar { display: none; }
    .page { margin: 0; padding: 0; box-shadow: none; }
    .label { border: 1px solid #000; }
  }
</style></head><body>
  <div class="toolbar">
    <button onclick="window.print()">🖨️ 打印全部标签</button>
    <span>共 ${list.length} 条 · 标签尺寸 37.8mm × 29.6mm · 5 列布局</span>
  </div>
  <div class="page"><div class="grid">${labels}</div></div>
</body></html>`)
  w.document.close()
}

// 单标签打印（浏览器直接打印 + 真实二维码）
const printSingleLabel = async (row) => {
  const code = row.assetCode || 'N/A'
  const meCode = row.model || row.serialNumber || 'N/A'
  const desc = row.assetName || 'N/A'
  const date = (() => { const m = String(row.purchaseDate||'').match(/(\d{4})[-/](\d{1,2})/); return m ? `${m[1]}年${m[2].padStart(2,'0')}月` : '2024年12月' })()

  // 本地生成二维码（无需外网）
  let qrDataUrl = ''
  try { qrDataUrl = await QRCode.toDataURL(code, { width: 200, margin: 0, errorCorrectionLevel: 'H' }) }
  catch (e) { qrDataUrl = '' }

  const w = window.open('', '_blank', 'width=500,height=420,left=200,top=100,resizable=yes')
  if (!w) return
  w.document.write(`<!DOCTYPE html><html><head><meta charset="utf-8"><title>标签预览 - ${code}</title>
<style>
  body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
  .container { max-width: 440px; margin: 0 auto; }
  .preview { background: #fff; width: 170px; height: 135px; border: 2px solid #000; padding: 8px 10px; position: relative; margin: 0 auto 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
  .preview .row { font-size: 8pt; line-height: 1.4; }
  .preview .qr { position: absolute; top: 6px; right: 6px; width: 50px; height: 50px; }
  .preview .qr img { width: 100%; height: 100%; display: block; }
  .footer { text-align: center; }
  .footer button { padding: 8px 24px; border: 0; border-radius: 4px; cursor: pointer; font-size: 14px; color: #fff; background: #67C23A; }
  @media print {
    body { background: #fff; margin: 0; }
    .footer { display: none; }
    .preview { border: none; box-shadow: none; }
  }
</style></head><body>
<div class="container">
  <div class="preview">
    <div class="qr"><img src="${qrDataUrl}" alt="QR" /></div>
    <div class="row"><b>FA:</b> ${code}</div>
    <div class="row"><b>ME:</b> ${meCode}</div>
    <div class="row"><b>Desc:</b> ${desc}</div>
    <div class="row"><b>Date:</b> ${date}</div>
  </div>
  <div class="footer"><button onclick="window.print()">🖨️ 打印标签</button></div>
</div>
<` + `/body></html>`)
  w.document.close()
}

// ========== 批量修改相关 ==========
const assetTableRef = ref(null)
const selectedRows = ref([])
const batchEditVisible = ref(false)
const batchSubmitting = ref(false)
const batchForm = reactive({
  department: '', storageLocation: '', userName: '',
  responsiblePerson: '', categoryId: null, status: null
})

const onSelectionChange = (rows) => { selectedRows.value = rows }

const showBatchEditDialog = () => {
  if (selectedRows.value.length === 0) { ElMessage.warning('请先勾选资产'); return }
  Object.assign(batchForm, { department: '', storageLocation: '', userName: '', responsiblePerson: '', categoryId: null, status: null })
  batchEditVisible.value = true
}

const submitBatchEdit = async () => {
  const fields = {}
  if (batchForm.department) fields.department = batchForm.department
  if (batchForm.storageLocation) fields.storageLocation = batchForm.storageLocation
  if (batchForm.userName) fields.userName = batchForm.userName
  if (batchForm.responsiblePerson) fields.responsiblePerson = batchForm.responsiblePerson
  if (batchForm.categoryId !== null && batchForm.categoryId !== '') fields.categoryId = batchForm.categoryId
  if (batchForm.status !== null && batchForm.status !== '') fields.status = batchForm.status

  if (Object.keys(fields).length === 0) { ElMessage.warning('请至少设置一个字段'); return }

  batchSubmitting.value = true
  try {
    const res = await request.post('/assetInfo/batchUpdate', {
      assetIds: selectedRows.value.map(r => r.assetId),
      fields
    })
    if (res.code === 200) { ElMessage.success(res.msg); batchEditVisible.value = false; assetTableRef.value.clearSelection(); handleSearch() }
    else ElMessage.error(res.msg)
  } catch { ElMessage.error('批量修改失败') } finally { batchSubmitting.value = false }
}

// ========== 批量导入相关 ==========
const importDialogVisible = ref(false)
const fileInputRef = ref(null)
const importFileName = ref('')
const importPreviewData = ref([])
const importErrors = ref([])
const importLoading = ref(false)

// 打开批量导入弹窗
const handleBatchImport = () => {
  importDialogVisible.value = true
}

// 重置导入状态
const resetImport = () => {
  importFileName.value = ''
  importPreviewData.value = []
  importErrors.value = []
  importLoading.value = false
  if (fileInputRef.value) fileInputRef.value.value = ''
}

// 下载模板（生成CSV）
const downloadTemplate = () => {
  const headers = [
    '资产名称*', '资产分类名称*', '品牌', '型号', '序列号',
    '数量', '原始价值', '购置日期(YYYY-MM-DD)', '折旧方法',
    '使用部门', '责任人', '存放地点', '供应商', '维保信息', '备注'
  ]
  const example = [
    '笔记本电脑', '计算机', 'Dell', 'Latitude 5520', 'SN12345678',
    '1', '8500.00', '2024-01-10', '直线法',
    '技术部', '张三', '办公区域制冷', '戴尔中国', '2026-01-10到期', ''
  ]
  const csvContent = '\uFEFF' + headers.join(',') + '\n' + example.map(v => `"${v}"`).join(',')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '资产批量导入模板.csv'
  link.click()
  ElMessage.success('模板下载成功')
}

// 解析CSV文本
const parseCSV = (text) => {
  const lines = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n').filter(l => l.trim())
  if (lines.length < 2) return { data: [], errors: [{ row: 0, msg: '文件内容为空或只有标题行' }] }
  
  const parseLine = (line) => {
    const result = []
    let inQuote = false
    let cell = ''
    for (let i = 0; i < line.length; i++) {
      const ch = line[i]
      if (ch === '"') {
        if (inQuote && line[i + 1] === '"') { cell += '"'; i++ }
        else inQuote = !inQuote
      } else if (ch === ',' && !inQuote) {
        result.push(cell); cell = ''
      } else {
        cell += ch
      }
    }
    result.push(cell)
    return result
  }

  const headers = parseLine(lines[0])
  const data = []
  const errors = []

  for (let i = 1; i < lines.length; i++) {
    const cols = parseLine(lines[i])
    const row = {}
    headers.forEach((h, idx) => { row[h.replace('*', '').trim()] = (cols[idx] || '').trim() })
    
    if (!row['资产名称']) {
      errors.push({ row: i + 1, msg: '资产名称不能为空' })
      continue
    }
    if (!row['资产分类名称']) {
      errors.push({ row: i + 1, msg: '资产分类名称不能为空' })
      continue
    }
    data.push({
      assetName: row['资产名称'],
      categoryName: row['资产分类名称'],
      brand: row['品牌'] || '',
      model: row['型号'] || '',
      serialNumber: row['序列号'] || '',
      quantity: parseInt(row['数量']) || 1,
      purchasePrice: row['原始价值'] || '',
      purchaseDate: row['购置日期(YYYY-MM-DD)'] || '',
      depreciationMethod: row['折旧方法'] || '',
      department: row['使用部门'] || '',
      responsiblePerson: row['责任人'] || '',
      storageLocation: row['存放地点'] || '',
      supplier: row['供应商'] || '',
      warrantyInfo: row['维保信息'] || '',
      remark: row['备注'] || ''
    })
  }
  return { data, errors }
}

// 读取并解析文件
const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  importFileName.value = file.name
  importPreviewData.value = []
  importErrors.value = []

  const ext = file.name.split('.').pop().toLowerCase()

  if (ext === 'csv') {
    const reader = new FileReader()
    reader.onload = (ev) => {
      const text = ev.target.result
      const { data, errors } = parseCSV(text)
      importPreviewData.value = data
      importErrors.value = errors
      if (data.length === 0 && errors.length === 0) {
        ElMessage.warning('文件解析结果为空，请检查文件内容')
      } else {
        ElMessage.success(`解析完成：${data.length} 条有效数据${errors.length > 0 ? '，' + errors.length + ' 条跳过' : ''}`)
      }
    }
    reader.readAsText(file, 'UTF-8')
  } else {
    // xlsx/xls：动态加载 SheetJS CDN
    ElMessage.info('正在加载Excel解析器...')
    if (window.XLSX) {
      readExcel(file)
    } else {
      const script = document.createElement('script')
      script.src = 'https://cdn.sheetjs.com/xlsx-0.20.3/package/dist/xlsx.full.min.js'
      script.onload = () => readExcel(file)
      script.onerror = () => ElMessage.error('Excel解析器加载失败，请使用CSV格式')
      document.head.appendChild(script)
    }
  }
}

// 读取Excel文件
const readExcel = (file) => {
  const reader = new FileReader()
  reader.onload = (ev) => {
    try {
      const wb = window.XLSX.read(ev.target.result, { type: 'array' })
      const ws = wb.Sheets[wb.SheetNames[0]]
      const csvText = window.XLSX.utils.sheet_to_csv(ws)
      const { data, errors } = parseCSV(csvText)
      importPreviewData.value = data
      importErrors.value = errors
      if (data.length === 0 && errors.length === 0) {
        ElMessage.warning('文件解析结果为空，请检查文件内容')
      } else {
        ElMessage.success(`解析完成：${data.length} 条有效数据${errors.length > 0 ? '，' + errors.length + ' 条跳过' : ''}`)
      }
    } catch (err) {
      ElMessage.error('Excel解析失败：' + err.message)
    }
  }
  reader.readAsArrayBuffer(file)
}

// 提交导入
const submitImport = async () => {
  if (importPreviewData.value.length === 0) {
    ElMessage.warning('没有可导入的数据')
    return
  }

  // 把分类名称转换为 categoryId
  const resolvedData = importPreviewData.value.map(item => {
    const cat = categoryList.value.find(c => c.categoryName === item.categoryName)
    return {
      assetName: item.assetName,
      categoryId: cat ? cat.categoryId : null,
      brand: item.brand,
      model: item.model,
      serialNumber: item.serialNumber,
      quantity: item.quantity,
      purchasePrice: item.purchasePrice ? parseFloat(item.purchasePrice) : null,
      purchaseDate: item.purchaseDate || null,
      depreciationMethod: item.depreciationMethod,
      department: item.department,
      responsiblePerson: item.responsiblePerson,
      storageLocation: item.storageLocation,
      supplier: item.supplier,
      warrantyInfo: item.warrantyInfo,
      remark: item.remark,
      status: 0
    }
  })

  const invalid = resolvedData.filter(d => !d.categoryId)
  if (invalid.length > 0) {
    const names = [...new Set(importPreviewData.value.filter((_, i) => !resolvedData[i].categoryId).map(d => d.categoryName))]
    ElMessage.warning(`以下分类名称不存在：${names.join('、')}，请先添加分类或修正模板`)
    return
  }

  importLoading.value = true
  try {
    const res = await request.post('/assetInfo/batchSave', resolvedData)
    if (res.code === 200) {
      ElMessage.success(`成功导入 ${resolvedData.length} 条资产`)
      importDialogVisible.value = false
      getAssetList()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (err) {
    ElMessage.error('导入请求失败：' + (err.message || '网络异常'))
  } finally {
    importLoading.value = false
  }
}

// 辅助函数
const getCategoryName = (categoryId) => {
  const cat = categoryList.value.find(c => c.categoryId === categoryId)
  return cat ? cat.categoryName : ''
}

const getStatusText = (status) => {
  const map = { 0: '未领用', 1: '已领用', 2: '维修中', 3: '已报废' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-unused', 1: 'status-used', 2: 'status-repair', 3: 'status-scrapped' }
  return map[status] || 'status-unused'
}

const getDepText = (m) => {
  if (m === 'straight_line') return '直线折旧'
  if (m === 'declining_balance') return '余额递减'
  return m || '-'
}

// ========== 领用申请相关 ==========

// 从资产列表直接申请领用
const handleApplyAsset = (row) => {
  applyAsset.value = row
  applyForm.assetId = row.assetId
  applyForm.department = parseDepartment(row.remark) !== '-' ? parseDepartment(row.remark) : ''
  applyForm.contactPerson = ''
  applyForm.contactPhone = ''
  applyForm.remark = ''
  applyDialogVisible.value = true
}

// 重置领用申请表单
const resetApplyForm = () => {
  if (applyFormRef.value) {
    applyFormRef.value.resetFields()
  }
  applyAsset.value = null
  applyForm.assetId = null
  applyForm.department = ''
  applyForm.contactPerson = ''
  applyForm.contactPhone = ''
  applyForm.remark = ''
}

// 提交领用申请
const submitApply = async () => {
  if (!applyFormRef.value) return
  try {
    await applyFormRef.value.validate()
    await request.post('/use/apply', applyForm)
    ElMessage.success('领用申请提交成功')
    applyDialogVisible.value = false
    getAssetList() // 刷新资产列表
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('申请失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 从资产列表直接归还
const handleReturnAsset = (row) => {
  ElMessageBox.confirm(`确认要归还资产"${row.assetName}"吗？`, '归还确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/use/return', null, { params: { assetId: row.assetId } })
      ElMessage.success('归还成功')
      getAssetList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('归还失败：' + (error.response?.data?.msg || error.message))
      }
    }
  }).catch(() => {})
}

// ========== 领用记录相关 ==========

// 显示资产的领用记录
const showUseRecords = async (row) => {
  useRecordAsset.value = row
  useRecordDialogVisible.value = true
  useRecordLoading.value = true
  try {
    const res = await request.get('/use/list', { params: { assetId: row.assetId } })
    useRecordList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载领用记录失败')
  } finally {
    useRecordLoading.value = false
  }
}

// 跳转到领用申请页面
const goToAssetUse = () => {
  router.push('/asset-use')
}

// ========== 辅助函数 ==========

const getUseTypeText = (type) => {
  const map = { 1: '领用', 2: '归还', 3: '调拨' }
  return map[type] || '领用'
}

const getApproveStatusText = (status) => {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const getApproveStatusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-approved', 2: 'status-rejected' }
  return map[status] || 'status-pending'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
/* 领用申请资产信息 */
.apply-asset-info {
  background: #ecf5ff;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 16px;
  border-left: 3px solid #409eff;
}

.apply-asset-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.apply-asset-detail {
  font-size: 13px;
  color: #606266;
  display: flex;
  gap: 16px;
}

.apply-asset-detail span {
  color: #909399;
}

/* 领用记录弹窗 */
.use-record-header {
  margin-bottom: 12px;
  font-size: 15px;
}

.use-record-code {
  color: #909399;
  font-size: 13px;
}

/* 批量导入弹窗 */
.import-steps {
  padding: 4px 0;
}
.import-step {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}
.step-header {
  background: #f5f7fa;
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  flex-shrink: 0;
}
.step-body {
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.step-tip {
  font-size: 12px;
  color: #909399;
}
.import-errors {
  margin-top: 10px;
  padding: 8px 12px;
  background: #fef0f0;
  border-radius: 4px;
  width: 100%;
}
.error-title {
  font-size: 13px;
  color: #f56c6c;
  margin-bottom: 4px;
}
.error-item {
  font-size: 12px;
  color: #f56c6c;
  line-height: 1.8;
}
</style>
