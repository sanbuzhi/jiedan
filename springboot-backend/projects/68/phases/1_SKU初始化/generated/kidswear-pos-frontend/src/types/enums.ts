// SKU状态
export enum SkuStatus {
  NORMAL = 1,
  OFF_SHELF = 2,
  OUT_OF_STOCK = 3,
  PRE_SALE = 4
}

export const SKU_STATUS_MAP: Record<number, string> = {
  [SkuStatus.NORMAL]: '正常销售',
  [SkuStatus.OFF_SHELF]: '已下架',
  [SkuStatus.OUT_OF_STOCK]: '缺货',
  [SkuStatus.PRE_SALE]: '预售中'
}

// 性别适用
export enum GenderType {
  UNISEX = 0,
  BOY = 1,
  GIRL = 2
}

export const GENDER_TYPE_MAP: Record<number, string> = {
  [GenderType.UNISEX]: '男女通用',
  [GenderType.BOY]: '男童',
  [GenderType.GIRL]: '女童'
}

// 尺码组
export enum SizeGroup {
  INFANT = 1,
  TODDLER = 2,
  CHILDREN = 3,
  TEEN = 4
}

export const SIZE_GROUP_MAP: Record<number, string> = {
  [SizeGroup.INFANT]: '婴儿装',
  [SizeGroup.TODDLER]: '幼童装',
  [SizeGroup.CHILDREN]: '儿童装',
  [SizeGroup.TEEN]: '大童装'
}

export const SIZE_GROUP_SIZES: Record<number, string[]> = {
  [SizeGroup.INFANT]: ['59', '66', '73', '80', '90'],
  [SizeGroup.TODDLER]: ['90', '100', '110', '120'],
  [SizeGroup.CHILDREN]: ['120', '130', '140', '150', '160'],
  [SizeGroup.TEEN]: ['160', '165', '170', '175']
}