-- 
INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `uri`, `component_path`, `code`, `icon`, `priority`)
VALUES (973100929905920, '管理', 0, 1, '/business/mods', '/business/mods/mods-list.vue', NULL, 'Location', 100),
       (973100929905921, '查询',973100929905920, 3, NULL, NULL, 'business:mods:get', 'Place', 1),
       (973100929905922, '新增',973100929905920, 3, NULL, NULL, 'business:mods:add', 'Place', 2),
       (973100929905923, '修改',973100929905920, 3, NULL, NULL, 'business:mods:upd', 'Place', 3),
       (973100929905924, '删除',973100929905920, 3, NULL, NULL, 'business:mods:del', 'Place', 4);

INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
VALUES (1, 973100929905920 ),
       (1, 973100929905921 ),
       (1, 973100929905922 ),
       (1, 973100929905923 ),
       (1, 973100929905924 );

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `uri`, `component_path`, `code`, `icon`, `priority`)
VALUES (973100929905930, '分类管理', 0, 1, '/business/category', '/business/category/category-list.vue', NULL, 'Folder', 101),
       (973100929905931, '查询',973100929905930, 3, NULL, NULL, 'business:category:get', 'Place', 1),
       (973100929905932, '新增',973100929905930, 3, NULL, NULL, 'business:category:add', 'Place', 2),
       (973100929905933, '修改',973100929905930, 3, NULL, NULL, 'business:category:upd', 'Place', 3),
       (973100929905934, '删除',973100929905930, 3, NULL, NULL, 'business:category:del', 'Place', 4);

INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
VALUES (1, 973100929905930 ),
       (1, 973100929905931 ),
       (1, 973100929905932 ),
       (1, 973100929905933 ),
       (1, 973100929905934 );

INSERT INTO `t_menu` (`menu_id`, `menu_name`, `parent_id`, `type`, `uri`, `component_path`, `code`, `icon`, `priority`)
VALUES (973100929905940, '标签管理', 0, 1, '/business/tag', '/business/tag/tag-list.vue', NULL, 'PriceTag', 102),
       (973100929905941, '查询',973100929905940, 3, NULL, NULL, 'business:tag:get', 'Place', 1),
       (973100929905942, '新增',973100929905940, 3, NULL, NULL, 'business:tag:add', 'Place', 2),
       (973100929905943, '修改',973100929905940, 3, NULL, NULL, 'business:tag:upd', 'Place', 3),
       (973100929905944, '删除',973100929905940, 3, NULL, NULL, 'business:tag:del', 'Place', 4);

INSERT INTO `t_role_menu` (`role_id`, `menu_id`)
VALUES (1, 973100929905940 ),
       (1, 973100929905941 ),
       (1, 973100929905942 ),
       (1, 973100929905943 ),
       (1, 973100929905944 );
-- -----------------------------------------------------------------

