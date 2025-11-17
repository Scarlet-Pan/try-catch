# try-catch

A coroutine-friendly, type-safe wrapper for enhanced error handling, extending the capabilities of `kotlin.runCatching`.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.scarlet-pan/try-catch?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.scarlet-pan/try-catch)  
[![Kotlin](https://img.shields.io/badge/Kotlin-1.6%2B-blue?logo=kotlin&logoColor=white)](https://kotlinlang.org/)  
[![Test Status](https://github.com/Scarlet-Pan/try-catch/actions/workflows/test.yml/badge.svg)](https://github.com/Scarlet-Pan/try-catch/actions/workflows/test.yml)

---

## 🤔 Why Use It?

In coroutine-based code, **handling exceptions without breaking structured concurrency is essential**. Consider these three approaches:

### ❌ Traditional `try-catch` — Swallows cancellation!

```kotlin
suspend fun fetchConfig(): Config = try {
    remoteConfigService.getConfig() // Suspending call that may throw IllegalStateException
} catch (e: Exception) { // ⚠️ Catches CancellationException — breaks structured concurrency!
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

### ⚠️ Using `catch` — Delicate API in coroutines

```kotlin
suspend fun fetchConfig(): Config = runCatching {
    remoteConfigService.getConfig() // Suspending call that may throw IllegalStateException
} catch { e -> // ❌ Avoid in coroutines: handles CancellationException (delicate API)
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

### ✅ Using `catchNonCancel` — Coroutine-safe recovery (recommended)

```kotlin
suspend fun fetchConfig(): Config = runCatching {
    remoteConfigService.getConfig() // Suspending call that may throw IllegalStateException
} catchNonCancel { e: IllegalStateException ->
    Log.w(TAG, "Invalid remote config state.", e)
    defaultConfig
} catchNonCancel { e ->
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

> 💡 **Best Practice**: Always prefer `catchNonCancel` over `catch` in `suspend` functions or any coroutine context.

---

## 📦 Install

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.scarlet-pan:try-catch:1.0.0")
}
```

> ✅ Compatible with Kotlin 1.6+ and JVM 8+.  
> ✅ Requires `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4` or higher.

---

## 🚀 Usage

### Basic Usage

```kotlin
runCatching {
    // do something
} catch { e: IOException ->
    handleIOException(e)
} catch { e ->
    handle(e)
}
```

Supports chaining multiple typed exception handlers. Crucially, it allows you to distinguish whether `kotlinx.coroutines.CancellationException` should be handled or left to propagate.

In coroutine contexts, **it is strongly recommended to use `catchNonCancel`** instead of `catch`, to avoid accidentally swallowing cancellation signals and breaking structured concurrency.

### Coroutine-Safe Handling with `catchNonCancel`

```kotlin
suspend fun fetchConfig(): Config = runCatching {
    remoteConfigService.getConfig() // Suspending call that may throw IllegalStateException
} catchNonCancel { e: IllegalStateException ->
    Log.w(TAG, "Invalid remote config state.", e)
    defaultConfig
} catchNonCancel { e ->
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

- If the exception is a `CancellationException`, it is rethrown immediately.
- Otherwise, the handler recovers with a fallback value.

---

## 📄 License

MIT

---

<br><br>

<div align="center">
  <hr width="80%" />
  <p><em>—— 中文文档 Chinese Documentation ——</em></p>
  <hr width="80%" />
</div>
<br><br>

# try-catch（中文）

一个协程友好的、类型安全的异常处理包装器，扩展了 `kotlin.runCatching` 的能力。

[![Maven Central](https://img.shields.io/maven-central/v/io.github.scarlet-pan/try-catch?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.scarlet-pan/try-catch)  
[![Kotlin](https://img.shields.io/badge/Kotlin-1.6%2B-blue?logo=kotlin&logoColor=white)](https://kotlinlang.org/)  
[![测试状态](https://github.com/Scarlet-Pan/try-catch/actions/workflows/test.yml/badge.svg)](https://github.com/Scarlet-Pan/try-catch/actions/workflows/test.yml)

---

## 🤔 为什么使用它？

在协程代码中，**处理异常的同时不破坏结构化并发至关重要**。请看以下三种方式：

### ❌ 传统 `try-catch` — 会吞掉取消信号！

```kotlin
suspend fun fetchConfig(): Config = try {
    remoteConfigService.getConfig() // 可能抛出 IllegalStateException 的挂起调用
} catch (e: Exception) { // ⚠️ 会捕获 CancellationException — 破坏结构化并发！
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

### ⚠️ 使用 `catch` — 在协程中属于 delicate API

```kotlin
suspend fun fetchConfig(): Config = runCatching {
    remoteConfigService.getConfig() // 可能抛出 IllegalStateException 的挂起调用
} catch { e -> // ❌ 协程中应避免：会处理 CancellationException（delicate API）
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

### ✅ 使用 `catchNonCancel` — 协程安全的恢复方式（推荐）

```kotlin
suspend fun fetchConfig(): Config = runCatching {
    remoteConfigService.getConfig() // 可能抛出 IllegalStateException 的挂起调用
} catchNonCancel { e: IllegalStateException ->
    Log.w(TAG, "Invalid remote config state.", e)
    defaultConfig
} catchNonCancel { e ->
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

> 💡 **最佳实践**：在 `suspend` 函数或任何协程上下文中，始终优先使用 `catchNonCancel` 而非 `catch`。

---

## 📦 安装

在 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation("io.github.scarlet-pan:try-catch:1.0.0")
}
```

> ✅ 兼容 Kotlin 1.6+ 和 JVM 8+。  
> ✅ 需要 `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4` 或更高版本。

---

## 🚀 用法

### 基础用法

```kotlin
runCatching {
    // 执行某些操作
} catch { e: IOException ->
    handleIOException(e)
} catch { e ->
    handle(e)
}
```

支持链式调用多个类型化的异常处理器。关键在于：你可以明确区分是否应处理 `kotlinx.coroutines.CancellationException`。

在协程上下文中，**强烈建议使用 `catchNonCancel` 而非 `catch`**，以避免意外吞掉取消信号，破坏结构化并发。

### 使用 `catchNonCancel` 实现协程安全的异常处理

```kotlin
suspend fun fetchConfig(): Config = runCatching {
    remoteConfigService.getConfig() // 可能抛出 IllegalStateException 的挂起调用
} catchNonCancel { e: IllegalStateException ->
    Log.w(TAG, "Invalid remote config state.", e)
    defaultConfig
} catchNonCancel { e ->
    Log.w(TAG, "Failed to fetch config.", e)
    defaultConfig
}
```

- 若异常为 `CancellationException`，会立即重新抛出；
- 否则，通过处理器返回兜底值进行恢复。

---

## 📄 许可证

MIT
