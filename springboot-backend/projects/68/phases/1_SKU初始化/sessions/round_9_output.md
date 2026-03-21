===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/enums/ErrorCodeEnum.java===
```java
package com.kidswear.pos.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
    // 通用错误
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限操作"),
    NOT_FOUND(404, "数据不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已禁用"),

    // 分类相关
    CATEGORY_NAME_EXISTS(2001, "分类名称已存在"),
    CATEGORY_HAS_CHILDREN(2002, "该分类下有子分类，无法删除"),
    CATEGORY_HAS_SKUS(2003, "该分类下有商品，无法删除"),
    CATEGORY_PARENT_NOT_FOUND(2004, "父分类不存在"),

    // SKU相关
    SKU_CODE_EXISTS(3001, "SKU编码已存在"),
    SKU_NOT_FOUND(3002, "SKU不存在"),
    SKU_STATUS_INVALID(3003, "SKU状态无效");

    private final Integer code;
    private final String message;
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/vo/CategoryVO.java===
```java
package com.kidswear.pos.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类视图对象
 */
@Data
@Schema(description = "分类视图对象")
public class CategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "父分类ID，顶级分类为0")
    private Long parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "分类图标")
    private String icon;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "是否叶子节点")
    private Boolean isLeaf;

    @Schema(description = "关联SKU数量")
    private Integer skuCount;

    @Schema(description = "子分类列表")
    private List<CategoryVO> children;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/vo/SkuVO.java===
```java
package com.kidswear.pos.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kidswear.pos.enums.GenderType;
import com.kidswear.pos.enums.SizeGroup;
import com.kidswear.pos.enums.SkuStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU视图对象
 */
@Data
@Schema(description = "SKU视图对象")
public class SkuVO {

    @Schema(description = "SKU ID")
    private Long id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "SKU编码")
    private String skuCode;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品主图URL")
    private String mainImage;

    @Schema(description = "商品图片URL列表，逗号分隔")
    private String images;

    @Schema(description = "尺码组")
    private SizeGroup sizeGroup;

    @Schema(description = "尺码组名称")
    private String sizeGroupName;

    @Schema(description = "具体尺码")
    private String size;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "性别类型")
    private GenderType genderType;

    @Schema(description = "性别类型名称")
    private String genderTypeName;

    @Schema(description = "适用年龄段描述")
    private String ageRange;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "零售价")
    private BigDecimal retailPrice;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "预警库存")
    private Integer warningStock;

    @Schema(description = "SKU状态")
    private SkuStatus status;

    @Schema(description = "SKU状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
```

===FILE:kidswear-pos-frontend/src/components/ConfirmDialog.vue===
```vue
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @closed="handleClosed"
  >
    <div class="confirm-content">
      <el-icon v-if="icon" :size="48" :color="iconColor" class="confirm-icon">
        <component :is="icon" />
      </el-icon>
      <div class="confirm-text">{{ content }}</div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" :disabled="loading">
          {{ cancelText }}
        </el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Warning, Info, Success, Error } from '@element-plus/icons-vue'

interface Props {
  modelValue: boolean
  title?: string
  content?: string
  confirmText?: string
  cancelText?: string
  icon?: 'warning' | 'info' | 'success' | 'error' | null
  iconColor?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '提示',
  content: '确定执行此操作吗？',
  confirmText: '确定',
  cancelText: '取消',
  icon: 'warning',
  iconColor: '',
  loading: false
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel', 'closed'])

const visible = ref(props.modelValue)
const iconMap = {
  warning: Warning,
  info: Info,
  success: Success,
  error: Error
}
const defaultIconColorMap = {
  warning: '#e6a23c',
  info: '#409eff',
  success: '#67c23a',
  error: '#f56c6c'
}

const currentIcon = computed(() => {
  if (!props.icon) return null
  return iconMap[props.icon] || null
})

const currentIconColor = computed(() => {
  if (props.iconColor) return props.iconColor
  if (!props.icon) return ''
  return defaultIconColorMap[props.icon] || ''
})

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleClosed = () => {
  emit('closed')
}
</script>

<style scoped lang="scss">
.confirm-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 0;

  .confirm-icon {
    flex-shrink: 0;
    margin-top: 4px;
  }

  .confirm-text {
    flex: 1;
    font-size: 14px;
    line-height: 1.6;
    color: #606266;
    word-break: break-word;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
```

