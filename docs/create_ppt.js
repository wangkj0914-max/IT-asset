const pptxgen = require("pptxgenjs");
const pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.author = "Tim Wang";
pres.title = "IT固定资产管理系统汇报";

const TEAL = "028090";
const SEA = "00A896";
const MINT = "02C39A";
const DARK = "1A1A2E";
const WHITE = "FFFFFF";
const GRAY = "64748B";
const LIGHT = "F0FDF9";

// ===== SLIDE 1: TITLE =====
const s1 = pres.addSlide();
s1.background = { color: DARK };
s1.addShape(pres.shapes.RECTANGLE, { x: 0, y: 4.8, w: 10, h: 0.825, fill: { color: TEAL } });
s1.addText("IT固定资产管理系统", { x: 0.8, y: 1.2, w: 8.4, h: 1.2, fontSize: 44, fontFace: "Arial Black", color: WHITE, bold: true, margin: 0 });
s1.addText("从Excel管理到数字化资产管理平台", { x: 0.8, y: 2.5, w: 8.4, h: 0.8, fontSize: 22, fontFace: "Calibri", color: MINT, margin: 0 });
s1.addText("苏州 - Penang  双站点  |  2026年7月", { x: 0.8, y: 3.5, w: 8.4, h: 0.6, fontSize: 14, fontFace: "Calibri", color: GRAY, margin: 0 });

// ===== SLIDE 2: BEFORE vs AFTER =====
const s2 = pres.addSlide();
s2.background = { color: WHITE };
s2.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.08, fill: { color: TEAL } });
s2.addText("背景与痛点", { x: 0.6, y: 0.3, w: 8.8, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: DARK, bold: true, margin: 0 });

// Left card
s2.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.3, w: 4.3, h: 3.8, fill: { color: "F8FAFC" }, shadow: { type: "outer", blur: 4, offset: 2, angle: 135, color: "000000", opacity: 0.08 } });
s2.addText("BEFORE - Excel管理", { x: 0.8, y: 1.45, w: 3.8, h: 0.5, fontSize: 16, fontFace: "Arial Black", color: "DC2626", margin: 0 });
s2.addText([
  { text: "手工录入，效率低、易出错", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "资产状态无法实时追踪", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "折旧靠人工计算，费时费力", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "盘点靠纸质清单，容易遗漏", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "缺少领用归还闭环，找不到责任人", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "多站点数据隔离困难", options: { bullet: true, fontSize: 13, color: DARK } }
], { x: 0.8, y: 2.1, w: 3.8, h: 2.8, valign: "top" });

// Right card
s2.addShape(pres.shapes.RECTANGLE, { x: 5.2, y: 1.3, w: 4.3, h: 3.8, fill: { color: LIGHT }, shadow: { type: "outer", blur: 4, offset: 2, angle: 135, color: "000000", opacity: 0.08 } });
s2.addText("AFTER - 数字化管理", { x: 5.5, y: 1.45, w: 3.8, h: 0.5, fontSize: 16, fontFace: "Arial Black", color: TEAL, margin: 0 });
s2.addText([
  { text: "Web端录入，批量导入，数据规范", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "资产状态、位置、责任人一目了然", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "自动直线折旧，EOL预警，价值追踪", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "扫码盘点，差异报告，精准高效", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "Checkout/Checkin闭环，逾期自动标记", options: { bullet: true, breakLine: true, fontSize: 13, color: DARK } },
  { text: "苏州/Penang双站点，数据完全隔离", options: { bullet: true, fontSize: 13, color: DARK } }
], { x: 5.5, y: 2.1, w: 3.8, h: 2.8, valign: "top" });

// ===== SLIDE 3: MODULES =====
const s3 = pres.addSlide();
s3.background = { color: WHITE };
s3.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.08, fill: { color: TEAL } });
s3.addText("系统功能概览", { x: 0.6, y: 0.3, w: 8.8, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: DARK, bold: true, margin: 0 });

const modules = [
  { title: "资产管理", items: "资产信息 - 入库 - 标签打印\n领用 - 归还 - 维修 - 报废\n盘点 - 调拨", color: TEAL },
  { title: "基础数据", items: "资产分类 - 资产模型\n状态标签 - 存放地点\n部门信息", color: SEA },
  { title: "物资管理", items: "耗材管理 - 许可证管理\n库存预警 - 消耗趋势", color: MINT },
  { title: "系统管理", items: "用户管理 - 用户组ACL\n自定义字段 - 操作日志\n流程设置 - 接口管理", color: "0E7490" },
  { title: "综合报表", items: "折旧报表 - 部门汇总\n资产生命周期 - 领用统计\n耗材对比 - CSV导出", color: "0891B2" },
  { title: "审批中心", items: "统一审批入口\n待审数量实时提醒", color: "155E75" }
];

