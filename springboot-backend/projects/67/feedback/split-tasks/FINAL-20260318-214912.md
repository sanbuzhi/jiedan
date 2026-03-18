# 美妆小店轻量级后端管理系统V1.0 技术任务书(TASKS.md)

---

## 1. 项目技术规格
### 技术栈
- **后端**：Java 17 + Spring Boot 3.2.5 + MyBatis Plus 3.5.5 + Swagger 3.0.0 + JWT 0.11.5 + Hutool 5.8.26 + Redisson 3.25.2（用于分布式锁、库存锁定缓存）
- **前端**：Vue 3.4.27 + Element Plus 2.7.3 + Vite 5.2.11 + ECharts 5.5.0 + xlsx 0.18.5 + jspdf-autotable 3.5.31 + Axios 1.7.2 + Pinia 2.1.7
- **数据库**：MySQL 8.0.36 + Redis 7.2.4（热点商品、权限配置、JWT黑名单、库存锁定、流水号生成）
- **服务器**：Nginx 1.24.0 + Spring Boot内置Tomcat 10.1.20

### 项目结构规范
#### 后端项目结构（beauty-shop-manage）
```
beauty-shop-manage
├── beauty-shop-manage-common          # 公共模块
│   ├── src/main/java/com/beauty/common
│   │   ├── constant                   # 常量定义（前缀BEAUTY_、状态、支付方式等）
│   │   ├── enums                      # 枚举定义（BeautyPurchaseStatus、BeautyPayType、BeautyPermission等）
│   │   ├── exception                  # 全局异常（BeautyBusinessException、BeautyParamException、BeautyPermissionException）
│   │   ├── result                     # 统一响应结果（BeautyResult、BeautyPageResult、BeautyBatchImportResult）
│   │   ├── util                       # 工具类（AES256Util、SHA256Util、BeautyDateUtil、BeautyFileUtil、BeautySmsUtil、BeautyBarcodeUtil、BeautyNoGenerator）
│   │   └── annotation                 # 自定义注解（@BeautyPermission权限校验、@BeautyLog日志记录、@BeautySecondVerify二次验证）
│   └── src/main/resources
├── beauty-shop-manage-domain          # 领域模块
│   ├── src/main/java/com/beauty/domain
│   │   ├── entity                     # 数据库实体（带MyBatis Plus注解，如BeautyProduct、BeautyPurchase）
│   │   ├── vo                         # 前端展示对象（如BeautyProductListVO、BeautyPurchaseDetailVO）
│   │   ├── dto                        # 数据传输对象（请求DTO、查询DTO，如BeautyProductSaveDTO、BeautyPurchaseListQueryDTO）
│   │   └── mapper                     # MyBatis Plus Mapper接口（含XML补充文件，复杂查询放XML）
│   └── src/main/resources/mapper
├── beauty-shop-manage-service         # 业务逻辑模块
│   ├── src/main/java/com/beauty/service
│   │   ├── impl                       # 业务实现类
│   │   ├── purchase                   # 采购管理业务接口
│   │   ├── sale                       # 销售管理业务接口
│   │   ├── inventory                  # 库存管理业务接口
│   │   ├── user                       # 用户管理业务接口
│   │   ├── report                     # 流水报表业务接口
│   │   └── system                     # 系统设置业务接口
│   └── src/main/resources
├── beauty-shop-manage-controller      # 接口控制模块
│   ├── src/main/java/com/beauty/controller
│   │   ├── purchase                   # 采购管理接口
│   │   ├── sale                       # 销售管理接口
│   │   ├── inventory                  # 库存管理接口
│   │   ├── user                       # 用户管理接口
│   │   ├── report                     # 流水报表接口
│   │   ├── system                     # 系统设置接口
│   │   └── api                        # 通用前端对接接口（独立鉴权）
│   └── src/main/resources
├── beauty-shop-manage-config          # 配置模块
│   ├── src/main/java/com/beauty/config
│   │   ├── MyBatisPlusConfig.java     # 分页、自动填充、逻辑删除、乐观锁配置
│   │   ├── JwtConfig.java             # JWT密钥、有效期、黑名单配置
│   │   ├── SwaggerConfig.java         # 接口文档分组（管理端、前端API）配置
│   │   ├── RedissonConfig.java        # Redisson单机/哨兵配置
│   │   ├── FileUploadConfig.java      # 文件上传路径、格式、大小配置
│   │   └── SecurityConfig.java        # 跨域、JWT拦截器、权限拦截器配置
│   └── src/main/resources
└── beauty-shop-manage-app             # 启动模块
    ├── src/main/java/com/beauty/BeautyShopManageApplication.java
    └── src/main/resources
        ├── application.yml
        ├── application-dev.yml
        └── application-prod.yml
```

#### 前端项目结构（beauty-shop-manage-web）
```
beauty-shop-manage-web
├── public                              # 静态资源（favicon.ico、默认店铺logo、默认小票模板）
├── src
│   ├── api                             # 接口请求（统一封装前缀、响应拦截、错误处理、自动刷新Token）
│   │   ├── purchase.js
│   │   ├── sale.js
│   │   ├── inventory.js
│   │   ├── user.js
│   │   ├── report.js
│   │   ├── system.js
│   │   └── index.js                    # 全局拦截器配置
│   ├── assets                          # 内部静态资源（CSS变量、默认图标SVG）
│   ├── components                      # 公共组件
│   │   ├── BeautySearchBar.vue        # 美妆搜索栏（支持条码/名称/日期/多条件联动、扫码枪监听嵌入）
│   │   ├── BeautyTable.vue            # 美妆数据表格（支持分页、排序、多选、列设置、导出入口）
│   │   ├── BeautyExport.vue           # 美妆导出组件（支持Excel/PDF选择、预览、参数配置）
│   │   ├── BeautyBarcodeReader.vue    # 美妆扫码枪组件（全局键盘钩子监听、过滤非条码字符、防抖）
│   │   ├── BeautyPreviewPrint.vue     # 美妆预览打印组件（适配A4/POS-58小票格式、支持自定义样式）
│   │   ├── BeautySecondVerify.vue     # 美妆二次验证弹窗（支持短信验证码、倒计时、错误次数限制）
│   │   ├── BeautyTreeSelect.vue       # 美妆树状选择组件（商品类别、员工权限、支持多选/单选、搜索）
│   │   └── BeautyMemberSelect.vue     # 美妆会员选择组件（支持条码/手机号/卡号搜索、会员信息快速展示）
│   ├── layouts                         # 布局组件
│   │   ├── BeautyMainLayout.vue       # 美妆主布局（侧边栏权限过滤、顶部栏预警展示、面包屑、登出/个人中心）
│   │   └── BeautyLoginLayout.vue      # 美妆登录布局（账号密码登录、首次强制修改密码、响应式）
│   ├── router                          # 路由配置（懒加载、权限守卫、页面缓存）
│   │   └── index.js
│   ├── store                           # 状态管理（Pinia）
│   │   ├── modules
│   │   │   ├── user.js                 # 用户登录信息、权限列表、是否首次登录
│   │   │   └── beautyShop.js           # 店铺信息、基础参数配置（抹零规则、积分比例等）
│   │   └── index.js
│   ├── utils                           # 工具函数
│   │   ├── request.js                  # Axios封装（JWT自动添加、刷新Token、统一错误提示、超时处理）
│   │   ├── auth.js                     # JWT存储、获取、删除、刷新
│   │   ├── validate.js                 # 表单校验（手机号、价格、数量、条码、身份证号可选）
│   │   ├── common.js                   # 日期格式化、金额计算（精度处理）、条码生成、小票HTML生成
│   │   └── print.js                    # 小票打印逻辑（调用window.print、隐藏不需要的元素）
│   ├── views                           # 页面
│   │   ├── purchase                    # 采购管理
│   │   │   ├── PurchaseCreate.vue
│   │   │   ├── PurchaseList.vue
│   │   │   ├── PurchaseDraftList.vue
│   │   │   ├── PurchaseStockIn.vue
│   │   │   ├── PurchaseDetail.vue
│   │   │   └── SupplierList.vue
│   │   ├── sale                        # 销售管理
│   │   │   ├── RetailCreate.vue
│   │   │   ├── SaleList.vue
│   │   │   ├── OnlineOrderList.vue
│   │   │   ├── SaleDeliver.vue
│   │   │   ├── SaleReturn.vue
│   │   │   └── SaleDetail.vue
│   │   ├── inventory                   # 库存管理
│   │   │   ├── ProductList.vue
│   │   │   ├── CategoryList.vue
│   │   │   ├── LocationList.vue
│   │   │   ├── InventoryWarning.vue
│   │   │   ├── InventoryTransfer.vue
│   │   │   ├── TransferList.vue
│   │   │   ├── TransferDetail.vue
│   │   │   ├── InventoryCheckCreate.vue
│   │   │   ├── InventoryCheckList.vue
│   │   │   ├── InventoryCheckPhysical.vue
│   │   │   ├── InventoryCheckAudit.vue
│   │   │   └── InventoryCheckDetail.vue
│   │   ├── user                        # 用户管理
│   │   │   ├── StaffList.vue
│   │   │   ├── RoleList.vue
│   │   │   ├── MemberList.vue
│   │   │   ├── LevelList.vue
│   │   │   └── MemberDetail.vue
│   │   ├── report                      # 流水报表
│   │   │   ├── FinanceIncomeExpense.vue
│   │   │   ├── FinanceSupplierPayable.vue
│   │   │   ├── FinanceMemberAsset.vue
│   │   │   ├── BusinessSale.vue
│   │   │   ├── BusinessProductRanking.vue
│   │   │   └── BusinessInventoryCheck.vue
│   │   ├── system                      # 系统设置
│   │   │   ├── BasicParam.vue
│   │   │   ├── SecondVerify.vue
│   │   │   ├── ApiConfig.vue
│   │   │   ├── SmsConfig.vue
│   │   │   └── LogList.vue
│   │   ├── Login.vue
│   │   └── Index.vue
│   ├── App.vue
│   └── main.js
├── vite.config.js
├── package.json
├── .env.development
├── .env.production
└── README.md
```

