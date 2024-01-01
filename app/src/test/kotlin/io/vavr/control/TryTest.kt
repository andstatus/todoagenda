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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Objects
import java.util.Optional
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

@Suppress("deprecation")
internal class TryTest {
    // -- static .of(Callable)
    @Test
    fun shouldCreateSuccessWhenCallingTryOfWithNullValue() {
        Assertions.assertNotNull(Try.of<Any?> { null })
    }

    @Test
    fun shouldCreateSuccessWhenCallingTryOfCallable() {
        Assertions.assertTrue(Try.of { SUCCESS_VALUE }.isSuccess)
    }

    @Test
    fun shouldCreateFailureWhenCallingTryOfCallable() {
        Assertions.assertTrue(Try.of<Any> { throw FAILURE_CAUSE }.isFailure)
    }

    @Test
    fun shouldRethrowLinkageErrorWhenCallingTryOfCallable() {
        Assertions.assertSame(
            LINKAGE_ERROR,
            Assertions.assertThrows(LINKAGE_ERROR.javaClass) {
                Try.of(
                    Callable<Any> { throw LINKAGE_ERROR })
            }
        )
    }

    @Test
    fun shouldRethrowThreadDeathWhenCallingTryOfCallable() {
        Assertions.assertSame(
            THREAD_DEATH,
            Assertions.assertThrows(THREAD_DEATH.javaClass) {
                Try.of(
                    Callable<Any> { throw THREAD_DEATH })
            }
        )
    }

    @Test
    fun shouldRethrowVirtualMachoneErrorWhenCallingTryOfCallable() {
        Assertions.assertSame(
            VM_ERROR,
            Assertions.assertThrows(VM_ERROR.javaClass) {
                Try.of(
                    Callable<Any> { throw VM_ERROR })
            }
        )
    }

    @Test
    fun shouldBeIndistinguishableWhenCreatingFailureWithOfFactoryOrWithFailureFactory() {
        val failure1 = Try.of<String> { throw FAILURE_CAUSE }
        val failure2 = Try.failure<String>(FAILURE_CAUSE)
        Assertions.assertSame(
            FAILURE_CAUSE,
            Assertions.assertThrows(NonFatalException::class.java) { failure1.get() }.cause
        )
        Assertions.assertSame(
            FAILURE_CAUSE,
            Assertions.assertThrows(NonFatalException::class.java) { failure2.get() }.cause
        )
        Assertions.assertSame(failure1.cause, failure2.cause)
        Assertions.assertEquals(failure1.isFailure, failure2.isFailure)
        Assertions.assertEquals(failure1.isSuccess, failure2.isSuccess)
        Assertions.assertEquals(failure1, failure2)
        Assertions.assertEquals(failure1.hashCode(), failure2.hashCode())
        Assertions.assertEquals(failure1.toString(), failure2.toString())
    }

    @Test
    fun shouldBeIndistinguishableWhenCreatingSuccessWithOfFactoryOrWithSuccessFactory() {
        val success1 = Try.of { SUCCESS_VALUE }
        val success2 = Try.success(SUCCESS_VALUE)
        Assertions.assertSame(success1.get(), success2.get())
        Assertions.assertThrows(UnsupportedOperationException::class.java) { success1.cause }
        Assertions.assertThrows(UnsupportedOperationException::class.java) { success2.cause }
        Assertions.assertEquals(success1.isFailure, success2.isFailure)
        Assertions.assertEquals(success1.isSuccess, success2.isSuccess)
        Assertions.assertEquals(success1, success2)
        Assertions.assertEquals(success1.hashCode(), success2.hashCode())
        Assertions.assertEquals(success1.toString(), success2.toString())
    }

    // -- static .run(CheckedRunnable)
    @Test
    fun shouldCreateSuccessWhenCallingTryRunCheckedRunnable() {
        Assertions.assertTrue(Try.run {}.isSuccess)
    }

    @Test
    fun shouldCreateFailureWhenCallingTryRunCheckedRunnable() {
        Assertions.assertTrue(Try.run { throw ERROR }.isFailure)
    }

