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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import org.cp.elements.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import lombok.Getter;

/**
 * Integration Tests using Redis Pipeline in a concurrent, multi-Threaded context to load a Redis Set
 * with {@literal Lettuce} (default), or alternatively, {@literal Jedis}.
 * <p>
 * The test(s) results in the following {@link NullPointerException} (NPE) caused by a race condition when run
 * in a multi-Threaded context. The NPE is caused by Spring Data Redis's {@link LettuceConnection} class
 * being non-Thread-safe, or using non-Thread-safe data structures even though {@literal Lettuce} is generally
 * a Thread-safe Redis driver:
 *
 * <pre>
 * <code>
 *     java.lang.NullPointerException: Cannot invoke "org.springframework.data.redis.connection.lettuce.LettuceResult.getResultHolder()" because "result" is null
 *
 *     at org.springframework.data.redis.connection.lettuce.LettuceConnection.closePipeline(LettuceConnection.java:437)
 *     at org.springframework.data.redis.connection.DefaultStringRedisConnection.closePipeline(DefaultStringRedisConnection.java:2456)
 *     ...
 *     at jdk.proxy2/jdk.proxy2.$Proxy135.closePipeline(Unknown Source)
 *     at org.springframework.data.redis.core.RedisTemplate.lambda$executePipelined$1(RedisTemplate.java:490)
 *     at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:406)
 *     at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:373)
 *     at org.springframework.data.redis.core.RedisTemplate.execute(RedisTemplate.java:360)
 *     at org.springframework.data.redis.core.RedisTemplate.executePipelined(RedisTemplate.java:481)
 *     at org.springframework.data.redis.core.RedisTemplate.executePipelined(RedisTemplate.java:475)
 *     at io.vmware.spring.data.redis.client.tests.ConcurrentRedisPipeliningIntegrationTests.storeInRedisUsingPipelineThenParallelStream(ConcurrentRedisPipeliningIntegrationTests.java:99)
 *     ...
 * </code>
 * </pre>
 *
 * The NPE does not always reproduce given the nature of a race condition.
 * <p>
 * Additionally, it is never safe to use the Jedis driver (client library) in a concurrent, multi-Threaded context
 * as <a href="https://github.com/redis/jedis/wiki/Getting-started#using-jedis-in-a-multithreaded-environment">documented</a>.
 * So, it would be expected that this test exhibit unexpected behavior due to Thread interference when run with Jedis.
 * <p>
 * Finally, none of Spring Data Redis's {@link RedisConnection} classes actually guarantee Thread-safety. Therefore, it
 * is generally understood that SD Redis connections should not be shared across multiple Threads.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.data.redis.core.RedisTemplate
 * @see org.springframework.test.context.ActiveProfiles
 * @see io.vmware.spring.data.redis.client.tests.AbstractRedisIntegrationTests
 * @since 0.1.0
 */
@Getter
@ActiveProfiles("lettuce")
@DataRedisTest(properties = "spring.data.redis.repositories.enabled=false")
@SuppressWarnings("unused")
public class ConcurrentRedisPipeliningIntegrationTests extends AbstractRedisIntegrationTests {

	private static final boolean DISABLE_STORE_IDS_IN_REDIS_USING_PARALLEL_STREAM_THEN_PIPELINE_TEST_CASE = true;
	private static final boolean DISABLE_STORE_IDS_IN_REDIS_USING_PIPELINE_THEN_PARALLEL_STREAM_TEST_CASE = false;

	private static final int ELEMENT_COUNT = 500_000;

	private static final String SET_KEY_ONE = "SetKeyOne";
	private static final String SET_KEY_TWO = "SetKeyOne";

	private static final Set<String> elements = new HashSet<>();

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@BeforeAll
	public static void createElementsToStoreInRedis() {

		IntStream.range(0, ELEMENT_COUNT)
			.forEach(count -> elements.add(UUID.randomUUID().toString()));
	}

	@BeforeEach
	public void assertRedisTemplate() {

		assertThat(this.redisTemplate).isNotNull();
		assertThat(this.redisTemplate.getConnectionFactory()).isInstanceOf(LettuceConnectionFactory.class);
	}

	@Test
	@DisabledIf("isStoreInRedisUsingPipelineThenParallelStreamDisabled")
	void storeInRedisUsingPipelineThenParallelStream() {

		Set<String> threadNames = new ConcurrentSkipListSet<>();

		this.redisTemplate.executePipelined((RedisCallback<?>) redisConnection -> {

			elements.parallelStream().forEach(element -> {
				threadNames.add(Thread.currentThread().getName());
				redisConnection.setCommands().sAdd(SET_KEY_ONE.getBytes(), element.getBytes());
			});

			return null;
		});

		log("PIPELINE-STREAM THREADS [%s]%n", CollectionUtils.toString(threadNames));

		assertThat(this.redisTemplate.opsForSet().size(SET_KEY_ONE)).isEqualTo(ELEMENT_COUNT);
	}

	private boolean isStoreInRedisUsingPipelineThenParallelStreamDisabled() {
		return DISABLE_STORE_IDS_IN_REDIS_USING_PIPELINE_THEN_PARALLEL_STREAM_TEST_CASE;
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisabledIf("isStoreInRedisUsingParallelStreamThenPipelineDisabled")
	void storeInRedisUsingParallelStreamThenPipeline() {

		Set<String> threadNames = new ConcurrentSkipListSet<>();

		elements.parallelStream().forEach(element -> {
			threadNames.add(Thread.currentThread().getName());
			this.redisTemplate.executePipelined(toSessionCallback(redisOperations ->
				redisOperations.opsForSet().add(SET_KEY_TWO, element)));
		});

		log("STREAM-PIPELINE THREADS [%s]%n", threadNames);

		assertThat(this.redisTemplate.opsForSet().size(SET_KEY_TWO)).isEqualTo(ELEMENT_COUNT);
	}

	private boolean isStoreInRedisUsingParallelStreamThenPipelineDisabled() {
		return DISABLE_STORE_IDS_IN_REDIS_USING_PARALLEL_STREAM_THEN_PIPELINE_TEST_CASE;
	}

	@SpringBootConfiguration
	static class RedisTestConfiguration {

		@Bean
		RedisConfiguration redisConfiguration(RedisProperties redisProperties) {
			return redisStandaloneConfiguration(redisProperties);
		}
	}

	@Profile("lettuce")
	@SpringBootConfiguration
	static class LettuceTestConfiguration {

		@Bean
		LettuceClientConfiguration lettuceClientConfiguration() {
			return LettucePoolingClientConfiguration.defaultConfiguration();
		}
	}
}