### 代码生成规则
- **依赖版本**：严格遵循上述技术栈版本，禁止随意升级/降级，需在pom.xml/package.json中固定版本号
- **JWT配置**：
  - 签名算法：HS256
  - Token有效期：7天
  - Refresh Token有效期：30天
  - 黑名单缓存：Redis存储，过期时间=Token剩余有效期
  - 密钥：随机生成32位字符串，生产环境需配置在环境变量中，禁止硬编码
- **MyBatis Plus配置**：
  - 自动填充：create_time（插入时）、update_time（插入/更新时）、create_by（插入时，员工ID）、update_by（插入/更新时，员工ID）
  - 逻辑删除：deleted字段（0未删除，1已删除），全局逻辑删除值统一
  - 分页插件：最大单页500条记录，默认单页20条
  - 乐观锁：version字段，用于库存更新防止并发
  - 数据库类型：MySQL 8.0
- **数据库连接配置**：
  - 字符集：utf8mb4
  - 连接池：HikariCP
  - 最小空闲连接：5
  - 最大连接数：20
  - 连接超时：30000ms
  - 空闲超时：600000ms
  - 最大生命周期：1800000ms
- **文件上传配置**：
  - 允许格式：JPG、JPEG、PNG
  - 单张大小：商品图片≤2MB，等级图标/店铺logo≤1MB
  - 最多上传：商品5张，其他1张
  - 存储路径：本地存储可配置绝对路径，默认存储在项目根目录的upload文件夹下，文件命名规则：当前时间戳+32位随机数+原始文件后缀
  - 访问路径：通过Nginx反向代理访问，禁止直接访问本地文件系统
- **流水号生成规则**：使用Redisson分布式锁生成，格式为：业务前缀（2位）+8位年月日+4位流水号（每日从0001开始）

### 核心业务领域锚点
美妆小店轻量级后端管理系统

---

## 2. 前端页面开发清单
### 模块1：采购管理
| 页面路径 | 核心功能 | 必用组件 | 接口调用 | 跳转关系 |
|----------|----------|----------|----------|----------|
| /purchase/purchase-create | 创建采购单、保存草稿、自动计算小计/总金额、临时供应商保存提示 | BeautySearchBar、BeautyBarcodeReader、Element Plus Form/Table/Dialog/Select/NumberInput/Message | /api/beauty/purchase/save, /api/beauty/purchase/draft-save, /api/beauty/supplier/list, /api/beauty/product/list, /api/beauty/product/detail | 保存并提交跳转至/purchase/purchase-list，保存草稿跳转至/purchase/purchase-draft-list |
| /purchase/purchase-list | 查询采购单、导出采购单、查看详情、入库（待入库/部分入库）、打印 | BeautySearchBar、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus DatePicker/Select/Pagination | /api/beauty/purchase/list, /api/beauty/purchase/export, /api/beauty/supplier/list, /api/beauty/staff/list | 点击「入库」跳转至/purchase/purchase-stock-in，点击「详情」跳转至/purchase/purchase-detail，点击「打印」弹出BeautyPreviewPrint |
| /purchase/purchase-draft-list | 查询采购草稿、编辑/删除采购草稿、导出 | BeautySearchBar、BeautyTable、BeautyExport、Element Plus DatePicker/Select/Pagination/MessageBox | /api/beauty/purchase/draft-list, /api/beauty/purchase/draft-delete, /api/beauty/supplier/list, /api/beauty/staff/list | 点击「编辑」跳转至/purchase/purchase-create（带草稿数据） |
| /purchase/purchase-stock-in | 采购单入库、自动计算剩余待入库数量、应付账款全部入库提示 | BeautySearchBar、BeautyBarcodeReader、Element Plus Form/Table/Select/NumberInput/Message/Dialog | /api/beauty/purchase/detail, /api/beauty/purchase/stock-in, /api/beauty/location/list | 确认入库跳转至/purchase/purchase-list |
| /purchase/purchase-detail | 查看采购单详情、导出采购单、打印 | BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Dialog | /api/beauty/purchase/detail, /api/beauty/purchase/export-single | 无跳转 |
| /purchase/supplier-list | 供应商档案管理（增删改查）、供应商条码生成/扫描 | BeautySearchBar、BeautyBarcodeReader、BeautyTable、Element Plus Form/Dialog/Select/Pagination/MessageBox | /api/beauty/supplier/list, /api/beauty/supplier/save, /api/beauty/supplier/update, /api/beauty/supplier/delete, /api/beauty/category/list | 无跳转 |

