# 本地启动

状态：有效（服务端接口与 App 均已实现）
用途：从零启动 server 与手机端；跑不起来时读。
更新时间：2026-11-02

## 1 前置

- JDK 21、MySQL 8；Android Studio（SDK 36）。
- 手机与电脑在同一局域网，关闭路由器 AP 隔离、访客网络隔离。
- 命令在仓库根目录执行，使用自带 wrapper。

## 2 建库

```powershell
mysql -u root -p < sql\001-create-database.sql
```

只建 `pbox` 库，表由 Flyway 在启动时创建。

## 3 环境变量

环境变量放在 `a_run_env/pbox.env`，模板已提供：

```powershell
New-Item -ItemType Directory -Force a_run_env
Copy-Item a_run_env_example\pbox.env a_run_env\pbox.env
```

```text
PBOX_DB_URL=jdbc:mysql://localhost:3306/pbox?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
PBOX_DB_USERNAME=root
PBOX_DB_PASSWORD=******
```

## 4 启动 server

```powershell
.\gradlew :server:bootRun
```

日志只打印 Wi-Fi 网卡的地址，形如 `pbox server listening on hostname: http://192.168.x.x:8080  [WLAN]`；没有输出说明未连 Wi-Fi。虚拟网卡（Hyper-V/WSL/Docker）不会被打印。
启动后浏览器打开 `http://{ip}:8080/`，顶部用「手机号 / 特殊发送方」两个页签切换模块；手机号页可注册/查看/删除沙盒手机号，发送方页可注册、查看列表、复制 API 代码、修改名称、删除发送方。

## 5 手机端

- 先在网页端「注册手机号」登记 App 要用的手机号（例如 `13606666666`、`18358166666`），否则连接会被拒绝。
- 从 GitHub Releases 下载 `phonebox-<版本>.apk` 并安装；先用手机浏览器访问 `http://{ip}:8080/sms/health`，确认可达。
- 打开 App，填入 `{ip}:8080` 与自己的手机号，点「连接」；通过后自动进入主界面，配置会记住。
- 「记录」页每秒拉取一次短信，点击条目标已读，右上「全部已读」批量处理，条目右下「删除」软删除。
- 「发送」页填接收方手机号与内容即可投递；可发给自己验证闭环。只落库，不发真实短信。
- 顶部「更换」可回到配置页，改地址或手机号后重新校验。

## 6 验收

```powershell
curl -X POST http://{ip}:8080/sms/phones -H "Content-Type: application/json" -d '{"phone":"13900139000"}'
curl http://{ip}:8080/sms/phones
curl -X DELETE "http://{ip}:8080/sms/phones?phone=13900139000"
curl -X POST http://{ip}:8080/sms/senders -H "Content-Type: application/json" -d '{"phone":"13800138000","senderName":"测试发送方"}'
curl -X PUT http://{ip}:8080/sms/senders -H "Content-Type: application/json" -d '{"phone":"13800138000","senderName":"新名称"}'
curl http://{ip}:8080/sms/senders
curl -X DELETE "http://{ip}:8080/sms/senders?phone=13800138000"
curl -X POST http://{ip}:8080/sms/send -H "Content-Type: application/json" -d '{"senderPhone":"13800138000","apiCode":"ps_xxxxxxxx","receiverPhone":"13900139000","content":"123456","messageType":"CODE"}'
curl "http://{ip}:8080/sms/messages?receiverPhone=13900139000&afterId=0"
curl -X POST http://{ip}:8080/sms/messages/read -H "Content-Type: application/json" -d '{"receiverPhone":"13900139000","lastId":12}'
curl -X DELETE "http://{ip}:8080/sms/messages/12?receiverPhone=13900139000"
curl http://{ip}:8080/sms/health
```

注册接口返回 200 与 `apiCode`；同一手机号重复注册返回 200 与首次登记的记录，不再报 409。手机号注册重复调用同样返回 200 且只保留一行；格式不符返回 400，删除未登记号码返回 404。修改名称用 `PUT`，手机号未登记返回 404。管理页只做沙盒手机号与特殊发送方管理；发送短信与短信记录在 App 内。普通用户投递可省略 `apiCode`，特殊发送方填注册返回的代码。第二次轮询不应重复返回同一条记录。

## 7 排障

- 手机打不开页面 → 检查 Windows 防火墙是否放行 8080、是否同一网段、路由器是否开启 AP 隔离。
- 启动日志没有局域网 IP → 多网卡会列出全部候选地址，手机逐个尝试。
- 手机报 `CLEARTEXT communication not permitted` → 配置 network security config 或临时启用 `usesCleartextTraffic`。
- 拉不到消息 → 先核对投递与轮询的 `receiverPhone` 完全一致，再检查 `afterId` 是否已推进过头。
- 启动报数据库占位符未解析 → 进程 cwd 不是仓库根，或 env 文件名/键名不符。
