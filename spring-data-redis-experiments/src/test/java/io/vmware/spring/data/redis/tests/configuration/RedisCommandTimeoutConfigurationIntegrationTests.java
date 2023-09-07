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

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import lombok.Getter;

/**
 * Integration Tests testing and asserting the configuration of Redis's command {@link Duration timeout}
 * using the Spring Boot {@literal spring.data.redis.timeout} property.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @since 0.1.0
 */
@Getter
@DataRedisTest(properties = "spring.data.redis.timeout=74s")
@ContextConfiguration(classes = RedisCommandTimeoutConfigurationIntegrationTests.TestConfiguration.class)
public class RedisCommandTimeoutConfigurationIntegrationTests {

	@Autowired
	@SuppressWarnings("unused")
	private RedisConnectionFactory connectionFactory;

	@Test
	void commandTimeoutConfiguredCorrectly() {

		RedisConnectionFactory connectionFactory = getConnectionFactory();

		assertThat(connectionFactory).isInstanceOf(LettuceConnectionFactory.class);

		assertThat(connectionFactory)
			.asInstanceOf(InstanceOfAssertFactories.type(LettuceConnectionFactory.class))
			.extracting(LettuceConnectionFactory::getClientConfiguration)
			.extracting(LettuceClientConfiguration::getCommandTimeout)
			.isEqualTo(Duration.ofSeconds(74L));
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfiguration {

	}
}
