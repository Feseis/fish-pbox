# pbox-server 接口契约

状态：已实现（手机号、特殊发送方、短信投递/轮询/已读/软删除、健康检查均已可用）
用途：第三方投递与手机端轮询的唯一对接标准；改接口前读，冲突时以此为准。
更新时间：2026-11-02

## 0 通用

- Base URL：`http://{server-lan-ip}:8080`；手机与 server 必须同一局域网。
- 成功：HTTP 200 + 裸 DTO；失败：`application/problem+json`（RFC 9457），含 `code`、`timestamp`。
- 时间格式 ISO-8601，时区 `Asia/Shanghai`。
- 首版无鉴权，见 README 的「已知限制」。

## 1 投递短信（第三方 → server）

`POST /sms/send`

请求：

```json
{
  "senderPhone": "13800138000",
  "apiCode": "ps_xxxxxxxx",
  "receiverPhone": "13900139000",
  "content": "123456",
  "messageType": "CODE"
}
```

响应（200）：

```json
{
  "id": 12,
  "senderPhone": "13800138000",
  "senderName": "测试发送方",
  "receiverPhone": "13900139000",
  "content": "123456",
  "messageType": "CODE",
  "receiverRead": false,
  "createdAt": "2026-11-02T12:00:00+08:00"
}
```

- `senderPhone`、`receiverPhone` 非空，≤20；`content` 非空，≤1024；`messageType` 取 `CODE`（验证码）或 `TEXT`（普通短信）。
- `apiCode` 可选。有值时查 `special_sender`：不存在或与 `senderPhone` 不匹配返回 403 `INVALID_API_CODE`；匹配则记录 `sender_api_code` 并返回 `senderName`。
- 无 `apiCode` 时 `sender_api_code` 存 `NULL`，`senderName` 为 `null`，按普通用户处理。
- 只落库，不触发真实短信下发；参数不合法返回 400 `INVALID_ARGUMENT`。

## 2 注册特殊发送方

`POST /sms/senders`

请求：

```json
{ "phone": "13800138000", "senderName": "测试发送方" }
```

响应（200）：

```json
{
  "phone": "13800138000",
  "senderName": "测试发送方",
  "apiCode": "ps_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
}
```

- `phone` 非空，≤20 字符，登记后唯一；`senderName` 非空，≤64 字符。
- 服务端生成 `apiCode`：`ps_` + 24 字节随机数的 Base64URL 串，共 35 字符。
- 入库三字段后返回；同一手机号重复注册：200，返回首次登记的 `senderName` 与 `apiCode`，不重复插入。
- `apiCode` 供 `/sms/send` 识别特殊发送方。

### 2.1 修改发送方名称

`PUT /sms/senders`

请求：

```json
{ "phone": "13800138000", "senderName": "新名称" }
```

响应（200）：

```json
{
  "phone": "13800138000",
  "senderName": "新名称",
  "apiCode": "ps_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
}
```

- `phone` 必须已登记；不存在返回 404 `SENDER_NOT_FOUND`。
- 只更新 `sender_name`，`api_code` 不变；重复调用结果一致。

### 2.2 查询发送方列表

`GET /sms/senders`

响应（200）：

```json
[
  {
    "phone": "13800138000",
    "senderName": "测试发送方",
    "apiCode": "ps_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  }
]
```

- 按手机号升序返回全部特殊发送方，不分页。
- `apiCode` 是发送方凭据，仅管理端展示与复制，不应下发给手机端。

### 2.3 删除发送方

`DELETE /sms/senders?phone={phone}`

响应（200）：

```json
{ "deleted": true }
```

- 直接从 `special_sender` 表物理删除该手机号记录，不可恢复。
- `phone` 为空返回 400 `INVALID_ARGUMENT`；手机号未登记返回 404 `SENDER_NOT_FOUND`。
- 删除后该 API 代码立即失效；历史短信记录仍保留，但其 `senderName` 不再可解析（返回 `null`）。

## 3 拉取短信（手机 → server）

`GET /sms/messages?receiverPhone={phone}&afterId={id}`

