package dev.scarlet.lang.coroutine

import dev.scarlet.lang.Catcher
import dev.scarlet.lang.coroutine.Catchers.EXPRESSION
import dev.scarlet.lang.coroutine.Catchers.IMPORT
import dev.scarlet.lang.coroutine.Catchers.MESSAGE
import dev.scarlet.lang.getOrElse
import dev.scarlet.lang.orElse
import dev.scarlet.lang.recover
import dev.scarlet.lang.recoverCatching
import kotlinx.coroutines.CancellationException

/**
 * A utility object containing extension helpers for [Catcher].
 *
 * @author Scarlet Pan
 * @version 1.0.0
 */
object Catchers {

    internal const val MESSAGE =
        "It catches CancellationException, breaking cooperative cancellation; per best practices, exempt it or use catchNonCancel."

    internal const val EXPRESSION = "catchNonCancel"

    internal const val IMPORT = "dev.scarlet.lang.coroutine.catchNonCancel"
}

/**
 * Rethrows this throwable immediately if it is a [kotlinx.coroutines.CancellationException];
 * otherwise, does nothing.
 *
 * `CancellationException` is the signal for coroutine cancellation. Swallowing it prevents
 * the coroutine from terminating properly and breaks structured concurrency.
 * This function ensures the cancellation signal is preserved in generic error-handling code.
 */
fun Throwable.checkNonCancel() {
    if (this is CancellationException) throw this
}

/**
 * Adds a cancellation-aware exception handler to a [Result] (supports infix notation).
 *
 * If the current result is a failure and its exception is an [IllegalStateException],
 * **but not a [kotlinx.coroutines.CancellationException]**,
 * then [transform] is invoked to recover a value, which is wrapped in a successful [Catcher].
 * Otherwise—including success, a mismatched exception type, or a cancellation signal—the original
 * result is wrapped unchanged into a [Catcher].
 *
 * Example:
 * ```kotlin
 * suspend fun getUserData(): User = runCatching {
 *     service.getUserData() // suspending call that may throw IllegalStateException
 * } catchNonCancel { e: IllegalStateException ->
 *     Log.w(TAG, "Invalid user state, using default.", e)
 *     defaultUser
 * } catchNonCancel { e ->
 *     Log.w(TAG, "Failed to load user, using default.", e)
 *     defaultUser
 * }
 * ```
 */
@JvmName("catchNonCancelFromResult")
inline infix fun <T> Result<T>.catchNonCancel(transform: (e: IllegalStateException) -> T): Catcher<T> =
    Catcher.of(this).recoverCatching<T, T> { e ->
        e.checkNonCancel()
        when (e) {
            is IllegalStateException -> transform(e)
            else -> throw e
        }
    }.let { Catcher.of(it) }

/**
 * Returns the success value if the [Result] is successful.
 * If it failed with an [Exception] that is **not** a [kotlinx.coroutines.CancellationException],
 * applies [transform] to recover and return a value.
 * If the failure is a [CancellationException], it is rethrown immediately.
 *
 * Example: Safely fetch data in a coroutine with a fallback for non-cancellation exceptions
 * ```kotlin
 * suspend fun getUserData(): User = runCatching {
 *     service.getUserData()
 * } catchNonCancel { e ->
 *     Log.w(TAG, "Failed to load user, using default.", e)
 *     defaultUser
 * }
 * ```
 *
 * @return The original success value, or a fallback value produced by [transform].
 */
@JvmName("catchAllNonCancelFromResult")
infix fun <T> Result<T>.catchNonCancel(transform: (e: Exception) -> T): T =
    Catcher.of(this).catchNonCancel(transform)

/**
 * Adds a cancellation-aware exception handler to a [Catcher] (supports infix notation).
 *
 * If the underlying result is a failure and its exception is an [IllegalStateException],
 * **but not a [kotlinx.coroutines.CancellationException]**,
 * then [transform] is invoked to recover a value, which is wrapped in a successful [Catcher].
 * Otherwise—including success, a mismatched exception type, or a cancellation signal—the current [Catcher] is returned unchanged.
 *
 * Example:
 * ```kotlin
 * suspend fun getUserData(): User = runCatching {
 *     service.getUserData() // suspending call that may throw IllegalStateException
 * } catch { e: IOException ->
 *     throw e
 * } catchNonCancel { e: IllegalStateException ->
 *     Log.w(TAG, "Invalid user state, using default.", e)
 *     defaultUser
 * } catchNonCancel { e ->
 *     Log.w(TAG, "Failed to load user, using default.", e)
 *     defaultUser
 * }
 * ```
 */
