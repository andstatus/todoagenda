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
 * A [java.util.function.Function] which may throw.
 *
 * @param <T> the type of this function's domain
 * @param <R> the type of this function's codomain, i.e. the return type
</R></T> */
fun interface CheckedFunction<T, R> {
    /**
     * Applies this function to one argument and returns the result.
     *
     * @param t argument of type `T`
     * @return the result of the function application
     * @throws Exception if something goes wrong applying this function to the given argument
     */
    @Throws(Exception::class)
    fun apply(t: T): R

    /**
     * Returns a composed function that first applies this to the given argument and then applies
     * `after` to the result.
     *
     * @param <U> return type of after
     * @param after the function applied after this
     * @return a function composed of this and `after`
     * @throws NullPointerException if `after` is null
    </U> */
    fun <U> andThen(after: CheckedFunction<in R, out U>): CheckedFunction<T, U> {
        Objects.requireNonNull(after, "after is null")
        return CheckedFunction { t: T -> after.apply(apply(t)) }
    }

    /**
     * Returns a composed function that first applies `before` to the given argument and then applies this
     * to the result.
     *
     * @param <U> argument type of before
     * @param before the function applied before this
     * @return a function composed of `before` and this
     * @throws NullPointerException if `before` is null
    </U> */
    fun <U> compose(before: CheckedFunction<in U, out T>): CheckedFunction<U, R> {
        Objects.requireNonNull(before, "before is null")
        return CheckedFunction { u: U -> apply(before.apply(u)) }
    }

    companion object {
        /**
         * Returns the identity `CheckedFunction`, i.e. the function that returns its input.
         *
         * @param <T> argument type (and return type) of the identity function
         * @return the identity `CheckedFunction`
        </T> */
        @JvmStatic
        fun <T> identity(): CheckedFunction<T, T> {
            return CheckedFunction { t: T -> t }
        }
    }
}
