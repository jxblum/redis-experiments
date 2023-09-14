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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

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
}
