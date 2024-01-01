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

import java.util.Objects

/**
 * A [java.util.function.Predicate] which may throw.
 *
 * @param <T> the type of the input to the predicate
</T> */
fun interface CheckedPredicate<T> {
    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return `true` if the input argument matches the predicate, otherwise `false`
     * @throws Exception if an error occurs
     */
    @Throws(Exception::class)
    fun test(t: T): Boolean

    /**
     * Combines this predicate with `that` predicate using logical and (&amp;&amp;).
     *
     * @param that a `CheckedPredicate`
     * @return a new `CheckedPredicate` with `p1.and(p2).test(t) == true :<=> p1.test(t) && p2.test(t) == true`
     * @throws NullPointerException if the given predicate `that` is null
     */
    fun and(that: CheckedPredicate<in T>): CheckedPredicate<T> {
        Objects.requireNonNull(that, "that is null")
        return CheckedPredicate { t: T -> test(t) && that.test(t) }
    }

    /**
     * Negates this predicate.
     *
     * @return A new `CheckedPredicate` with `p.negate().test(t) == true :<=> p.test(t) == false`
     */
    fun negate(): CheckedPredicate<T> {
        return CheckedPredicate { t: T -> !test(t) }
    }

    /**
     * Combines this predicate with `that` predicate using logical or (||).
     *
     * @param that a `CheckedPredicate`
     * @return a new `CheckedPredicate` with `p1.or(p2).test(t) :<=> p1.test(t) || p2.test(t)`
     * @throws NullPointerException if the given predicate `that` is null
     */
    fun or(that: CheckedPredicate<in T>): CheckedPredicate<T> {
        Objects.requireNonNull(that, "that is null")
        return CheckedPredicate { t: T -> test(t) || that.test(t) }
    }

    companion object {
        /**
         * Negates a given predicate by calling `that.negate()`.
         *
         * @param <T>  argument type of `that`
         * @param that a predicate
         * @return the negation of the given predicate `that`
         * @throws NullPointerException if the given predicate `that` is null
        </T> */
        @JvmStatic
        fun <T> not(that: CheckedPredicate<in T>): CheckedPredicate<T> {
            Objects.requireNonNull(that, "that is null")
            return that.negate() as CheckedPredicate<T>
        }
    }
}