    @Test
    fun shouldRethrowLinkageErrorWhenCallingTryRunCheckedRunnable() {
        Assertions.assertSame(
            LINKAGE_ERROR,
            Assertions.assertThrows(LINKAGE_ERROR.javaClass) { Try.run { throw LINKAGE_ERROR } }
        )
    }

    @Test
    fun shouldRethrowThreadDeathWhenCallingTryRunCheckedRunnable() {
        Assertions.assertSame(
            THREAD_DEATH,
            Assertions.assertThrows(THREAD_DEATH.javaClass) { Try.run { throw THREAD_DEATH } }
        )
    }

    @Test
    fun shouldRethrowVirtualMachineErrorWhenCallingTryRunCheckedRunnable() {
        Assertions.assertSame(
            VM_ERROR,
            Assertions.assertThrows(VM_ERROR.javaClass) { Try.run { throw VM_ERROR } }
        )
    }

    // -- static .success(Object)
    @Test
    fun shouldCreateSuccessWithNullValue() {
        Assertions.assertNotNull(Try.success<Any?>(null))
    }

    @Test
    fun shouldCreateSuccess() {
        Assertions.assertNotNull(Try.success(SUCCESS_VALUE))
    }

    @Test
    fun shouldVerifyBasicSuccessProperties() {
        Assertions.assertSame(SUCCESS_VALUE, SUCCESS.get())
        Assertions.assertSame(
            "getCause() on Success",
            Assertions.assertThrows(UnsupportedOperationException::class.java) { SUCCESS.cause }.message
        )
        Assertions.assertFalse(SUCCESS.isFailure)
        Assertions.assertTrue(SUCCESS.isSuccess)
        Assertions.assertEquals(Try.success(SUCCESS_VALUE), SUCCESS)
        Assertions.assertEquals(31 + Objects.hashCode(SUCCESS_VALUE), SUCCESS.hashCode())
        Assertions.assertEquals("Success(" + SUCCESS_VALUE + ")", SUCCESS.toString())
    }

    @Test
    fun shouldCreateFailure() {
        Assertions.assertNotNull(Try.failure<Any>(FAILURE_CAUSE))
    }

    @Test
    fun shouldVerifyBasicFailureProperties() {
        Assertions.assertSame(
            FAILURE_CAUSE,
            Assertions.assertThrows(RuntimeException::class.java) { FAILURE.get() }.cause
        )
        Assertions.assertSame(FAILURE_CAUSE, FAILURE.cause)
        Assertions.assertFalse(FAILURE.isSuccess)
        Assertions.assertTrue(FAILURE.isFailure)
        Assertions.assertEquals(Try.failure<Any>(FAILURE_CAUSE), FAILURE)
        Assertions.assertEquals(Objects.hashCode(FAILURE_CAUSE), FAILURE.hashCode())
        Assertions.assertEquals("Failure(" + FAILURE_CAUSE + ")", FAILURE.toString())
    }

    @Test
    fun shouldRethrowLinkageErrorWhenCallingTryFailure() {
        Assertions.assertSame(
            LINKAGE_ERROR,
            Assertions.assertThrows(LINKAGE_ERROR.javaClass) { Try.failure<Any>(LINKAGE_ERROR) }
        )
    }

    @Test
    fun shouldRethrowThreadDeathWhenCallingTryFailure() {
        Assertions.assertSame(
            THREAD_DEATH,
            Assertions.assertThrows(THREAD_DEATH.javaClass) { Try.failure<Any>(THREAD_DEATH) }
        )
    }

    @Test
    fun shouldRethrowVirtualMachineErrorWhenCallingTryFailure() {
        Assertions.assertSame(
            VM_ERROR,
            Assertions.assertThrows(VM_ERROR.javaClass) { Try.failure<Any>(VM_ERROR) }
        )
    }

    // -- .collect(Collector)
    @Test
    fun shouldCollectNone() {
        Assertions.assertEquals("", FAILURE.collect(Collectors.joining()))
    }

    @Test
    fun shouldCollectSome() {
        Assertions.assertEquals(SUCCESS_VALUE, SUCCESS.collect(Collectors.joining()))
    }