### 模块2：销售管理
| 页面路径 | 核心功能 | 必用组件 | 接口调用 | 跳转关系 |
|----------|----------|----------|----------|----------|
| /sale/retail-create | 创建零售单、自动计算小计/总金额/会员折扣/抹零抹分、打印小票、积分/储值验证 | BeautySearchBar、BeautyBarcodeReader、BeautyMemberSelect、Element Plus Form/Table/Dialog/Select/NumberInput/Message/MessageBox | /api/beauty/sale/retail-create, /api/beauty/member/list, /api/beauty/member/detail, /api/beauty/product/list, /api/beauty/product/detail, /api/beauty/location/list | 确认收款弹出BeautyPreviewPrint，打印后可返回首页或继续开单 |
| /sale/sale-list | 查询销售单、导出销售单、查看详情、发货（线上待发货）、退货（已完成/已部分退货）、打印 | BeautySearchBar、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus DatePicker/Select/Pagination | /api/beauty/sale/list, /api/beauty/sale/export, /api/beauty/member/list, /api/beauty/product/list, /api/beauty/staff/list | 点击「发货」跳转至/sale/sale-deliver，点击「退货」跳转至/sale/sale-return，点击「详情」跳转至/sale/sale-detail |
| /sale/online-order-list | 查询线上订单、同步（可选手动刷新）、发货、取消、导出、打印 | BeautySearchBar、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus DatePicker/Select/Pagination/MessageBox | /api/beauty/sale/online-list, /api/beauty/sale/online-sync, /api/beauty/sale/online-deliver, /api/beauty/sale/online-cancel, /api/beauty/logistics/list | 点击「发货」跳转至/sale/sale-deliver，点击「取消」弹出取消原因输入框 |
| /sale/sale-deliver | 线上订单发货、临时物流添加 | Element Plus Form/Table/Select/Input/Message | /api/beauty/sale/detail, /api/beauty/sale/online-deliver, /api/beauty/logistics/list | 确认发货跳转至/sale/online-order-list |
| /sale/sale-return | 销售单退货、自动计算退款金额、原支付方式默认 | BeautySearchBar、BeautyBarcodeReader、Element Plus Form/Table/Select/NumberInput/Message/MessageBox | /api/beauty/sale/detail, /api/beauty/sale/return-create, /api/beauty/location/list | 确认退货跳转至/sale/sale-list |
| /sale/sale-detail | 查看销售单详情、导出销售单、打印 | BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Dialog | /api/beauty/sale/detail, /api/beauty/sale/export-single | 无跳转 |

### 模块3：库存管理
| 页面路径 | 核心功能 | 必用组件 | 接口调用 | 跳转关系 |
|----------|----------|----------|----------|----------|
| /inventory/product-list | 商品档案管理（增删改查、批量导入、批量导出、模板下载）、库存预警商品直接创建采购单、条码生成/扫描 | BeautySearchBar、BeautyBarcodeReader、BeautyTable、BeautyExport、Element Plus Form/Dialog/Select/Pagination/Upload/MessageBox/Button | /api/beauty/product/list, /api/beauty/product/save, /api/beauty/product/update, /api/beauty/product/delete, /api/beauty/product/batch-import, /api/beauty/product/template-download, /api/beauty/category/list, /api/beauty/location/list | 点击「创建采购单」跳转至/purchase/purchase-create（带预警商品明细） |
| /inventory/category-list | 商品类别管理（树状结构增删改查、拖拽排序） | Element Plus Tree/Form/Dialog/Select/MessageBox | /api/beauty/category/list, /api/beauty/category/save, /api/beauty/category/update, /api/beauty/category/delete | 无跳转 |
| /inventory/location-list | 库位档案管理（增删改查）、条码生成/扫描 | BeautySearchBar、BeautyBarcodeReader、BeautyTable、Element Plus Form/Dialog/Select/Pagination/MessageBox | /api/beauty/location/list, /api/beauty/location/save, /api/beauty/location/update, /api/beauty/location/delete | 无跳转 |
| /inventory/inventory-warning | 查看库存预警（不足/积压/过期分类切换）、导出预警商品、库存不足直接创建采购单 | BeautyTable、BeautyExport、Element Plus Tabs/Select/Pagination/Button | /api/beauty/inventory/warning-list, /api/beauty/inventory/warning-export | 点击「创建采购单」跳转至/purchase/purchase-create（带预警商品明细） |
| /inventory/inventory-transfer | 库存调拨、自动计算原库位剩余库存、条码扫描 | BeautySearchBar、BeautyBarcodeReader、Element Plus Form/Table/Select/NumberInput/Message | /api/beauty/product/list, /api/beauty/product/detail, /api/beauty/location/list, /api/beauty/inventory/transfer | 确认调拨跳转至/inventory/transfer-list |
| /inventory/transfer-list | 查询调拨单、导出调拨单、查看详情、打印 | BeautySearchBar、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus DatePicker/Select/Pagination | /api/beauty/inventory/transfer-list, /api/beauty/inventory/transfer-export, /api/beauty/staff/list | 点击「详情」跳转至/inventory/transfer-detail |
| /inventory/transfer-detail | 查看调拨单详情、导出调拨单、打印 | BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Dialog | /api/beauty/inventory/transfer-detail, /api/beauty/inventory/transfer-export-single | 无跳转 |
| /inventory/inventory-check-create | 创建盘点单、按类型生成盘点范围、条码扫描 | Element Plus Form/Table/Select/Pagination/Message/MessageBox | /api/beauty/category/list, /api/beauty/location/list, /api/beauty/product/list, /api/beauty/inventory/check-create | 确认创建跳转至/inventory/inventory-check-list |
| /inventory/inventory-check-list | 查询盘点单、取消盘点单、导出盘点单、实盘录入（待盘点/实盘中）、审核（待审核）、查看详情 | BeautySearchBar、BeautyTable、BeautyExport、Element Plus DatePicker/Select/Pagination/MessageBox | /api/beauty/inventory/check-list, /api/beauty/inventory/check-cancel, /api/beauty/inventory/check-export, /api/beauty/staff/list | 点击「实盘录入」跳转至/inventory/inventory-check-physical，点击「审核」跳转至/inventory/inventory-check-audit，点击「详情」跳转至/inventory/inventory-check-detail |
| /inventory/inventory-check-physical | 盘点单实盘录入、自动计算差异数量/金额、差异备注必填、条码扫描 | BeautySearchBar、BeautyBarcodeReader、Element Plus Form/Table/Select/NumberInput/Message/MessageBox | /api/beauty/inventory/check-detail, /api/beauty/inventory/check-physical-save, /api/beauty/inventory/check-physical-submit | 提交审核跳转至/inventory/inventory-check-list |
| /inventory/inventory-check-audit | 盘点单审核、审核备注驳回必填、盘盈盘亏查看 | Element Plus Form/Table/Select/Message/MessageBox | /api/beauty/inventory/check-detail, /api/beauty/inventory/check-audit | 审核通过/驳回跳转至/inventory/inventory-check-list |
| /inventory/inventory-check-detail | 查看盘点单详情、导出盘点单/盘盈盘亏单、打印 | BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Tabs/Dialog | /api/beauty/inventory/check-detail, /api/beauty/inventory/check-export-single, /api/beauty/inventory/check-profit-loss-export | 无跳转 |

### 模块4：用户管理
| 页面路径 | 核心功能 | 必用组件 | 接口调用 | 跳转关系 |
|----------|----------|----------|----------|----------|
| /user/staff-list | 员工档案管理（增删改查、批量导入、批量导出、模板下载、重置密码）、条码生成/扫描 | BeautySearchBar、BeautyBarcodeReader、BeautyTable、BeautyExport、Element Plus Form/Dialog/Select/Pagination/Upload/MessageBox/Button | /api/beauty/staff/list, /api/beauty/staff/save, /api/beauty/staff/update, /api/beauty/staff/delete, /api/beauty/staff/batch-import, /api/beauty/staff/template-download, /api/beauty/staff/reset-password, /api/beauty/role/list | 重置密码需弹出BeautySecondVerify |
| /user/role-list | 员工角色配置（增删改查、权限树多选） | Element Plus Form/Dialog/Tree/Select/Pagination/MessageBox | /api/beauty/role/list, /api/beauty/role/save, /api/beauty/role/update, /api/beauty/role/delete | 无跳转 |
| /user/member-list | 会员档案管理（增删改查、批量导入、批量导出、模板下载、储值充值、积分调整）、条码生成/扫描 | BeautySearchBar、BeautyBarcodeReader、BeautyTable、BeautyExport、Element Plus Form/Dialog/Select/Pagination/Upload/MessageBox/Button | /api/beauty/member/list, /api/beauty/member/save, /api/beauty/member/update, /api/beauty/member/delete, /api/beauty/member/batch-import, /api/beauty/member/template-download, /api/beauty/member/recharge, /api/beauty/member/point-adjust, /api/beauty/level/list | 大额储值充值需弹出BeautySecondVerify |
| /user/level-list | 会员等级配置（增删改查、等级排序、折扣验证）、等级图标上传 | Element Plus Form/Dialog/Select/Upload/Pagination/MessageBox | /api/beauty/level/list, /api/beauty/level/save, /api/beauty/level/update, /api/beauty/level/delete | 无跳转 |
| /user/member-detail | 查看会员详情、查看会员流水（分类切换）、导出会员流水、打印 | BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Tabs/DatePicker/Select/Pagination | /api/beauty/member/detail, /api/beauty/member/flow-list, /api/beauty/member/flow-export | 无跳转 |

