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
package io.vmware.spring.data.redis.tests.commands.set;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Integration Tests testing the {@link RedisSetCommands} from Spring Data Redis.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.data.redis.connection.RedisSetCommands
 * @see org.springframework.data.redis.core.StringRedisTemplate;
 * @see <a href="https://github.com/spring-projects/spring-data-redis/issues/2759">RedisSetCommands.isMember(K key, Object... objects) throws ClassCastException</a>
 * @since 0.1.0
 */
@Getter(AccessLevel.PROTECTED)
@ContextConfiguration(classes = RedisSetOperationsIntegrationTests.TestConfiguration.class)
@DataRedisTest(excludeAutoConfiguration = RedisRepositoriesAutoConfiguration.class)
@SuppressWarnings("unused")
public class RedisSetOperationsIntegrationTests extends AbstractRedisIntegrationTests {

	@Autowired
	private StringRedisTemplate template;

	@Test
	void setIsMembersWorksCorrectly() {

		SetOperations<String, String> setOperations = getTemplate().opsForSet();

		setOperations.add("TestSet", "one", "two", "three");

		assertThat(setOperations.size("TestSet")).isEqualTo(3L);

		Map<Object, Boolean> members = setOperations.isMember("TestSet", "one", "five", "four", "two", "four");

		assertThat(members).isNotNull();
		assertThat(members.values()).containsExactly(true, false, false, true);
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