@JvmName("catchNonCancelIllegalStateFromCatcher")
infix fun <T> Catcher<T>.catchNonCancel(transform: (e: IllegalStateException) -> T): Catcher<T> =
    recover<T, T> { e ->
        e.checkNonCancel()
        when (e) {
            is IllegalStateException -> transform(e)
            else -> throw e
        }
    }.let { Catcher.of(it) }

/**
 * If the underlying result of this [Catcher] is a failure with an [Exception]
 * (**but not a [kotlinx.coroutines.CancellationException]**),
 * invokes [transform] to recover and returns its result; otherwise returns the original success value.
 * If the exception is a [CancellationException], it is rethrown immediately.
 *
 * Example:
 * ```kotlin
 * suspend fun getUserData(): User = runCatching {
 *     service.getUserData() // suspending call that may throw IllegalStateException
 * } catchNonCancel { e: IllegalStateException ->
 *     Log.w(TAG, "Invalid user state.", e)
 *     defaultUser
 * } catchNonCancel { e ->
 *     Log.w(TAG, "Fail to get user.", e)
 *     defaultUser
 * }
 * ```
 *
 * @return The original success value, or a fallback value produced by [transform].
 */
@JvmName("catchAllNonCancelFromCatcher")
infix fun <T> Catcher<T>.catchNonCancel(transform: (e: Exception) -> T): T = getOrElse { e ->
    e.checkNonCancel()
    when (e) {
        is Exception -> transform(e)
        else -> throw e
    }
}

/**
 * Catches exceptions of type [IllegalStateException] from a [Catcher] (supports infix notation).
 *
 * ⚠️ **Important Warning**: [kotlinx.coroutines.CancellationException] is a subclass of [IllegalStateException].
 * In coroutine contexts, catching [IllegalStateException] without rethrowing cancellation exceptions
 * may unintentionally suppress coroutine cancellation, breaking structured concurrency.
 *
 * This API is explicitly marked as `@Deprecated` not due to incorrect behavior,
 * but as a **safety guardrail**: it forces developers to acknowledge the risk of accidentally
 * catching cancellation signals and encourages the use of a safer alternative.
 *
 * ✅ **Recommended**: In coroutine code, prefer [dev.scarlet.lang.coroutine.catchNonCancel],
 * which automatically skips [kotlinx.coroutines.CancellationException] and only handles genuine business exceptions.
 *
 * @deprecated For coroutine safety, use [dev.scarlet.lang.coroutine.catchNonCancel] instead.
 */
@Deprecated(MESSAGE, replaceWith = ReplaceWith(EXPRESSION, imports = [IMPORT]))
@JvmName("catchIllegalStateFromCatcher")
inline infix fun <T> Catcher<T>.catch(transform: (e: IllegalStateException) -> T): Catcher<T> =
    recoverCatching<T, T> { e ->
        when (e) {
            is IllegalStateException -> transform(e)
            else -> throw e
        }
    }.let { Catcher.of(it) }

/**
 * Catches exceptions of type [Exception] from a [Catcher] and returns a plain value of type [T] (terminal infix operation).
 *
 * ⚠️ **Important Warning**: [kotlinx.coroutines.CancellationException] is a subclass of [Exception].
 * In coroutine contexts, catching [Exception] without rethrowing cancellation exceptions
 * may unintentionally suppress coroutine cancellation, breaking structured concurrency.
 *
 * This API is explicitly marked as `@Deprecated` not due to incorrect behavior,
 * but as a **safety guardrail**: it forces developers to acknowledge the risk of accidentally
 * catching cancellation signals and encourages the use of a safer alternative.
 *
 * ✅ **Recommended**: In coroutine code, prefer building a safe chain with [dev.scarlet.lang.coroutine.catchNonCancel]
 * and terminate it explicitly using [Catcher.orElse] or [orElse(throwIt)].
 *
 * @return The recovered value (if a non-cancellation exception occurred), or the original success value.
 * @deprecated For coroutine safety, avoid catching generic [Exception]. Use cancellation-aware error handling instead.
 */
@Deprecated(MESSAGE, replaceWith = ReplaceWith(EXPRESSION, imports = [IMPORT]))
@JvmName("catchAllFromCatcher")
inline infix fun <T> Catcher<T>.catch(transform: (e: Exception) -> T): T = getOrElse { e ->
    when (e) {
        is Exception -> transform(e)
        else -> throw e
    }
}