    // -- .failed()
    @Test
    fun shouldInvertSuccessByCallingFailed() {
        val testee = SUCCESS.failed()
        Assertions.assertTrue(testee.isFailure)
        Assertions.assertEquals(UnsupportedOperationException::class.java, testee.cause.javaClass)
        Assertions.assertEquals("Success.failed()", testee.cause.message)
    }

    @Test
    fun shouldInvertSuccessWithNullValueByCallingFailed() {
        Assertions.assertNotNull(Try.success<Any?>(null).failed())
    }

    @Test
    fun shouldInvertFailureByCallingFailed() {
        Assertions.assertEquals(Try.success(FAILURE_CAUSE), FAILURE.failed())
    }

    // -- .filter(CheckedPredicate)
    @Test
    fun shouldFilterMatchingPredicateOnFailure() {
        Assertions.assertSame(FAILURE, FAILURE.filter { s: String? -> true })
    }

    @Test
    fun shouldFilterNonMatchingPredicateOnFailure() {
        Assertions.assertSame(FAILURE, FAILURE.filter { s: String? -> false })
    }

    @Test
    fun shouldFilterWithExceptionOnFailure() {
        Assertions.assertSame(FAILURE, FAILURE.filter { t: String? -> throw ERROR })
    }

    @Test
    fun shouldFilterMatchingPredicateOnSuccess() {
        Assertions.assertSame(SUCCESS, SUCCESS.filter { s: String? -> true })
    }

    @Test
    fun shouldFilterNonMatchingPredicateOnSuccess() {
        val testee = SUCCESS.filter { s: String? -> false }
        Assertions.assertTrue(testee.isFailure)
        Assertions.assertEquals(NoSuchElementException::class.java, testee.cause.javaClass)
        Assertions.assertEquals("Predicate does not hold for " + SUCCESS_VALUE, testee.cause.message)
    }

    @Test
    fun shouldFilterWithExceptionOnSuccess() {
        val testee = SUCCESS.filter { t: String? -> throw ERROR }
        Assertions.assertTrue(testee.isFailure)
        Assertions.assertSame(ERROR, testee.cause)
    }

    @Test
    fun shouldFilterSuccessWithNullValue() {
        Assertions.assertNotNull(Try.success<Any?>(null).filter { x: Any? -> true })
    }

    // -- .flatMap(CheckedFunction)
    @Test
    fun shouldFlatMapSuccessToNull() {
        Assertions.assertNull(SUCCESS.flatMap<Any?> { ignored: String? -> FAILURE })
    }

    @Test
    fun shouldFlatMapToSuccessOnSuccess() {
        Assertions.assertSame(SUCCESS, SUCCESS.flatMap { ignored: String? -> SUCCESS })
    }

    @Test
    fun shouldFlatMapToFailureOnSuccess() {
        Assertions.assertSame(FAILURE, SUCCESS.flatMap { ignored: String? -> FAILURE })
    }

    @Test
    fun shouldFlatMapOnFailure() {
        Assertions.assertSame(FAILURE, FAILURE.flatMap<Any> { ignored: String? -> throw ASSERTION_ERROR })
    }

    @Test
    fun shouldCaptureExceptionWhenFlatMappingSuccess() {
        Assertions.assertEquals(Try.failure<Any>(ERROR), SUCCESS.flatMap<Any> { ignored: String? -> throw ERROR })
    }

    @Test
    fun shouldIgnoreExceptionWhenFlatMappingFailure() {
        Assertions.assertSame(FAILURE, FAILURE.flatMap<Any> { ignored: String? -> throw ERROR })
    }

    @Test
    fun shouldFlatMapSuccessWithNullValue() {
        Assertions.assertSame(SUCCESS, Try.success<Any?>(null).flatMap { s: Any? -> SUCCESS })
    }

    @Test
    fun shouldFoldSuccessWhenValueIsNull() {
        Assertions.assertEquals(1, Try.success<Any?>(null).fold({ x: Throwable? -> 0 }) { s: Any? -> 1 })
    }

