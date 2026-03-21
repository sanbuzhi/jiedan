【当前阶段】
阶段2：后端项目底座搭建，继续开发

【已开发文件】
- back/pom.xml
- back/src/main/resources/application.yml
- back/src/main/resources/application-dev.yml
- back/src/main/resources/application-prod.yml
- back/src/main/java/com/tongquyouyi/TongquyouyiApplication.java
- back/src/main/java/com/tongquyouyi/common/Result.java
- back/src/main/java/com/tongquyouyi/common/ErrorCode.java
- back/src/main/java/com/tongquyouyi/common/TqyException.java
- back/src/main/java/com/tongquyouyi/common/GlobalExceptionHandler.java
- back/src/main/java/com/tongquyouyi/common/PageResult.java
- back/src/main/java/com/tongquyouyi/config/JwtProperties.java
- back/src/main/java/com/tongquyouyi/config/FileProperties.java
- back/src/main/java/com/tongquyouyi/config/RedisProperties.java
- back/src/main/java/com/tongquyouyi/config/AliOssProperties.java
- back/src/main/java/com/tongquyouyi/config/AliSmsProperties.java
- back/src/main/java/com/tongquyouyi/config/WechatPayProperties.java
- back/src/main/java/com/tongquyouyi/config/AlipayProperties.java
- back/src/main/java/com/tongquyouyi/config/MybatisPlusConfig.java
- back/src/main/java/com/tongquyouyi/config/Knife4jConfig.java
- back/src/main/java/com/tongquyouyi/config/CorsConfig.java
- back/src/main/java/com/tongquyouyi/config/RedisConfig.java
- back/src/main/java/com/tongquyouyi/utils/JwtUtils.java
- back/src/main/java/com/tongquyouyi/utils/BCryptUtils.java
- back/src/main/java/com/tongquyouyi/utils/CaptchaUtils.java
- back/src/main/java/com/tongquyouyi/utils/RedisUtils.java
- back/src/main/java/com/tongquyouyi/interceptor/IpBrushInterceptor.java
- back/src/main/java/com/tongquyouyi/interceptor/JwtInterceptor.java
- back/src/main/java/com/tongquyouyi/interceptor/WebMvcConfig.java
- back/src/main/java/com/tongquyouyi/constant/RedisConstant.java
- back/src/main/java/com/tongquyouyi/constant/SystemConstant.java
- back/src/main/java/com/tongquyouyi/base/BaseEntity.java
- back/src/main/java/com/tongquyouyi/base/BaseDTO.java
- back/src/main/java/com/tongquyouyi/base/BaseVO.java
- back/src/main/java/com/tongquyouyi/base/PageQueryDTO.java
- back/src/main/java/com/tongquyouyi/enums/CommonStatusEnum.java
- back/src/main/java/com/tongquyouyi/enums/DeleteFlagEnum.java
- back/src/main/java/com/tongquyouyi/utils/DateUtils.java
- back/src/main/java/com/tongquyouyi/utils/IdWorker.java
- back/src/main/java/com/tongquyouyi/utils/StringUtils.java
- back/src/main/java/com/tongquyouyi/utils/BeanCopyUtils.java
- back/src/main/java/com/tongquyouyi/config/ThreadPoolConfig.java
- back/src/main/java/com/tongquyouyi/handler/MyMetaObjectHandler.java
- back/src/main/java/com/tongquyouyi/listener/ApplicationStartedListener.java
- back/src/main/java/com/tongquyouyi/base/BaseMapper.java
- back/src/main/java/com/tongquyouyi/base/BaseService.java
- back/src/main/java/com/tongquyouyi/base/BaseServiceImpl.java
- back/src/main/java/com/tongquyouyi/base/BaseController.java
- back/src/main/java/com/tongquyouyi/constant/MessageConstant.java
- back/src/main/java/com/tongquyouyi/enums/AccountTypeEnum.java
- back/src/main/java/com/tongquyouyi/enums/PayTypeEnum.java
- back/src/main/java/com/tongquyouyi/enums/OrderStatusEnum.java

【继续开发】
请继续生成剩余未开发的文件，已生成的文件请勿重复。
优先生成尚未生成的关键文件。

【输出格式】【重要】每个文件必须严格按以下格式输出：
   ===FILE:文件路径（不允许出现中文）===
   ```
   代码内容
   ```
   【正确格式示例】
   ===FILE:src/main/java/com/example/User.java===
   ```java
   public class User {}
   ```
   【错误格式示例 - 禁止这样写】
   ```java
   -- ===FILE:src/main/java/com/example/User.java===  【错误：代码块在===FILE:前面】
   public class User {}
   ```
- 已在【已开发文件】列表中的文件请勿重复输出

【重要提醒】
- 目标产出：back/pom.xml, back/src/main/resources/application.yml, back/src/main/resources/application-dev.yml, back/src/main/resources/application-prod.yml, back/src/main/java/com/tongquyouyi/config/*.java, back/src/main/java/com/tongquyouyi/utils/*.java, back/src/main/java/com/tongquyouyi/interceptor/*.java, back/src/main/java/com/tongquyouyi/common/*.java, back/src/main/java/com/tongquyouyi/TongquyouyiApplication.java
- 关键词：SpringBoot 2.7.18, MyBatis Plus 3.5.5, Knife4j 4.4.0, JWT HS256, 阿里云OSS, 阿里云短信, 微信支付V3 JSAPI, 支付宝沙箱/正式, Redis, 跨域配置, 公共工具类, 公共拦截器, 公共响应类, 全局异常处理, Knife4j接口文档分组
- 注意：===FILE:必须单独一行，且在代码块```之前，绝不能放在代码块内部或前面加--注释
