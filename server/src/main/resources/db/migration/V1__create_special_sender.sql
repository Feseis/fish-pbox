CREATE TABLE special_sender (
    phone       VARCHAR(20) NOT NULL COMMENT '特殊手机号',
    sender_name VARCHAR(64) NOT NULL COMMENT '发送方名称',
    api_code    VARCHAR(64) NOT NULL COMMENT 'API 代码',
    PRIMARY KEY (api_code),
    UNIQUE KEY uk_special_sender_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='特殊发送方名单';
