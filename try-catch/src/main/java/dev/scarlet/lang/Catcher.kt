package dev.scarlet.lang

import dev.scarlet.lang.coroutine.Catchers.EXPRESSION
import dev.scarlet.lang.coroutine.Catchers.IMPORT
import dev.scarlet.lang.coroutine.Catchers.MESSAGE
import dev.scarlet.lang.coroutine.catch as _catch

/**
 * An exception catcher—a coroutine-friendly, type-safe wrapper for enhanced error handling,
 * extending the capabilities of [kotlin.runCatching].
 *
 * ### Basic Usage
 * ```kotlin
 * runCatching {
 *     // do something
 * } catch { e: IOException ->
 *     handleIOException(e)
 * } catch { e ->
 *     handle(e)
 * }
 * ```
 *
 * Supports chaining multiple typed exception handlers. Crucially, it allows you to distinguish
 * whether [kotlinx.coroutines.CancellationException] should be handled or left to propagate.
 *
 * In coroutine contexts, **it is strongly recommended to use [dev.scarlet.lang.coroutine.catchNonCancel]**
 * instead of [catch], to avoid accidentally swallowing cancellation signals and breaking structured concurrency.
 *
 * @param T The type of the wrapped result value.
 * @see dev.scarlet.lang.coroutine.catchNonCancel
 * @author Scarlet Pan
 * @version 1.0.0
 */
@JvmInline
value class Catcher<T> private constructor(private val result: Result<T>) {

    companion object {

        /** Creates a [Catcher] from a [Result]. */
        @JvmStatic
        fun <T> of(result: Result<T>) = Catcher(result)

    }

    /** Converts to a [Result]. */
    fun asResult() = result

}

/**
 * Provides a fallback handler that is invoked only if none of the preceding [catch] blocks handled the exception.
 *
 * If the [Result] is successful, its value is returned directly.
 * If it failed, the original exception is passed to [fallback], which may either return a value or throw an exception.
 *
 * @param fallback A function that receives the unhandled exception.
 * @return The final result value.
 * @throws Throwable if [fallback] throws an exception.
 */
inline fun <R, T : R> Catcher<T>.getOrElse(fallback: (Throwable) -> T) =
    asResult().getOrElse(fallback)

/**
 * If the operation failed, recovers by transforming the exception into a successful value.
 * If already successful, returns the original value unchanged.
 */
inline fun <R, T : R> Catcher<T>.recover(transform: (Throwable) -> T) =
    asResult().recover(transform)

/**
 * If the operation failed, attempts recovery using [transform], which may itself throw.
 * The result of [transform] is wrapped in a new [Catcher] (success or failure).
 * If already successful, returns the original value unchanged.
 */
inline fun <R, T : R> Catcher<T>.recoverCatching(transform: (Throwable) -> T) =
    asResult().recoverCatching(transform)

/**
 * Adds an exception handler for a specific exception type [E] to a [Result] (supports infix notation).
 *
 * If the current [Result] represents a failure and its exception is of type [E] (or a subclass),
 * the provided [transform] is invoked to handle the exception, and its return value is wrapped in a successful [Catcher].
 * Otherwise, the original [Result] is wrapped in a [Catcher] and passed through unchanged.
 *
 * Example:
 * ```kotlin
 * runCatching {
 *     // do something
 * } catch { e: UnknownHostException ->
 *     handle(e)
 * } catch { e: IOException ->
 *     handle(e)
 * }
 * ```
 *
 * ⚠️ Note: This function catches all matching exceptions, including [kotlinx.coroutines.CancellationException].
 * In coroutine contexts, avoid specifying [E] as [kotlinx.coroutines.CancellationException]
 * or any of its supertypes—such as [IllegalStateException], [RuntimeException], or [Exception]—
 * unless you rethrow the cancellation exception. Failing to do so may interfere with
 * structured concurrency and prevent proper coroutine cancellation.
 * For safe exception handling in coroutines, prefer using
 * [dev.scarlet.lang.coroutine.catchNonCancel].
 */
@JvmName("catchFromResult")
inline infix fun <T, reified E : Exception> Result<T>.catch(transform: (e: E) -> T): Catcher<T> =
    Catcher.of(this).catch<T, E>(transform)