    @Test
    fun shouldFoldFailureToNull() {
        Assertions.assertNull(FAILURE.fold({ x: Throwable? -> null }) { s: String? -> "" })
    }

    @Test
    fun shouldFoldSuccessToNull() {
        Assertions.assertNull(SUCCESS.fold({ x: Throwable? -> "" }) { s: String? -> null })
    }

    @Test
    fun shouldFoldAndReturnValueIfSuccess() {
        val folded = SUCCESS.fold({ x: Throwable? -> throw ASSERTION_ERROR }) { obj: String -> obj.length }
        Assertions.assertEquals(SUCCESS_VALUE.length, folded)
    }

    @Test
    fun shouldFoldAndReturnAlternateValueIfFailure() {
        val folded = FAILURE.fold({ x: Throwable? -> SUCCESS_VALUE }) { a: String? -> throw ASSERTION_ERROR }
        Assertions.assertEquals(SUCCESS_VALUE, folded)
    }

    // -- .forEach(Consumer)
    @Test
    fun shouldConsumeFailureWithForEach() {
        val list: MutableList<String> = ArrayList()
        FAILURE.forEach(Consumer { e: String -> list.add(e) })
        Assertions.assertEquals(emptyList<Any>(), list)
    }

    @Test
    fun shouldConsumeSuccessWithForEach() {
        val list: MutableList<String> = ArrayList()
        SUCCESS.forEach(Consumer { e: String -> list.add(e) })
        Assertions.assertEquals(listOf(SUCCESS_VALUE), list)
    }

    @Test
    fun shouldThrowNPEWhenConsumingFailureWithForEachAndActionIsNull() {
        Assertions.assertThrows(NullPointerException::class.java) { FAILURE.forEach(null) }
    }

    @Test
    fun shouldThrowNPEWhenConsumingSuccessWithForEachAndActionIsNull() {
        Assertions.assertThrows(NullPointerException::class.java) { SUCCESS.forEach(null) }
    }

    // -- .get()
    @Test
    fun shouldGetOnSuccessWhenValueIsNull() {
        Assertions.assertNull(Try.success<Any?>(null).get())
    }

    @Test
    fun shouldGetOnSuccessWhenValueIsNonNull() {
        Assertions.assertEquals(SUCCESS_VALUE, SUCCESS.get())
    }

    @Test
    fun shouldThrowCauseWrappedInRuntimeExceptionWhenGetOnFailure() {
        Assertions.assertSame(
            FAILURE_CAUSE,
            Assertions.assertThrows(NonFatalException::class.java) { FAILURE.get() }.cause
        )
    }

    @Test
    fun shouldThrowNullCauseWrappedInRuntimeExceptionWhenGetOnFailure() {
        val ex = IllegalStateException("failure")
        Assertions.assertEquals(ex, Assertions.assertThrows(NonFatalException::class.java) {
            Try.failure<Any>(ex).get()
        }.cause)
    }

    @Test
    fun shouldGetCauseOnFailure() {
        Assertions.assertSame(FAILURE_CAUSE, FAILURE.cause)
    }

    @Test
    fun shouldThrowWhenCallingGetCauseOnSuccess() {
        Assertions.assertEquals(
            "getCause() on Success",
            Assertions.assertThrows(UnsupportedOperationException::class.java) { SUCCESS.cause }.message
        )
    }

    // -- .getOrElse(Object)
    @Test
    fun shouldReturnElseWhenGetOrElseOnFailure() {
        Assertions.assertSame(SUCCESS_VALUE, FAILURE.getOrElse(SUCCESS_VALUE))
    }

    @Test
    fun shouldGetOrElseOnSuccess() {
        Assertions.assertSame(SUCCESS_VALUE, SUCCESS.getOrElse(SUCCESS_VALUE2))
    }

    // -- .getOrElseGet(Supplier)
    @Test
    fun shouldReturnElseWhenGetOrElseGetOnFailure() {
        Assertions.assertSame(SUCCESS_VALUE, FAILURE.getOrElseGet { SUCCESS_VALUE })
    }

