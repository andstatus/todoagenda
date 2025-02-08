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

import java.io.Serializable
import java.lang.Error
import java.util.Collections
import java.util.Objects
import java.util.Optional
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Stream

/**
 * The `Try` control gives us the ability to write safe code without focusing on try-catch blocks in the presence
 * of exceptions.
 *
 *
 * A real-world use-case is to defer error handling and recovery to outer applications layers. With `Try`, we
 * achieve this by capturing the error state of a computation and passing it around.
 *
 *
 * `Try` has one of two states, `Success` and `Failure`. A `Success` wraps the value of a given
 * computation, a `Failure` wraps an exception that occurred during the computation.
 *
 *
 * The following exceptions are considered to be fatal/non-recoverable and will be re-thrown:
 *
 *
 *  * [LinkageError]
 *  * [ThreadDeath]
 *  * [VirtualMachineError] (i.e. [OutOfMemoryError] or [StackOverflowError])
 *
 *
 * <h2>Creation</h2>
 *
 * Try is intended to be used as value which contains the result of a computation. For that purpose, [.of]
 * is called. See also [.success] and [.failure].
 *
 *
 * However, some use `Try` as syntactic sugar for try-catch blocks that only perform side-effects. For that purpose,
 * [.run] is called. This variant does not contain a value but is still able to observe, handle
 * and recover an error state.
 *
 * <h2>Capturing exceptions</h2>
 *
 * Opposed to other types, higher-order functions that *transform* this type take checked functions, or more
 * precisely, lambdas or method references that may throw
 * [checked exceptions](https://www.baeldung.com/java-lambda-exceptions).
 *
 *
 * We intentionally do not provide alternate methods that take unchecked functions (like `map` vs `mapTry`).
 * Instead we make it explicit on the API layer that exceptions are properly handled when transforming values.
 * An exception will not escape the context of a `Try` in these cases.
 *
 *
 * Another reason for not providing unchecked variants is that Vavr's higher-order functions always take the most
 * general argument type. Checked functions that may throw `Throwable` are more general than unchecked functions
 * because unchecked exceptions are restricted to throw runtime exceptions.
 *
 *
 * Higher-order functions that return a concrete value, like [.getOrElseGet] and
 * [.fold], will not handle exceptions when calling function arguments. The parameter
 * types make this clear.
 *
 * <h2>Transforming a Try</h2>
 *
 * Transformations that are focused on a successful state are:
 *
 *
 *  * [.map]
 *  * [.flatMap]
 *  * [.filter]
 *
 *
 * Transformations that are focused on a failed state are:
 *
 *
 *  * [.failed] - transforms a failure into a success
 *  * [.mapFailure] - transforms the cause of a failure
 *  * [.orElse] - performs another computation in the case of a failure
 *  * [.recover] - recovers a specific failure by providing an alternate value
 *  * [.recoverWith] - recovers a specific failure by performing an alternate computation
 *
 *
 * More general transformations that take both states (success/failure) into account are:
 *
 *
 *  * [.fold]
 *  * [.transform]
 *
 *
 * <h2>Handling the state of a Try</h2>
 *
 * Opposed to Java (see [Optional.ifPresent]), we are able to chain one or more of the following actions:
 *
 *
 *  * [.onFailure]
 *  * [.onSuccess]
 *
 *
 * <h2>Getting the value of a Try</h2>
 *
 * At some point, we might need to operate on the unwrapped value of a Try. These are our options to reduce a successful
 * or failed state to a value:
 *
 *
 *  * [.fold] - **safe** alternative to get()
 *  * [.get] - **unsafe**, throws in the case of a failure
 *  * [.getOrElse]
 *  * [.getOrElseGet]
 *  * [.getOrElseThrow]
 *
 *
 * <h2>Try with resources</h2>
 *
 * It is also possible to use `Try` directly with [AutoCloseable] resources:
 *
 * <pre>`final Try<T> calc = Try.of(() -> {
 * try (final ac1 = someAutoCloseable1(); ...; final acn = someAutoCloseableN()) {
 * return doSth(ac1, ..., acn);
 * } finally {
 * doSth();
 * }
 * });
`</pre> *
 *
 * @param <T> Value type of a successful computation
 * @author Daniel Dietrich
</T> */
abstract class Try<T>
private constructor() : Iterable<T>, Serializable {
    /**
     * Collects the underlying value (if present) using the provided `collector`.
     *
     *
     * Shortcut for `.stream().collect(collector)`.
     *
     * @param <A>       the mutable accumulation type of the reduction operation
     * @param <R>       the result type of the reduction operation
     * @param collector Collector performing reduction
     * @return the reduction result of type `R`
     * @throws NullPointerException if the given `collector` is null
    </R></A> */
    fun <R, A> collect(collector: Collector<in T, A, R>?): R {
        return stream().collect(collector)
    }

    /**
     * Inverts this `Try`.
     *
     * @return `Success(throwable)` if this is a `Failure(throwable)`,
     * otherwise a `Failure(new UnsupportedOperationException("Success.failed()"))` if this is a
     * `Success`.
     */
    fun failed(): Try<Throwable> {
        return if (isFailure) {
            Success(cause)
        } else {
            failure(UnsupportedOperationException("Success.failed()"))
        }
    }

    /**
     * Returns `this` if this is a Failure or this is a Success and the value satisfies the predicate.
     *
     *
     * Returns a new Failure, if this is a Success and the value does not satisfy the Predicate or an exception
     * occurs testing the predicate. The returned Failure wraps a [NoSuchElementException] instance.
     *
     * @param predicate A checked predicate
     * @return a `Try` instance
     * @throws NullPointerException if `predicate` is null
     */
    fun filter(predicate: CheckedPredicate<in T>): Try<T> {
        Objects.requireNonNull(predicate, "predicate is null")
        if (isSuccess) {
            try {
                val value = get() as T
                if (!predicate.test(value)) {
                    return failure(NoSuchElementException("Predicate does not hold for $value"))
                }
            } catch (t: Throwable) {
                return failure(t)
            }
        }
        return this
    }

    /**
     * FlatMaps the value of a Success or returns a Failure.
     *
     * @param mapper A mapper
     * @param <U>    The new component type
     * @return a `Try`
     * @throws NullPointerException if `mapper` is null
    </U> */
    fun <U> flatMap(mapper: CheckedFunction<in T, out Try<out U>>): Try<U> {
        Objects.requireNonNull(mapper, "mapper is null")
        return if (isSuccess) {
            try {
                mapper.apply(get()) as Try<U>
            } catch (t: Throwable) {
                failure(t)
            }
        } else {
            this as Try<U>
        }
    }

    /**
     * Folds either the `Failure` or the `Success` side of the Try value.
     *
     * @param ifFailure maps the cause if this is a `Failure`
     * @param ifSuccess maps the value if this is a `Success`
     * @param <U>       type of the folded value
     * @return A value of type U
     * @throws NullPointerException if one of the given `ifFailure` or `ifSuccess` is null
    </U> */
    fun <U> fold(ifFailure: Function<in Throwable?, out U>, ifSuccess: Function<in T, out U>): U {
        Objects.requireNonNull(ifFailure, "ifFailure is null")
        Objects.requireNonNull(ifSuccess, "ifSuccess is null")
        return if (isSuccess) ifSuccess.apply(get()) else ifFailure.apply(cause)
    }

    /**
     * Gets the result of this Try if this is a `Success` or throws if this is a `Failure`.
     *
     *
     * If this is a `Failure`, it will throw cause wrapped in a [NonFatalException].
     *
     * @return The computation result if this is a `Success`
     * @throws NonFatalException if this is a [Failure]
     */
    @Throws(NonFatalException::class)
    abstract fun get(): T

    @get:Throws(UnsupportedOperationException::class)
    @get:Deprecated("TODO: description")
    abstract val cause: Throwable

    /**
     * Returns the underlying value if present, otherwise `other`.
     *
     * @param other An alternative value.
     * @return A value of type `T`
     */
    fun getOrElse(other: T): T {
        return if (isSuccess) get() else other
    }

    /**
     * Returns the underlying value if present, otherwise the result of `other.get()`.
     *
     * @param supplier A `Supplier` of an alternative value.
     * @return A value of type `T`
     * @throws NullPointerException if the given `other` is null
     */
    fun getOrElseGet(supplier: Supplier<out T>): T {
        Objects.requireNonNull(supplier, "supplier is null")
        return if (isSuccess) get() else supplier.get()
    }

    /**
     * Returns the underlying value if present, otherwise throws a user-specific exception.
     *
     * @param exceptionProvider provides a user-specific exception
     * @param <X>               exception type
     * @return A value of type `T`
     * @throws X                    if this is a `Failure`
     * @throws NullPointerException if the given `exceptionProvider` is null
    </X> */
    @Throws()
    fun <X : Throwable> getOrElseThrow(exceptionProvider: Function<in Throwable, out X>): T {
        Objects.requireNonNull(exceptionProvider, "exceptionProvider is null")
        return if (isSuccess) {
            get()
        } else {
            throw exceptionProvider.apply(cause)
        }
    }

    /**
     * Checks if this is a Failure.
     *
     * @return true, if this is a Failure, otherwise false, if this is a Success
     */
    abstract val isFailure: Boolean

    /**
     * Checks if this is a Success.
     *
     * @return true, if this is a Success, otherwise false, if this is a Failure
     */
    abstract val isSuccess: Boolean
    override fun iterator(): Iterator<T> {
        return if (isSuccess) setOf(get() as T).iterator() else Collections.emptyIterator()
    }

    /**
     * Runs the given checked function if this is a [Success],
     * passing the result of the current expression to it.
     * If this expression is a [Failure] then it'll return a new
     * [Failure] of type R with the original exception.
     *
     *
     * The main use case is chaining checked functions using method references:
     *
     * <pre>
     * `
     * Try.of(() -> 0)
     * .map(x -> 1 / x); // division by zero
    ` *
    </pre> *
     *
     * @param <U>    The new component type
     * @param mapper A checked function
     * @return a `Try`
     * @throws NullPointerException if `mapper` is null
    </U> */
    fun <U> map(mapper: CheckedFunction<in T, out U>): Try<U> {
        Objects.requireNonNull(mapper, "mapper is null")
        return if (isSuccess) {
            try {
                success(mapper.apply(get()))
            } catch (t: Throwable) {
                failure(t)
            }
        } else {
            this as Try<U>
        }
    }

    /**
     * Maps the cause to a new exception if this is a `Failure` or returns this instance if this is a `Success`.
     *
     * @param mapper A function that maps the cause of a failure to another exception.
     * @return A new `Try` if this is a `Failure`, otherwise this.
     * @throws NullPointerException if the given `mapper` is null
     */
    fun mapFailure(mapper: CheckedFunction<in Throwable?, out Throwable>): Try<T> {
        Objects.requireNonNull(mapper, "mapper is null")
        return if (isFailure) {
            try {
                failure(mapper.apply(cause))
            } catch (t: Throwable) {
                failure(t)
            }
        } else {
            this
        }
    }

    /**
     * Consumes the cause if this is a [Try.Failure].
     *
     * <pre>`// (does not print anything)
     * Try.success(1).onFailure(System.out::println);
     *
     * // prints "java.lang.Error"
     * Try.failure(new Error()).onFailure(System.out::println);
    `</pre> *
     *
     * @param action An exception consumer
     * @return this
     * @throws NullPointerException if `action` is null
     */
    fun onFailure(action: Consumer<in Throwable>): Try<T> {
        Objects.requireNonNull(action, "action is null")
        if (isFailure) {
            action.accept(cause)
        }
        return this
    }

    /**
     * Consumes the value if this is a [Try.Success].
     *
     * <pre>`// prints "1"
     * Try.success(1).onSuccess(System.out::println);
     *
     * // (does not print anything)
     * Try.failure(new Error()).onSuccess(System.out::println);
    `</pre> *
     *
     * @param action A value consumer
     * @return this
     * @throws NullPointerException if `action` is null
     */
    fun onSuccess(action: Consumer<in T>): Try<T> {
        Objects.requireNonNull(action, "action is null")
        if (isSuccess) {
            action.accept(get())
        }
        return this
    }

    /**
     * Returns this `Try` in the case of a `Success`, otherwise `other.call()`.
     *
     * @param callable a [Callable]
     * @return a `Try` instance
     */
    fun orElse(callable: Callable<out Try<out T>>): Try<T> {
        Objects.requireNonNull(callable, "callable is null")
        return if (isSuccess) {
            this
        } else {
            try {
                callable.call() as Try<T>
            } catch (x: Throwable) {
                failure(x)
            }
        }
    }

    /**
     * Returns `this`, if this is a `Success` or this is a `Failure` and the cause is not assignable
     * from `cause.getClass()`.
     *
     *
     * Otherwise tries to recover the exception of the failure with `recoveryFunction`.
     *
     * <pre>`// = Success(13)
     * Try.of(() -> 27/2).recover(ArithmeticException.class, x -> Integer.MAX_VALUE);
     *
     * // = Success(2147483647)
     * Try.of(() -> 1/0)
     * .recover(Error.class, x -> -1)
     * .recover(ArithmeticException.class, x -> Integer.MAX_VALUE);
     *
     * // = Failure(java.lang.ArithmeticException: / by zero)
     * Try.of(() -> 1/0).recover(Error.class, x -> Integer.MAX_VALUE);
    `</pre> *
     *
     * @param <X>              Exception type
     * @param exceptionType    The specific exception type that should be handled
     * @param recoveryFunction A recovery function taking an exception of type `X`
     * @return a `Try` instance
     * @throws NullPointerException if `exception` is null or `recoveryFunction` is null
    </X> */
    fun <X : Throwable> recover(exceptionType: Class<X>, recoveryFunction: CheckedFunction<in X, out T>): Try<T> {
        Objects.requireNonNull(exceptionType, "exceptionType is null")
        Objects.requireNonNull(recoveryFunction, "recoveryFunction is null")
        if (isFailure) {
            val cause = cause
            if (exceptionType.isAssignableFrom(cause!!.javaClass)) {
                return of { recoveryFunction.apply(cause as X) }
            }
        }
        return this
    }

    /**
     * Returns `this`, if this is a `Success` or this is a `Failure` and the cause is not assignable
     * from `cause.getClass()`. Otherwise tries to recover the exception of the failure with `recoveryFunction` **which returns Try**.
     * If [Try.isFailure] returned by `recoveryFunction` function is `true` it means that recovery cannot take place due to some circumstances.
     *
     * <pre>`// = Success(13)
     * Try.of(() -> 27/2).recoverWith(ArithmeticException.class, x -> Try.success(Integer.MAX_VALUE));
     *
     * // = Success(2147483647)
     * Try.of(() -> 1/0)
     * .recoverWith(Error.class, x -> Try.success(-1))
     * .recoverWith(ArithmeticException.class, x -> Try.success(Integer.MAX_VALUE));
     *
     * // = Failure(java.lang.ArithmeticException: / by zero)
     * Try.of(() -> 1/0).recoverWith(Error.class, x -> Try.success(Integer.MAX_VALUE));
    `</pre> *
     *
     * @param <X>              Exception type
     * @param exceptionType    The specific exception type that should be handled
     * @param recoveryFunction A recovery function taking an exception of type `X` and returning Try as a result of recovery.
     * If Try is [Try.isSuccess] then recovery ends up successfully. Otherwise the function was not able to recover.
     * @return a `Try` instance
     * @throws NullPointerException if `exceptionType` or `recoveryFunction` is null
     * @throws Error                if the given recovery function `recoveryFunction` throws a fatal error
    </X> */
    fun <X : Throwable> recoverWith(
        exceptionType: Class<X>,
        recoveryFunction: CheckedFunction<in X, out Try<out T>>
    ): Try<T> {
        Objects.requireNonNull(exceptionType, "exceptionType is null")
        Objects.requireNonNull(recoveryFunction, "recoveryFunction is null")
        if (isFailure) {
            val cause = cause
            if (exceptionType.isAssignableFrom(cause!!.javaClass)) {
                return try {
                    recoveryFunction.apply(cause as X) as Try<T>
                } catch (t: Throwable) {
                    failure(t)
                }
            }
        }
        return this
    }

    /**
     * Converts this `Try` to a [Stream].
     *
     * @return `Stream.of(get()` if this is a success, otherwise `Stream.empty()`
     */
    fun stream(): Stream<T> {
        return if (isSuccess) Stream.of(get()) else Stream.empty()
    }

    /**
     * Converts this `Try` to an [Optional].
     *
     * @return `Optional.ofNullable(get())` if this is a success, otherwise `Optional.empty()`
     */
    abstract fun toOptional(): Optional<T>

    /**
     * Transforms this `Try` by applying either `ifSuccess` to this value or `ifFailure` to this cause.
     *
     * @param ifFailure maps the cause if this is a `Failure`
     * @param ifSuccess maps the value if this is a `Success`
     * @param <U>       type of the transformed value
     * @return A new `Try` instance
     * @throws NullPointerException if one of the given `ifSuccess` or `ifFailure` is null
    </U> */
    fun <U> transform(
        ifFailure: CheckedFunction<in Throwable?, out Try<out U>>,
        ifSuccess: CheckedFunction<in T, out Try<out U>>
    ): Try<U>? {
        Objects.requireNonNull(ifFailure, "ifFailure is null")
        Objects.requireNonNull(ifSuccess, "ifSuccess is null")
        return try {
            if (isSuccess) ifSuccess.apply(get()) as Try<U> else ifFailure.apply(cause) as Try<U>
        } catch (t: Throwable) {
            failure(t)
        }
    }

    /**
     * Checks if this `Try` is equal to the given object `o`.
     *
     * @param that an object, may be null
     * @return true, if `this` and `that` both are a success and the underlying values are equal
     * or if `this` and `that` both are a failure and the underlying causes refer to the same object.
     * Otherwise it returns false.
     */
    abstract override fun equals(that: Any?): Boolean

    /**
     * Computes the hash of this `Try`.
     *
     * @return `31 + Objects.hashCode(get())` if this is a success, otherwise `Objects.hashCode(getCause())`
     */
    abstract override fun hashCode(): Int

    /**
     * Returns a string representation of this `Try`.
     *
     * @return `"Success(" + get() + ")"` if this is a success, otherwise `"Failure(" + getCause() + ")"`
     */
    abstract override fun toString(): String

    /**
     * A succeeded Try.
     *
     * @param <T> component type of this Success
    </T> */
    private class Success<T>
    /**
     * Constructs a Success.
     *
     * @param value The value of this Success.
     */(private val value: T) : Try<T>(), Serializable {
        override fun get(): T {
            return value as T
        }

        override val cause: Throwable
            get() = throw UnsupportedOperationException("get cause on Success")

        override val isFailure: Boolean = false

        override val isSuccess: Boolean = true
        override fun toOptional(): Optional<T> = Optional.ofNullable(get()) as Optional<T>


        override fun equals(obj: Any?): Boolean {
            return obj === this || obj is Success<*> && value == obj.value
        }

        override fun hashCode(): Int {
            return 31 + Objects.hashCode(value)
        }

        override fun toString(): String {
            return "Success($value)"
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    /**
     * A failed Try. It represents an exceptional state.
     *
     *
     * The cause of type `Throwable` is internally stored for further processing.
     *
     * @param <T> component type of this Failure
    </T> */
    private class Failure<T>(cause: Throwable) : Try<T>(), Serializable {
        override val cause: Throwable

        /**
         * Constructs a Failure.
         *
         * @param cause                 A cause of type Throwable, may be null.
         * @throws NullPointerException if `cause` is null
         * @throws Error                if the given `cause` is fatal, i.e. non-recoverable
         */
        init {
            if (cause is LinkageError || cause is ThreadDeath || cause is VirtualMachineError) {
                throw (cause as Error?)!!
            }
            this.cause = cause
        }

        @Throws(NonFatalException::class)
        override fun get(): T = throw NonFatalException(cause)

        override val isFailure: Boolean = true

        override val isSuccess: Boolean = false
        override fun toOptional(): Optional<T> = Optional.empty<T>() as Optional<T>

        override fun equals(obj: Any?): Boolean {
            return obj === this || obj is Failure<*> && obj.cause == cause
        }

        override fun hashCode(): Int {
            return Objects.hashCode(cause)
        }

        override fun toString(): String {
            return "Failure($cause)"
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 1L

        /**
         * Creates a Try of a Callable.
         *
         * @param callable A supplier that may throw a checked exception
         * @param <T>      Component type
         * @return `Success(callable.call())` if no exception occurs, otherwise `Failure(cause)` if a
         * non-fatal error occurs calling `callable.call()`.
         * @throws Error if the cause of the [Failure] is fatal, i.e. non-recoverable
        </T> */
        @JvmStatic
        fun <T> of(callable: Callable<out T>): Try<T> {
            Objects.requireNonNull(callable, "callable is null")
            return try {
                success(callable.call())
            } catch (t: Throwable) {
                failure(t)
            }
        }

        /**
         * Runs a `CheckedRunnable` and captures any non-fatal exception in a `Try`.
         *
         *
         * Because running a unit of work is all about performing side-effects rather than returning a value,
         * a `Try<Void>` is created.
         *
         * @param runnable A checked runnable, i.e. a runnable that may throw a checked exception.
         * @return `Success(null)` if no exception occurs, otherwise `Failure(throwable)` if an exception occurs
         * calling `runnable.run()`.
         * @throws Error if the cause of the [Failure] is fatal, i.e. non-recoverable
         */
        @JvmStatic
        fun run(runnable: CheckedRunnable): Try<Unit> {
            Objects.requireNonNull(runnable, "runnable is null")
            return try {
                runnable.run()
                success(Unit) // null represents the absence of an value, i.e. Void
            } catch (t: Throwable) {
                failure(t)
            }
        }

        /**
         * Creates a [Success] that contains the given `value`. Shortcut for `new Success<>(value)`.
         *
         * @param value A value.
         * @param <T>   Type of the given `value`.
         * @return A new `Success`.
        </T> */
        fun <T> success(value: T): Try<T> {
            return Success(value)
        }

        /**
         * Creates a [Failure] that contains the given `exception`. Shortcut for `new Failure<>(exception)`.
         *
         * @param exception An exception.
         * @param <T>       Component type of the `Try`.
         * @return A new `Failure`.
         * @throws Error if the given `exception` is fatal, i.e. non-recoverable
        </T> */
        @JvmStatic
        fun <T> failure(exception: Throwable): Try<T> {
            return Failure(exception)
        }
    }
}