### 模块5：流水报表
| 页面路径 | 核心功能 | 必用组件 | 接口调用 | 跳转关系 |
|----------|----------|----------|----------|----------|
| /report/finance-income-expense | 收支报表（日报/周报/月报/年报分类切换）、统计图表切换、导出、打印 | ECharts、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Tabs/DatePicker/Select/Pagination | /api/beauty/report/finance-income-expense-summary, /api/beauty/report/finance-income-expense-detail, /api/beauty/report/finance-income-expense-export | 无跳转 |
| /report/finance-supplier-payable | 供应商应付/已付账款报表、导出、打印 | BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus DatePicker/Select/Pagination | /api/beauty/report/finance-supplier-payable-summary, /api/beauty/report/finance-supplier-payable-detail, /api/beauty/report/finance-supplier-payable-export, /api/beauty/supplier/list | 无跳转 |
| /report/finance-member-asset | 会员储值/积分流水报表（分类切换）、导出、打印 | ECharts、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Tabs/DatePicker/Select/Pagination | /api/beauty/report/finance-member-asset-summary, /api/beauty/report/finance-member-asset-detail, /api/beauty/report/finance-member-asset-export, /api/beauty/member/list | 无跳转 |
| /report/business-sale | 销售报表（日报/周报/月报/年报分类切换）、统计图表切换、导出、打印 | ECharts、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus Tabs/DatePicker/Select/Pagination | /api/beauty/report/business-sale-summary, /api/beauty/report/business-sale-detail, /api/beauty/report/business-sale-export, /api/beauty/category/list, /api/beauty/product/list, /api/beauty/staff/list | 无跳转 |
| /report/business-product-ranking | 商品销售排行榜（统计维度切换、排名数量调整）、导出、打印 | ECharts、BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus DatePicker/Select/Pagination | /api/beauty/report/business-product-ranking-summary, /api/beauty/report/business-product-ranking-export, /api/beauty/category/list | 无跳转 |
| /report/business-inventory-check | 库存盘点报表、导出、打印 | BeautyTable、BeautyExport、BeautyPreviewPrint、Element Plus DatePicker/Select/Pagination | /api/beauty/report/business-inventory-check-summary, /api/beauty/report/business-inventory-check-detail, /api/beauty/report/business-inventory-check-export, /api/beauty/staff/list | 无跳转 |

### 模块6：系统设置
| 页面路径 | 核心功能 | 必用组件 | 接口调用 | 跳转关系 |
|----------|----------|----------|----------|----------|
| /system/basic-param | 基础参数设置（店铺信息、库存参数、采购参数、销售参数、会员参数、小票模板配置）、店铺logo上传、小票模板预览 | Element Plus Form/Upload/Dialog/Tabs/Button | /api/beauty/system/basic-param-get, /api/beauty/system/basic-param-set | 修改需弹出BeautySecondVerify |
| /system/second-verify | 敏感操作二次验证设置（操作列表配置、验证方式配置、短信验证码配置） | Element Plus Form/Tree/Select/Dialog/MessageBox | /api/beauty/system/second-verify-get, /api/beauty/system/second-verify-set | 修改需弹出BeautySecondVerify |
| /system/api-config | 通用前端对接接口配置（API Key生成/修改/删除、签名验证规则调整、模拟测试） | Element Plus Form/Dialog/Button/MessageBox | /api/beauty/system/api-key-list, /api/beauty/system/api-key-generate, /api/beauty/system/api-key-update, /api/beauty/system/api-key-delete, /api/beauty/system/api-config-get, /api/beauty/system/api-config-set | 生成/修改/删除API Key需弹出BeautySecondVerify |
| /system/sms-config | 第三方短信接口配置（服务商选择、参数配置、单条测试、发送记录查询） | Element Plus Form/Select/Dialog/Button/Tabs/Pagination | /api/beauty/system/sms-config-get, /api/beauty/system/sms-config-set, /api/beauty/system/sms-test-send, /api/beauty/system/sms-record-list | 修改需弹出BeautySecondVerify |
| /system/log-list | 系统日志查询（分类切换）、导出 | BeautySearchBar、BeautyTable、BeautyExport、Element Plus Tabs/DatePicker/Select/Pagination | /api/beauty/system/log-list, /api/beauty/system/log-export, /api/beauty/staff/list | 导出需弹出BeautySecondVerify |

### 模块7：登录/首页
| 页面路径 | 核心功能 | 必用组件 | 接口调用 | 跳转关系 |
|----------|----------|----------|----------|----------|
| /login | 员工登录、首次登录强制修改密码、忘记密码（可选，需对接短信接口） | Element Plus Form/Button/Message/MessageBox | /api/beauty/auth/login, /api/beauty/auth/first-password-change | 登录成功跳转至/index |
| /index | 系统首页、预警统计（顶部红色/黄色/蓝色提示）、今日数据概览、快捷操作、最近7天销售趋势图 | ECharts、Element Plus Card/Button/Dialog/Alert | /api/beauty/dashboard/warning-count, /api/beauty/dashboard/today-summary, /api/beauty/auth/logout | 点击快捷操作跳转至对应页面 |

---

