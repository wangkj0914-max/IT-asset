#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IT 固定资产管理系统 - 性能测试脚本
使用 Python + requests 进行接口性能测试
"""

import requests
import time
import statistics
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime

# 配置
BASE_URL = "http://localhost:8080/asset"
HEADERS = {"Content-Type": "application/json"}

# 测试结果存储
results = {
    "asset_list": [],
    "use_apply": [],
    "inbound_apply": []
}


def test_asset_list(thread_id):
    """测试资产列表查询接口"""
    try:
        start = time.time()
        response = requests.get(
            f"{BASE_URL}/assetInfo/list",
            params={"current": 1, "size": 10},
            timeout=30
        )
        elapsed = time.time() - start
        results["asset_list"].append({
            "thread": thread_id,
            "time": elapsed,
            "status": response.status_code,
            "success": response.status_code == 200
        })
        return elapsed, response.status_code == 200
    except Exception as e:
        results["asset_list"].append({
            "thread": thread_id,
            "time": -1,
            "status": 0,
            "success": False,
            "error": str(e)
        })
        return -1, False


def test_use_apply(thread_id):
    """测试资产领用申请接口"""
    try:
        data = {
            "assetId": 1,
            "department": "技术部",
            "contactPerson": f"测试用户{thread_id}",
            "contactPhone": "13800138000",
            "remark": f"性能测试 -{thread_id}"
        }
        start = time.time()
        response = requests.post(
            f"{BASE_URL}/use/apply",
            json=data,
            headers=HEADERS,
            timeout=30
        )
        elapsed = time.time() - start
        results["use_apply"].append({
            "thread": thread_id,
            "time": elapsed,
            "status": response.status_code,
            "success": response.status_code == 200
        })
        return elapsed, response.status_code == 200
    except Exception as e:
        results["use_apply"].append({
            "thread": thread_id,
            "time": -1,
            "status": 0,
            "success": False,
            "error": str(e)
        })
        return -1, False


def test_inbound_apply(thread_id):
    """测试资产入库申请接口"""
    try:
        data = {
            "assetName": f"测试电脑{thread_id}",
            "categoryId": 1,
            "brand": "联想",
            "model": "ThinkPad X1",
            "serialNumber": f"SN2026{thread_id:04d}",
            "purchasePrice": 8999.00,
            "storageLocation": "A 区 -01",
            "remark": f"性能测试 -{thread_id}"
        }
        start = time.time()
        response = requests.post(
            f"{BASE_URL}/inbound/apply",
            json=data,
            headers=HEADERS,
            timeout=30
        )
        elapsed = time.time() - start
        results["inbound_apply"].append({
            "thread": thread_id,
            "time": elapsed,
            "status": response.status_code,
            "success": response.status_code == 200
        })
        return elapsed, response.status_code == 200
    except Exception as e:
        results["inbound_apply"].append({
            "thread": thread_id,
            "time": -1,
            "status": 0,
            "success": False,
            "error": str(e)
        })
        return -1, False


def run_test(test_func, test_name, concurrency):
    """运行性能测试"""
    print(f"\n{'='*60}")
    print(f"开始测试：{test_name}")
    print(f"并发用户数：{concurrency}")
    print(f"开始时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print('='*60)
    
    start_time = time.time()
    success_count = 0
    fail_count = 0
    response_times = []
    
    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(test_func, i) for i in range(concurrency)]
        for future in as_completed(futures):
            elapsed, success = future.result()
            if success:
                success_count += 1
                if elapsed > 0:
                    response_times.append(elapsed)
            else:
                fail_count += 1
    
    total_time = time.time() - start_time
    
    # 统计结果
    if response_times:
        avg_time = statistics.mean(response_times) * 1000  # 转换为毫秒
        min_time = min(response_times) * 1000
        max_time = max(response_times) * 1000
        p90_time = statistics.quantiles(response_times, n=10)[8] * 1000 if len(response_times) >= 10 else max_time
    else:
        avg_time = min_time = max_time = p90_time = 0
    
    tps = success_count / total_time if total_time > 0 else 0
    
    # 打印结果
    print(f"\n{'='*60}")
    print(f"测试完成：{test_name}")
    print(f"结束时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"总耗时：{total_time:.2f}秒")
    print('='*60)
    print(f"请求总数：{concurrency}")
    print(f"成功数：{success_count}")
    print(f"失败数：{fail_count}")
    print(f"成功率：{success_count/concurrency*100:.2f}%")
    print('-'*60)
    print(f"平均响应时间：{avg_time:.2f}ms")
    print(f"最小响应时间：{min_time:.2f}ms")
    print(f"最大响应时间：{max_time:.2f}ms")
    print(f"90% 响应时间：{p90_time:.2f}ms")
    print('-'*60)
    print(f"吞吐量 (TPS): {tps:.2f} req/s")
    print('='*60)
    
    return {
        "test_name": test_name,
        "concurrency": concurrency,
        "total": concurrency,
        "success": success_count,
        "fail": fail_count,
        "success_rate": success_count/concurrency*100,
        "avg_time": avg_time,
        "min_time": min_time,
        "max_time": max_time,
        "p90_time": p90_time,
        "tps": tps,
        "total_time": total_time
    }


def main():
    """主测试函数"""
    print("\n" + "="*60)
    print("IT 固定资产管理系统 - 性能测试")
    print("="*60)
    print(f"测试服务器：{BASE_URL}")
    print(f"测试时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    all_results = []
    
    # 测试 1: 资产列表查询 (100 并发)
    result1 = run_test(test_asset_list, "资产列表查询接口", 100)
    all_results.append(result1)
    
    # 测试 2: 资产领用申请 (50 并发)
    result2 = run_test(test_use_apply, "资产领用申请接口", 50)
    all_results.append(result2)
    
    # 测试 3: 资产入库申请 (30 并发)
    result3 = run_test(test_inbound_apply, "资产入库申请接口", 30)
    all_results.append(result3)
    
    # 汇总报告
    print("\n" + "="*80)
    print(" " * 30 + "性能测试汇总报告")
    print("="*80)
    print(f"{'测试接口':<20} {'并发数':>8} {'成功率':>10} {'平均响应 (ms)':>15} {'TPS':>10}")
    print("-"*80)
    for r in all_results:
        print(f"{r['test_name']:<20} {r['concurrency']:>8} {r['success_rate']:>9.2f}% "
              f"{r['avg_time']:>15.2f} {r['tps']:>10.2f}")
    print("="*80)
    
    # 结论
    print("\n测试结论:")
    avg_success_rate = sum(r['success_rate'] for r in all_results) / len(all_results)
    avg_response_time = sum(r['avg_time'] for r in all_results) / len(all_results)
    
    if avg_success_rate >= 99 and avg_response_time <= 2000:
        print("✅ 性能测试通过 - 系统性能符合预期")
    else:
        print("⚠️  性能测试未完全达标 - 建议优化")
    
    print(f"\n详细报告已保存至：performance_test_report.md")
    
    # 保存报告
    save_report(all_results)


def save_report(all_results):
    """保存测试报告"""
    report = f"""# IT 固定资产管理系统 - 性能测试报告

**测试时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}  
**测试服务器**: {BASE_URL}

## 测试结果汇总

| 测试接口 | 并发数 | 成功率 | 平均响应 (ms) | 最小响应 (ms) | 最大响应 (ms) | TPS |
|----------|--------|--------|---------------|---------------|---------------|-----|
"""
    for r in all_results:
        report += f"| {r['test_name']} | {r['concurrency']} | {r['success_rate']:.2f}% | {r['avg_time']:.2f} | {r['min_time']:.2f} | {r['max_time']:.2f} | {r['tps']:.2f} |\n"
    
    report += f"""
## 测试结论

- **平均成功率**: {sum(r['success_rate'] for r in all_results) / len(all_results):.2f}%
- **平均响应时间**: {sum(r['avg_time'] for r in all_results) / len(all_results):.2f}ms
- **总吞吐量**: {sum(r['tps'] for r in all_results):.2f} req/s

✅ 系统性能测试完成
"""
    
    with open("performance_test_report.md", "w", encoding="utf-8") as f:
        f.write(report)
    
    print("报告已保存至 performance_test_report.md")


if __name__ == "__main__":
    main()
