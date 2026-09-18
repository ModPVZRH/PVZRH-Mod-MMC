CREATE TABLE mods (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    mod_name VARCHAR(100) NOT NULL COMMENT 'Mod名称',
    english_name VARCHAR(100) COMMENT 'Mod英文名',
    author_id INT COMMENT '作者ID',
    mod_description TEXT COMMENT 'Mod介绍',
    icon_url VARCHAR(500) COMMENT '模组图标',
    category_id INT COMMENT '分类ID',
    game_name VARCHAR(100) COMMENT '支持游戏',
    supported_versions VARCHAR(100) COMMENT '支持版本',
    framework_name VARCHAR(100) COMMENT 'Mod框架',
    download_direct_url VARCHAR(500) NOT NULL COMMENT '直链下载地址',
    download_cloud_url VARCHAR(500) NOT NULL COMMENT '网盘下载地址',
    version VARCHAR(50) COMMENT 'Mod版本',
    file_size BIGINT COMMENT '文件大小(',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    is_approved BOOLEAN DEFAULT FALSE COMMENT '是否通过审核',
    is_modpack BOOLEAN DEFAULT FALSE COMMENT '是否整合包',
    is_featured BOOLEAN DEFAULT FALSE COMMENT '是否推荐',
    is_visible BOOLEAN DEFAULT TRUE COMMENT '是否可见',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_author (author_id),
    INDEX idx_category (category_id),
    INDEX idx_game (game_id),
    INDEX idx_framework (framework_id),
    INDEX idx_approved (is_approved),
    INDEX idx_visible (is_visible),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 已有数据库执行：
-- ALTER TABLE mods ADD COLUMN is_modpack BOOLEAN DEFAULT FALSE COMMENT '是否整合包';
-- ALTER TABLE mods ADD COLUMN icon_url VARCHAR(500) COMMENT '模组图标';
-- ALTER TABLE mods ADD COLUMN category_id INT COMMENT '分类ID';
-- ALTER TABLE mods ADD INDEX idx_category (category_id);

CREATE TABLE IF NOT EXISTS mod_category (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模组分类';

CREATE TABLE IF NOT EXISTS mod_tag (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模组标签';

CREATE TABLE IF NOT EXISTS mods_tag (
    mod_id INT NOT NULL COMMENT '模组ID',
    tag_id INT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (mod_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模组标签关联';