# try-catch

[![JitPack](https://jitpack.io/v/Scarlet-Pan/try-catch.svg)](https://jitpack.io/#Scarlet-Pan/try-catch)

A coroutine-friendly, type-safe wrapper for enhanced error handling, extending the capabilities of `kotlin.runCatching`.

---

## 📦 Install

Add JitPack to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

Add the dependency:

```kotlin
implementation("com.github.Scarlet-Pan:try-catch:1.0.0")
```

> Compatible with Kotlin 1.6+ and JVM 8+.  
> Requires `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4` or higher.

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
suspend fun getUserData(): User = runCatching {
    service.getUserData() // suspending call that may throw IllegalStateException
} catchNonCancel { e: IllegalStateException ->
    Log.w(TAG, "Invalid user state.", e)
    defaultUser
} catchNonCancel { e ->
    Log.w(TAG, "Fail to get user.", e)
    defaultUser
}
```

- If the exception is a `CancellationException`, it is rethrown immediately.
- Otherwise, the handler recovers with a fallback value.

---

## 📄 License
MIT

---

<br><br>

# try-catch（中文）

[![JitPack](https://jitpack.io/v/Scarlet-Pan/try-catch.svg)](https://jitpack.io/#Scarlet-Pan/try-catch)

一个协程友好的、类型安全的异常捕获器，扩展了 `kotlin.runCatching` 的能力。

---

## 📦 安装

在 `settings.gradle.kts` 中添加 JitPack 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

添加依赖：

```kotlin
implementation("com.github.Scarlet-Pan:try-catch:1.0.0")
```

> 兼容 Kotlin 1.6+ 和 JVM 8+。  
> 需要 `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4` 或更高版本。

---

## 🚀 用法

### 基础用法

```kotlin
runCatching {
    // do something
} catch { e: IOException ->
    handleIOException(e)
} catch { e ->
    handle(e)
}
```

支持链式调用多个类型化的异常处理器。关键在于：你能明确区分是否应处理 `kotlinx.coroutines.CancellationException`。

在协程上下文中，**强烈建议使用 `catchNonCancel` 而非 `catch`**，以避免意外吞掉取消信号，破坏结构化并发。

### 使用 `catchNonCancel` 实现协程安全处理

```kotlin
suspend fun getUserData(): User = runCatching {
    service.getUserData() // 挂起调用，可能抛出 IllegalStateException
} catchNonCancel { e: IllegalStateException ->
    Log.w(TAG, "Invalid user state.", e)
    defaultUser
} catchNonCancel { e ->
    Log.w(TAG, "Fail to get user.", e)
    defaultUser
}
```

- 若异常为 `CancellationException`，会立即重新抛出。
- 否则，通过处理器返回兜底值进行恢复。

---

## 📄 许可证
MIT