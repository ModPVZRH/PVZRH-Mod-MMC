# 分类 / 标签接口文档

本文档覆盖分类、标签，以及模组与分类、标签的关联接口。

- 本地：`http://127.0.0.1:9091`
- 生产：`https://mod.ehre.top/api`

公共接口无需登录。管理接口需要在请求头携带登录 Token：

```http
token: <登录后返回的 token>
```

## 统一响应

```json
{
  "code": 0,
  "msg": null,
  "data": {}
}
```

| 字段 | 说明 |
| --- | --- |
| `code` | `0` 成功，非 `0` 失败 |
| `msg` | 失败时的错误信息 |
| `data` | 业务数据，无数据时为 `null` |

分页 `data` 结构：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "total": 20,
  "pages": 2,
  "list": []
}
```

---

## 一、公共接口（无需登录）

客户端、官网可直接调用。按分类查询模组时，只返回已发布（`isVisible = true`）的模组。

### 1. 查询分类列表

```http
GET /public/category
```

按 `sortOrder`、`id` 升序返回全部分类。

**响应示例**

```json
{
  "code": 0,
  "msg": null,
  "data": [
    {
      "id": "1",
      "name": "生存",
      "description": "生存类模组",
      "sortOrder": 0,
      "createdAt": "2026-09-18T12:00:00",
      "updatedAt": "2026-09-18T12:00:00"
    }
  ]
}
```

### 2. 查询标签列表

```http
GET /public/tag
```

返回全部标签，按 `id` 升序。

**响应示例**

```json
{
  "code": 0,
  "msg": null,
  "data": [
    {
      "id": "1",
      "name": "联机",
      "color": "#409EFF",
      "createdAt": "2026-09-18T12:00:00",
      "updatedAt": "2026-09-18T12:00:00"
    }
  ]
}
```

### 3. 按分类查询模组列表

两种写法效果相同：

```http
GET /public/mod/category/{categoryId}
GET /public/mod?categoryId={categoryId}
```

不传 `categoryId` 时，`GET /public/mod` 仍返回全部已发布模组。

**路径参数 / Query**

| 参数 | 位置 | 必填 | 说明 |
| --- | --- | --- | --- |
| `categoryId` | Path 或 Query | 是（按分类查时） | 分类 ID |

**说明**

- 只返回已发布模组
- 排序：推荐优先，再按更新时间倒序
- 分类不存在时：`code = 1`，`msg = "分类不存在"`

**响应示例**

```json
{
  "code": 0,
  "msg": null,
  "data": [
    {
      "id": "12",
      "modName": "示例模组",
      "englishName": "Example Mod",
      "authorId": "1",
      "authorName": "作者名",
      "categoryId": "1",
      "categoryName": "生存",
      "tagIds": ["1", "2"],
      "tags": [
        { "id": "1", "name": "联机", "color": "#409EFF" },
        { "id": "2", "name": "优化", "color": "#67C23A" }
      ],
      "modDescription": "介绍",
      "iconUrl": "https://example.com/icon.png",
      "version": "1.0.0",
      "isFeatured": true,
      "isVisible": true,
      "updatedAt": "2026-09-18T12:00:00"
    }
  ]
}
```

### 公共接口一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/public/category` | 分类列表 |
| GET | `/public/tag` | 标签列表 |
| GET | `/public/mod` | 全部已发布模组 |
| GET | `/public/mod?categoryId={id}` | 按分类查模组 |
| GET | `/public/mod/category/{categoryId}` | 按分类查模组 |

---

## 二、分类管理（需登录）

管理端「分类管理」使用这组接口。`GET /category/list` 登录即可调用，其余需要对应权限。

### 1. 分页查询

```http
POST /category/page
权限：business:category:get
Content-Type: application/json
```

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "name": "生存",
  "sortItemList": [
    { "column": "sort_order", "isAsc": true }
  ]
}
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `pageNum` | 是 | 页码 |
| `pageSize` | 是 | 每页条数 |
| `name` | 否 | 按名称模糊搜索 |
| `sortItemList` | 否 | 排序，`column` 为数据库字段名 |

### 2. 全量列表

```http
GET /category/list
```

登录即可。返回结构与 `GET /public/category` 相同，供模组表单下拉选择。

### 3. 查询详情

```http
GET /category/{id}
权限：business:category:get
```

### 4. 新增分类

```http
POST /category/add
权限：business:category:add
Content-Type: application/json
```

