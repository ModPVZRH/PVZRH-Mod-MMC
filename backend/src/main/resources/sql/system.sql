-- 菜单表
CREATE TABLE `t_menu`
(
    `menu_id`        BIGINT      NOT NULL COMMENT '菜单ID',
    `menu_name`      VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `parent_id`      BIGINT NULL DEFAULT 0 COMMENT '父菜单ID',
    `type`           INT NULL DEFAULT NULL COMMENT '权限类型',
    `code`           VARCHAR(50)  DEFAULT NULL COMMENT '权限字符串',
    `uri`            varchar(255) DEFAULT NULL COMMENT '路由地址',
    `component_path` varchar(255) DEFAULT NULL COMMENT '组件路径',
    `icon`           varchar(100) DEFAULT NULL COMMENT '图标',
    `priority`       INT          DEFAULT 100 COMMENT '排序优先级',
    `create_time`    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`    VARCHAR(50) NULL DEFAULT NULL COMMENT '创建用户',
    `update_user`    VARCHAR(50) NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`menu_id`)
) ENGINE = InnoDB COMMENT = '菜单表';

-- 角色表
CREATE TABLE `t_role`
(
    `role_id`     BIGINT      NOT NULL COMMENT '角色ID',
    `role_name`   VARCHAR(50) NOT NULL COMMENT '角色名称',
    `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建用户',
    `update_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`role_id`)
) ENGINE = InnoDB COMMENT = '角色表';

-- 角色菜单关系表
CREATE TABLE `t_role_menu`
(
    `id`          BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键ID',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID',
    `menu_id`     BIGINT NOT NULL COMMENT '菜单ID',
    `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建用户',
    `update_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '角色菜单关系表';

-- 用户角色关系表
CREATE TABLE `t_user_role`
(
    `id`          BIGINT AUTO_INCREMENT NOT NULL COMMENT '主键ID',
    `user_id`     BIGINT NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID',
    `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建用户',
    `update_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '用户角色关系表';

-- 用户表
CREATE TABLE `t_user`
(
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `nickname`    VARCHAR(50)  NOT NULL COMMENT '昵称',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码',
    `gender`      TINYINT DEFAULT 0 COMMENT '性别',
    `avatar`      VARCHAR(255) NULL DEFAULT NULL COMMENT '头像',
    `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建用户',
    `update_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 文件表
CREATE TABLE `t_file`
(
    `file_id`     BIGINT    NOT NULL COMMENT '文件ID',
    `folder_type` TINYINT   NOT NULL COMMENT '文件夹类型',
    `file_name`   VARCHAR(100) NULL DEFAULT NULL COMMENT '文件名称',
    `file_size`   INT NULL DEFAULT NULL COMMENT '文件大小',
    `file_type`   VARCHAR(50) NULL DEFAULT NULL COMMENT '文件类型',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建用户',
    `update_user` VARCHAR(50) NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';


INSERT INTO `t_user` (`user_id`, `username`, `nickname`, `password`, `gender`, `avatar`)
VALUES (1, 'cengxuyuan', '曾续缘', '$2a$10$Opn9c/2NRAGk9h9toOrX4.ibSlLdGi6l057mtvD9fi1p4LXfb2BSm', 1,
        'http://127.0.0.1:9091/default/avatar.png');
INSERT INTO `t_role` (`role_id`, `role_name`)
VALUES (1, '管理员');
INSERT INTO `t_user_role` (`user_id`, `role_id`)
VALUES (1, 1);

-- 系统设置：是否允许自行注册。已有库重复执行不会覆盖已保存的开关。
CREATE TABLE IF NOT EXISTS system_setting (
    id INT NOT NULL COMMENT '固定为 1',
    register_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否开启注册',
    update_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置';

INSERT INTO system_setting (id, register_enabled)
SELECT 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_setting WHERE id = 1);


INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `uri`, `component_path`, `icon`, `priority`)
VALUES (1, '系统管理', 0, 1, '/system', NULL, 'Location', 1),
       (2, '用户管理', 1, 2, '/system/user', '/system/user/user-list.vue', 'Location', 1),
       (3, '角色管理', 1, 2, '/system/role', '/system/role/role-tree.vue', 'Location', 2),
       (4, '权限管理', 1, 2, '/system/menu', '/system/menu/menu-list.vue', 'Location', 3),
       (5, '文件管理', 1, 2, '/system/file', '/system/file/file-list.vue', 'Location', 4);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `code`, `icon`, `priority`)
VALUES (6, '查询', 2, 3, 'system:user:get', 'Place', 1),
       (7, '新增', 2, 3, 'system:user:add', 'Place', 2),
       (8, '修改', 2, 3, 'system:user:upd', 'Place', 3),
       (9, '删除', 2, 3, 'system:user:del', 'Place', 4);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `code`, `icon`, `priority`)
VALUES (10, '查询', 3, 3, 'system:role:get', 'Place', 1),
       (11, '新增', 3, 3, 'system:role:add', 'Place', 2),
       (12, '修改', 3, 3, 'system:role:upd', 'Place', 3),
       (13, '删除', 3, 3, 'system:role:del', 'Place', 4);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `code`, `icon`, `priority`)
VALUES (14, '查询', 4, 3, 'system:menu:get', 'Place', 1),
       (15, '新增', 4, 3, 'system:menu:add', 'Place', 2),
       (16, '修改', 4, 3, 'system:menu:upd', 'Place', 3),
       (17, '删除', 4, 3, 'system:menu:del', 'Place', 4);

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `code`, `icon`, `priority`)
VALUES (18, '查询', 5, 3, 'system:file:get', 'Place', 1),
       (19, '新增', 5, 3, 'system:file:add', 'Place', 2),
       (20, '修改', 5, 3, 'system:file:upd', 'Place', 3),
       (21, '删除', 5, 3, 'system:file:del', 'Place', 4),
       (22, '上传', 5, 3, 'system:file:upload', 'Place', 5);


INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 5),
    (1, 6),
    (1, 7),
    (1, 8),
    (1, 9),
    (1, 10),
    (1, 11),
    (1, 12),
    (1, 13),
    (1, 14),
    (1, 15),
    (1, 16),
    (1, 17),
    (1, 18),
    (1, 19),
    (1, 20),
    (1, 21),
    (1, 22);
