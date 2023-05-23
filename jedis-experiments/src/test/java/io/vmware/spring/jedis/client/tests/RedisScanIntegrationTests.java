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
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import io.vmware.spring.jedis.client.support.RedisCommandsResolver;
import io.vmware.spring.jedis.client.support.ScanIterator;
import io.vmware.spring.jedis.client.tests.support.AbstractRedisIntegrationTests;
import lombok.Getter;
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

		List<String> pagedKeys = new ArrayList<>();

		for (int iteration = 0; iteration < ITERATIONS; iteration++) {

			int count = 4;

			ScanIterator scanIterator = ScanIterator.from(this::runInRedis, "people:doe:*", count);

			for (int size = count; scanIterator.hasNext(); size += count) {

				List<String> scanResults = scanIterator.next();

				String concatenatedKeys = scanResults.stream()
					.reduce((keyOne, keyTwo) -> keyOne.concat(";").concat(keyTwo))
					.orElse("");

				if (!pagedKeys.contains(concatenatedKeys) && StringUtils.hasText(concatenatedKeys)) {
					pagedKeys.add(concatenatedKeys);
				}
			}
		}

		assertThat(pagedKeys)
			.describedAs("Expected paged keys List [%s] to only have a size of 2")
			.hasSize(3);
	}

	@SpringBootConfiguration
	static class RedisTestConfiguration {

		@Bean
		JedisPool jedisPool() {
			return new JedisPool(redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT));
		}
	}
}