## 3. 后端接口开发清单
### 模块1：采购管理
| URL | 请求方式 | 请求参数 | 响应格式 | 业务逻辑 | 关联数据库表 |
|-----|----------|----------|----------|----------|--------------|
| /api/beauty/purchase/save | POST | BeautyPurchaseSaveDTO | BeautyResult<BeautyPurchaseVO> | 验证@BeautyPermission("purchase:save")、验证供应商信息（正式/临时至少一个不为空）、验证商品明细SKU存在且状态正常、验证采购数量≥1、验证采购单价≥0、验证折扣率0.01-1.00、自动计算小计（采购数量×采购单价×折扣率）和总金额、使用Redisson生成采购单号（CG+8位年月日+4位流水号）、保存采购单状态为「待入库」、应付账款支付方式下使用乐观锁增加供应商应付余额、记录@BeautyLog日志 | beauty_purchase, beauty_purchase_detail, beauty_supplier, beauty_product, beauty_staff, beauty_system_log |
| /api/beauty/purchase/draft-save | POST | BeautyPurchaseDraftSaveDTO | BeautyResult<BeautyPurchaseVO> | 验证@BeautyPermission("purchase:draft:save")、验证商品明细SKU存在且状态正常（可选）、自动计算小计和总金额（若有商品明细）、保存/更新采购单状态为「草稿」、记录@BeautyLog日志 | beauty_purchase, beauty_purchase_detail, beauty_supplier, beauty_staff, beauty_system_log |
| /api/beauty/purchase/list | GET | BeautyPurchaseListQueryDTO | BeautyResult<BeautyPageResult<BeautyPurchaseListVO>> | 验证@BeautyPermission("purchase:list")、按条件分页查询采购单列表、关联供应商/创建人信息、逻辑删除过滤、按create_time倒序、返回分页结果 | beauty_purchase, beauty_supplier, beauty_staff |
| /api/beauty/purchase/draft-list | GET | BeautyPurchaseDraftListQueryDTO | BeautyResult<BeautyPageResult<BeautyPurchaseListVO>> | 验证@BeautyPermission("purchase:draft:list")、按条件分页查询状态为「草稿」的采购单列表、关联供应商/创建人信息、逻辑删除过滤、按update_time倒序、返回分页结果 | beauty_purchase, beauty_supplier, beauty_staff |
| /api/beauty/purchase/detail | GET | Long id | BeautyResult<BeautyPurchaseDetailVO> | 验证@BeautyPermission("purchase:detail")、查询采购单详情及商品明细、关联供应商/创建人/商品信息、逻辑删除过滤、返回结果 | beauty_purchase, beauty_purchase_detail, beauty_supplier, beauty_product, beauty_staff |
| /api/beauty/purchase/stock-in | POST | BeautyPurchaseStockInDTO | BeautyResult<BeautyPurchaseVO> | 验证@BeautyPermission("purchase:stock:in")、验证采购单状态为「待入库/部分入库」、验证入库员身份（自动获取当前登录员工）、验证入库明细SKU属于该采购单且状态正常、验证入库数量≤剩余待入库数量、验证库位状态为「正常」、使用乐观锁更新采购单状态（全部入库为「已完成」，部分为「部分入库」）、使用乐观锁同步更新商品库存（库位+数量、总金额=入库数量×采购单价×折扣率）、生成入库记录单、应付账款全部入库提示可选但不强制、记录@BeautyLog日志 | beauty_purchase, beauty_purchase_detail, beauty_product, beauty_product_inventory, beauty_inventory_record, beauty_location, beauty_staff, beauty_system_log |
| /api/beauty/purchase/export | GET | BeautyPurchaseListQueryDTO | void（直接输出Excel文件） | 验证@BeautyPermission("purchase:export")、按条件查询前10万条采购单列表、关联供应商/创建人信息、逻辑删除过滤、用Hutool SXSSF生成Excel文件、设置响应头直接输出 | beauty_purchase, beauty_supplier, beauty_staff |
| /api/beauty/purchase/export-single | GET | Long id, String format | void（直接输出Excel/PDF文件） | 验证@BeautyPermission("purchase:export")、查询采购单详情及商品明细、关联供应商/创建人/商品信息、逻辑删除过滤、用Hutool/jspdf-autotable生成对应格式文件、设置响应头直接输出 | beauty_purchase, beauty_purchase_detail, beauty_supplier, beauty_product, beauty_staff |
| /api/beauty/purchase/draft-delete | DELETE | Long id | BeautyResult<Boolean> | 验证@BeautyPermission("purchase:draft:delete")、验证采购单状态为「草稿」、验证创建人或有采购管理权限、逻辑删除采购单及商品明细、记录@BeautyLog日志 | beauty_purchase, beauty_purchase_detail, beauty_staff, beauty_system_log |
| /api/beauty/supplier/list | GET | BeautySupplierListQueryDTO | BeautyResult<BeautyPageResult<BeautySupplierListVO>> | 验证@BeautyPermission("supplier:list")、按条件分页查询供应商列表、逻辑删除过滤、按create_time倒序、返回分页结果 | beauty_supplier |
| /api/beauty/supplier/save | POST | BeautySupplierSaveDTO | BeautyResult<BeautySupplierVO> | 验证@BeautyPermission("supplier:save")、验证供应商名称/联系方式至少一个不为空、验证联系方式（若有）唯一、保存供应商档案、记录@BeautyLog日志 | beauty_supplier, beauty_staff, beauty_system_log |
| /api/beauty/supplier/update | PUT | BeautySupplierUpdateDTO | BeautyResult<BeautySupplierVO> | 验证@BeautyPermission("supplier:update")、验证供应商名称/联系方式至少一个不为空、验证联系方式（若有）唯一（排除自身）、更新供应商档案、记录@BeautyLog日志 | beauty_supplier, beauty_staff, beauty_system_log |
| /api/beauty/supplier/delete | DELETE | Long id | BeautyResult<Boolean> | 验证@BeautyPermission("supplier:delete")、验证供应商未关联任何有效采购单（状态≠草稿且未删除）、逻辑删除供应商档案、记录@BeautyLog日志 | beauty_supplier, beauty_purchase, beauty_staff, beauty_system_log |

