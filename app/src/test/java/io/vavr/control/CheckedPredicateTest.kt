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

internal class CheckedPredicateTest {
    // -- static .not(CheckedPredicate)
    @Test
    @Throws(Exception::class)
    fun shouldApplyStaticNotToGivenPredicate() {
        Assertions.assertFalse(CheckedPredicate.not(TRUE).test(null))
        Assertions.assertTrue(CheckedPredicate.not(FALSE).test(null))
    }

    // -- .and(CheckedPredicate)
    @Test
    @Throws(Exception::class)
    fun shouldBehaveLikeLogicalAnd() {
        Assertions.assertTrue(TRUE.and(TRUE).test(null))
        Assertions.assertFalse(TRUE.and(FALSE).test(null))
        Assertions.assertFalse(FALSE.and(TRUE).test(null))
        Assertions.assertFalse(FALSE.and(FALSE).test(null))
    }

    @Test
    fun shouldRethrowWhenFirstPredicateFailsUsingAnd() {
        val p = CheckedPredicate { ignored: Any? -> throw ERROR }
        Assertions.assertThrows(ERROR.javaClass) { p.and(TRUE).test(null) }
    }

    @Test
    fun shouldRethrowWhenFirstPredicateReturnsTrueSecondPredicateFailsUsingAnd() {
        val p = CheckedPredicate { ignored: Any? -> throw ERROR }
        Assertions.assertThrows(ERROR.javaClass) { TRUE.and(p).test(null) }
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotRethrowWhenFirstPredicateReturnsFalseSecondPredicateFailsUsingAnd() {
        val p = CheckedPredicate { ignored: Any? -> throw ERROR }
        Assertions.assertFalse(FALSE.and(p).test(null))
    }

    // -- .negate()
    @Test
    @Throws(Exception::class)
    fun shouldBehaveLikeLogicalNegation() {
        Assertions.assertFalse(TRUE.negate().test(null))
        Assertions.assertTrue(FALSE.negate().test(null))
    }

    @Test
    fun shouldRethrowWhenNegatedPredicateFails() {
        val p = CheckedPredicate { ignored: String? -> throw ERROR }
        Assertions.assertThrows(ERROR.javaClass) { p.negate().test(null) }
    }

    // -- .or(CheckedPredicate)
    @Test
    @Throws(Exception::class)
    fun shouldBehaveLikeLogicalOr() {
        Assertions.assertTrue(TRUE.or(TRUE).test(null))
        Assertions.assertTrue(TRUE.or(FALSE).test(null))
        Assertions.assertTrue(FALSE.or(TRUE).test(null))
        Assertions.assertFalse(FALSE.or(FALSE).test(null))
    }

    @Test
    fun shouldRethrowWhenFirstPredicateFailsUsingOr() {
        val p = CheckedPredicate { ignored: Any? -> throw ERROR }
        Assertions.assertThrows(ERROR.javaClass) { p.or(TRUE).test(null) }
    }

    @Test
    fun shouldRethrowWhenFirstPredicateReturnsFalseAndSecondPredicateFailsUsingOr() {
        val p = CheckedPredicate { ignored: Any? -> throw ERROR }
        Assertions.assertThrows(ERROR.javaClass) { FALSE.or(p).test(null) }
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotRethrowWhenFirstPredicateReturnsTrueAndSecondPredicateFailsUsingOr() {
        val p = CheckedPredicate { ignored: Any? -> throw ERROR }
        Assertions.assertTrue(TRUE.or(p).test(null))
    }

    companion object {
        // -- Testees
        private val TRUE = CheckedPredicate { o: Any? -> true }
        private val FALSE = CheckedPredicate { o: Any? -> false }

        // ---- error for testing the exceptional case
        private val ERROR = Error()
    }
}