- `afterId` 可省略，默认 0；只返回 `id > afterId` 且 `deleted_at IS NULL` 的记录，按 `id` 升序。
- 建议每 1 秒调用一次；可选 `limit`，默认 20、最大 100。
- 无新消息时 `messages` 为空数组，`lastId` 等于请求的 `afterId`。
- 返回 `senderName` 由 `special_sender` 关联得到，不返回 `sender_api_code`。

响应（200）：

```json
{
  "messages": [
    {
      "id": 12,
      "senderPhone": "13800138000",
      "senderName": "测试发送方",
      "receiverPhone": "13900139000",
      "content": "123456",
      "messageType": "CODE",
      "receiverRead": false,
      "readAt": null,
      "createdAt": "2026-11-02T12:00:00+08:00"
    }
  ],
  "lastId": 12
}
```

- 客户端收到后把 `lastId` 作为下次的 `afterId`；重复轮询不会重复返回旧记录。
- 普通用户消息的 `senderName` 为 `null`。

## 4 标记已读

`POST /sms/messages/read`

请求：

```json
{ "receiverPhone": "13900139000", "lastId": 12 }
```

- 将 `receiverPhone` 对应、`id <= lastId`、未读且未删除的消息置为已读，并写 `read_at`。
- 响应（200）：`{"updated": 3}`；重复调用幂等。

## 5 软删除

`DELETE /sms/messages/{id}?receiverPhone={phone}`

- 只写 `deleted_at`，不物理删除；轮询自动排除。
- 不存在、不属于该接收方或已删除：404 `MESSAGE_NOT_FOUND`。
- 响应（200）：`{"deleted": true}`。

## 6 健康检查

`GET /sms/health`

- 可用：200，`{"database":true}`。
- 数据库不可用：503，ProblemDetail，`code=DB_UNAVAILABLE`。

## 7 沙盒手机号

`POST /sms/phones`

请求：

```json
{ "phone": "13800138000" }
```

响应（200）：

```json
{ "phone": "13800138000" }
```

- `phone` 必须匹配 `^1[3-9]\d{9}$`，否则 400 `INVALID_ARGUMENT`。
- 已登记手机号重复注册返回 200 原手机号，不重复插入；并发唯一约束同样按成功处理。
- 只有登记过的手机号才允许在 App 中使用。

`GET /sms/phones`

响应（200）：

```json
["13800138000", "13900139000"]
```

- 按手机号升序返回全部，不分页。

`DELETE /sms/phones?phone={phone}`

响应（200）：

```json
{ "deleted": true }
```

- `phone` 格式非法返回 400 `INVALID_ARGUMENT`；未登记返回 404 `PHONE_NOT_FOUND`。
- 物理删除，删除后该手机号不再允许在 App 使用。

## 8 错误码

| code | HTTP | 场景 |
| --- | --- | --- |
| `INVALID_ARGUMENT` | 400 | 手机号/内容为空或超长，`messageType` 非法，`afterId` 非法 |
| `INVALID_API_CODE` | 403 | `apiCode` 不存在或与 `senderPhone` 不匹配 |
| `SENDER_ALREADY_EXISTS` | 409 | 并发重复登记时唯一约束兜底；正常重复请求返回 200 |
| `SENDER_NOT_FOUND` | 404 | 修改名称时手机号未登记 |
| `PHONE_NOT_FOUND` | 404 | 删除沙盒手机号时未登记 |
| `MESSAGE_NOT_FOUND` | 404 | 软删除时消息不存在、不属于该接收方或已删除 |
| `DB_UNAVAILABLE` | 503 | 数据库连接或查询失败 |
| `INTERNAL_ERROR` | 500 | 未预期异常 |

## 9 状态

| 接口 | 状态 |
| --- | --- |
| `GET /sms/senders` | 已实现 |
| `POST /sms/senders` | 已实现 |
| `PUT /sms/senders` | 已实现 |
| `DELETE /sms/senders` | 已实现 |
| `GET /sms/phones` | 已实现 |
| `POST /sms/phones` | 已实现 |
| `DELETE /sms/phones` | 已实现 |
| `sms_message` 表 | 已建 |
| `POST /sms/send` | 已实现 |
| `GET /sms/messages` | 已实现 |
| `POST /sms/messages/read` | 已实现 |
| `DELETE /sms/messages/{id}` | 已实现 |
| `GET /sms/health` | 已实现 |