    @Test
    fun shouldGetOrElseGetOnSuccess() {
        Assertions.assertSame(SUCCESS_VALUE, SUCCESS.getOrElseGet { throw ASSERTION_ERROR })
    }

    // -- .getOrElseThrow(Function)
    @Test
    fun shouldThrowOtherWhenGetOrElseThrowOnFailure() {
        Assertions.assertSame(
            ERROR,
            Assertions.assertThrows(ERROR.javaClass) {
                FAILURE.getOrElseThrow(
                    Function { x: Throwable? -> ERROR })
            }
        )
    }

    @Test
    fun shouldOrElseThrowOnSuccess() {
        Assertions.assertSame(SUCCESS_VALUE, SUCCESS.getOrElseThrow { x: Throwable? -> RuntimeException() })
    }

    // -- .isFailure()
    @Test
    fun shouldDetectFailureIfFailure() {
        Assertions.assertTrue(FAILURE.isFailure)
    }

    @Test
    fun shouldDetectNonFailureIfSuccess() {
        Assertions.assertFalse(SUCCESS.isFailure)
    }

    // -- .isSuccess()
    @Test
    fun shouldDetectSuccessIfSuccess() {
        Assertions.assertTrue(SUCCESS.isSuccess)
    }

    @Test
    fun shouldDetectNonSuccessIfSuccess() {
        Assertions.assertFalse(FAILURE.isSuccess)
    }

    // -- .iterator()
    @Test
    fun shouldIterateSuccess() {
        val testee: Iterator<String> = SUCCESS.iterator()
        Assertions.assertTrue(testee.hasNext())
        Assertions.assertSame(SUCCESS_VALUE, testee.next())
        Assertions.assertFalse(testee.hasNext())
        Assertions.assertThrows(NoSuchElementException::class.java) { testee.next() }
    }

    @Test
    fun shouldIterateFailure() {
        val testee: Iterator<String> = FAILURE.iterator()
        Assertions.assertFalse(testee.hasNext())
        Assertions.assertThrows(NoSuchElementException::class.java) { testee.next() }
    }

    // -- .map(CheckedFunction)
    @Test
    fun shouldMapFailure() {
        Assertions.assertSame(FAILURE, FAILURE.map<Any> { ignored: String? -> throw ASSERTION_ERROR })
    }

    @Test
    fun shouldMapSuccess() {
        Assertions.assertEquals(Try.success(SUCCESS_VALUE + "!"), SUCCESS.map { s: String -> "$s!" })
    }

    @Test
    fun shouldMapSuccessWhenValueIsNull() {
        Assertions.assertEquals(Try.success("null!"), Try.success<Any?>(null).map { s: Any? -> s.toString() + "!" })
    }

    @Test
    fun shouldMapSuccessWithException() {
        Assertions.assertEquals(Try.failure<Any>(ERROR), SUCCESS.map<Any> { ignored: String? -> throw ERROR })
    }

    // -- .mapFailure(CheckedFunction)
    @Test
    fun shouldMapFailureOnFailure() {
        Assertions.assertEquals(Try.failure<Any>(ERROR), FAILURE.mapFailure { x: Throwable? -> ERROR })
    }

    @Test
    fun shouldMapFailureOnFailureWhenCauseIsNull() {
        Assertions.assertEquals(Try.failure<Any>(ERROR), Try.failure<Any>(FAILURE_CAUSE)
            .mapFailure { x: Throwable? -> ERROR })
    }

    @Test
    fun shouldMapFailureWithExceptionOnFailure() {
        Assertions.assertEquals(Try.failure<Any>(ERROR), FAILURE.mapFailure { x: Throwable? -> throw ERROR })
    }

    @Test
    fun shouldMapFailureOnSuccess() {
        Assertions.assertSame(SUCCESS, SUCCESS.mapFailure { x: Throwable? -> throw ASSERTION_ERROR })
    }

    // -- .onFailure(Consumer)
    @Test
    fun shouldConsumeThrowableWhenCallingOnFailureGivenFailure() {
        val sideEffect: MutableList<Throwable> = ArrayList()
        FAILURE.onFailure { e: Throwable -> sideEffect.add(e) }
        Assertions.assertEquals(listOf(FAILURE_CAUSE), sideEffect)
    }

