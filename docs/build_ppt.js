const pptxgen = require("pptxgenjs");
const fs = require("fs");
const pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.author = "Tim Wang";
pres.title = "IT资产管理系统建设项目汇报";

const TEAL = "028090", SEA = "00A896", MINT = "02C39A", DARK = "0F172A";
const WHITE = "FFFFFF", GRAY = "64748B", LIGHT = "F0FDF9", RED = "DC2626";
const BODY = "1E293B";

const img1 = "data:image/png;base64," + fs.readFileSync("E:/it-asset-system/docs/compare_pain.png").toString("base64");
const img2 = "data:image/png;base64," + fs.readFileSync("E:/it-asset-system/docs/compare_table.png").toString("base64");

const card = function() {
  return { type: "outer", blur: 4, offset: 2, angle: 135, color: "000000", opacity: 0.08 };
};

// ===== SLIDE 1: COVER =====
let s = pres.addSlide();
s.background = { color: DARK };
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 0.12, h: 5.625, fill: { color: TEAL } });
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 4.9, w: 10, h: 0.725, fill: { color: TEAL, transparency: 70 } });
s.addText("IT资产管理系统建设项目汇报", { x: 0.8, y: 1.2, w: 8.5, h: 1.0, fontSize: 38, fontFace: "Arial Black", color: WHITE, bold: true, margin: 0 });
s.addText("从Excel表格到 AI驱动的智能化管理", { x: 0.8, y: 2.3, w: 8.5, h: 0.6, fontSize: 20, fontFace: "Calibri", color: MINT, margin: 0 });
s.addText("汇报人：Tim Wang  |  日期：2026年7月", { x: 0.8, y: 3.2, w: 8.5, h: 0.5, fontSize: 13, fontFace: "Calibri", color: GRAY, margin: 0 });

// ===== SLIDE 2: BACKGROUND =====
s = pres.addSlide();
s.background = { color: WHITE };
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.08, fill: { color: TEAL } });
s.addText("为什么要做这个项目？", { x: 0.6, y: 0.35, w: 8.5, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: DARK, bold: true, margin: 0 });

// Problem statement box
s.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.2, w: 9.0, h: 1.6, fill: { color: "FEF2F2" }, shadow: card() });
s.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.2, w: 0.08, h: 1.6, fill: { color: RED } });
s.addText("核心矛盾：资产越来越多，Excel 越来越不够用", { x: 0.9, y: 1.3, w: 8.4, h: 0.5, fontSize: 20, fontFace: "Arial Black", color: RED, margin: 0 });
s.addText([
  { text: "公司IT资产数量持续增长：电脑、服务器、网络设备、软件许可等，Excel管理方式已不堪重负。", options: { breakLine: true, fontSize: 14, color: BODY } },
  { text: "资产盘点周期长、数据不一致、状态不透明、责任不清晰。", options: { breakLine: true, fontSize: 14, color: BODY } },
  { text: "无法追溯资产变更历史，无法实时掌握资产分布和利用效率。", options: { fontSize: 14, color: BODY } }
], { x: 0.9, y: 1.95, w: 8.4, h: 0.8, paraSpaceAfter: 6 });

// Goal box  
s.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 3.3, w: 9.0, h: 1.1, fill: { color: LIGHT }, shadow: card() });
s.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 3.3, w: 0.08, h: 1.1, fill: { color: TEAL } });
s.addText("目标", { x: 0.9, y: 3.4, w: 0.9, h: 0.35, fontSize: 12, fontFace: "Arial Black", color: TEAL, margin: 0 });
s.addText("建立\u201C采购 - 使用 - 维护 - 处置\u201D全生命周期的数字化管理体系", { x: 1.6, y: 3.4, w: 7.7, h: 0.35, fontSize: 15, fontFace: "Calibri", color: BODY, margin: 0 });

