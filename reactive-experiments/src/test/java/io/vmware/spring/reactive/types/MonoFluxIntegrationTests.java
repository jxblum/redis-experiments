/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.vmware.spring.reactive.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.ThrowableOperation;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration Tests for {@literal Project Reactor} {@link Mono} and {@link Flux} Reactive Types.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see reactor.core.publisher.Flux
 * @see reactor.core.publisher.Mono
 * @since 0.1.0
 */
class MonoFluxIntegrationTests {

	@Test
	void fluxMappedToNullElementsIgnoresNullCorrectly() {

		AtomicReference<Optional<RuntimeException>> causeReference = new AtomicReference<>(Optional.empty());

		List<Integer> evenIntegers = new CopyOnWriteArrayList<>();

		Flux.fromIterable(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
			.mapNotNull(element -> element % 2 == 0 ? element : null)
			.subscribe(evenIntegers::add, cause ->
				causeReference.set(Optional.of(new RuntimeException("Reactive Stream processing failed", cause))));

		assertThat(causeReference.get()).isEmpty();
		assertThat(evenIntegers).containsExactly(0, 2, 4, 6, 8);
	}

	@Test
	void fluxWithStreamOfElementsContainingNullThrowsNullPointerException() {

		AtomicReference<Optional<RuntimeException>> causeReference = new AtomicReference<>(Optional.empty());

		// None of Flux.filter(..), Flux.handle(..) nor Flux.mapNotNull(..) prevent a NullPointerException
		// when the data source contains null elements.
		Flux.fromIterable(Arrays.asList(0, 1, 2, null, 4, null, null, null, 8, null))
			.filter(Objects::nonNull)
			.handle((value, sink) -> sink.next(value != null ? value : 0))
			.mapNotNull(Function.identity())
			.onErrorContinue((cause, value) -> {})
			.subscribe(System.out::println, cause ->
				causeReference.set(Optional.of(new RuntimeException("Reactive Stream processing failed", cause))));

		Exception exception = assertThrows(RuntimeException.class,
			() -> causeReference.get().ifPresent(cause -> {
				//cause.printStackTrace();
				throw cause;
			}));

		assertThat(exception).isInstanceOf(RuntimeException.class);
		assertThat(exception).hasMessage("Reactive Stream processing failed");
		assertThat(exception).hasCauseInstanceOf(NullPointerException.class);
		assertThat(exception.getCause()).hasMessageContaining("iterator returned a null value");
		assertThat(exception.getCause()).hasNoCause();
	}

	@Test
	@SuppressWarnings("all")
	void monoFromSupplierOnSubscriptionWorksAsExpected() throws InterruptedException{

		AtomicInteger count = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(1);

		Supplier<Integer> blockingIntegerSupplier = () -> {

			System.out.println("Waiting on CountDownLatch...");

			ObjectUtils.doOperationSafely(ThrowableOperation.fromVoidReturning(args -> latch.await()));

			System.out.println("CountDownLatch.await() returned...");

			return count.incrementAndGet();
		};

		/*
		Disposable disposable = Mono.fromSupplier(blockingIntegerSupplier)
			.subscribe(System.out::println);
		*/

		Disposable disposable = Mono.fromFuture(CompletableFuture.supplyAsync(blockingIntegerSupplier))
			.subscribe(System.out::println);

		assertThat(disposable).isNotNull();
		assertThat(count.get()).isZero();
		assertThat(latch.getCount()).isOne();

		System.out.println("YOU ARE HERE");

		latch.countDown();

		while (!disposable.isDisposed()) {
			synchronized (disposable) {
				TimeUnit.MILLISECONDS.timedWait(disposable, 500);
			}
		}

		assertThat(count.get()).isOne();
		assertThat(latch.getCount()).isZero();
	}

	@Test
	@SuppressWarnings("unchecked")
	void monoEmittingValueWithMapWorksAsExpected() {

		Function<String, String> mockFunction = mock(Function.class);

		doAnswer(invocationOnMock -> {
			String argument = invocationOnMock.getArgument(0);
			return String.valueOf(argument).toUpperCase();
		}).when(mockFunction).apply(any());

		assertThat(Mono.fromSupplier(() -> "test").map(mockFunction).block()).isEqualTo("TEST");

		verify(mockFunction, times(1)).apply(eq("test"));
		verifyNoMoreInteractions(mockFunction);
	}

	@Test
	@SuppressWarnings("unchecked")
	void monoEmittingNullWithMapWorksAsExpected() {

		Function<Object, Object> mockFunction = mock(Function.class);

		doAnswer(invocationOnMock ->
			ObjectUtils.requireObject(invocationOnMock.getArgument(0), "Argument is required")
		).when(mockFunction).apply(any());

		assertThat(Mono.fromSupplier(() -> null).map(mockFunction).block()).isNull();

		verify(mockFunction, never()).apply(any());
		verifyNoInteractions(mockFunction);
	}

	@Test
	void monoEmittingValueWithThenWorksAsExpected() {

		Mono<String> thenMono = Mono.fromSupplier(() -> "mock");

		assertThat(Mono.fromSupplier(() -> "test").then(thenMono).block()).isEqualTo("mock");
	}

	@Test
	void monoEmittingNullWithThenWorksAsExpected() {

		Mono<String> thenMono = Mono.fromSupplier(() -> "nonnull");

		assertThat(Mono.fromSupplier(() -> null).then(thenMono).block()).isEqualTo("nonnull");
	}
}
