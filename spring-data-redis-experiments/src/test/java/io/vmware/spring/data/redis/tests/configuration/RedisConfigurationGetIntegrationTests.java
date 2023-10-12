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
package io.vmware.spring.data.redis.tests.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import lombok.Getter;

/**
 * Integration Tests testing the Redis {@literal CONFIG GET} command.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.data.redis.RedisProperties
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.data.redis.connection.RedisConfiguration
 * @see org.springframework.test.context.ContextConfiguration
 * @since 0.1.0
 */
@Getter
@DataRedisTest
@ActiveProfiles("jedis")
@ContextConfiguration(classes = RedisConfigurationGetIntegrationTests.TestConfiguration.class)
@SuppressWarnings("unused")
public class RedisConfigurationGetIntegrationTests extends AbstractRedisIntegrationTests {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Test
	 void redisConfigGetCommand() {

		RedisConnectionFactory connectionFactory = getRedisTemplate().getConnectionFactory();

		assertThat(connectionFactory).isNotNull();

		RedisConnection connection = connectionFactory.getConnection();

		assertThat(connection).isNotNull();
		assertThat(connection.isClosed()).isFalse();

		Properties redisServerConfiguration = connection.serverCommands().getConfig("*");

		assertThat(redisServerConfiguration).isNotNull();
		assertThat(redisServerConfiguration).isNotEmpty();
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