// Key stats
var bgStats = [
  { num: "380+", label: "IT资产规模", icon: "1" },
  { num: "34", label: "数据表", icon: "2" },
  { num: "2", label: "站点", icon: "3" },
  { num: "Excel", label: "当前管理方式", icon: "!" }
];
bgStats.forEach(function(st, i) {
  var sx = 0.6 + i * 2.35;
  s.addShape(pres.shapes.RECTANGLE, { x: sx, y: 4.7, w: 2.05, h: 0.7, fill: { color: "F8FAFC" }, shadow: card() });
  s.addText(st.num, { x: sx + 0.12, y: 4.75, w: 1.5, h: 0.45, fontSize: 28, fontFace: "Arial Black", color: TEAL, bold: true, margin: 0 });
  s.addText(st.label, { x: sx + 0.12, y: 5.15, w: 1.8, h: 0.25, fontSize: 10, fontFace: "Calibri", color: GRAY, margin: 0 });
});

// ===== SLIDE 3: BEFORE - PAIN POINTS =====
s = pres.addSlide();
s.background = { color: WHITE };
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.08, fill: { color: RED } });
s.addText("改善前 — Excel的四大痛点", { x: 0.6, y: 0.35, w: 8.5, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: DARK, bold: true, margin: 0 });

var pains = [
  { num: "01", title: "版本混乱", desc: "多版本并存，同一资产多人编辑，无法确认哪个是最新数据", clr: "DC2626" },
  { num: "02", title: "数据孤岛", desc: "各部门表格各自为政，信息割裂，跨部门核对全靠邮件汇总", clr: "EA580C" },
  { num: "03", title: "盘点低效", desc: "人工核对耗时数周，账实不符成常态，年终审计压力大", clr: "CA8A04" },
  { num: "04", title: "缺乏追溯", desc: "资产领用、调拨、维修等变动无系统记录，责任追踪困难", clr: "DC2626" }
];
pains.forEach(function(p, i) {
  var px = 0.4 + i * 2.35;
  s.addShape(pres.shapes.RECTANGLE, { x: px, y: 1.2, w: 2.2, h: 3.8, fill: { color: WHITE }, shadow: card() });
  s.addShape(pres.shapes.RECTANGLE, { x: px, y: 1.2, w: 2.2, h: 0.06, fill: { color: p.clr } });
  s.addShape(pres.shapes.RECTANGLE, { x: px + 0.2, y: 1.55, w: 0.7, h: 0.7, fill: { color: p.clr } });
  s.addText(p.num, { x: px + 0.2, y: 1.55, w: 0.7, h: 0.7, fontSize: 22, fontFace: "Arial Black", color: WHITE, bold: true, align: "center", valign: "middle", margin: 0 });
  s.addText(p.title, { x: px + 0.2, y: 2.45, w: 1.8, h: 0.4, fontSize: 18, fontFace: "Arial Black", color: p.clr, bold: true, margin: 0 });
  s.addText(p.desc, { x: px + 0.2, y: 2.95, w: 1.8, h: 1.5, fontSize: 12, fontFace: "Calibri", color: BODY, margin: 0, lineSpacingMultiple: 1.7 });
});

// Bottom: Impact summary
s.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 5.15, w: 9.0, h: 0.32, fill: { color: "FEF2F2" } });
s.addText("影响： 资产管理严重依赖个人经验，数据质量参差不齐，无法支撑公司规模化发展需求", { x: 0.7, y: 5.17, w: 8.6, h: 0.28, fontSize: 11, fontFace: "Calibri", color: RED, margin: 0 });

// ===== SLIDE 4: AFTER - SYSTEM FEATURES =====
s = pres.addSlide();
s.background = { color: WHITE };
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.08, fill: { color: TEAL } });
s.addText("改善后 — 系统核心功能", { x: 0.6, y: 0.35, w: 8.5, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: DARK, bold: true, margin: 0 });