```json
{
  "name": "生存",
  "description": "生存类模组",
  "sortOrder": 0
}
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `name` | 是 | 分类名称，不能重复 |
| `description` | 否 | 描述 |
| `sortOrder` | 否 | 排序，默认 `0`，越小越靠前 |

成功时 `data` 为 `null`。名称重复返回 `分类名称已存在`。

### 5. 修改分类

```http
PUT /category/update
权限：business:category:upd
Content-Type: application/json
```

```json
{
  "id": "1",
  "name": "生存",
  "description": "生存类模组",
  "sortOrder": 1
}
```

### 6. 删除分类

```http
DELETE /category/{id}
权限：business:category:del
```

该分类下仍有模组时，返回 `该分类下仍有模组，无法删除`。

### 7. 批量删除

```http
POST /category/batchDelete
权限：business:category:del
Content-Type: application/json
```

```json
["1", "2"]
```

---

## 三、标签管理（需登录）

管理端「标签管理」使用这组接口。`GET /tag/list` 登录即可调用，其余需要对应权限。

模组只能从已有标签中选择，不能在给模组打标签时新建标签。

### 1. 分页查询

```http
POST /tag/page
权限：business:tag:get
Content-Type: application/json
```

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "name": "联机"
}
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `pageNum` | 是 | 页码 |
| `pageSize` | 是 | 每页条数 |
| `name` | 否 | 按名称模糊搜索 |

### 2. 全量列表

```http
GET /tag/list
```

登录即可。返回结构与 `GET /public/tag` 相同，供模组表单多选。

### 3. 查询详情

```http
GET /tag/{id}
权限：business:tag:get
```

### 4. 新增标签

```http
POST /tag/add
权限：business:tag:add
Content-Type: application/json
```

```json
{
  "name": "联机",
  "color": "#409EFF"
}
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `name` | 是 | 标签名称，不能重复 |
| `color` | 否 | 颜色，默认 `#409EFF` |

名称重复返回 `标签名称已存在`。

### 5. 修改标签

```http
PUT /tag/update
权限：business:tag:upd
Content-Type: application/json
```

```json
{
  "id": "1",
  "name": "联机",
  "color": "#67C23A"
}
```

### 6. 删除标签

```http
DELETE /tag/{id}
权限：business:tag:del
```

删除标签时会同时去掉模组上的该标签关联。

### 7. 批量删除

```http
POST /tag/batchDelete
权限：business:tag:del
Content-Type: application/json
```

```json
["1", "2"]
```

---

## 四、模组关联分类 / 标签

模组原有增删改查接口不变，新增以下字段。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `categoryId` | string | 分类 ID，一个模组只能选一个，可为空 |
| `tagIds` | string[] | 标签 ID 列表，可多选，必须是已有标签 |
| `categoryName` | string | 仅返回。分类名称 |
| `tags` | object[] | 仅返回。标签详情，含 `id` / `name` / `color` |

### 新增模组

```http
POST /mods/add
权限：business:mods:add
```

请求体在原有字段基础上增加：

```json
{
  "modName": "示例模组",
  "downloadDirectUrl": "https://example.com/mod.zip",
  "downloadCloudUrl": "https://example.com/cloud",
  "categoryId": "1",
  "tagIds": ["1", "2"]
}
```

- `categoryId` 不存在：`分类不存在`
- `tagIds` 中有无效 ID：`存在无效的标签，只能从已有标签中选择`

### 修改模组

```http
PUT /mods/update
权限：business:mods:upd
```

```json
{
  "id": "12",
  "categoryId": "1",
  "tagIds": ["1"]
}
```

- `categoryId` 传空字符串会清空分类
- `tagIds` 不传则不改标签；传 `[]` 会清空全部标签

### 管理端分页筛选

```http
POST /mods/page
权限：business:mods:get
```

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "modName": "示例",
  "categoryId": "1",
  "tagId": "2"
}
```

| 字段 | 说明 |
| --- | --- |
| `categoryId` | 按分类筛选 |
| `tagId` | 按单个标签筛选 |

列表和详情都会带回 `categoryId`、`categoryName`、`tagIds`、`tags`。

---

## 五、常见错误

| msg | 说明 |
| --- | --- |
| 分类名称不能为空 | 新增/修改分类时未填名称 |
| 分类名称已存在 | 分类名称重复 |
| 分类不存在 | 分类 ID 无效，或按分类查模组时分类不存在 |
| 该分类下仍有模组，无法删除 | 先把模组改到其他分类或清空分类 |
| 标签名称不能为空 | 新增/修改标签时未填名称 |
| 标签名称已存在 | 标签名称重复 |
| 存在无效的标签，只能从已有标签中选择 | 给模组绑定了不存在的标签 ID |
| 不存在该对象 | 记录不存在 |
| 无权限 | 非作者且非超级管理员编辑模组 |