### 模块2：销售管理
| URL | 请求方式 | 请求参数 | 响应格式 | 业务逻辑 | 关联数据库表 |
|-----|----------|----------|----------|----------|--------------|
| /api/beauty/sale/retail-create | POST | BeautyRetailCreateDTO | BeautyResult<BeautySaleVO> | 验证@BeautyPermission("sale:retail:create")、验证收银员身份（自动获取当前登录员工）、验证商品明细SKU存在且状态正常、验证销售数量≥1、验证销售单价≥最低零售价、验证会员等级折扣（若关联会员优先使用等级折扣，可覆盖）、验证折扣率0.01-1.00、自动计算小计、总金额、抹零/抹分（按beauty_system_param的规则）、验证抹零/抹分后金额≥0、验证积分抵扣/储值消费需关联有效会员、验证储值余额≥储值消费金额（使用乐观锁）、验证可用积分≥积分抵扣数量（使用乐观锁）、使用Redisson生成销售单号（XS+8位年月日+4位流水号）、保存销售单状态为「已完成」、使用乐观锁同步更新商品库存（销售库位-数量、总金额=销售数量×成本价）、同步更新会员数据（消费次数+1、累计消费金额+实付金额、可用积分+实付金额×积分兑换比例（仅消费金额，不含赠送）、等级进度自动计算是否升级）、同步扣减会员储值余额/可用积分（若使用）、生成零售记录单、生成财务流水（零售收入）、生成会员流水（储值消费/积分扣减/积分增加）、记录@BeautyLog日志 | beauty_sale, beauty_sale_detail, beauty_product, beauty_product_inventory, beauty_member, beauty_member_level, beauty_inventory_record, beauty_member_flow, beauty_finance_flow, beauty_location, beauty_staff, beauty_system_log, beauty_system_param |
| /api/beauty/sale/list | GET | BeautySaleListQueryDTO | BeautyResult<BeautyPageResult<BeautySaleListVO>> | 验证@BeautyPermission("sale:list")、按条件分页查询销售单列表、关联会员/收银员/发货员/物流公司信息、逻辑删除过滤、按create_time倒序、返回分页结果 | beauty_sale, beauty_member, beauty_staff, beauty_logistics |
| /api/beauty/sale/online-list | GET | BeautyOnlineOrderListQueryDTO | BeautyResult<BeautyPageResult<BeautyOnlineOrderListVO>> | 验证@BeautyPermission("sale:online:list")、按条件分页查询状态为「待发货/已发货/已取消」的销售单列表、关联会员/发货员/物流公司信息、逻辑删除过滤、按create_time倒序、返回分页结果 | beauty_sale, beauty_member, beauty_staff, beauty_logistics |
| /api/beauty/sale/online-sync | POST | BeautyOnlineOrderSyncDTO | BeautyResult<BeautySaleVO> | 验证API签名（SHA256：API Key+当前时间戳（毫秒级）+32位随机数，时间戳误差≤beauty_system_param的分钟数）、验证订单数据完整性、验证商品明细SKU存在且状态正常、验证商品库存可用性（自动分配库存充足的主仓库，主仓库不足则按库位优先级锁定其他库位库存≥下单数量，使用乐观锁）、验证积分抵扣/储值消费需关联有效会员JWT Token（若前端传）、使用Redisson生成销售单号（同零售单）、保存销售单状态为「待发货」、同步更新商品库存（锁定状态，locked_quantity+=下单数量）、同步更新会员数据（同零售单，若关联会员）、同步扣减会员储值余额/可用积分（若使用）、生成待发货记录单、生成财务流水（线上订单收入）、生成会员流水（同零售单）、记录@BeautyLog日志 | beauty_sale, beauty_sale_detail, beauty_product, beauty_product_inventory, beauty_member, beauty_member_level, beauty_inventory_record, beauty_member_flow, beauty_finance_flow, beauty_system_api_key, beauty_system_param, beauty_staff, beauty_system_log |
| /api/beauty/sale/online-deliver | POST | BeautyOnlineOrderDeliverDTO | BeautyResult<BeautySaleVO> | 验证@BeautyPermission("sale:online:deliver")、验证销售单状态为「待发货」、验证发货员身份（自动获取当前登录员工）、验证物流单号必填、更新销售单状态为「已发货」、使用乐观锁同步更新商品库存（从锁定状态转为扣减，locked_quantity-=下单数量，stock_quantity-=下单数量）、生成发货记录单、调用通用前端回调接口同步发货状态和物流信息（配置beauty_system_param的回调地址）、记录@BeautyLog日志 | beauty_sale, beauty_sale_detail, beauty_product, beauty_product_inventory, beauty_logistics, beauty_inventory_record, beauty_staff, beauty_system_log |
| /api/beauty/sale/online-cancel | POST | BeautyOnlineOrderCancelDTO | BeautyResult<BeautySaleVO> | 验证@BeautyPermission("sale:online:cancel")、验证销售单状态为「待发货」、验证取消原因必填、验证审核人身份（自动获取当前登录有取消权限的员工）、更新销售单状态为「已取消」、使用乐观锁同步释放锁定/扣减的商品库存（locked_quantity-=下单数量，若已扣减则stock_quantity+=下单数量，总金额恢复）、同步退还会员储值余额/可用积分（若使用，使用乐观锁）、生成财务流水（退款支出）、生成会员流水（储值退款/积分增加）、调用通用前端回调接口同步取消状态（配置beauty_system_param的回调地址）、记录@BeautyLog日志 | beauty_sale, beauty_sale_detail, beauty_product, beauty_product_inventory, beauty_member, beauty_member_flow, beauty_finance_flow, beauty_staff, beauty_system_log |
| /api/beauty/sale/return-create | POST | BeautySaleReturnCreateDTO | BeautyResult<BeautySaleReturnVO> | 验证@BeautyPermission("sale:return:create")、验证销售单状态为「已完成/已部分退货」、验证退货审核人身份（自动获取当前登录有退货权限的员工）、验证退货明细SKU属于该销售单且状态正常、验证退货数量≤原销售数量-已退货数量、验证退货库位状态为「正常」、验证退货原因必填、验证退款金额≥0、自动计算退款金额（原商品明细小计金额×退货数量/原销售数量-积分抵扣金额×退货数量/原销售数量，可手动调整）、使用Redisson生成退货单号（TH+8位年月日+4位流水号）、更新原销售单状态（全部退货为「已全部退货」，部分为「已部分退货」）、使用乐观锁同步更新商品库存（退货库位+数量、总金额=退货数量×成本价）、同步扣减会员数据（消费次数-1（全部退货）、累计消费金额-原实付金额×退货数量/原销售数量、等级进度自动计算是否降级）、同步退还会员积分（若使用原积分抵扣，使用乐观锁）、同步增加会员储值余额（若退款方式为储值卡，使用乐观锁）、生成退货记录单、生成财务流水（退货退款支出）、生成会员流水（积分增加/储值退款/消费扣减）、记录@BeautyLog日志 | beauty_sale, beauty_sale_detail, beauty_sale_return, beauty_sale_return_detail, beauty_product, beauty_product_inventory, beauty_member, beauty_member_level, beauty_inventory_record, beauty_member_flow, beauty_finance_flow, beauty_location, beauty_staff, beauty_system_log |
| /api/beauty/sale/detail | GET | Long id | BeautyResult<BeautySaleDetailVO> | 验证@BeautyPermission("sale:detail")、查询销售单详情及商品明细、关联会员/收银员/发货员/物流公司/商品信息、逻辑删除过滤、返回结果 | beauty_sale, beauty_sale_detail, beauty_member, beauty_staff, beauty_logistics, beauty_product |
| /api/beauty/sale/export | GET | BeautySaleListQueryDTO | void（直接输出Excel文件） | 验证@BeautyPermission("sale:export")、按条件查询前10万条销售单列表、关联会员/收银员/发货员/物流公司信息、逻辑删除过滤、用Hutool SXSSF生成Excel文件、直接输出 | beauty_sale, beauty_member, beauty_staff, beauty_logistics |
| /api/beauty/sale/export-single | GET | Long id, String format | void（直接输出Excel/PDF文件） | 验证@BeautyPermission("sale:export")、查询销售单详情及商品明细、关联会员/收银员/发货员/物流公司/商品信息、逻辑删除过滤、用Hutool/jspdf-autotable生成对应格式文件、直接输出 | beauty_sale, beauty_sale_detail, beauty_member, beauty_staff, beauty_logistics, beauty_product |

---