var feats = [
  { icon: "1", title: "一物一码", desc: "每件资产生成唯一二维码/标签，手机扫码即可查看完整信息，结合标签打印机实现即打即贴" },
  { icon: "2", title: "全生命周期管理", desc: "采购→入库→领用→调拨→维修→报废，全流程线上闭环，状态实时同步，不再遗漏任何环节" },
  { icon: "3", title: "智能移动盘点", desc: "手机扫码采集，数据实时同步云端，分钟级完成资产盘点，差异报告自动生成" },
  { icon: "4", title: "数据驾驶舱", desc: "多维度可视化报表：折旧概览、部门汇总、领用统计、耗材对比，资产分布与使用效率一目了然" }
];
feats.forEach(function(f, i) {
  var fx = 0.4 + (i % 2) * 4.75, fy = 1.2 + Math.floor(i / 2) * 2.1;
  s.addShape(pres.shapes.RECTANGLE, { x: fx, y: fy, w: 4.55, h: 1.9, fill: { color: WHITE }, shadow: card() });
  s.addShape(pres.shapes.RECTANGLE, { x: fx, y: fy, w: 0.08, h: 1.9, fill: { color: TEAL } });
  s.addShape(pres.shapes.RECTANGLE, { x: fx + 0.3, y: fy + 0.25, w: 0.45, h: 0.45, fill: { color: TEAL } });
  s.addText(f.icon, { x: fx + 0.3, y: fy + 0.25, w: 0.45, h: 0.45, fontSize: 18, fontFace: "Arial Black", color: WHITE, bold: true, align: "center", valign: "middle", margin: 0 });
  s.addText(f.title, { x: fx + 0.95, y: fy + 0.2, w: 3.4, h: 0.45, fontSize: 17, fontFace: "Arial Black", color: TEAL, bold: true, margin: 0 });
  s.addText(f.desc, { x: fx + 0.3, y: fy + 0.85, w: 4.1, h: 0.9, fontSize: 11, fontFace: "Calibri", color: BODY, margin: 0, lineSpacingMultiple: 1.7 });
});

// ===== SLIDE 5: COMPARISON TABLE =====
s = pres.addSlide();
s.background = { color: WHITE };
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.08, fill: { color: TEAL } });
s.addText("改善前后核心对比", { x: 0.6, y: 0.35, w: 8.5, h: 0.6, fontSize: 28, fontFace: "Arial Black", color: DARK, bold: true, margin: 0 });

var headerOpts = { fill: { color: DARK }, color: WHITE, bold: true, fontSize: 12, fontFace: "Calibri", align: "center", valign: "middle" };
var beforeOpts = { fill: { color: "FEF2F2" }, color: BODY, fontSize: 11, fontFace: "Calibri", valign: "middle" };
var afterOpts = { fill: { color: LIGHT }, color: BODY, fontSize: 11, fontFace: "Calibri", valign: "middle" };
var dimOpts = { fill: { color: "F8FAFC" }, color: DARK, fontSize: 12, fontFace: "Arial Black", bold: true, valign: "middle", align: "center" };

var tableData = [
  [
    { text: "对比维度", options: Object.assign({}, headerOpts) },
    { text: "改善前（Excel）", options: Object.assign({}, headerOpts, { fill: { color: RED } }) },
    { text: "改善后（AI系统）", options: Object.assign({}, headerOpts, { fill: { color: TEAL } }) }
  ],
  [
    { text: "数据录入", options: dimOpts },
    { text: "手工录入，易错漏", options: beforeOpts },
    { text: "扫码自动采集，准确高效", options: afterOpts }
  ],
  [
    { text: "数据一致性", options: dimOpts },
    { text: "多版本混乱，无法确认", options: beforeOpts },
    { text: "云端统一，实时同步", options: afterOpts }
  ],
  [
    { text: "盘点效率", options: dimOpts },
    { text: "耗时数周", options: beforeOpts },
    { text: "分钟级完成", options: afterOpts }
  ],
  [
    { text: "资产追溯", options: dimOpts },
    { text: "变动无记录，责任不清", options: beforeOpts },
    { text: "全流程留痕，操作可查", options: afterOpts }
  ],
  [
    { text: "报表统计", options: dimOpts },
    { text: "手工汇总，按天计算", options: beforeOpts },
    { text: "自动生成，分钟级响应", options: afterOpts }
  ],
  [
    { text: "决策支持", options: dimOpts },
    { text: "凭经验判断", options: beforeOpts },
    { text: "数据驾驶舱可视化", options: afterOpts }
  ]
];

