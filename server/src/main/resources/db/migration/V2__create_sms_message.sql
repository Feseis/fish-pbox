CREATE TABLE sms_message (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '消息 ID，轮询游标',
    sender_phone    VARCHAR(20)   NOT NULL COMMENT '发送方手机号',
    sender_api_code VARCHAR(64)   NULL COMMENT '特殊发送方 API 代码；NULL 表示普通用户',
    receiver_phone  VARCHAR(20)   NOT NULL COMMENT '接收方手机号',
    content         VARCHAR(1024) NOT NULL COMMENT '短信信息',
    message_type    VARCHAR(16)   NOT NULL COMMENT '消息类型：CODE 验证码，TEXT 普通短信',
    receiver_read   TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '接收方是否已读：0 否，1 是',
    read_at         DATETIME(3)   NULL COMMENT '已读时间',
    deleted_at      DATETIME(3)   NULL COMMENT '软删除时间；NULL 表示未删除',
    created_at      DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sms_receiver_id (receiver_phone, id),
    KEY idx_sms_sender_id (sender_phone, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='短信存储';
