@echo off
chcp 65001 >nul
echo ========================================
echo    IT 固定资产管理系统 - 快速启动
echo ========================================
echo.

:: 检查 Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java，请先安装 JDK 17+
    pause
    exit /b 1
)
echo [✓] Java 已安装

:: 检查 Node.js
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js 16+
    pause
    exit /b 1
)
echo [✓] Node.js 已安装

echo.
echo ========================================
echo    第一步：启动后端服务
echo ========================================
echo.

cd /d "%~dp0it-asset-system"

if exist "target\*.jar" (
    echo [信息] 检测到已打包的 jar 文件，直接运行...
    start "后端服务" java -jar target\it-asset-system-0.0.1-SNAPSHOT.jar
) else (
    echo [信息] 使用 Maven 启动开发模式...
    start "后端服务" cmd /k "call mvnw.cmd spring-boot:run"
)

echo [✓] 后端服务已启动（端口：8080）
echo.
echo 等待 5 秒后启动前端...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo    第二步：启动前端服务
echo ========================================
echo.

cd /d "%~dp0it-asset-frontend"

if not exist "node_modules" (
    echo [信息] 首次运行，正在安装依赖...
    call npm install
)

echo [信息] 启动前端开发服务器...
start "前端服务" cmd /k "call npm run serve"

echo.
echo ========================================
echo    启动完成！
echo ========================================
echo.
echo 后端地址：http://localhost:8080/asset
echo 前端地址：http://localhost:8081
echo.
echo 默认账号：admin / 123456
echo.
echo 按任意键打开浏览器...
pause >nul

start http://localhost:8081