s.addTable(tableData, {
  x: 0.5, y: 1.15, w: 9.0,
  colW: [2.0, 3.5, 3.5],
  rowH: [0.45, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5],
  border: { pt: 0.5, color: "E2E8F0" },
  autoPage: false
});

// Bottom highlight
s.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 4.95, w: 9.0, h: 0.5, fill: { color: LIGHT }, shadow: card() });
s.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 4.95, w: 0.08, h: 0.5, fill: { color: TEAL } });
s.addText("核心价值：从\u201C人治\u201D走向\u201C数治\u201D \u2014 盘得准、管得住、算得清", { x: 0.85, y: 4.97, w: 8.4, h: 0.46, fontSize: 15, fontFace: "Arial Black", color: TEAL, margin: 0, valign: "middle" });

// ===== SLIDE 6: SUMMARY & OUTLOOK =====
s = pres.addSlide();
s.background = { color: DARK };
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 0.12, h: 5.625, fill: { color: TEAL } });
s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 4.9, w: 10, h: 0.725, fill: { color: TEAL, transparency: 70 } });
s.addText("总结与展望", { x: 0.8, y: 0.4, w: 8.5, h: 0.7, fontSize: 28, fontFace: "Arial Black", color: WHITE, bold: true, margin: 0 });

// Key achievements
var achieves = [
  { n: "4", label: "迭代版本", sub: "P0 - P3 持续优化" },
  { n: "380+", label: "资产上线", sub: "苏州 + Penang 双站点" },
  { n: "34", label: "数据表", sub: "覆盖全生命周期" },
  { n: "2.1万", label: "行代码", sub: "SpringBoot + Vue3" }
];
achieves.forEach(function(a, i) {
  var ax = 0.6 + i * 2.3;
  s.addShape(pres.shapes.RECTANGLE, { x: ax, y: 1.3, w: 2.0, h: 1.5, fill: { color: "1E293B" }, shadow: card() });
  s.addText(a.n, { x: ax, y: 1.35, w: 2.0, h: 0.6, fontSize: 32, fontFace: "Arial Black", color: MINT, bold: true, align: "center", margin: 0 });
  s.addText(a.label, { x: ax, y: 1.95, w: 2.0, h: 0.35, fontSize: 13, fontFace: "Calibri", color: WHITE, align: "center", margin: 0 });
  s.addText(a.sub, { x: ax, y: 2.3, w: 2.0, h: 0.3, fontSize: 9, fontFace: "Calibri", color: GRAY, align: "center", margin: 0 });
});

// Core conclusion
s.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 3.1, w: 8.8, h: 0.6, fill: { color: TEAL } });
s.addText("资产管理从\u201C人治\u201D走向\u201C数治\u201D \u2014 盘得准、管得住、算得清", { x: 0.8, y: 3.12, w: 8.4, h: 0.56, fontSize: 16, fontFace: "Arial Black", color: WHITE, margin: 0, valign: "middle" });

// Future plans
s.addText("未来规划", { x: 0.8, y: 4.0, w: 2, h: 0.35, fontSize: 14, fontFace: "Arial Black", color: MINT, bold: true, margin: 0 });
var plans = [
  "AI智能预警：低库存自动提醒、EOL到期预警、异常操作检测",
  "系统深度集成：与财务/OA系统对接，打通企业数字化最后一公里",
  "移动端APP：支持手机审批、移动盘点、消息推送"
];
plans.forEach(function(p, i) {
  s.addShape(pres.shapes.RECTANGLE, { x: 0.8, y: 4.38 + i * 0.35, w: 0.18, h: 0.18, fill: { color: MINT } });
  s.addText(p, { x: 1.15, y: 4.3 + i * 0.35, w: 8.2, h: 0.35, fontSize: 12, fontFace: "Calibri", color: "CBD5E1", margin: 0, valign: "middle" });
});

pres.writeFile({ fileName: "E:/it-asset-system/docs/IT资产管理系统汇报.pptx" }).then(function() {
  console.log("PPT created successfully");
}).catch(function(err) {
  console.error("Error:", err.message);
});