modules.forEach(function(m, i) {
  var col = i % 3, row = Math.floor(i / 3), x = 0.5 + col * 3.1, y = 1.2 + row * 2.2;
  s3.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 2.9, h: 2.0, fill: { color: WHITE }, shadow: { type: "outer", blur: 4, offset: 2, angle: 135, color: "000000", opacity: 0.08 } });
  s3.addShape(pres.shapes.RECTANGLE, { x: x, y: y, w: 2.9, h: 0.06, fill: { color: m.color } });
  s3.addText(m.title, { x: x + 0.2, y: y + 0.2, w: 2.5, h: 0.4, fontSize: 15, fontFace: "Arial Black", color: m.color, bold: true, margin: 0 });
  s3.addText(m.items, { x: x + 0.2, y: y + 0.6, w: 2.5, h: 1.3, fontSize: 11, fontFace: "Calibri", color: DARK, margin: 0, lineSpacingMultiple: 1.6 });
});

// ===== SLIDE 4: HIGHLIGHTS =====
const s4 = pres.addSlide();
s4.background = { color: WHITE };
s4.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.08, fill: { color: TEAL } });
s4.addText("核心亮点", { x: 0.6, y: 0.3, w: 8.8, h: 0.7, fontSize: 32, fontFace: "Arial Black", color: DARK, bold: true, margin: 0 });

var statData = [
  { num: "34", label: "数据表", sub: "支持双站点隔离" },
  { num: "380+", label: "资产记录", sub: "苏州 + Penang" },
  { num: "82", label: "API接口", sub: "完整RESTful" },
  { num: "2.1万", label: "行源码", sub: "Spring Boot + Vue 3" }
];
statData.forEach(function(s, i) {
  var sx = 0.5 + i * 2.35;
  s4.addShape(pres.shapes.RECTANGLE, { x: sx, y: 1.3, w: 2.15, h: 1.8, fill: { color: LIGHT }, shadow: { type: "outer", blur: 4, offset: 2, angle: 135, color: "000000", opacity: 0.08 } });
  s4.addText(s.num, { x: sx, y: 1.45, w: 2.15, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: TEAL, bold: true, align: "center", margin: 0 });
  s4.addText(s.label, { x: sx, y: 2.2, w: 2.15, h: 0.4, fontSize: 14, fontFace: "Calibri", color: DARK, bold: true, align: "center", margin: 0 });
  s4.addText(s.sub, { x: sx, y: 2.55, w: 2.15, h: 0.35, fontSize: 10, fontFace: "Calibri", color: GRAY, align: "center", margin: 0 });
});

var featData = [
  { t: "资产模型 + 自动折旧", d: "选模型自动继承参数，直线法计算当前价值与EOL" },
  { t: "Checkout/Checkin闭环", d: "领用设预期归还日期，归还自动计算逾期状态" },
  { t: "树形地点 + 自定义字段", d: "存放地点父子层级，支持5种类型动态字段" },
  { t: "综合报表 + CSV导出", d: "6维度报表一键导出CSV，兼容Excel直接打开" }
];
featData.forEach(function(h, i) {
  var fy = 3.4 + i * 0.5;
  s4.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: fy, w: 0.08, h: 0.35, fill: { color: TEAL } });
  s4.addText(h.t, { x: 0.8, y: fy, w: 3.8, h: 0.35, fontSize: 12, fontFace: "Calibri", color: DARK, bold: true, margin: 0, valign: "middle" });
  s4.addText(h.d, { x: 4.8, y: fy, w: 4.9, h: 0.35, fontSize: 11, fontFace: "Calibri", color: GRAY, margin: 0, valign: "middle" });
});

// ===== SLIDE 5: CONCLUSION =====
const s5 = pres.addSlide();
s5.background = { color: DARK };
s5.addShape(pres.shapes.RECTANGLE, { x: 0, y: 4.8, w: 10, h: 0.825, fill: { color: TEAL } });
s5.addText("总结与展望", { x: 0.8, y: 0.4, w: 8.4, h: 0.8, fontSize: 32, fontFace: "Arial Black", color: WHITE, bold: true, margin: 0 });

var items = [
  "替代Excel，实现资产数字化、流程化管理",
  "支持苏州、Penang双站点，数据完全隔离",
  "P0-P3四轮迭代，覆盖IT资产全生命周期",
  "后续方向：扫码APP、移动审批、邮件通知"
];
items.forEach(function(txt, i) {
  var iy = 1.5 + i * 0.65;
  s5.addShape(pres.shapes.RECTANGLE, { x: 0.8, y: iy + 0.08, w: 0.25, h: 0.25, fill: { color: MINT } });
  s5.addText(txt, { x: 1.3, y: iy, w: 8, h: 0.45, fontSize: 16, fontFace: "Calibri", color: WHITE, margin: 0, valign: "middle" });
});

s5.addText("Thank You", { x: 0.8, y: 4.0, w: 8.4, h: 0.6, fontSize: 24, fontFace: "Calibri", color: MINT, italic: true, margin: 0 });

pres.writeFile({ fileName: "E:/it-asset-system/docs/IT资产管理系统汇报.pptx" }).then(function() {
  console.log("PPT created: E:/it-asset-system/docs/IT资产管理系统汇报.pptx");
}).catch(function(err) {
  console.error("Error:", err);
});