===FILE:kidswear-pos-frontend/src/components/StatusTag.vue===
```vue
<template>
  <el-tag :type="tagType" :size="size" :effect="effect">
    {{ tagText }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { SkuStatus } from '@/types/enums'

interface Props {
  status: SkuStatus | string | number
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  size?: 'large' | 'default' | 'small'
  effect?: 'dark' | 'light' | 'plain'
  textMap?: Record<string | number, string>
  typeMap?: Record<string | number, 'primary' | 'success' | 'warning' | 'danger' | 'info'>
}

const props = withDefaults(defineProps<Props>(), {
  type: undefined,
  size: 'small',
  effect: 'light',
  textMap: () => ({
    [SkuStatus.ACTIVE]: '在售',
    [SkuStatus.INACTIVE]: '下架',
    [SkuStatus.OUT_OF_STOCK]: '缺货',
    [SkuStatus.DISCONTINUED]: '停产'
  }),
  typeMap: () => ({
    [SkuStatus.ACTIVE]: 'success',
    [SkuStatus.INACTIVE]: 'info',
    [SkuStatus.OUT_OF_STOCK]: 'warning',
    [SkuStatus.DISCONTINUED]: 'danger'
  })
})

const tagText = computed(() => {
  return props.textMap[props.status] || String(props.status)
})

const tagType = computed(() => {
  if (props.type) return props.type
  return props.typeMap[props.status] || 'info'
})
</script>

<style scoped lang="scss">
</style>
```

===FILE:kidswear-pos-frontend/src/utils/date.ts===
```typescript
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

// 配置dayjs
dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

/**
 * 格式化日期时间
 * @param date 日期对象/时间戳/字符串
 * @param format 格式化字符串，默认YYYY-MM-DD HH:mm:ss
 * @returns 格式化后的字符串
 */
export const formatDateTime = (
  date: dayjs.ConfigType,
  format: string = 'YYYY-MM-DD HH:mm:ss'
): string => {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化日期
 * @param date 日期对象/时间戳/字符串
 * @param format 格式化字符串，默认YYYY-MM-DD
 * @returns 格式化后的字符串
 */
export const formatDate = (
  date: dayjs.ConfigType,
  format: string = 'YYYY-MM-DD'
): string => {
  return formatDateTime(date, format)
}

/**
 * 格式化时间
 * @param date 日期对象/时间戳/字符串
 * @param format 格式化字符串，默认HH:mm:ss
 * @returns 格式化后的字符串
 */
export const formatTime = (
  date: dayjs.ConfigType,
  format: string = 'HH:mm:ss'
): string => {
  return formatDateTime(date, format)
}

/**
 * 获取相对时间
 * @param date 日期对象/时间戳/字符串
 * @returns 相对时间字符串，如"5分钟前"
 */
export const formatRelativeTime = (date: dayjs.ConfigType): string => {
  if (!date) return ''
  return dayjs(date).fromNow()
}

/**
 * 判断是否是今天
 * @param date 日期对象/时间戳/字符串
 * @returns 是否是今天
 */
export const isToday = (date: dayjs.ConfigType): boolean => {
  return dayjs(date).isSame(dayjs(), 'day')
}

/**
 * 判断是否是昨天
 * @param date 日期对象/时间戳/字符串
 * @returns 是否是昨天
 */
export const isYesterday = (date: dayjs.ConfigType): boolean => {
  return dayjs(date).isSame(dayjs().subtract(1, 'day'), 'day')
}

export default dayjs
```

===FILE:kidswear-pos-frontend/src/types/common.ts===
```typescript
/**
 * 通用API响应类型
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应类型
 */
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页请求参数类型
 */
export interface PageParams {
  current: number
  size: number
}

/**
 * 通用排序参数类型
 */
export interface SortParams {
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

/**
 * 通用查询参数类型
 */
export interface QueryParams extends PageParams, SortParams {
  [key: string]: any
}

/**
 * 通用下拉选项类型
 */
export interface Option<T = string | number> {
  label: string
  value: T
  disabled?: boolean
  children?: Option<T>[]
}

/**
 * 通用树形结构类型
 */
export interface TreeNode<T = any> {
  id: string | number
  parentId: string | number
  name: string
  children?: TreeNode<T>[]
  [key: string]: any
}
```