    @Test
    fun shouldNotHandleUnexpectedExceptionWhenCallingOnFailureGivenFailure() {
        Assertions.assertSame(
            ERROR,
            Assertions.assertThrows(ERROR.javaClass) { FAILURE.onFailure { ignored: Throwable? -> throw ERROR } }
        )
    }

    @Test
    fun shouldDoNothingWhenCallingOnFailureGivenSuccess() {
        Assertions.assertSame(SUCCESS, SUCCESS.onFailure { x: Throwable? -> throw ASSERTION_ERROR })
    }

    // -- .onSuccess(Consumer)
    @Test
    fun shouldConsumeValueWhenCallingOnSuccessGivenSuccess() {
        val sideEffect: MutableList<String> = ArrayList()
        SUCCESS.onSuccess { e: String -> sideEffect.add(e) }
        Assertions.assertEquals(listOf(SUCCESS_VALUE), sideEffect)
    }

    @Test
    fun shouldNotHandleUnexpectedExceptionWhenCallingOnSuccessGivenSuccess() {
        Assertions.assertSame(
            ERROR,
            Assertions.assertThrows(ERROR.javaClass) { SUCCESS.onSuccess { ignored: String? -> throw ERROR } }
        )
    }

    @Test
    fun shouldDoNothingWhenCallingOnSuccessGivenFailure() {
        Assertions.assertSame(FAILURE, FAILURE.onSuccess { x: String? -> throw ASSERTION_ERROR })
    }

    // -- .orElse(Callable)
    @Test
    fun shouldReturnSelfOnOrElseIfSuccess() {
        Assertions.assertSame(SUCCESS, SUCCESS.orElse { null })
    }

    @Test
    fun shouldReturnAlternativeOnOrElseIfFailure() {
        Assertions.assertSame(SUCCESS, FAILURE.orElse { SUCCESS })
    }

    @Test
    fun shouldCaptureErrorOnOrElseIfFailure() {
        Assertions.assertSame(ERROR, FAILURE.orElse { throw ERROR }.cause)
    }

    // -- .recover(Class, CheckedFunction)
    @Test
    fun shouldRecoverWhenFailureMatchesExactly() {
        Assertions.assertEquals(SUCCESS, FAILURE.recover(FAILURE_CAUSE.javaClass) { x: Exception? -> SUCCESS_VALUE })
    }

    @Test
    fun shouldRecoverWhenFailureIsAssignableFrom() {
        Assertions.assertEquals(SUCCESS, FAILURE.recover(Throwable::class.java) { x: Throwable? -> SUCCESS_VALUE })
    }

    @Test
    fun shouldNotRecoverWhenFailureIsNotAssignableFrom() {
        Assertions.assertEquals(
            FAILURE, FAILURE.recover(
                VirtualMachineError::class.java
            ) { x: VirtualMachineError? -> SUCCESS_VALUE })
    }

    @Test
    fun shouldRecoverWhenSuccess() {
        Assertions.assertSame(SUCCESS, SUCCESS.recover(Throwable::class.java) { x: Throwable? -> "FAILURE" })
    }

    // -- .recoverWith(Class, CheckedFunction)
    @Test
    fun shouldRecoverWithWhenFailureMatchesExactly() {
        Assertions.assertSame(SUCCESS, FAILURE.recoverWith(FAILURE_CAUSE.javaClass) { x: Exception? -> SUCCESS })
    }

    @Test
    fun shouldRecoverWithSuccessWhenFailureIsAssignableFrom() {
        Assertions.assertSame(SUCCESS, FAILURE.recoverWith(Throwable::class.java) { x: Throwable? -> SUCCESS })
    }

    @Test
    fun shouldRecoverWithFailureWhenFailureIsAssignableFrom() {
        val failure = Try.failure<String>(ERROR)
        Assertions.assertSame(failure, FAILURE.recoverWith(Throwable::class.java) { x: Throwable? -> failure })
    }

