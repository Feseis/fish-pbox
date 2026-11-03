UPDATE sms_message SET message_type = 'USER' WHERE message_type = 'TEXT';
UPDATE sms_message SET message_type = 'API' WHERE message_type = 'CODE';

ALTER TABLE sms_message
    DROP COLUMN sender_api_code,
    DROP COLUMN receiver_read;

ALTER TABLE sms_message
    MODIFY COLUMN message_type VARCHAR(16) NOT NULL COMMENT '消息类型：USER 手机号用户发送，API 特殊发送方接口发送';