## 4. 数据库表结构设计
### 初始化脚本说明
- 数据库名：`beauty_shop_manage`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`
- 必须先执行初始化脚本创建数据库、默认「美妆全品类」商品类别（id=1，parent_id=0）、默认「普通会员」会员等级（id=1，level_condition=0，general_discount=1.00，point_ratio=1.00，birthday_discount=0.90）、默认「店长」员工角色（id=1，拥有所有权限）、默认管理员账号（id=1，login_account=admin，password=AES256加密后的Admin@123，staff_no=YG0001，role_id=1，首次登录强制修改）

---

### 核心业务表
#### 1. 美妆商品类别表（beauty_category）
| 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
|--------|----------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| category_name | VARCHAR(50) | NOT NULL | - | 商品类别名称 |
| parent_id | BIGINT | NOT NULL | 0 | 父类别ID（0为「美妆全品类」） |
| general_expiration_warning_days | INT | NOT NULL | 30 | 通用过期预警天数 |
| general_safety_stock | INT | NOT NULL | 10 | 通用安全库存（件） |
| sort_order | INT | NOT NULL | 0 | 排序顺序 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | - | 创建人ID（关联beauty_staff） |
| update_by | BIGINT | NULL | - | 更新人ID（关联beauty_staff） |
| deleted | TINYINT | NOT NULL | 0 | 逻辑删除（0未删除，1已删除） |
| **索引** | | | | |
| idx_parent_id | parent_id | | | 父类别ID索引 |

#### 2. 美妆商品档案表（beauty_product）
| 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
|--------|----------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| product_barcode | VARCHAR(50) | NOT NULL, UNIQUE | - | 商品条码 |
| product_name | VARCHAR(100) | NOT NULL | - | 商品名称 |
| category_id | BIGINT | NOT NULL | - | 商品类别ID（关联beauty_category） |
| brand | VARCHAR(50) | NULL | - | 品牌 |
| specification | VARCHAR(50) | NULL | - | 规格（如50ml、片装） |
| production_date | DATE | NULL | - | 生产日期 |
| shelf_life_months | INT | NULL | - | 保质期（月） |
| expiration_warning_days | INT | NULL | - | 过期预警天数（为空则继承类别） |
| cost_price | DECIMAL(10,2) | NOT NULL | - | 成本价（元） |
| min_retail_price | DECIMAL(10,2) | NOT NULL | - | 最低零售价（元） |
| suggested_retail_price | DECIMAL(10,2) | NOT NULL | - | 建议零售价（元） |
| member_level_discount | DECIMAL(3,2) | NULL | - | 会员等级折扣（为空则继承等级） |
| safety_stock | INT | NULL | - | 安全库存（件，为空则继承类别） |
| product_images | TEXT | NULL | - | 商品图片URL（逗号分隔，最多5张） |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | - | 创建人ID（关联beauty_staff） |
| update_by | BIGINT | NULL | - | 更新人ID（关联beauty_staff） |
| deleted | TINYINT | NOT NULL | 0 | 逻辑删除（0未删除，1已删除） |
| **索引** | | | | |
| idx_product_barcode | product_barcode | | | 商品条码唯一索引 |
| idx_category_id | category_id | | | 商品类别ID索引 |
| idx_product_name | product_name(50) | | | 商品名称前缀索引（模糊查询） |

#### 3. 美妆库位档案表（beauty_location）
| 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
|--------|----------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| location_name | VARCHAR(100) | NOT NULL | - | 库位名称（如主仓库-口红区-第一层） |
| capacity | INT | NULL | - | 库位容量（件） |
| location_status | TINYINT | NOT NULL | 1 | 库位状态（1正常，0停用） |
| priority | INT | NOT NULL | 0 | 库位优先级（用于线上订单库存分配，数字越大优先级越高） |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | - | 创建人ID（关联beauty_staff） |
| update_by | BIGINT | NULL | - | 更新人ID（关联beauty_staff） |
| deleted | TINYINT | NOT NULL | 0 | 逻辑删除（0未删除，1已删除） |
| **索引** | | | | |
| idx_location_status | location_status | | | 库位状态索引 |

#### 4. 美妆商品库存表（beauty_product_inventory）
| 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
|--------|----------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| product_id | BIGINT | NOT NULL | - | 商品ID（关联beauty_product） |
| location_id | BIGINT | NOT NULL | - | 库位ID（关联beauty_location） |
| stock_quantity | INT | NOT NULL | 0 | 库存数量（件） |
| locked_quantity | INT | NOT NULL | 0 | 锁定库存数量（件，用于盘点/线上订单） |
| total_amount | DECIMAL(12,2) | NOT NULL | 0.00 | 库存总金额（元） |
| batch_production_date | DATE | NULL | - | 库存批次生产日期（可选，用于过期预警） |
| version | INT | NOT NULL | 0 | 乐观锁版本号 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| **索引** | | | | |
| idx_product_location | product_id, location_id | UNIQUE | | 商品-库位联合唯一索引 |
| idx_location_id | location_id | | | 库位ID索引 |
| idx_product_id | product_id | | | 商品ID索引 |

#### 5. 美妆供应商档案表（beauty_supplier）
| 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
|--------|----------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| supplier_name | VARCHAR(100) | NOT NULL | - | 供应商名称 |
| contact_phone | VARCHAR(20) | NULL | - | 联系方式（手机号/座机号） |
| address | VARCHAR(200) | NULL | - | 地址 |
| supply_category_ids | TEXT | NULL | - | 供应商品类别ID（逗号分隔） |
| cooperation_discount_rate | DECIMAL(3,2) | NOT NULL | 1.00 | 合作折扣率 |
| payable_balance | DECIMAL(12,2) | NOT NULL | 0.00 | 应付账款余额（元） |
| version | INT | NOT NULL | 0 | 乐观锁版本号 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| create_by | BIGINT | NULL | - | 创建人ID（关联beauty_staff） |
| update_by | BIGINT | NULL | - | 更新人ID（关联beauty_staff） |
| deleted | TINYINT | NOT NULL | 0 | 逻辑删除（0未删除，1已删除） |
| **索引** | | | | |
| idx_supplier_name | supplier_name(50) | | | 供应商名称前缀索引 |
| idx_contact_phone | contact_phone | | | 联系方式索引 |

---

## 5. 业务逻辑规则
### 5.1 核心业务流程
#### 美妆商品进销存全流程
1. **商品建档流程**：店长/仓库管理员创建商品类别→创建商品档案（含初始库存则自动生成初始入库记录）→商品档案生效
2. **采购入库流程**：仓库管理员创建采购单（草稿/直接提交）→有采购权限的员工提交采购单→仓库管理员采购单入库→库存同步更新→采购单状态更新为已完成
3. **零售开单流程**：收银员选择会员/散客→添加商品明细（扫码/手动）→自动计算金额/抹零→确认收款→库存同步更新→会员数据同步更新→生成财务/会员流水
4. **线上订单流程**：通用前端提交订单→系统验证签名/库存/会员数据→锁定库存→生成待发货销售单→仓库管理员发货→库存同步释放/扣减→同步前端发货状态
5. **库存盘点流程**：仓库管理员创建盘点单→锁定盘点范围库存→仓库管理员实盘录入→店长审核→审核通过则差异调整库存/解锁/生成盘盈盘亏单→审核驳回则修改实盘后重新提交

#### 美妆会员等级自动升降级流程
1. 会员消费/退货后，系统自动计算累计消费金额
2. 查找会员等级表中累计消费金额≥X元的最高等级
3. 若当前等级≠最高等级，自动更新会员等级，并记录会员流水

#### 美妆库存锁定自动解除流程
1. 创建盘点单时，在Redis中设置库存锁定截止时间（系统设置的时间）
2. 系统启动定时任务（每小时执行1次），查询Redis中已过期的库存锁定
3. 查询对应盘点单状态，若为「待盘点/实盘中/已驳回」，则自动更新盘点单状态为「已取消」，并同步解锁库存
4. 删除Redis中已过期的库存锁定缓存

### 5.2 数据校验规则
#### 美妆商品类校验规则
1. 商品条码：必须唯一，支持字母+数字+符号，长度≤50位
2. 商品价格：最低零售价≥成本价，建议零售价≥最低零售价
3. 商品日期：生产日期≤当前时间，保质期≥1个月（若有）
4. 商品库存：安全库存≥0，库位容量≥0（若有）

#### 美妆采购/销售类校验规则
1. 数量：采购/销售/退货/调拨数量≥1
2. 折扣率：0.01-1.00
3. 金额：小计/总金额≥0，抹零/抹分后金额≥0
4. 日期：销售日期≥商品入库日期（可选，系统设置开关），退货日期≥销售日期，采购日期≥当前时间-30天（可选，系统设置开关）

#### 美妆会员类校验规则
1. 手机号：必须唯一，符合国内手机号格式（11位，1开头）
2. 会员卡号：必须唯一，支持字母+数字，长度≤20位
3. 储值/积分：储值余额≥0，可用积分≥0

### 5.3 状态流转规则
#### 美妆采购单状态流转
- 草稿 → 待入库：保存并提交
- 待入库 → 部分入库：部分商品入库
- 部分入库 → 已完成：剩余商品全部入库
- 待入库 → 已完成：全部商品一次性入库
- 草稿 → 已删除：删除草稿
- 所有状态（除已完成）→ 无变更：已完成状态不可修改/删除

#### 美妆销售单状态流转
- 待发货 → 已发货：确认发货
- 待发货 → 已取消：确认取消
- 已完成 → 已部分退货：部分商品退货
- 已部分退货 → 已全部退货：剩余商品全部退货
- 已完成 → 已全部退货：全部商品一次性退货
- 所有状态（除待发货）→ 无变更：待发货状态仅可取消/发货

#### 美妆盘点单状态流转
- 待盘点 → 实盘中：保存部分实盘明细
- 实盘中 → 待审核：提交全部实盘明细
- 待审核 → 已完成：审核通过
- 待审核 → 已驳回：审核驳回
- 已驳回 → 实盘中：修改实盘明细
- 待盘点/实盘中/已驳回 → 已取消：确认取消
- 已完成 → 无变更：已完成状态不可修改/删除

---

## 6. 开发执行顺序
### 6.1 阶段划分
| 阶段 | 核心任务 | 依赖关系 | 预计工时（人天，按2个全栈开发计算） |
|------|----------|----------|--------------------------------------|
| 阶段1：数据库与公共模块开发 | 1. 设计并创建所有数据库表<br>2. 编写初始化脚本<br>3. 开发公共模块（常量、枚举、异常、结果、工具类、自定义注解）<br>4. 配置项目（pom.xml/package.json、配置类、启动类） | 无 | 3 |
| 阶段2：用户管理与认证模块开发 | 1. 开发员工/角色管理后端接口<br>2. 开发会员/等级管理后端接口<br>3. 开发认证接口（登录、首次修改密码、刷新Token、登出）<br>4. 开发员工/角色管理前端页面<br>5. 开发会员/等级管理前端页面<br>6. 开发登录/首页前端页面 | 阶段1 | 5 |
| 阶段3：库存管理模块开发 | 1. 开发商品/类别/库位管理后端接口<br>2. 开发库存预警/调拨后端接口<br>3. 开发库存盘点管理后端接口<br>4. 开发商品/类别/库位管理前端页面<br>5. 开发库存预警/调拨前端页面<br>6. 开发库存盘点管理前端页面 | 阶段2 | 7 |
| 阶段4：采购/销售管理模块开发 | 1. 开发采购管理后端接口<br>2. 开发销售管理后端接口<br>3. 开发通用前端对接接口<br>4. 开发采购管理前端页面<br>5. 开发销售管理前端页面 | 阶段3 | 7 |
| 阶段5：流水报表模块开发 | 1. 开发财务流水类报表后端接口<br>2. 开发业务运营类报表后端接口<br>3. 开发流水报表前端页面 | 阶段4 | 4 |
| 阶段6：系统设置模块开发 | 1. 开发基础参数/二次验证设置后端接口<br>2. 开发API配置/短信配置后端接口<br>3. 开发系统日志后端接口<br>4. 开发系统设置前端页面 | 阶段5 | 3 |
| 阶段7：联调与测试 | 1. 前后端联调所有接口<br>2. 编写单元测试<br>3. 编写集成测试<br>4. 性能测试<br>5. 安全测试<br>6. 修复Bug | 阶段6 | 5 |
| **总计** | | | **34** |

### 6.2 依赖关系
- 所有业务模块依赖公共模块
- 所有业务模块依赖用户管理与认证模块（需要权限校验）
- 采购/销售管理模块依赖库存管理模块（需要库存同步）
- 流水报表模块依赖采购/销售/库存/用户管理模块（需要基础数据）
- 系统设置模块依赖所有业务模块（需要基础数据配置）
- 联调与测试依赖所有开发阶段

### 6.3 执行优先级
#### 高优先级（必须先完成）
1. 数据库设计与创建
2. 公共模块开发
3. 员工/角色管理模块开发
4. 认证模块开发
5. 商品/类别/库位管理模块开发
6. 零售开单模块开发

#### 中优先级（其次完成）
1. 库存预警/调拨模块开发
2. 采购管理模块开发
3. 线上订单同步/发货/取消模块开发
4. 销售退货模块开发
5. 会员/等级管理模块开发
6. 财务流水类报表模块开发
7. 业务运营类报表模块开发

#### 低优先级（最后完成）
1. 库存盘点管理模块开发
2. 系统设置模块开发（除基础参数/API配置）
3. 短信接口集成
4. 通用前端回调接口集成
5. 高级性能优化
6. 高级安全优化

---

## 7. 代码生成规范
### 7.1 命名规范
#### 通用命名规范
- 所有命名必须使用英文，禁止使用拼音
- 所有命名必须包含核心业务领域关键词「Beauty」（除公共模块的基础类）
- 类名/接口名/枚举名：大驼峰命名法（PascalCase）
- 方法名/变量名/参数名：小驼峰命名法（camelCase）
- 常量名：全大写下划线分隔命名法（UPPER_SNAKE_CASE）
- 数据库表名/字段名：全小写下划线分隔命名法（lower_snake_case），表名前缀必须为「beauty_」

#### 类名/接口名/枚举名示例
- 类名：BeautyProductController、BeautyProductService、BeautyProductServiceImpl、BeautyProductMapper
- 接口名：BeautyPermission、BeautyLog
- 枚举名：BeautyPurchaseStatus、BeautyPayType、BeautyPermissionCode

#### 方法名/变量名/参数名示例
- 方法名：getBeautyProductList、saveBeautyProduct、updateBeautyProduct
- 变量名：beautyProduct、beautyProductList、beautyPurchaseNo
- 参数名：beautyProductSaveDTO、beautyPurchaseListQueryDTO

#### 常量名示例
- 常量名：BEAUTY_PURCHASE_NO_PREFIX、BEAUTY_SALE_NO_PREFIX、BEAUTY_DEFAULT_SAFETY_STOCK

#### 数据库表名/字段名示例
- 表名：beauty_product、beauty_purchase、beauty_purchase_detail
- 字段名：product_barcode、purchase_no、total_amount

### 7.2 代码结构
#### 后端代码结构
严格遵循MVC+Service分层架构：
- **Controller层**：负责接收请求、参数校验、权限校验、调用Service层、返回统一响应结果
- **Service层**：负责业务逻辑处理、数据库操作（调用Mapper层）、缓存操作、消息发送、事务管理（使用@Transactional注解，事务回滚规则为RuntimeException和Error）
- **Mapper层**：负责数据库操作（简单查询使用MyBatis Plus内置方法，复杂查询使用XML文件）
- **Domain层**：负责数据库实体、VO、DTO的定义

#### 前端代码结构
严格遵循组件化+模块化架构：
- **Views层**：负责页面布局、组件调用、数据展示、用户交互
- **Components层**：负责公共组件的定义，可复用性≥3次的组件必须放在Components层
- **Store层**：负责全局状态的管理（用户信息、店铺信息、基础参数配置）
- **Api层**：负责接口请求的统一封装
- **Utils层**：负责工具函数的定义，可复用性≥3次的函数必须放在Utils层

### 7.3 注释要求
#### 后端注释要求
- **类注释**：必须包含类的功能描述、作者、创建时间
- **方法注释**：必须包含方法的功能描述、请求参数说明、响应结果说明、业务逻辑说明、异常说明
- **复杂逻辑注释**：必须包含单行/多行注释，说明逻辑的目的、步骤、注意事项
- **数据库实体注释**：每个字段必须包含注释，使用MyBatis Plus的@TableField注解的comment属性

#### 前端注释要求
- **组件注释**：必须包含组件的功能描述、Props说明、Events说明、Slots说明
- **方法注释**：必须包含方法的功能描述、参数说明、返回值说明
- **复杂逻辑注释**：必须包含单行/多行注释，说明逻辑的目的、步骤、注意事项

### 7.4 错误码定义
#### 错误码前缀
- 通用错误码前缀：`BEAUTY_COMMON_`
- 采购管理错误码前缀：`BEAUTY_PURCHASE_`
- 销售管理错误码前缀：`BEAUTY_SALE_`
- 库存管理错误码前缀：`BEAUTY_INVENTORY_`
- 用户管理错误码前缀：`BEAUTY_USER_`
- 流水报表错误码前缀：`BEAUTY_REPORT_`
- 系统设置错误码前缀：`BEAUTY_SYSTEM_`
- 通用前端API错误码前缀：`BEAUTY_API_`

#### 通用错误码示例
- `BEAUTY_COMMON_001`：参数校验失败
- `BEAUTY_COMMON_002`：权限不足
- `BEAUTY_COMMON_00