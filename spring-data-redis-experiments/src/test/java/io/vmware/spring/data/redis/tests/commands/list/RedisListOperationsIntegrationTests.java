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
package io.vmware.spring.data.redis.tests.commands.list;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Integration Tests for {@link ListOperations} and {@link RedisListCommands} from Spring Data Redis.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.data.redis.connection.RedisListCommands
 * @see org.springframework.data.redis.core.ListOperations
 * @see org.springframework.data.redis.core.RedisTemplate
 * @see io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests
 * @see <a href="https://github.com/spring-projects/spring-data-redis/pull/2768">Enhance right/leftPushAll(key, Collection<E>) to account for type-mismatched Collection arguments</a>
 * @since 0.1.0
 */
@DataRedisTest
@Getter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class RedisListOperationsIntegrationTests extends AbstractRedisIntegrationTests {

	@Autowired
	@SuppressWarnings("rawtypes")
	private RedisTemplate redisTemplate;

	@Test
	@SuppressWarnings("all")
	void redisListOperationsWorkCorrectly() {

		ListOperations<Object, User> listOperations = getRedisTemplate().opsForList();

		Collection<User> users = List.of(User.as("JonDoe"), User.as("JaneDoe"));

		assertThat(listOperations.size("UserList")).isZero();

		Long listSize = listOperations.rightPushAll("UserList", users);

		assertThat(listSize).isEqualTo(2L);

		assertThat(listOperations.rightPop("UserList", listSize))
			.containsExactly(User.as("JaneDoe"), User.as("JonDoe"));
		assertThat(listOperations.size("UserList")).isZero();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfiguration {

		@Bean
		RedisConfiguration redisConfiguration(RedisProperties redisProperties) {
			return redisStandaloneConfiguration(redisProperties);
		}
	}

	@Getter
	@ToString
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "as")
	static class User implements Serializable {
		private final String name;
	}
}
