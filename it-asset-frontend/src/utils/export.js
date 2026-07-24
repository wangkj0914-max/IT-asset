/**
 * 通用 CSV 导出工具
 * 用法: exportCSV('filename', columns, data)
 *   columns: [{ label:'资产编号', key:'assetCode' }, ...]
 */
export function exportCSV(filename, columns, data) {
  if (!data || data.length === 0) return
  const BOM = '\uFEFF'
  const header = columns.map(c => `"${c.label}"`).join(',')
  const rows = data.map(row =>
    columns.map(c => {
      let val = (row[c.key] ?? '')
      val = String(val).replace(/"/g, '""')
      return `"${val}"`
    }).join(',')
  )
  const csv = BOM + header + '\n' + rows.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = filename + '.csv'
  a.click(); URL.revokeObjectURL(url)
}
