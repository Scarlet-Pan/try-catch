package dev.scarlet.lang.coroutine

import dev.scarlet.lang.catch
import dev.scarlet.lang.orElse
import dev.scarlet.lang.throwIt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * @author Scarlet Pan
 * @version 1.0.0
 */
class CatchersTest {

    @Test
    fun throwCancellationException_shouldNotBeCaught() = runBlocking {
        val job = launch {
            runCatching {
                delay(1000)
            } catchNonCancel {
                fail("CancellationException should not be caught!")
            }
            fail("Cancel failure!")
        }
        delay(100)
        job.cancel()
    }

    @Test
    fun throwAnyOtherException_couldBeCaught() {
        runCatching {
            throw IllegalArgumentException("Error state.")
        } catchNonCancel { e: IllegalStateException ->
            fail("$e should not be caught by other case.")
        } catchNonCancel {
            MatcherAssert.assertThat(it, instanceOf(IllegalArgumentException::class.java))
        }
    }

    @Test
    fun throwAnyOtherException_couldBeCaughtAccurately() {
        runCatching {
            throw IllegalStateException("Error state.")
        } catch { e: IllegalArgumentException ->
            fail("Unexpected ${e}.")
        } catchNonCancel { e: IllegalStateException ->
            println("Caught ${e}.")
        } catchNonCancel {
            fail("$it should not be caught by other case.")
        }
    }

    @Test
    fun inputErrorNumber_thenUseOtherInstead() {
        val input = Random.nextInt(-10, 10)
        println("input = $input")
        val result = runCatching {
            require(input >= 0) { "Input is $input, it should not be a negative number." }
            input
        } catchNonCancel { e ->
            println("Caught ${e}.")
            -input
        }
        println("result = $result")
        MatcherAssert.assertThat(result, equalTo(input.absoluteValue))
    }

    @Test
    fun inputErrorNumber_couldBeThrown() {
        assertThrows(IllegalArgumentException::class.java) {
            val input = Random.nextInt(Int.MIN_VALUE, -1)
            println("input = $input")
            val result = runCatching {
                require(input >= 0) { "Input is $input, it should not be a negative number." }
                input
            } catch { e: IOException ->
                throw AssertionError("$e should not be caught by other case.")
            } orElse throwIt
            println("result = $result")
            fail("It's impossible to take result=${result}.")
        }
    }

    @Test
    fun throwError_badCodeSample() {
        assertThrows(Error::class.java) {
            runCatching {
                throw Error()
            } catch { e: IllegalStateException ->   // Discouraged: does not exempt CancellationException; prefer catchNonCancel
                fail("Unexpected ${e}.")
            } catch { e: IllegalStateException ->   // Discouraged: does not exempt CancellationException; prefer catchNonCancel
                fail("Unexpected ${e}.")
            } catch { e ->                          // Discouraged: does not exempt CancellationException; prefer catchNonCancel
                fail("Unexpected ${e}.")
            }
        }
    }

    @Test
    fun throwCancellationException_badCodeSample() = runBlocking {
        val job = launch {
            runCatching {
                delay(1000)
            } catch { e: IllegalStateException ->   // Discouraged: does not exempt CancellationException; prefer catchNonCancel
                throw e
            } catch { e: IllegalStateException ->   // Discouraged: does not exempt CancellationException; prefer catchNonCancel
                throw e
            } catch { e ->                          // Discouraged: does not exempt CancellationException; prefer catchNonCancel
                throw e
            }
            fail("Cancel failure!")
        }
        delay(100)
        job.cancel()
    }
}