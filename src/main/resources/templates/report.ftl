<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Security Analysis Report</title>
  <style>
    /* Global reset */
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: "Segoe UI", Tahoma, sans-serif;
      background-color: #f4f6f8;
      color: #333;
      line-height: 1.6;
    }
    .header {
      background-color: #2c3e50;
      color: #ecf0f1;
      padding: 20px;
      text-align: center;
    }
    .header h1 {
      font-size: 28px;
      font-weight: 400;
    }
    .container {
      max-width: 1000px;
      margin: 30px auto;
      background-color: #ffffff;
      border-radius: 6px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      overflow: hidden;
    }
    .summary {
      padding: 20px;
      border-bottom: 1px solid #e1e4e8;
      font-size: 16px;
    }
    .summary span {
      font-weight: bold;
      color: #e74c3c;
    }
    .summary .no-issues {
      color: #27ae60;
    }
    table {
      width: 100%;
      border-collapse: collapse;
    }
    thead {
      background-color: #34495e;
    }
    th, td {
      padding: 12px 16px;
      text-align: left;
    }
    th {
      color: #ecf0f1;
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 0.03em;
    }
    tbody tr:nth-child(even) {
      background-color: #f9fafb;
    }
    tbody tr:hover {
      background-color: #eef1f5;
    }
    td {
      font-size: 14px;
      word-break: break-all;
    }
    .report-footer {
      background-color: #f4f6f8;
      padding: 10px 20px;
      font-size: 12px;
      color: #777;
      text-align: right;
      border-top: 1px solid #e1e4e8;
    }
  </style>
</head>
<body>
<div class="header">
  <h1>Security Analysis Report</h1>
</div>
<div class="container">
  <div class="summary">
      <#if findings?size == 0>
        <p class="no-issues">✔ No security issues detected.</p>
      <#else>
        <p>Total issues found: <span>${findings?size}</span></p>
      </#if>
    <p>Generated on ${.now?string("yyyy-MM-dd HH:mm:ss")}</p>
  </div>
  <table>
    <thead>
    <tr>
      <th>Rule ID</th>
      <th>File</th>
      <th>Line</th>
      <th>Message</th>
    </tr>
    </thead>
    <tbody>
    <#list findings as f>
      <tr>
        <td>${f.ruleId?html}</td>
        <td>${f.filePath?html}</td>
        <td>${f.line}</td>
        <td>${f.message?html}</td>
      </tr>
    </#list>
    </tbody>
  </table>
  <div class="report-footer">
    &copy; ${.now?string("yyyy")} Java Security Analyzer
  </div>
</div>
</body>
</html>