/**
 * Catches exceptions of type [IllegalStateException] from a [Result] (supports infix notation).
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
@Suppress("DEPRECATION")
@JvmName("catchIllegalStateFromResult")
inline infix fun <T> Result<T>.catch(transform: (e: IllegalStateException) -> T): Catcher<T> =
    Catcher.of(this).catch(transform)

/**
 * Catches any [Exception] from a [Result] and recovers by returning a plain value of type [T].
 *
 * ⚠️ **Critical Warning**: [kotlinx.coroutines.CancellationException] is a subclass of [Exception].
 * In coroutine contexts, this function will catch cancellation exceptions. If not rethrown in [func],
 * it silently breaks structured concurrency and prevents proper cancellation.
 *
 * Unlike intermediate handlers that return [Catcher<T>], **this function acts as the terminal step**
 * in an infix exception-handling chain: it returns a plain [T] and does not allow further chaining
 * (e.g., no additional [catch] or [recover] calls).
 *
 * It is marked `@Deprecated` as a proactive safety measure to discourage blanket-catch of [Exception]
 * in coroutine-aware code.
 *
 * ✅ **Recommended approach**:
 * Use [dev.scarlet.lang.coroutine.catchNonCancel] for safe, composable error handling,
 * and terminate the chain explicitly with [Catcher.orElse] or [orElse(throwIt)].
 *
 * @return The recovered value (if a non-cancellation exception occurred), or the original success value.
 * @deprecated For coroutine safety, avoid catching generic [Exception]. Prefer precise, cancellation-aware error handling.
 */
@Deprecated(MESSAGE, replaceWith = ReplaceWith(EXPRESSION, imports = [IMPORT]))
@Suppress("DEPRECATION")
@JvmName("catchAllFromResult")
inline infix fun <T> Result<T>.catch(func: (e: Exception) -> T): T = Catcher.of(this).catch(func)


/**
 * Adds an exception handler for a specific exception type [E] to a [Catcher] (supports infix notation).
 *
 * If the wrapped result is a failure and its exception is of type [E] (or a subclass),
 * the provided [transform] is invoked to handle the exception, and its return value is wrapped in a successful [Catcher].
 * Otherwise, the original [Catcher] is returned unchanged.
 *
 * Example:
 * ```kotlin
 * runCatching {
 *     // do something
 * } catch { e: UnknownHostException ->
 *     handle(e)
 * } catch { e: IOException ->
 *     handle(e)
 * }
 * ```
 *
 * ⚠️ Note: This function catches all matching exceptions, including [kotlinx.coroutines.CancellationException].
 * In coroutine contexts, avoid specifying [E] as [kotlinx.coroutines.CancellationException]
 * or any of its supertypes—such as [IllegalStateException], [RuntimeException], or [Exception]—
 * unless you rethrow the cancellation exception. Failing to do so may interfere with
 * structured concurrency and prevent proper coroutine cancellation.
 * For safe exception handling in coroutines, prefer using
 * [dev.scarlet.lang.coroutine.catchNonCancel].
 */
@JvmName("catchFromCatcher")
inline infix fun <T, reified E : Exception> Catcher<T>.catch(transform: (e: E) -> T): Catcher<T> =
    recoverCatching<T, T> {
        when {
            it is E -> transform(it)
            else -> throw it
        }
    }.let { Catcher.of(it) }

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
@Suppress("DEPRECATION")
@JvmName("catchIllegalStateFromCatcher")
inline infix fun <T> Catcher<T>.catch(transform: (e: IllegalStateException) -> T): Catcher<T> =
    _catch(transform)

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
@Suppress("DEPRECATION")
@JvmName("catchAllFromCatcher")
inline infix fun <T> Catcher<T>.catch(transform: (e: Exception) -> T): T = _catch(transform)

/**
 * Terminal fallback for any unhandled [Throwable] in the [Catcher] chain.
 * Returns the success value if present; otherwise, applies [fallback] to the failure and returns its result.
 *
 * ⚠️ Receives all throwables, including [kotlinx.coroutines.CancellationException]. In coroutines, avoid swallowing cancellation—
 * rethrow it or use safer alternatives like [dev.scarlet.lang.coroutine.catchNonCancel] + explicit handling.
 *
 * @return A plain [T]; this ends the chaining.
 */
infix fun <T> Catcher<T>.orElse(fallback: (Throwable) -> T) = getOrElse(fallback)

/**
 * A reusable terminal handler that rethrows any given [Throwable].
 *
 * Useful as a default fallback in [Catcher.orElse] to propagate unhandled errors.
 */
val throwIt by lazy { { e: Throwable -> throw e } }