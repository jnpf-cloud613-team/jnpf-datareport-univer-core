> 特别说明：源码、JDK、数据库、Redis等安装或存放路径禁止包含中文、空格、特殊字符等

## 一 环境要求

| 类目 | 版本说明或建议           |
| --- |------------------|
| 硬件 | 开发电脑建议使用I3及以上CPU，16G及以上内存  |
| 操作系统 | Windows 10/11，MacOS |
| JDK | 默认使用JDK 21，如需要切换JDK 8/11/17版本请参考文档调整代码，推荐使用 `OpenJDK`，如 `Liberica JDK`、`Eclipse Temurin`、`Alibaba Dragonwell`、`BiSheng`等发行版； |
| Maven | 依赖管理工具，推荐使用 `3.6.3` 及以上版本  |
| IDE   | 代码集成开发环境，推荐使用 `IDEA2024` 及以上版本，兼容 `Eclipse`、 `Spring Tool Suite` 等IDE工具 |

## 二 关联项目
> 为以下项目提供基础依赖

| 项目            | 分支            | 说明         |
|---------------|---------------|------------|
| jnpf-java-datareport-univer | v6.1.x-stable  | Univer报表源码 |

## 三 选择是否加密

> 是否加密将会影响 `jnpf-java-datareport-univer` 项目的启动方式
> 如果此项目选择加密 `jnpf-java-datareport-univer` 项目也需要选择加密

### 3.1 不使用加密

在IDEA中, 展开右侧 `Maven` 中 `Profiles` 去除勾选 `encrypted` 选项, 再点击Maven `刷新` 图标刷新 Maven

### 3.2 使用加密

在IDEA中, 展开右侧 `Maven` 中 `Profiles` 勾选 `encrypted` 选项, 再点击Maven `刷新` 图标刷新 Maven

#### 3.2.1 安装加密插件

在IDEA中，双击右侧 `Maven` 中 `jnpf-datareport-univer-core` > `clean` 将会自动安装加密打包插件

## 四 使用方式

### 4.1 本地安装

在IDEA中，双击右侧 `Maven` 中 `jnpf-datareport-univer-core` > `Lifecycle` > `install`，将 `jnpf-datareport-univer-core` 包安装至本地

### 4.2 发布到私服

在IDEA中，双击右侧 `Maven` 中 `jnpf-datareport-univer-core` > `Lifecycle` > `deploy` 发布至私服。