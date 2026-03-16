# 微信小程序语法规范文档

> 版本：v1.0.0  
> 适用基础库：2.19.4+  
> 目标：为AI助手提供全面的微信小程序开发语法规范

---

## 目录

1. [框架概述](#1-框架概述)
2. [WXML模板语法](#2-wxml模板语法)
3. [WXSS样式规范](#3-wxss样式规范)
4. [JavaScript逻辑层](#4-javascript逻辑层)
5. [JSON配置规范](#5-json配置规范)
6. [组件系统](#6-组件系统)
7. [API调用规范](#7-api调用规范)
8. [数据绑定与事件](#8-数据绑定与事件)
9. [常见错误与避坑指南](#9-常见错误与避坑指南)
10. [最佳实践](#10-最佳实践)

---

## 1. 框架概述

### 1.1 小程序架构

微信小程序采用**双线程模型**：

```
┌─────────────────┐     ┌─────────────────┐
│   逻辑层 (JS)    │ ←→ │  视图层 (WXML)   │
│   (JSCore)      │     │  (WebView)      │
└─────────────────┘     └─────────────────┘
         ↑                       ↑
         └──────────┬────────────┘
                    ↓
           ┌─────────────────┐
           │   客户端原生能力  │
           └─────────────────┘
```

### 1.2 文件类型

| 文件类型 | 扩展名 | 用途 |
|---------|--------|------|
| 页面逻辑 | `.js` | 页面生命周期、数据处理、事件响应 |
| 页面模板 | `.wxml` | 页面结构、数据绑定、条件渲染 |
| 页面样式 | `.wxss` | 页面样式、布局、动画 |
| 页面配置 | `.json` | 页面导航栏、窗口表现、组件配置 |

### 1.3 目录结构规范

```
miniprogram/
├── app.js                 # 小程序逻辑入口
├── app.json               # 小程序全局配置
├── app.wxss               # 小程序全局样式
├── sitemap.json           # 小程序搜索配置
├── pages/                 # 页面目录
│   ├── index/
│   │   ├── index.js
│   │   ├── index.wxml
│   │   ├── index.wxss
│   │   └── index.json
│   └── logs/
│       └── ...
├── components/            # 自定义组件目录
│   └── my-component/
│       ├── my-component.js
│       ├── my-component.wxml
│       ├── my-component.wxss
│       └── my-component.json
├── utils/                 # 工具函数目录
│   └── util.js
├── images/                # 图片资源目录
└── services/              # 服务层目录
    └── api.js
```

---

## 2. WXML模板语法

### 2.1 数据绑定

#### 2.1.1 基本绑定

```xml
<!-- 文本绑定 -->
<text>{{message}}</text>

<!-- 属性绑定 -->
<image src="{{imageUrl}}" mode="{{imageMode}}"></image>

<!-- 布尔属性绑定 -->
<button disabled="{{isDisabled}}">提交</button>

<!-- 注意：布尔值必须绑定，不能直接使用 true/false 字符串 -->
<!-- 错误 -->
<button disabled="true">提交</button>
<!-- 正确 -->
<button disabled="{{true}}">提交</button>
```

#### 2.1.2 运算表达式（支持）

```xml
<!-- 三元运算 -->
<text>{{isVip ? 'VIP用户' : '普通用户'}}</text>

<!-- 算术运算 -->
<text>{{price * discount}}</text>
<text>{{count + 1}}</text>

<!-- 字符串拼接 -->
<text>{{'Hello ' + name}}</text>

<!-- 逻辑运算 -->
<text>{{isLogin && isVip ? 'VIP会员' : ''}}</text>

<!-- 括号优先级 -->
<text>{{(price + tax) * quantity}}</text>
```

#### 2.1.3 不支持的表达式（重要！）

```xml
<!-- ❌ 错误：函数调用 -->
<text>{{formatPrice(price)}}</text>

<!-- ❌ 错误：复杂方法调用 -->
<text>{{items.filter(i => i.active).length}}</text>

<!-- ❌ 错误：赋值操作 -->
<text>{{count = count + 1}}</text>

<!-- ❌ 错误：new 操作符 -->
<text>{{new Date().getFullYear()}}</text>

<!-- ❌ 错误：正则表达式 -->
<text>{{/^1[3-9]\d{9}$/.test(phone)}}</text>

<!-- ❌ 错误：模板字符串 -->
<text>{{`Hello ${name}`}}</text>
```

**解决方案**：在JS中预处理数据，WXML只负责简单渲染

```javascript
// page.js
Page({
  data: {
    items: [],
    activeCount: 0,  // 预处理计算结果
    formattedPrice: '', // 预处理格式化结果
    currentYear: 2024  // 预处理日期
  },
  
  onLoad() {
    // 在JS中计算，不在WXML中计算
    const activeCount = this.data.items.filter(i => i.active).length;
    this.setData({ activeCount });
  }
});
```

### 2.2 条件渲染

#### 2.2.1 wx:if

```xml
<!-- 基本用法 -->
<view wx:if="{{condition}}">条件为真时显示</view>

<!-- wx:if / wx:elif / wx:else -->
<view wx:if="{{score >= 90}}">优秀</view>
<view wx:elif="{{score >= 80}}">良好</view>
<view wx:elif="{{score >= 60}}">及格</view>
<view wx:else>不及格</view>

<!-- 注意：wx:if 会销毁和重建组件，适合条件不频繁切换的场景 -->
```

#### 2.2.2 hidden

```xml
<!-- 基本用法 -->
<view hidden="{{!condition}}">条件为真时显示</view>

<!-- 注意：hidden 只是切换 display:none，组件始终渲染 -->
<!-- 适合频繁切换显示/隐藏的场景 -->
```

#### 2.2.3 wx:if vs hidden 选择

| 特性 | wx:if | hidden |
|------|-------|--------|
| 渲染方式 | 条件为真时渲染 | 始终渲染 |
| 切换开销 | 高（销毁/重建） | 低（CSS切换） |
| 初始渲染 | 可能更快 | 可能更慢 |
| 适用场景 | 条件不常改变 | 频繁切换显示状态 |

### 2.3 列表渲染

#### 2.3.1 wx:for 基本用法

```xml
<!-- 基本用法 -->
<view wx:for="{{items}}" wx:key="id">
  {{item.name}} - {{item.price}}
</view>

<!-- 指定索引和项变量名 -->
<view wx:for="{{items}}" wx:for-item="product" wx:for-index="idx" wx:key="id">
  {{idx + 1}}. {{product.name}}
</view>

<!-- 嵌套循环 -->
<view wx:for="{{categories}}" wx:key="id">
  <text>{{item.name}}</text>
  <view wx:for="{{item.products}}" wx:for-item="product" wx:key="id">
    {{product.name}}
  </view>
</view>
```

#### 2.3.2 wx:key 的重要性

```xml
<!-- ✅ 正确：使用唯一标识 -->
<view wx:for="{{list}}" wx:key="id">{{item.name}}</view>

<!-- ✅ 正确：使用 *this 当item本身是字符串或数字 -->
<view wx:for="{{['a', 'b', 'c']}}" wx:key="*this">{{item}}</view>

<!-- ❌ 错误：省略 wx:key -->
<view wx:for="{{list}}">{{item.name}}</view>
<!-- 会导致列表更新时的性能问题和状态错误 -->
```

#### 2.3.3 列表渲染注意事项

```xml
<!-- ❌ 错误：在 wx:for 中使用复杂的索引计算 -->
<view wx:for="{{items}}" wx:key="id">
  {{items[index + 1]?.name}}
</view>

<!-- ✅ 正确：在JS中预处理数据 -->
<view wx:for="{{processedItems}}" wx:key="id">
  {{item.nextName}}
</view>
```

### 2.4 模板

#### 2.4.1 定义和使用模板

```xml
<!-- 定义模板 -->
<template name="productCard">
  <view class="product-card">
    <image src="{{image}}" mode="aspectFill"/>
    <text class="name">{{name}}</text>
    <text class="price">¥{{price}}</text>
  </view>
</template>

<!-- 使用模板 -->
<template is="productCard" data="{{...item}}"/>

<!-- 使用模板并传递对象 -->
<template is="productCard" data="{{name: product.name, price: product.price, image: product.image}}"/>
```

#### 2.4.2 模板的作用域

```xml
<!-- 模板只能访问传入的 data，不能访问页面其他数据 -->
<template name="userInfo">
  <!-- ✅ 可以访问传入的 name 和 avatar -->
  <text>{{name}}</text>
  <image src="{{avatar}}"/>
  
  <!-- ❌ 不能访问页面全局的 globalData -->
  <text>{{globalData.appName}}</text>
</template>
```

### 2.5 引用

#### 2.5.1 import

```xml
<!-- header.wxml -->
<template name="header">
  <view class="header">{{title}}</view>
</template>

<!-- page.wxml -->
<import src="../templates/header.wxml"/>
<template is="header" data="{{title: '页面标题'}}"/>

<!-- 注意：import 不会递归引用，A import B，B import C，A 无法使用 C 的模板 -->
```

#### 2.5.2 include

```xml
<!-- common.wxml -->
<view class="loading">加载中...</view>
<view class="error">加载失败</view>

<!-- page.wxml -->
<include src="../templates/common.wxml"/>
<!-- 相当于将 common.wxml 的内容复制到这里 -->

<!-- 注意：include 会引入除模板外的所有内容 -->
```

### 2.6 事件绑定

#### 2.6.1 基本事件绑定

```xml
<!-- 点击事件 -->
<button bindtap="onTap">点击我</button>

<!-- 输入事件 -->
<input bindinput="onInput" placeholder="请输入"/>

<!-- 表单提交 -->
<form bindsubmit="onSubmit">
  <button form-type="submit">提交</button>
</form>

<!-- 阻止冒泡 -->
<view bindtap="onParentTap">
  <button catchtap="onChildTap">阻止冒泡</button>
</view>
```

#### 2.6.2 事件传参

```xml
<!-- ✅ 正确：使用 data-* 传参 -->
<view 
  wx:for="{{list}}" 
  wx:key="id"
  bindtap="onItemTap"
  data-id="{{item.id}}"
  data-index="{{index}}"
  data-name="{{item.name}}"
>
  {{item.name}}
</view>

<!-- ❌ 错误：不能在 bindtap 中直接传参 -->
<view bindtap="onTap(item.id)">错误示例</view>
```

```javascript
// page.js
Page({
  onItemTap(e) {
    // 获取传递的参数
    const { id, index, name } = e.currentTarget.dataset;
    console.log(id, index, name);
  }
});
```

#### 2.6.3 常用事件类型

| 事件类型 | 说明 | 适用组件 |
|---------|------|---------|
| `tap` | 点击 | 所有组件 |
| `longtap` | 长按 | 所有组件 |
| `touchstart` | 触摸开始 | 所有组件 |
| `touchmove` | 触摸移动 | 所有组件 |
| `touchend` | 触摸结束 | 所有组件 |
| `input` | 输入 | input, textarea |
| `change` | 值改变 | picker, checkbox, radio |
| `submit` | 表单提交 | form |
| `scroll` | 滚动 | scroll-view |
| `scrolltolower` | 滚动到底部 | scroll-view |

### 2.7 特殊组件

#### 2.7.1 scroll-view

```xml
<!-- 纵向滚动 -->
<scroll-view 
  scroll-y="{{true}}"
  scroll-top="{{scrollTop}}"
  bindscroll="onScroll"
  bindscrolltolower="onScrollToLower"
  style="height: 100vh;"
>
  <view wx:for="{{list}}" wx:key="id">{{item.name}}</view>
</scroll-view>

<!-- 横向滚动 -->
<scroll-view scroll-x="{{true}}" style="white-space: nowrap;">
  <view wx:for="{{tabs}}" wx:key="id" style="display: inline-block;">
    {{item.name}}
  </view>
</scroll-view>
```

#### 2.7.2 swiper

```xml
<swiper 
  indicator-dots="{{true}}"
  autoplay="{{true}}"
  interval="3000"
  duration="500"
  circular="{{true}}"
  bindchange="onSwiperChange"
>
  <swiper-item wx:for="{{banners}}" wx:key="id">
    <image src="{{item.image}}" mode="aspectFill"/>
  </swiper-item>
</swiper>
```

#### 2.7.3 block 包装元素

```xml
<!-- block 不会渲染为实际DOM，只用于逻辑控制 -->
<block wx:if="{{condition}}">
  <view>内容1</view>
  <view>内容2</view>
</block>

<block wx:for="{{list}}" wx:key="id">
  <view>{{item.name}}</view>
  <view>{{item.desc}}</view>
</block>
```

---

## 3. WXSS样式规范

### 3.1 尺寸单位

#### 3.1.1 rpx 响应式单位

```css
/* rpx (responsive pixel) 根据屏幕宽度自适应 */
/* 规定屏幕宽为 750rpx */

/* iPhone6/7/8 屏幕宽度 375px = 750rpx */
/* 1rpx = 0.5px */

.container {
  width: 750rpx;  /* 全屏宽度 */
  padding: 20rpx; /* 自适应内边距 */
}

.card {
  width: 345rpx;  /* 约一半宽度，减去边距 */
  margin: 15rpx;
}
```

#### 3.1.2 单位选择建议

| 场景 | 推荐单位 | 说明 |
|------|---------|------|
| 布局宽度/高度 | rpx | 自适应不同屏幕 |
| 字体大小 | rpx 或 px | 标题可用rpx，正文可用px固定 |
| 边框 | rpx | 保持比例 |
| 阴影/模糊 | px | 视觉效果需要固定值 |
| 动画位移 | rpx | 自适应 |

### 3.2 选择器

#### 3.2.1 支持的选择器

```css
/* 类选择器 */
.container { }

/* ID选择器 */
#header { }

/* 元素选择器 */
view { }
text { }

/* 后代选择器 */
.container .item { }

/* 子元素选择器 */
.container > .item { }

/* 相邻兄弟选择器 */
.item + .item { }

/* 伪类选择器 */
.item:hover { }
.item:active { }
.item:nth-child(odd) { }

/* 属性选择器 */
[data-type="primary"] { }
```

#### 3.2.2 不支持的选择器

```css
/* ❌ 不支持 * 通配符 */
* { margin: 0; }

/* ❌ 不支持属性值部分匹配 */
[class*="btn-"] { }

/* ❌ 不支持 :before / :after 伪元素 */
.item::before { content: ''; }

/* 替代方案：使用额外元素 */
```

### 3.3 样式导入

```css
/* 导入外部样式表 */
@import "../common/common.wxss";

/* 注意：@import 必须在文件开头 */
/* 注意：只支持相对路径 */
```

### 3.4 样式作用域

#### 3.4.1 页面样式隔离

```css
/* page.wxss 只作用于当前页面 */
/* app.wxss 作用于所有页面 */

/* 页面样式优先级高于 app.wxss */
```

#### 3.4.2 组件样式隔离

```css
/* 组件样式默认隔离，不影响外部 */
/* 外部样式也不影响组件内部 */

/* 组件内使用 :host 选择宿主元素 */
:host {
  display: block;
}
```

### 3.5 Flex布局

```css
/* 开启flex布局 */
.container {
  display: flex;
  flex-direction: row;      /* 主轴方向：row/column */
  justify-content: center;  /* 主轴对齐 */
  align-items: center;      /* 交叉轴对齐 */
  flex-wrap: wrap;          /* 换行 */
}

/* 子元素 */
.item {
  flex: 1;                  /* 占据剩余空间比例 */
  flex-shrink: 0;           /* 不收缩 */
  align-self: flex-end;     /* 单独对齐 */
}
```

### 3.6 安全区域适配

```css
/* 适配刘海屏、圆角屏 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 完整示例 */
.fixed-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
}
```

---

## 4. JavaScript逻辑层

### 4.1 页面生命周期

```javascript
Page({
  // ==================== 页面生命周期 ====================
  
  onLoad(options) {
    // 页面加载时触发，只触发一次
    // options 为页面跳转带来的参数
    console.log('页面参数:', options);
    
    // 适合：初始化数据、请求页面数据
  },
  
  onShow() {
    // 页面显示时触发，每次打开页面都触发
    // 适合：刷新数据、检查登录状态
  },
  
  onReady() {
    // 页面初次渲染完成
    // 适合：获取节点信息、操作DOM
  },
  
  onHide() {
    // 页面隐藏时触发
    // 适合：暂停播放、保存状态
  },
  
  onUnload() {
    // 页面卸载时触发
    // 适合：清理定时器、取消订阅
  },
  
  // ==================== 页面事件处理 ====================
  
  onPullDownRefresh() {
    // 用户下拉刷新
    // 需要先在页面配置中开启 enablePullDownRefresh
  },
  
  onReachBottom() {
    // 页面上拉触底
    // 适合：加载更多数据
  },
  
  onPageScroll(e) {
    // 页面滚动
    console.log('滚动位置:', e.scrollTop);
  },
  
  onShareAppMessage() {
    // 用户点击分享
    return {
      title: '分享标题',
      path: '/pages/index/index'
    };
  }
});
```

### 4.2 数据管理

#### 4.2.1 初始化数据

```javascript
Page({
  data: {
    // 基础类型
    message: 'Hello',
    count: 0,
    isLogin: false,
    
    // 对象
    userInfo: {
      name: '',
      avatar: ''
    },
    
    // 数组
    list: [],
    
    // 嵌套数据
    order: {
      items: [],
      total: 0,
      address: {
        province: '',
        city: ''
      }
    }
  }
});
```

#### 4.2.2 修改数据（setData）

```javascript
Page({
  data: {
    name: '张三',
    count: 0,
    list: [1, 2, 3],
    user: { age: 20 }
  },
  
  // ✅ 正确：修改单个字段
  updateName() {
    this.setData({
      name: '李四'
    });
  },
  
  // ✅ 正确：修改多个字段
  updateMultiple() {
    this.setData({
      name: '李四',
      count: 10
    });
  },
  
  // ✅ 正确：数组操作
  addToList() {
    const newList = [...this.data.list, 4];
    this.setData({ list: newList });
  },
  
  // ✅ 正确：修改嵌套对象
  updateNested() {
    this.setData({
      'user.age': 21           // 方式1：路径字符串
    });
    
    // 或者
    const user = { ...this.data.user, age: 21 };
    this.setData({ user });    // 方式2：展开运算符
  },
  
  // ✅ 正确：数组元素修改
  updateArrayItem() {
    this.setData({
      'list[0]': 10            // 修改第一个元素
    });
    
    // 或者
    const list = [...this.data.list];
    list[0] = 10;
    this.setData({ list });
  },
  
  // ❌ 错误：直接修改 data
  wrongWay() {
    this.data.count = 10;     // 这样不会触发视图更新！
  },
  
  // ❌ 错误：setData 数据过大
  wrongWay2() {
    // 避免一次性设置大量数据
    this.setData({
      hugeList: [/* 10000条数据 */]  // 会导致性能问题
    });
  }
});
```

#### 4.2.3 setData 性能优化

```javascript
Page({
  data: {
    list: []
  },
  
  // 分页加载大数据列表
  loadMore() {
    const newData = fetchData();
    
    // ✅ 正确：分页加载，每次只更新部分数据
    this.setData({
      [`list[${this.data.list.length}]`]: newData
    });
    
    // 或者使用数组展开
    this.setData({
      list: [...this.data.list, ...newData]
    });
  },
  
  // 只更新需要更新的字段
  updateSpecific() {
    // ✅ 正确：精确更新
    this.setData({
      'item.name': '新名称'
    });
    
    // ❌ 避免：更新整个对象
    const item = this.data.item;
    item.name = '新名称';
    this.setData({ item });  // 会触发整个item的重新渲染
  }
});
```

### 4.3 事件处理

#### 4.3.1 事件对象结构

```javascript
Page({
  onTap(e) {
    // e.type: 事件类型，如 "tap"
    // e.timeStamp: 事件触发时间戳
    // e.target: 触发事件的组件（事件源）
    // e.currentTarget: 当前组件（绑定事件的组件）
    // e.detail: 额外信息（如触摸位置、输入值等）
    
    console.log(e.target.dataset);        // 触发组件的 data-* 属性
    console.log(e.currentTarget.dataset); // 绑定事件组件的 data-* 属性
  },
  
  onInput(e) {
    // 输入框的值在 e.detail.value 中
    const value = e.detail.value;
    this.setData({ inputValue: value });
  },
  
  onScroll(e) {
    // 滚动位置
    console.log(e.detail.scrollTop);
    console.log(e.detail.scrollHeight);
  }
});
```

#### 4.3.2 自定义事件（组件间通信）

```javascript
// 父页面
Page({
  onMyEvent(e) {
    // 接收子组件触发的事件
    console.log(e.detail);  // 子组件传递的数据
  }
});

// 子组件
Component({
  methods: {
    triggerEvent() {
      // 触发自定义事件
      this.triggerEvent('myevent', { 
        message: '来自子组件的消息' 
      }, {
        bubbles: true,      // 事件冒泡
        composed: true,     // 跨越组件边界
        capturePhase: false // 捕获阶段
      });
    }
  }
});
```

### 4.4 路由导航

#### 4.4.1 页面跳转

```javascript
// 保留当前页面，跳转到新页面（可返回）
wx.navigateTo({
  url: '/pages/detail/detail?id=123&name=abc'
});

// 关闭当前页面，跳转到新页面（不可返回）
wx.redirectTo({
  url: '/pages/login/login'
});

// 跳转到 tabBar 页面
wx.switchTab({
  url: '/pages/index/index'
});

// 关闭所有页面，打开新页面
wx.reLaunch({
  url: '/pages/index/index'
});

// 返回上一页
wx.navigateBack({
  delta: 1  // 返回的页面数
});
```

#### 4.4.2 获取页面栈

```javascript
// 获取当前页面栈
const pages = getCurrentPages();
const currentPage = pages[pages.length - 1];
const prevPage = pages[pages.length - 2];

// 修改上一页数据（不推荐，但某些场景有用）
if (prevPage) {
  prevPage.setData({
    fromDetail: true
  });
}
```

### 4.5 存储

```javascript
// ==================== 同步存储 ====================

// 设置
wx.setStorageSync('key', 'value');
wx.setStorageSync('userInfo', { name: '张三', age: 20 });

// 获取
const value = wx.getStorageSync('key');
const userInfo = wx.getStorageSync('userInfo') || {};

// 删除
wx.removeStorageSync('key');

// 清空所有
wx.clearStorageSync();

// ==================== 异步存储 ====================

// 设置
wx.setStorage({
  key: 'key',
  data: 'value',
  success() {
    console.log('存储成功');
  }
});

// Promise 风格
wx.setStorage({ key: 'key', data: 'value' })
  .then(() => console.log('存储成功'));

// ==================== 存储限制 ====================

// 单个 key 最大 1MB
// 所有数据最大 10MB
// 建议：只存储必要数据，大数据使用后端存储
```

---

## 5. JSON配置规范

### 5.1 全局配置（app.json）

```json
{
  "pages": [
    "pages/index/index",
    "pages/logs/logs",
    "pages/user/user"
  ],
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#1890ff",
    "backgroundColor": "#ffffff",
    "borderStyle": "black",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "images/home.png",
        "selectedIconPath": "images/home-active.png"
      },
      {
        "pagePath": "pages/user/user",
        "text": "我的",
        "iconPath": "images/user.png",
        "selectedIconPath": "images/user-active.png"
      }
    ]
  },
  "window": {
    "navigationBarTitleText": "小程序名称",
    "navigationBarTextStyle": "black",
    "navigationBarBackgroundColor": "#ffffff",
    "backgroundColor": "#f5f5f5",
    "enablePullDownRefresh": true,
    "onReachBottomDistance": 50
  },
  "networkTimeout": {
    "request": 10000,
    "downloadFile": 10000
  },
  "debug": false,
  "functionalPages": false,
  "subpackages": [
    {
      "root": "packageA",
      "pages": [
        "pages/cat",
        "pages/dog"
      ]
    }
  ],
  "usingComponents": {
    "van-button": "@vant/weapp/button/index"
  },
  "permission": {
    "scope.userLocation": {
      "desc": "你的位置信息将用于小程序位置接口的效果展示"
    }
  }
}
```

### 5.2 页面配置（page.json）

```json
{
  "navigationBarTitleText": "页面标题",
  "navigationBarBackgroundColor": "#1890ff",
  "navigationBarTextStyle": "white",
  "backgroundColor": "#f5f5f5",
  "backgroundTextStyle": "dark",
  "enablePullDownRefresh": true,
  "onReachBottomDistance": 50,
  "disableScroll": false,
  "usingComponents": {
    "my-component": "/components/my-component/my-component"
  }
}
```

### 5.3 组件配置（component.json）

```json
{
  "component": true,
  "usingComponents": {
    "child-component": "../child-component/child-component"
  },
  "styleIsolation": "isolated",
  "multipleSlots": true
}
```

---

## 6. 组件系统

### 6.1 自定义组件

#### 6.1.1 组件定义

```javascript
// components/my-component/my-component.js
Component({
  // 组件属性
  properties: {
    // 简单属性
    title: {
      type: String,
      value: '默认标题'
    },
    
    // 数字属性
    count: {
      type: Number,
      value: 0,
      observer(newVal, oldVal) {
        console.log('count变化:', oldVal, '->', newVal);
      }
    },
    
    // 布尔属性
    visible: {
      type: Boolean,
      value: false
    },
    
    // 对象属性
    userInfo: {
      type: Object,
      value: {}
    },
    
    // 数组属性
    list: {
      type: Array,
      value: []
    }
  },
  
  // 组件内部数据
  data: {
    internalData: '内部数据'
  },
  
  // 计算属性（2.2.3+）
  computed: {
    fullName() {
      const { firstName, lastName } = this.data;
      return `${firstName} ${lastName}`;
    }
  },
  
  // 组件生命周期
  lifetimes: {
    created() {
      // 组件实例被创建，此时还不能调用 setData
    },
    attached() {
      // 组件挂载到页面
      console.log('组件属性:', this.properties);
    },
    ready() {
      // 组件渲染完成
    },
    moved() {
      // 组件被移动到另一个节点
    },
    detached() {
      // 组件被移除
    },
    error(err) {
      // 组件方法抛出错误
      console.error('组件错误:', err);
    }
  },
  
  // 页面生命周期（组件所在页面的生命周期）
  pageLifetimes: {
    show() {
      // 页面显示
    },
    hide() {
      // 页面隐藏
    },
    resize(size) {
      // 页面尺寸变化
    }
  },
  
  // 方法定义
  methods: {
    // 内部方法以下划线开头
    _privateMethod() {
      // 私有方法
    },
    
    // 公开方法
    publicMethod() {
      // 可以被外部调用
    },
    
    // 事件处理
    onTap(e) {
      // 触发自定义事件
      this.triggerEvent('tap', { 
        message: '组件被点击',
        timestamp: Date.now()
      });
    }
  }
});
```

#### 6.1.2 组件模板

```xml
<!-- components/my-component/my-component.wxml -->
<view class="my-component">
  <text class="title">{{title}}</text>
  <text class="count">{{count}}</text>
  <button bindtap="onTap">点击我</button>
  
  <!-- 使用多个 slot -->
  <view class="header">
    <slot name="header"/>
  </view>
  <view class="body">
    <slot/>
  </view>
  <view class="footer">
    <slot name="footer"/>
  </view>
</view>
```

#### 6.1.3 使用组件

```xml
<!-- 使用组件 -->
<my-component 
  title="我的标题"
  count="{{10}}"
  visible="{{true}}"
  userInfo="{{userInfo}}"
  list="{{itemList}}"
  bind:tap="onComponentTap"
>
  <view slot="header">自定义头部</view>
  <view>默认内容</view>
  <view slot="footer">自定义底部</view>
</my-component>
```

### 6.2 组件间通信

#### 6.2.1 父子组件通信

```javascript
// 父组件
Page({
  data: {
    parentData: '来自父组件'
  },
  
  // 通过属性传递数据给子组件
  // <child data="{{parentData}}"/>
  
  // 监听子组件事件
  onChildEvent(e) {
    console.log('收到子组件消息:', e.detail);
  }
});

// 子组件
Component({
  properties: {
    data: String
  },
  
  methods: {
    sendToParent() {
      // 向父组件发送消息
      this.triggerEvent('customevent', { 
        message: '来自子组件',
        data: this.properties.data
      });
    }
  }
});
```

#### 6.2.2 兄弟组件通信

```javascript
// 使用全局状态管理或事件总线
const eventBus = {
  events: {},
  on(event, callback) {
    if (!this.events[event]) this.events[event] = [];
    this.events[event].push(callback);
  },
  emit(event, data) {
    if (this.events[event]) {
      this.events[event].forEach(cb => cb(data));
    }
  }
};

// 组件A
Component({
  methods: {
    sendMessage() {
      eventBus.emit('message', { text: 'Hello' });
    }
  }
});

// 组件B
Component({
  attached() {
    eventBus.on('message', (data) => {
      console.log('收到消息:', data);
    });
  }
});
```

---

## 7. API调用规范

### 7.1 网络请求

```javascript
// 封装请求方法
const request = (options) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`,
        'Content-Type': 'application/json',
        ...options.header
      },
      timeout: options.timeout || 10000,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          // 未授权，跳转登录
          wx.navigateTo({ url: '/pages/login/login' });
          reject(new Error('未授权'));
        } else {
          reject(new Error(res.data.message || '请求失败'));
        }
      },
      fail: (err) => {
        reject(new Error('网络错误'));
      }
    });
  });
};

// 使用
request({ url: '/api/user/info' })
  .then(data => console.log(data))
  .catch(err => wx.showToast({ title: err.message, icon: 'none' }));
```

### 7.2 常用API

```javascript
// ==================== 界面API ====================

// 显示加载
wx.showLoading({ title: '加载中...', mask: true });
wx.hideLoading();

// 显示提示
wx.showToast({ title: '成功', icon: 'success', duration: 2000 });
wx.showToast({ title: '提示', icon: 'none' });
wx.hideToast();

// 显示模态框
wx.showModal({
  title: '提示',
  content: '确定删除吗？',
  confirmText: '删除',
  confirmColor: '#ff0000',
  success: (res) => {
    if (res.confirm) {
      console.log('用户点击确定');
    }
  }
});

// 显示操作菜单
wx.showActionSheet({
  itemList: ['选项1', '选项2', '选项3'],
  success: (res) => {
    console.log('选中索引:', res.tapIndex);
  }
});

// ==================== 媒体API ====================

// 选择图片
wx.chooseImage({
  count: 9,  // 最多选择数量
  sizeType: ['original', 'compressed'],
  sourceType: ['album', 'camera'],
  success: (res) => {
    console.log(res.tempFilePaths);
  }
});

// 预览图片
wx.previewImage({
  current: '当前图片url',
  urls: ['图片1url', '图片2url']
});

// 选择文件
wx.chooseMessageFile({
  count: 1,
  type: 'file',  // file, image, video, all
  extension: ['pdf', 'doc'],
  success: (res) => {
    console.log(res.tempFiles);
  }
});

// ==================== 位置API ====================

// 获取位置
wx.getLocation({
  type: 'wgs84',  // wgs84, gcj02
  success: (res) => {
    console.log(res.latitude, res.longitude);
  }
});

// 打开地图选择位置
wx.chooseLocation({
  success: (res) => {
    console.log(res.name, res.address, res.latitude, res.longitude);
  }
});

// ==================== 设备信息 ====================

// 获取系统信息
wx.getSystemInfo({
  success: (res) => {
    console.log(res.model);      // 手机型号
    console.log(res.pixelRatio); // 设备像素比
    console.log(res.windowWidth); // 窗口宽度
    console.log(res.windowHeight); // 窗口高度
    console.log(res.language);   // 语言
    console.log(res.version);    // 微信版本
    console.log(res.platform);   // 客户端平台
  }
});
```

---

## 8. 数据绑定与事件

### 8.1 双向绑定实现

```xml
<!-- 原生组件没有双向绑定，需要手动实现 -->
<input 
  value="{{inputValue}}"
  bindinput="onInput"
  placeholder="请输入"
/>
```

```javascript
Page({
  data: {
    inputValue: ''
  },
  
  onInput(e) {
    this.setData({
      inputValue: e.detail.value
    });
  }
});
```

### 8.2 表单处理

```xml
<form bindsubmit="onSubmit" bindreset="onReset">
  <input name="username" placeholder="用户名"/>
  <input name="password" password placeholder="密码"/>
  
  <radio-group name="gender">
    <radio value="male" checked/>男
    <radio value="female"/>女
  </radio-group>
  
  <checkbox-group name="hobbies">
    <checkbox value="reading"/>阅读
    <checkbox value="sports"/>运动
  </checkbox-group>
  
  <picker name="city" mode="selector" range="{{cities}}">
    <view>选择城市: {{cities[cityIndex]}}</view>
  </picker>
  
  <button form-type="submit">提交</button>
  <button form-type="reset">重置</button>
</form>
```

```javascript
Page({
  data: {
    cities: ['北京', '上海', '广州'],
    cityIndex: 0
  },
  
  onSubmit(e) {
    const formData = e.detail.value;
    console.log('表单数据:', formData);
    // { username: 'xxx', password: 'xxx', gender: 'male', hobbies: ['reading'], city: 0 }
  },
  
  onReset() {
    console.log('表单已重置');
  }
});
```

---

## 9. 常见错误与避坑指南

### 9.1 WXML常见错误

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| `Bad value with message: unexpected '>'` | WXML中使用了不支持的JS表达式 | 将复杂计算移到JS中 |
| `wx:key="{{item.id}}"` 警告 | wx:key 不需要双大括号 | 改为 `wx:key="id"` |
| `Expecting 'STRING', 'NUMBER', 'IDENTIFIER'` | 属性值缺少引号 | `attr="{{value}}"` 而非 `attr={{value}}` |
| 列表渲染数据不更新 | 修改数组元素未使用正确方式 | 使用 `setData` 并确保传入新引用 |
| 事件不触发 | 使用了 `bindtap` 但组件被覆盖 | 检查组件层级和 `z-index` |
| **事件方法名不匹配** | WXML中`bindtap="onNext"`与JS中`onNextStep()`不一致 | **必须保持WXML与JS方法名完全一致** |

### 9.2 JS常见错误

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| `this.setData is not a function` | this 指向错误 | 使用箭头函数或 bind(this) |
| 数据更新了视图未更新 | 直接修改 data 而非使用 setData | 始终使用 `this.setData()` |
| `Cannot read property 'xxx' of undefined` | 数据未初始化就使用 | 设置默认值或添加条件渲染 |
| 页面跳转参数获取失败 | onLoad 参数格式错误 | `onLoad(options)` 直接使用 |
| 异步数据未渲染 | 请求完成时页面已渲染 | 在 onLoad 中发起请求 |

### 9.3 样式常见错误

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| 样式不生效 | 使用了浏览器特有属性 | 查看 WXSS 支持列表 |
| 图片不显示 | 图片路径错误或尺寸为0 | 检查路径，设置具体宽高 |
| 布局错乱 | 使用了不支持的 flex 属性 | 使用标准 flex 属性 |
| 字体不生效 | 小程序不支持自定义字体 | 使用系统字体或图片替代 |

### 9.4 性能优化避坑

```javascript
// ❌ 错误：频繁调用 setData
for (let i = 0; i < 100; i++) {
  this.setData({ [`list[${i}]`]: i });  // 性能极差！
}

// ✅ 正确：批量更新
const updates = {};
for (let i = 0; i < 100; i++) {
  updates[`list[${i}]`] = i;
}
this.setData(updates);

// ✅ 更好：直接更新整个数组
const newList = Array.from({ length: 100 }, (_, i) => i);
this.setData({ list: newList });
```

---

## 10. 最佳实践

### 10.1 代码组织

```
miniprogram/
├── components/          # 公共组件
├── pages/              # 页面
├── services/           # 服务层（API封装）
├── utils/              # 工具函数
├── constants/          # 常量定义
├── styles/             # 公共样式
└── config/             # 配置文件
```

### 10.2 命名规范

```javascript
// 文件命名：kebab-case
// my-component.js, user-profile.js

// 组件命名：大驼峰（在json中注册）
// "MyComponent": "components/my-component/my-component"

// 变量命名：camelCase
const userInfo = {};
const isLogin = false;

// 常量命名：UPPER_SNAKE_CASE
const API_BASE_URL = 'https://api.example.com';
const MAX_RETRY_COUNT = 3;

// 方法命名：camelCase，动词开头
function fetchUserInfo() {}
function handleClick() {}
function updateData() {}

// 私有方法：下划线开头
function _privateMethod() {}
```

### 10.3 开发检查清单（必做）

#### WXML检查
- [ ] 事件绑定名与JS方法名完全一致（`bindtap="onNextStep"` ↔ `onNextStep()`）
- [ ] `wx:key`使用字段名而非表达式（`wx:key="id"`而非`wx:key="{{item.id}}"`）
- [ ] 属性值使用双引号包裹（`attr="{{value}}"`）
- [ ] 无复杂JS表达式（`filter`/`map`等移至JS处理）

#### JS检查
- [ ] 修改数据使用`this.setData()`而非直接赋值
- [ ] 事件回调使用箭头函数或正确绑定this
- [ ] 嵌套数据更新使用路径字符串或展开运算符
- [ ] 列表渲染数据使用新引用（`[...arr, newItem]`）

#### 调试技巧
```javascript
// 事件不触发时检查
console.log('方法存在:', typeof this.onNextStep === 'function');
console.log('方法名匹配:', e.currentTarget.dataset);

// 数据不更新时检查
console.log('更新前:', this.data.xxx);
this.setData({ xxx: newValue }, () => {
  console.log('更新后:', this.data.xxx);
});
```

### 10.4 开发建议

1. **数据驱动**：始终通过修改数据来驱动视图更新，避免直接操作DOM
2. **组件化**：将可复用的UI封装成组件，提高代码复用性
3. **懒加载**：使用分包加载，减少首屏加载时间
4. **错误处理**：所有API调用都要处理fail情况
5. **用户体验**：添加loading状态，避免用户重复操作
6. **安全性**：敏感数据不要存储在本地，接口做好鉴权

---

## 附录

### A. 官方文档链接

- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [小程序组件库](https://developers.weixin.qq.com/miniprogram/dev/component/)
- [小程序API文档](https://developers.weixin.qq.com/miniprogram/dev/api/)

### B. 版本兼容性

| 基础库版本 | 特性支持 |
|-----------|---------|
| 2.19.4+ | 推荐版本，支持大部分现代特性 |
| 2.10.0+ | 支持 npm 包 |
| 2.6.0+ | 支持自定义组件 computed |
| 2.2.3+ | 支持组件使用 behaviors |

### C. 调试技巧

```javascript
// 开启页面调试
// 在页面配置中添加
{
  "enableDebug": true
}

// 使用 vConsole
// 在真机预览时，点击右上角菜单开启调试

// 性能监控
wx.reportAnalytics('click', {
  button: 'submit'
});
```

---

**文档结束**

本文档涵盖了微信小程序开发的核心语法规范，AI助手在生成代码时应严格遵循以上规范，避免使用不支持的语法特性。