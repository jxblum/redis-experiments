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
package io.vmware.spring.data.redis.client.tests;

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
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import lombok.Getter;

/**
 * Integration Tests using Spring Data Redis {@link RedisTemplate} to execute the Redis {@literal SCAN} Command.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.redis.connection.RedisStandaloneConfiguration
 * @see org.springframework.data.redis.core.RedisTemplate
 * @see org.springframework.test.context.ActiveProfiles
 * @see <a href="https://redis.io/commands/scan/">Redis SCAN command</a>
 * @see <a href="https://www.baeldung.com/redis-list-available-keys">List All Available Redis Keys</a>
 * @since 0.1.0
 */
@Getter
//@SpringBootTest
//@ActiveProfiles("jedis")
@DataRedisTest(properties = "spring.data.redis.repositories.enabled=false")
@SuppressWarnings("unused")
public class SpringDataRedisScanIntegrationTests extends AbstractRedisIntegrationTests {

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

	@Autowired
	private StringRedisTemplate redisTemplate;

	@BeforeEach
	public void setupDataInRedisDatabase() {

		databaseInitialized.updateAndGet(initialized -> {

			if (!Boolean.TRUE.equals(initialized)) {

				ValueOperations<String, String> valueOperations = getRedisTemplate().opsForValue();

				valueOperations.set("people:bloom:jon", "Jon Bloom");
				valueOperations.set("people:doe:bob", "Bob Doe");
				valueOperations.set("people:doe:cookie", "Cookie Doe");
				valueOperations.set("people:doe:dill", "Dill Doe");
				valueOperations.set("people:doe:fro", "Fro Doe");
				valueOperations.set("people:doe:hoe", "Hoe Doe");
				valueOperations.set("people:doe:jane", "Jane Doe");
				valueOperations.set("people:doe:joe", "Joe Doe");
				valueOperations.set("people:doe:jon", "Jon Doe");
				valueOperations.set("people:doe:lan", "Lan Doe");
				valueOperations.set("people:doe:moe", "Moe Doe");
				valueOperations.set("people:doe:pie", "Pie Doe");
				valueOperations.set("people:doe:sour", "Sour Doe");
				valueOperations.set("people:frost:jack", "Jack Frost");
				valueOperations.set("people:handy:jack", "Jack Handy");
				valueOperations.set("people:handy:jill", "Jill Handy");

				return true;
			}

			return false;
		});
	}

	@Test
	public void dataIsPresent() {

		ValueOperations<String, String> valueOperations = getRedisTemplate().opsForValue();

		assertThat(valueOperations.get("people:doe:lan")).isEqualTo("Lan Doe");
		assertThat(valueOperations.get("people:doe:pie")).isEqualTo("Pie Doe");
		assertThat(valueOperations.get("people:handy:jack")).isEqualTo("Jack Handy");
	}

	@Test
	public void scanIsCorrect() {

		ScanOptions scanOptions = ScanOptions.scanOptions()
			.match("people:doe:*") // Spring Data Redis pattern for matching is not same as Jedis pattern for matching
			.count(3)
			.build();

		List<String> scanResults = new ArrayList<>();

		try (Cursor<String> cursor = getRedisTemplate().scan(scanOptions)) {
			while (cursor.hasNext()) {
				scanResults.add(cursor.next());
			}
		}

		assertThat(scanResults).hasSize(PEOPLE_DOE_KEYS.size());
		assertThat(scanResults).containsAll(PEOPLE_DOE_KEYS);
	}

	@SpringBootConfiguration
	static class RedisTestConfiguration {

		@Bean
		RedisStandaloneConfiguration redisStandaloneConfiguration(RedisProperties redisProperties) {
			return SpringDataRedisScanIntegrationTests.redisStandaloneConfiguration(redisProperties);
		}
	}

	@Profile("jedis")
	@SpringBootConfiguration
	static class JedisTestConfiguration {

		@Bean
		JedisConnectionFactory jedisConnectionFactory(RedisStandaloneConfiguration standaloneConfiguration) {
			return new JedisConnectionFactory(standaloneConfiguration);
		}
	}
}
