/* ____  ______________  ________________________  __________
 * \   \/   /      \   \/   /   __/   /      \   \/   /      \
 *  \______/___/\___\______/___/_____/___/\___\______/___/\___\
 *
 * Copyright 2018 Vavr, http://vavr.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vavr.control

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CheckedFunctionTest {
    // -- static .identity()
    @Test
    @Throws(Exception::class)
    fun shouldCreateIdentity() {
        val f = CheckedFunction.identity<Any?>()
        Assertions.assertNull(f.apply(null))
        Assertions.assertEquals(1, f.apply(1))
    }

    // -- .andThen(CheckedFunction)
    @Test
    @Throws(Exception::class)
    fun shouldApplyOneCheckedFunctionAndThenAnotherCheckedFunction() {
        val before = CheckedFunction { i: Int -> i % 2 == 0 }
        val after = CheckedFunction { obj: Boolean -> obj.toString() }
        val f = before.andThen(after)
        Assertions.assertEquals("true", f.apply(0))
        Assertions.assertEquals("false", f.apply(1))
    }

    @Test
    fun shouldNotApplyAfterWhenBeforeThrowsWhenCombiningWithAndThen() {
        val before = CheckedFunction<Int?, Boolean> { ignored: Int? -> throw Exception("before") }
        val after = CheckedFunction<Boolean, String> { ignored: Boolean? -> throw AssertionError("after called") }
        val f = before.andThen(after)
        Assertions.assertEquals(
            "before",
            Assertions.assertThrows(Exception::class.java) { f.apply(null) }.message
        )
    }

    @Test
    fun shouldApplyBeforeWhenAfterThrowsWhenCombiningWithAndThen() {
        val before = CheckedFunction { ignored: Int? -> true }
        val after = CheckedFunction<Boolean, String> { ignored: Boolean? -> throw Exception("after") }
        val f = before.andThen(after)
        Assertions.assertEquals(
            "after",
            Assertions.assertThrows(Exception::class.java) { f.apply(null) }.message
        )
    }

    // -- .apply(Object)
    @Test
    fun shouldBeAbleToThrowCheckedException() {
        val f = CheckedFunction<Any?, Any?> { ignored: Any? -> throw Exception() }
        Assertions.assertThrows(Exception::class.java) { f.apply(null) }
    }

    // -- .compose(CheckedFunction)
    @Test
    @Throws(Exception::class)
    fun shouldApplyOneCheckedFunctionComposedWithAnotherCheckedFunction() {
        val before = CheckedFunction { i: Int -> i % 2 == 0 }
        val after = CheckedFunction { obj: Boolean -> obj.toString() }
        val f = after.compose(before)
        Assertions.assertEquals("true", f.apply(0))
        Assertions.assertEquals("false", f.apply(1))
    }

    @Test
    fun shouldNotApplyAfterWhenBeforeThrowsWhenCombiningWithCompose() {
        val before = CheckedFunction<Int?, Boolean> { ignored: Int? -> throw Exception("before") }
        val after = CheckedFunction<Boolean, String> { ignored: Boolean? -> throw AssertionError("before called") }
        val f = after.compose(before)
        Assertions.assertEquals(
            "before",
            Assertions.assertThrows(Exception::class.java) { f.apply(null) }.message
        )
    }

    @Test
    fun shouldApplyBeforeWhenAfterThrowsWhenCombiningWithCompose() {
        val before = CheckedFunction { ignored: Int? -> true }
        val after = CheckedFunction<Boolean, String> { ignored: Boolean? -> throw Exception("after") }
        val f = after.compose(before)
        Assertions.assertEquals(
            "after",
            Assertions.assertThrows(Exception::class.java) { f.apply(null) }.message
        )
    }
}
