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
package io.vmware.spring.jedis.client.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cp.elements.lang.MathUtils;
import org.cp.elements.lang.Nameable;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.ArrayUtils;
import org.cp.elements.util.CollectionUtils;
import org.cp.elements.util.stream.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import io.vmware.spring.jedis.client.support.RedisCommandsResolver;
import io.vmware.spring.jedis.client.support.ScanIterator;
import io.vmware.spring.jedis.client.tests.support.AbstractRedisIntegrationTests;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.StringCommands;

/**
 * Integration Tests for the Redis {@literal SCAN} Command.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see redis.clients.jedis.JedisPool
 * @see io.vmware.spring.jedis.client.tests.support.AbstractRedisIntegrationTests
 * @see <a href="https://redis.io/commands/scan/">Redis SCAN command</a>
 * @see <a href="https://www.baeldung.com/redis-list-available-keys">List All Available Redis Keys</a>
 * @since 0.1.0
 */
@Getter
@SpringBootTest
@SuppressWarnings("unused")
public class RedisScanIntegrationTests extends AbstractRedisIntegrationTests {

	private static final int ITERATIONS = 10;

	private static final AtomicReference<Boolean> databaseInitialized = new AtomicReference<>(null);

	private static final Set<String> PEOPLE_DOE_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		"people:doe:jon",
		"people:doe:jane",
		"people:doe:bob",
		"people:doe:cookie",
		"people:doe:dill",
		"people:doe:fro",
		"people:doe:hoe",
		"people:doe:jane",
		"people:doe:joe",
		"people:doe:jon",
		"people:doe:lan",
		"people:doe:moe",
		"people:doe:pie",
		"people:doe:sour"
	)));

	@BeforeEach
	public void setupDataInRedisDatabase() {

		databaseInitialized.updateAndGet(initialized -> {

			if (!Boolean.TRUE.equals(initialized)) {
				runInRedis(jedis -> {

					StringCommands stringCommands =
						RedisCommandsResolver.resolveStringCommands(jedis);

					stringCommands.set("people:bloom:jon", "Jon Bloom");
					stringCommands.set("people:doe:bob", "Bob Doe");
					stringCommands.set("people:doe:cookie", "Cookie Doe");
					stringCommands.set("people:doe:dill", "Dill Doe");
					stringCommands.set("people:doe:fro", "Fro Doe");
					stringCommands.set("people:doe:hoe", "Hoe Doe");
					stringCommands.set("people:doe:jane", "Jane Doe");
					stringCommands.set("people:doe:joe", "Joe Doe");
					stringCommands.set("people:doe:jon", "Jon Doe");
					stringCommands.set("people:doe:lan", "Lan Doe");
					stringCommands.set("people:doe:moe", "Moe Doe");
					stringCommands.set("people:doe:pie", "Pie Doe");
					stringCommands.set("people:doe:sour", "Sour Doe");
					stringCommands.set("people:frost:jack", "Jack Frost");
					stringCommands.set("people:handy:jack", "Jack Handy");
					stringCommands.set("people:handy:jill", "Jill Handy");
					stringCommands.set("people:handy:mandy", "Jill Handy");
					stringCommands.set("people:handy:sandy", "Jill Handy");

					return true;
				});
			}

			return false;
		});
	}

	@Autowired
	private JedisPool jedisPool;

	@Test
	public void dataIsPresent() {

		runInRedis(jedis -> {

			assertThat(jedis.get("people:doe:lan")).isEqualTo("Lan Doe");
			assertThat(jedis.get("people:doe:pie")).isEqualTo("Pie Doe");
			assertThat(jedis.get("people:handy:jack")).isEqualTo("Jack Handy");

			return true;
		});
	}

	@Test
	public void scanIsCorrect() {

		runInRedis(jedis -> {

			int count = 3;

			ScanIterator scanIterator = ScanIterator.from(this::runInRedis, "people:doe:*", count);

			List<String> scanResults = new ArrayList<>();

			for (int size = count; scanIterator.hasNext(); size += count) {

				List<String> partialScanResults = scanIterator.next();

				assertThat(partialScanResults).isNotEmpty();
				assertThat(partialScanResults).hasSizeLessThanOrEqualTo(count);
				assertThat(scanResults.addAll(partialScanResults)).isTrue();
 				assertThat(scanResults).hasSizeLessThanOrEqualTo(size);
				assertThat(PEOPLE_DOE_KEYS).containsAll(scanResults);
				//assertThat(scanIterator.getCursor()).isEqualTo(size);
			}

			assertThat(scanResults).hasSize(PEOPLE_DOE_KEYS.size());
			assertThat(PEOPLE_DOE_KEYS).containsAll(scanResults);
			assertThat(scanIterator.getCursor()).isEqualTo(ScanIterator.END_OF_CURSOR);

			return true;
		});
	}

	@Test
	public void scanIsConsistent() {

		Set<Book> books = new HashSet<>(ITERATIONS);

		for (int iteration = 0; iteration < ITERATIONS; iteration++) {

			int count = 4;

			ScanIterator scanIterator = ScanIterator.from(this::runInRedis, "people:doe:*", count);

			List<Page> pages = new ArrayList<>();

			for (int size = count; scanIterator.hasNext(); size += count) {

				List<String> scanResults = scanIterator.next();

				pages.add(Page.of(scanResults));
			}

			books.add(Book.of(pages).named(String.valueOf(iteration)));
		}

		Book previousBook = null;

		for (Book book : books) {
			if (previousBook == null) {
				previousBook = book;
			}
			else {
				assertThat(book)
					.describedAs("Contents of Book [%s] are not the same as Book [%s]", book, previousBook)
					.isEqualTo(previousBook);
			}
		}
	}

	@SpringBootConfiguration
	static class RedisTestConfiguration {

		@Bean
		JedisPool jedisPool() {
			return new JedisPool(redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT));
		}
	}

	static class Book implements Iterable<Page>, Nameable<String> {

		static @NonNull Book of(Page... pages) {
			return new Book(CollectionUtils.asList(ArrayUtils.nullSafeArray(pages, Page.class)));
		}

		static @NonNull Book of(Iterable<Page> pages) {
			return new Book(CollectionUtils.asList(CollectionUtils.nullSafeIterable(pages)));
		}

		private final List<Page> pages;

		@Getter
		@Setter(AccessLevel.PROTECTED)
		private String name;

		private Book(@NonNull List<Page> pages) {
			this.pages = new ArrayList<>(CollectionUtils.nullSafeList(pages));
		}

		Page getPage(int pageNumber) {
			int index = pageNumber - 1;
			return getPages().get(index);
		}

		List<Page> getPages() {
			return Collections.unmodifiableList(this.pages);
		}

		int getPageCount() {
			return getPages().size();
		}

		public @NonNull Book named(@Nullable String name) {
			setName(name);
			return this;
		}

		@Override
		public Iterator<Page> iterator() {
			return getPages().iterator();
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Book that)) {
				return false;
			}

			if (this.getPageCount() != that.getPageCount()) {
				return false;
			}

			for (int pageNumber = 1; pageNumber <= getPageCount(); pageNumber++) {
				if (!this.getPage(pageNumber).equals(that.getPage(pageNumber))) {
					return false;
				}
			}

			return true;
		}

		@Override
		public int hashCode() {

			return StreamUtils.stream(this)
				.map(Object::hashCode)
				.reduce(MathUtils::sum)
				.orElseGet(() -> getPages().hashCode());
		}

		@Override
		public String toString() {

			StringBuilder buffer =
				new StringBuilder(String.format("Book [%s]:%s", getName(), StringUtils.LINE_SEPARATOR));

			List<Page> pages = getPages();

			for (int index = 0; index < pages.size(); index++) {
				int pageNumber = index + 1;
				buffer.append(pageNumber > 1 ? StringUtils.LINE_SEPARATOR : StringUtils.EMPTY_STRING);
				buffer.append(pageNumber).append(": ").append(pages.get(index));
			}

			return buffer.toString();
		}
	}

	static class Page implements Iterable<String> {

		static @NonNull Page of(String... keys) {
			return new Page(CollectionUtils.asList(ArrayUtils.nullSafeArray(keys, String.class)));
		}

		static @NonNull Page of(Iterable<String> keys) {
			return new Page(CollectionUtils.asList(CollectionUtils.nullSafeIterable(keys)));
		}

		private final List<String> keys;

		private Page(@NonNull List<String> keys) {
			this.keys = new ArrayList<>(CollectionUtils.nullSafeList(keys));
		}

		List<String> getKeys() {
			return Collections.unmodifiableList(this.keys);
		}

		@Override
		public Iterator<String> iterator() {
			return getKeys().iterator();
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Page that)) {
				return false;
			}

			return this.getKeys().equals(that.getKeys());
		}

		@Override
		public int hashCode() {

			return StreamUtils.stream(this)
				.map(Object::hashCode)
				.reduce(MathUtils::sum)
				.orElseGet(() -> getKeys().hashCode());
		}

		@Override
		public String toString() {

			return StreamUtils.stream(this)
				.reduce((keyOne, keyTwo) -> keyOne.concat(";").concat(keyTwo))
				.orElse(StringUtils.EMPTY_STRING);
		}
	}
}
