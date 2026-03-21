【当前阶段】
阶段1：SKU初始化，继续开发

【已开发文件】
- docker-compose.yml
- .env
- kidswear-pos-backend/pom.xml
- kidswear-pos-backend/Dockerfile
- kidswear-pos-backend/.gitignore
- kidswear-pos-backend/src/main/java/com/kidswear/pos/KidswearPosApplication.java
- kidswear-pos-backend/src/main/resources/application.yml
- kidswear-pos-backend/src/main/resources/application-dev.yml
- kidswear-pos-backend/src/main/resources/application-prod.yml
- kidswear-pos-backend/src/main/resources/db/init.sql
- kidswear-pos-frontend/package.json
- kidswear-pos-frontend/Dockerfile
- kidswear-pos-frontend/nginx.conf
- kidswear-pos-frontend/.gitignore
- kidswear-pos-frontend/vite.config.ts
- kidswear-pos-frontend/tsconfig.json
- kidswear-pos-frontend/tsconfig.node.json
- kidswear-pos-frontend/index.html
- kidswear-pos-frontend/src/vite-env.d.ts
- kidswear-pos-frontend/src/main.ts
- kidswear-pos-frontend/src/App.vue
- kidswear-pos-frontend/src/router/index.ts
- kidswear-pos-frontend/src/store/user.ts
- kidswear-pos-frontend/src/utils/request.ts
- kidswear-pos-frontend/src/views/login/index.vue
- kidswear-pos-frontend/src/views/layout/index.vue
- kidswear-pos-frontend/src/views/pos-layout/index.vue
- kidswear-pos-backend/src/main/java/com/kidswear/pos/common/Result.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/common/PageResult.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/entity/Category.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/entity/Sku.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/mapper/CategoryMapper.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/mapper/SkuMapper.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/service/CategoryService.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/service/SkuService.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/service/impl/CategoryServiceImpl.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/service/impl/SkuServiceImpl.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/controller/CategoryController.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/controller/SkuController.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/config/MybatisPlusConfig.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/config/MyMetaObjectHandler.java
- kidswear-pos-backend/src/main/resources/mapper/SkuMapper.xml
- kidswear-pos-frontend/src/api/sku.ts
- kidswear-pos-frontend/src/store/sku.ts
- kidswear-pos-frontend/src/views/sku-init/index.vue
- kidswear-pos-frontend/src/router/index.ts
- kidswear-pos-frontend/src/main.ts
- kidswear-pos-frontend/package.json
- kidswear-pos-backend/src/main/java/com/kidswear/pos/entity/User.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/mapper/UserMapper.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/service/UserService.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/utils/JwtUtil.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/interceptor/JwtInterceptor.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/config/WebMvcConfig.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/service/impl/UserServiceImpl.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/controller/AuthController.java
- kidswear-pos-backend/src/main/resources/db/init.sql
- kidswear-pos-backend/src/main/resources/application-dev.yml
- kidswear-pos-backend/src/main/java/com/kidswear/pos/config/SecurityConfig.java
- kidswear-pos-frontend/src/api/auth.ts
- kidswear-pos-frontend/src/api/category.ts
- kidswear-pos-frontend/src/store/user.ts
- kidswear-pos-frontend/src/utils/auth.ts
- kidswear-pos-frontend/src/utils/request.ts
- kidswear-pos-frontend/src/router/index.ts
- kidswear-pos-frontend/src/main.ts
- kidswear-pos-frontend/package.json
- kidswear-pos-frontend/vite.config.ts
- kidswear-pos-backend/pom.xml
- kidswear-pos-backend/src/main/java/com/kidswear/pos/entity/BaseEntity.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/SkuDTO.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/SkuQueryDTO.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/LoginDTO.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/LoginResponseDTO.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/UserDTO.java
- kidswear-pos-frontend/src/components/SkuForm.vue
- kidswear-pos-frontend/src/types/sku.ts
- kidswear-pos-frontend/src/types/category.ts
- kidswear-pos-frontend/src/types/user.ts
- kidswear-pos-backend/src/main/java/com/kidswear/pos/exception/BusinessException.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/exception/GlobalExceptionHandler.java
- kidswear-pos-frontend/src/styles/index.scss
- kidswear-pos-frontend/src/types/index.ts
- kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/CategoryDTO.java
- kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/CategoryQueryDTO.java
- kidswear-pos-frontend/src/store/category.ts
- kidswear-pos-frontend/src/components/SkuTable.vue

【继续开发】
请继续生成剩余未开发的文件，已生成的文件请勿重复。
优先生成尚未生成的关键文件。

【输出格式】（必须使用多行代码块标记符```包裹代码）
   ===FILE:文件路径（文件路径不允许出现中文）===
   ```
   代码内容
   ```
   例如：===FILE:src/main/java/com/example/User.java===
   ```
   public class User {}
   ```
- 已在【已开发文件】列表中的文件请勿重复输出

【重要提醒】
- 目标产出：*.java, *.vue, *.js
- 关键词：代码文件, 模块
