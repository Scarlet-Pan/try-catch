package dev.scarlet.lang

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
class CatcherTest {

    @Test
    fun throwRuntimeException_couldBeCaught() {
        assertThrows("Caught!", Exception::class.java) {
            runCatching {
                throw RuntimeException()
            } catch { e: IOException ->
                fail("Unexpected $e.")
            } catch { e: IllegalArgumentException ->
                fail("Unexpected $e.")
            } catch {
                throw Exception("Caught!", it)
            }
        }
    }

    @Test
    fun throwException_shouldBeCaughtOnly() {
        assertThrows(IllegalArgumentException::class.java) {
            runCatching {
                requireNotNull(null)    // throw IllArgumentException
            } catch { e: IOException ->
                fail("Unexpected $e.")
            } catch { e: NullPointerException ->
                fail("Unexpected $e.")
            } orElse throwIt
        }
    }

    @Test
    fun inputErrorNumber_couldBeCaught() {
        val input = Random.nextInt(-10, 10)
        println("input = $input")
        val result = runCatching {
            require(input >= 0) { "Input is $input, it should not be a negative number." }
            input
        } catch { e: IllegalArgumentException ->
            println("Caught ${e}.")
            -input
        } orElse throwIt
        println("result = $result")
        assertThat(result, equalTo(input.absoluteValue))
    }
}