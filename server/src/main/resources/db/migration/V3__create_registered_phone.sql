CREATE TABLE registered_phone (
    phone VARCHAR(20) NOT NULL COMMENT '已注册手机号',
    PRIMARY KEY (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='沙盒可用手机号';
