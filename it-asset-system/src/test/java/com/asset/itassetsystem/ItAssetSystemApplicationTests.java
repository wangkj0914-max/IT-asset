package com.asset.itassetsystem;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.SysUser;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.SysUserService;
import com.asset.itassetsystem.service.LicenseService;
import com.asset.itassetsystem.service.AssetCategoryService;
import com.asset.itassetsystem.service.SysDepartmentService;
import com.asset.itassetsystem.service.StorageLocationService;
import com.asset.itassetsystem.service.ConsumableService;
import com.asset.itassetsystem.security.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试 - 核心业务服务层
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItAssetSystemApplicationTests {

    @Autowired private AssetInfoService assetInfoService;
    @Autowired private SysUserService sysUserService;
    @Autowired private LicenseService licenseService;
    @Autowired private AssetCategoryService assetCategoryService;
    @Autowired private SysDepartmentService sysDepartmentService;
    @Autowired private StorageLocationService storageLocationService;
    @Autowired private ConsumableService consumableService;

    private static Long testAssetId;

    // ==================== 1. AssetInfo CRUD 测试 ====================
    @Test @Order(1) @DisplayName("UT-01: 资产新增")
    void testCreateAsset() {
        AssetInfo a = new AssetInfo();
        a.setAssetName("UT-UNIT-TEST");
        a.setCategoryId(2L);
        a.setModel("UT-Model");
        a.setDepartment("IT");
        a.setSite("Penang");
        a.setStatus(1);
        a.setSerialNumber("UT-SN-" + System.currentTimeMillis());
        boolean saved = assetInfoService.save(a);
        assertTrue(saved, "资产应保存成功");
        assertNotNull(a.getAssetId(), "应有自增ID");
        testAssetId = a.getAssetId();
    }

    @Test @Order(2) @DisplayName("UT-02: 资产查询")
    void testQueryAsset() {
        AssetInfo a = assetInfoService.getById(testAssetId);
        assertNotNull(a, "应查到资产");
        assertEquals("UT-UNIT-TEST", a.getAssetName());
        assertEquals("Penang", a.getSite());
    }

    @Test @Order(3) @DisplayName("UT-03: 资产更新")
    void testUpdateAsset() {
        AssetInfo a = assetInfoService.getById(testAssetId);
        a.setAssetName("UT-UPDATED");
        a.setStorageLocation("Updated-Lab");
        boolean updated = assetInfoService.updateById(a);
        assertTrue(updated, "更新应成功");
        AssetInfo reload = assetInfoService.getById(testAssetId);
        assertEquals("UT-UPDATED", reload.getAssetName());
        assertEquals("Updated-Lab", reload.getStorageLocation());
    }

    @Test @Order(4) @DisplayName("UT-04: 站点过滤")
    void testSiteFilter() {
        var w = new LambdaQueryWrapper<AssetInfo>();
        w.eq(AssetInfo::getSite, "Penang");
        w.eq(AssetInfo::getAssetName, "UT-UPDATED");
        List<AssetInfo> list = assetInfoService.list(w);
        assertEquals(1, list.size());
    }

    @Test @Order(5) @DisplayName("UT-05: 资产删除")
    void testDeleteAsset() {
        boolean deleted = assetInfoService.removeById(testAssetId);
        assertTrue(deleted, "删除应成功");
        AssetInfo a = assetInfoService.getById(testAssetId);
        assertNull(a, "删除后应查不到");
    }

    // ==================== 2. 用户认证测试 ====================
    @Test @Order(6) @DisplayName("UT-06: 正确密码登录")
    void testLoginSuccess() {
        SysUser user = sysUserService.login("admin", "CHNX#000");
        assertNotNull(user, "admin应登录成功");
        assertEquals("admin", user.getUsername());
        assertEquals(2, user.getRole()); // admin role=2
    }

    @Test @Order(7) @DisplayName("UT-07: 错误密码登录")
    void testLoginFail() {
        SysUser user = sysUserService.login("admin", "wrong-pass-999");
        assertNull(user, "错误密码应返回null");
    }

    @Test @Order(8) @DisplayName("UT-08: 不存在用户登录")
    void testLoginNonexist() {
        SysUser user = sysUserService.login("no_such_user_99", "any");
        assertNull(user, "不存在用户应返回null");
    }

    // ==================== 3. JWT Token 测试 ====================
    @Test @Order(9) @DisplayName("UT-09: JWT生成与解析")
    void testJwtCreateAndParse() {
        String token = JwtUtil.generateToken(1L, "admin", 2);
        assertNotNull(token);
        String username = JwtUtil.getUsername(token);
        assertEquals("admin", username);
    }

    @Test @Order(10) @DisplayName("UT-10: JWT过期Token")
    void testJwtExpired() {
        String badToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.invalidsig";
        String user = JwtUtil.getUsername(badToken);
        assertNull(user, "无效Token应返回null");
    }

    // ==================== 4. 分类/部门/许可证服务测试 ====================
    @Test @Order(11) @DisplayName("UT-11: 许可证即将过期")
    void testLicenseExpiring() {
        List<?> list = licenseService.getExpiring(365, "苏州");
        assertNotNull(list, "应有许可证列表");
    }

    @Test @Order(12) @DisplayName("UT-12: 分类按站点查询")
    void testCategoryBySite() {
        List<?> list = assetCategoryService.listAll("Penang");
        assertNotNull(list);
        assertFalse(list.isEmpty(), "Penang应有分类");
    }

    @Test @Order(13) @DisplayName("UT-13: 部门按站点查询")
    void testDepartmentBySite() {
        var w = new LambdaQueryWrapper<com.asset.itassetsystem.entity.SysDepartment>();
        w.eq(com.asset.itassetsystem.entity.SysDepartment::getSite, "Penang");
        List<?> list = sysDepartmentService.list(w);
        assertNotNull(list);
    }

    // ==================== 5. Result 包装类测试 ====================
    @Test @Order(14) @DisplayName("UT-14: Result.success")
    void testResultSuccess() {
        Result<String> r = Result.success("test-data");
        assertEquals(200, r.getCode());
        assertEquals("操作成功", r.getMsg());
        assertEquals("test-data", r.getData());
    }

    @Test @Order(15) @DisplayName("UT-15: Result.fail vs error")
    void testResultFail() {
        Result<String> r = Result.fail("业务异常");
        assertEquals(400, r.getCode());
        assertEquals("业务异常", r.getMsg());

        Result<String> e = Result.error("服务器错误");
        assertEquals(500, e.getCode());
    }

    // ==================== 6. 边界值测试 ====================
    @Test @Order(16) @DisplayName("UT-16: 空值场景")
    void testNullHandling() {
        AssetInfo a = assetInfoService.getById(Long.MAX_VALUE);
        assertNull(a, "不存在的ID应返回null");

        var w = new LambdaQueryWrapper<AssetInfo>();
        w.eq(AssetInfo::getSite, "NonExistent");
        List<AssetInfo> list = assetInfoService.list(w);
        assertTrue(list.isEmpty(), "不存在的站点应返回空列表");
    }
}