    @Test
    fun shouldNotRecoverWithWhenFailureIsNotAssignableFrom() {
        Assertions.assertSame(
            FAILURE, FAILURE.recoverWith(
                VirtualMachineError::class.java
            ) { x: VirtualMachineError? -> SUCCESS })
    }

    @Test
    fun shouldRecoverWithWhenSuccess() {
        Assertions.assertSame(SUCCESS, SUCCESS.recoverWith(Throwable::class.java) { x: Throwable? -> FAILURE })
    }

    @Test
    fun shouldCaptureExceptionWhenRecoverWithFailure() {
        Assertions.assertEquals(
            Try.failure<Any>(ERROR), FAILURE.recoverWith(
                Throwable::class.java
            ) { ignored: Throwable? -> throw ERROR })
    }

    // -- .stream()
    @Test
    fun shouldStreamFailure() {
        Assertions.assertEquals(emptyList<Any>(), FAILURE.stream().collect(Collectors.toList()))
    }

    @Test
    fun shouldStreamSuccess() {
        Assertions.assertEquals(listOf(SUCCESS_VALUE), SUCCESS.stream().collect(Collectors.toList()))
    }

    // -- .toOptional()
    @Test
    fun shouldConvertFailureToOptional() {
        Assertions.assertEquals(Optional.empty<Any>(), FAILURE.toOptional())
    }

    @Test
    fun shouldConvertSuccessOfNonNullToOptional() {
        Assertions.assertEquals(Optional.of(SUCCESS_VALUE), SUCCESS.toOptional())
    }

    @Test
    fun shouldConvertSuccessOfNullToOptional() {
        Assertions.assertEquals(Optional.empty<Any>(), Try.success<Any?>(null).toOptional())
    }

    // -- .transform(CheckedFunction, CheckedFunction)
    @Test
    fun shouldTransformFailureWhenCauseIsNull() {
        Assertions.assertSame(
            SUCCESS, Try.failure<Any>(ERROR).transform(
                { x: Throwable? -> SUCCESS }) { s: Any? -> throw ASSERTION_ERROR })
    }

    @Test
    fun shouldTransformSuccessWhenValueIsNull() {
        Assertions.assertSame(
            SUCCESS, Try.success<Any?>(null).transform(
                { x: Throwable? -> throw ASSERTION_ERROR }) { s: Any? -> SUCCESS })
    }

    @Test
    fun shouldTransformFailureToNull() {
        Assertions.assertEquals(FAILURE2,
            FAILURE.transform<Any>({ x: Throwable? -> FAILURE2 }) { s: String? -> throw ASSERTION_ERROR })
    }

    @Test
    fun shouldTransformSuccessToNull() {
        Assertions.assertEquals(
            FAILURE2,
            SUCCESS.transform<Any>({ x: Throwable? -> throw ASSERTION_ERROR }) { s: String? -> FAILURE2 })
    }

    @Test
    fun shouldTransformAndReturnValueIfSuccess() {
        val transformed =
            SUCCESS.transform({ x: Throwable? -> throw ASSERTION_ERROR }) { s: String -> Try.success(s.length) }
        Assertions.assertEquals(Try.success(SUCCESS_VALUE.length), transformed)
    }

    @Test
    fun shouldTransformAndReturnAlternateValueIfFailure() {
        val transformed = FAILURE.transform(
            { x: Throwable? -> SUCCESS }) { a: String? -> throw ASSERTION_ERROR }
        Assertions.assertSame(SUCCESS, transformed)
    }

    @Test
    fun shouldTransformFailureAndCaptureException() {
        val transformed = FAILURE.transform<String>(
            { x: Throwable? -> throw ERROR }) { s: String? -> throw ASSERTION_ERROR }
        Assertions.assertEquals(Try.failure<Any>(ERROR), transformed)
    }

    @Test
    fun shouldTransformSuccessAndCaptureException() {
        val transformed = SUCCESS.transform<String>(
            { x: Throwable? -> throw ASSERTION_ERROR }) { s: String? -> throw ERROR }
        Assertions.assertEquals(Try.failure<Any>(ERROR), transformed)
    }

