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
package io.vmware.spring.data.redis.tests.operations.hash;

import static org.assertj.core.api.Assertions.assertThat;

import io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Integration Tests for Redis {@link BoundHashOperations}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.data.redis.core.BoundHashOperations
 * @see org.springframework.data.redis.core.RedisTemplate
 * @see io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests
 * @see <a href="https://github.com/spring-projects/spring-data-redis/issues/2772">BoundHashOperations referenced from a method is not visible from class loader</a>
 * @since 0.1.0
 */
@DataRedisTest
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class RedisHashOperationsIntegrationTests extends AbstractRedisIntegrationTests {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Test
	void redisHashOperationsWorkCorrectly() {

		BoundHashOperations<String, String, String> hashOperations = getRedisTemplate().boundHashOps("TestHash");

		assertThat(hashOperations).isNotNull();
		assertThat(hashOperations.getKey()).isEqualTo("TestHash");
		assertThat(hashOperations.putIfAbsent("TestKey", "MOCK")).isTrue();
		assertThat(hashOperations.hasKey("TestKey")).isTrue();
		assertThat(hashOperations.get("TestKey")).isEqualTo("MOCK");
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfiguration {

		@Bean
		RedisConfiguration redisConfiguration(RedisProperties redisProperties) {
			return redisStandaloneConfiguration(redisProperties);
		}
	}
}