    // -- Object.equals(Object)
    @Test
    fun shouldEqualFailureIfObjectIsSame() {
        Assertions.assertEquals(FAILURE, FAILURE)
    }

    @Test
    fun shouldNotEqualFailureIfObjectIsNotSame() {
        Assertions.assertNotEquals(Try.failure<Any>(Error()), Try.failure<Any>(Error()))
    }

    @Test
    fun shouldEqualSuccessIfObjectIsSame() {
        Assertions.assertEquals(SUCCESS, SUCCESS)
    }

    @Test
    fun shouldNotEqualFailureAndSuccess() {
        Assertions.assertNotEquals(SUCCESS, FAILURE)
    }

    @Test
    fun shouldNotEqualSuccessAndFailure() {
        Assertions.assertNotEquals(FAILURE, SUCCESS)
    }

    @Test
    fun shouldNotEqualSuccessIfValuesDiffer() {
        Assertions.assertNotEquals(Try.success("1"), Try.success(1))
    }

    // -- Object.hashCode()
    @Test
    fun shouldHashFailure() {
        Assertions.assertEquals(Objects.hashCode(FAILURE_CAUSE), FAILURE.hashCode())
    }

    @Test
    fun shouldHashSuccess() {
        Assertions.assertEquals(31 + Objects.hashCode(SUCCESS_VALUE), SUCCESS.hashCode())
    }

    @Test
    fun shouldHashSuccessWithNullValue() {
        Assertions.assertEquals(31 + Objects.hashCode(null), Try.success<Any?>(null).hashCode())
    }

    // -- Object.toString()
    @Test
    fun shouldConvertFailureToString() {
        Assertions.assertEquals("Failure(" + FAILURE_CAUSE + ")", FAILURE.toString())
    }

    @Test
    fun shouldConvertSuccessToString() {
        Assertions.assertEquals("Success(" + SUCCESS_VALUE + ")", SUCCESS.toString())
    }

    @Test
    fun shouldConvertSuccessWithNullValueToString() {
        Assertions.assertEquals("Success(null)", Try.success<Any?>(null).toString())
    }

    // Serialization
    @Test
    @Throws(IOException::class, ClassNotFoundException::class)
    fun shouldSerializeFailure() {
        val testee = deserialize<Try<String>>(serialize(FAILURE))
        Assertions.assertSame(FAILURE.cause.javaClass, testee.cause.javaClass)
        Assertions.assertEquals(FAILURE.cause.message, testee.cause.message)
    }

    @Test
    @Throws(IOException::class, ClassNotFoundException::class)
    fun shouldSerializeSuccess() {
        Assertions.assertEquals(SUCCESS, deserialize(serialize(SUCCESS)))
    }

    companion object {
        // -- Testees
        private const val SUCCESS_VALUE = "success1"
        private const val SUCCESS_VALUE2 = "success2"
        private val SUCCESS = Try.success(SUCCESS_VALUE)
        private val FAILURE_CAUSE: Exception = IllegalStateException("failure")
        private val FAILURE = Try.failure<String>(FAILURE_CAUSE)
        private val FAILURE_CAUSE2: Exception = IllegalStateException("failure2")
        private val FAILURE2 = Try.failure<String>(FAILURE_CAUSE2)
        private val ERROR = Error()
        private val ASSERTION_ERROR = AssertionError("unexpected")
        private val LINKAGE_ERROR = LinkageError()
        private val THREAD_DEATH = ThreadDeath()
        private val VM_ERROR: VirtualMachineError = object : VirtualMachineError() {
            private val serialVersionUID = 1L
        }

        @Throws(IOException::class)
        private fun serialize(obj: Any): ByteArray {
            ByteArrayOutputStream().use { buf ->
                ObjectOutputStream(buf).use { stream ->
                    stream.writeObject(obj)
                    return buf.toByteArray()
                }
            }
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        private fun <T> deserialize(data: ByteArray): T {
            ObjectInputStream(ByteArrayInputStream(data)).use { stream -> return stream.readObject() as T }
        }
    }
}
