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
package io.vmware.spring.data.redis.tests.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ContextConfiguration;

import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration Tests testing Spring Data Redis's {@link RedisTemplate} and {@link ReactiveRedisTemplate}
 * and the de/serialization of values (with {@literal null} values) into and out of Redis Lists.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
 * @see org.springframework.data.redis.core.ReactiveRedisTemplate
 * @see org.springframework.data.redis.core.RedisTemplate
 * @see org.springframework.data.redis.serializer.RedisSerializationContext
 * @see org.springframework.data.redis.serializer.RedisSerializer
 * @see org.springframework.test.context.ContextConfiguration
 * @see reactor.core.publisher.Flux
 * @see reactor.core.publisher.Mono
 * @see io.vmware.spring.data.redis.tests.AbstractRedisIntegrationTests
 * @see <a href="https://github.com/spring-projects/spring-data-redis/issues/2655">Nullability of RedisElementReader.read(…) contradicts non-nullability of Flux.map(…)</a>
 * @since 0.1.0
 */
@Getter
@ContextConfiguration(classes = RedisTemplateIntegrationTests.TestConfiguration.class)
@DataRedisTest(excludeAutoConfiguration = RedisRepositoriesAutoConfiguration.class)
@SuppressWarnings("unused")
public class RedisTemplateIntegrationTests extends AbstractRedisIntegrationTests {

	@Autowired
	private ReactiveStringRedisTemplate reactiveRedisTemplate;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Test
	void imperativeAccessToRedisListWithNullValues() {

		ListOperations<String, String> listOperations = getRedisTemplate().opsForList();

		// Arrange (Setup)
		assertThat(storeInRedisListSynchronously("standard-redis-list")).isEqualTo(9L);
		assertThat(listOperations.size("standard-redis-list")).isEqualTo(9L);

		// Assert contents of Redis List
		// Nulls become empty String and 'b' becomes null
		assertThat(listOperations.range("standard-redis-list", 0L, 9L))
			.containsExactly("a1", "a2", "", null, "c", null, "", "d", "e");
	}

	private Long storeInRedisListSynchronously(String redisListKey) {
		return getRedisTemplate().opsForList().rightPushAll(redisListKey,
			"a1", "a2", null, "b", "c", "b", null, "d", "e");
	}

	@Test
	void reactiveAccessToRedisListWithNullValues() {

		ReactiveListOperations<String, String> reactiveListOperations = getReactiveRedisTemplate().opsForList();

		// Arrange (Setup)
		assertThat(storeInRedisListSynchronously("reactive-redis-list")).isEqualTo(9L);
		//assertThat(storeInRedisListReactively("reactive-redis-list")).isEqualTo(9L);
		assertThat(reactiveListOperations.size("reactive-redis-list").block()).isEqualTo(9L);

		/*
		`reactiveListOperations.range(..)` throws NullPointerException because stream contains null elements

		java.lang.NullPointerException: The mapper [org.springframework.data.redis.core.DefaultReactiveListOperations$$Lambda$1167/0x00000001306ddcc0] returned a null value.
			at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:108)
			at reactor.core.publisher.FluxFlatMap$FlatMapMain.tryEmit(FluxFlatMap.java:544)
			at reactor.core.publisher.FluxFlatMap$FlatMapInner.onNext(FluxFlatMap.java:985)
			at io.lettuce.core.RedisPublisher$ImmediateSubscriber.onNext(RedisPublisher.java:890)
			at io.lettuce.core.RedisPublisher$RedisSubscription.onNext(RedisPublisher.java:291)
			at io.lettuce.core.output.StreamingOutput$Subscriber.onNext(StreamingOutput.java:64)
			at io.lettuce.core.output.ValueListOutput.set(ValueListOutput.java:52)
			at io.lettuce.core.protocol.RedisStateMachine.safeSet(RedisStateMachine.java:810)
			at io.lettuce.core.protocol.RedisStateMachine.handleBytes(RedisStateMachine.java:572)
			at io.lettuce.core.protocol.RedisStateMachine$State$Type.handle(RedisStateMachine.java:206)
			at io.lettuce.core.protocol.RedisStateMachine.doDecode(RedisStateMachine.java:334)
			at io.lettuce.core.protocol.RedisStateMachine.decode(RedisStateMachine.java:295)
			at io.lettuce.core.protocol.CommandHandler.decode(CommandHandler.java:841)
			at io.lettuce.core.protocol.CommandHandler.decode0(CommandHandler.java:792)
			at io.lettuce.core.protocol.CommandHandler.decode(CommandHandler.java:775)
			at io.lettuce.core.protocol.CommandHandler.decode(CommandHandler.java:658)
			at io.lettuce.core.protocol.CommandHandler.channelRead(CommandHandler.java:598)
			at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
			at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
			at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
			at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
			at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440)
			at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
			at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
			at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166)
			at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:788)
			at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724)
			at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650)
			at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562)
			at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
			at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
			at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
		*/

		Flux<String> results = reactiveListOperations.range("reactive-redis-list", 0L, 9L)
			.filter(Objects::nonNull)
			.handle((value, sink) -> {
				String nonNullValue = String.valueOf(value);
				sink.next(nonNullValue);
			})
			.mapNotNull(value -> (String) value);

		System.out.println("YOU ARE HERE!");

		// Assert contents of Redis List
		// Nulls and 'b' become empty String
		assertThat(results.toStream().toList())
			.containsExactly("a1", "a2", "null", "null", "c", "null", "null", "d", "e");
	}

	private Long storeInRedisListReactively(String redisListKey) {

		// The reactive streams specification disallows null values in a sequence.
		// Therefore, this will throw an Exception when subscribed to.
		Mono<Long> rightPushAllOp = getReactiveRedisTemplate().opsForList().rightPushAll(redisListKey,
			"a1", "a2", null, "b", "c", "b", null, "d", "e");

		/*
		 `rightPushAllOp.block()` threw NullPointerException because stream contained null elements

		 java.lang.NullPointerException: The iterator returned a null value
			at java.base/java.util.Objects.requireNonNull(Objects.java:233)
			at reactor.core.publisher.FluxIterable$IterableSubscription.fastPath(FluxIterable.java:389)
			at reactor.core.publisher.FluxIterable$IterableSubscription.request(FluxIterable.java:291)
			at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
			at reactor.core.publisher.Operators$BaseFluxToMonoOperator.request(Operators.java:2041)
			at reactor.core.publisher.MonoFlatMap$FlatMapMain.request(MonoFlatMap.java:194)
			at reactor.core.publisher.FluxUsingWhen$UsingWhenSubscriber.request(FluxUsingWhen.java:319)
			at reactor.core.publisher.MonoNext$NextSubscriber.request(MonoNext.java:108)
			at reactor.core.publisher.BlockingSingleSubscriber.onSubscribe(BlockingSingleSubscriber.java:53)
			at reactor.core.publisher.MonoNext$NextSubscriber.onSubscribe(MonoNext.java:70)
			at reactor.core.publisher.FluxUsingWhen$UsingWhenSubscriber.onSubscribe(FluxUsingWhen.java:406)
			at reactor.core.publisher.MonoFlatMap$FlatMapMain.onSubscribe(MonoFlatMap.java:117)
			at reactor.core.publisher.Operators$BaseFluxToMonoOperator.onSubscribe(Operators.java:2025)
			at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
			at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:201)
			at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:83)
			at reactor.core.publisher.Mono.subscribe(Mono.java:4495)
			at reactor.core.publisher.FluxUsingWhen.subscribe(FluxUsingWhen.java:94)
			at reactor.core.publisher.Mono.subscribe(Mono.java:4495)
			at reactor.core.publisher.Mono.block(Mono.java:1711)
			...
			Suppressed: java.lang.Exception: #block terminated with an error
				at reactor.core.publisher.BlockingSingleSubscriber.blockingGet(BlockingSingleSubscriber.java:103)
				at reactor.core.publisher.Mono.block(Mono.java:1712)
				... 71 more
		 */

		return rightPushAllOp.block();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@SuppressWarnings("uunsed")
	static class TestConfiguration {

		@Bean
		RedisConfiguration redisConfiguration(RedisProperties redisProperties) {
			return redisStandaloneConfiguration(redisProperties);
		}

		@Bean
		StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory,
				RedisSerializationContext<String, String> serializationContext) {

			StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);

			Function<String, String> serializationFunction = value -> "b".equals(value) ? value.toUpperCase() : value;
			Function<String, String> deserializationFunction = value -> "B".equals(value) ? null : value;

			redisTemplate.setKeySerializer(RedisSerializer.string());
			redisTemplate.setValueSerializer(customStringRedisSerializer(serializationFunction, deserializationFunction));

			return redisTemplate;
		}

		@Bean
		ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
				RedisSerializationContext<String, String> serializationContext) {

			return new ReactiveStringRedisTemplate(connectionFactory, serializationContext);
		}

		@Bean
		RedisSerializationContext<String, String> redisSerializationContext() {

			Function<String, String> serializationFunction = String::valueOf;

			//Function<String, String> deserializationFunction =
			//	value -> Set.of("b", "", "null").contains(value) ? null : value;

			Function<String, String> deserializationFunction =
				value -> Set.of("B", "b", "").contains(value) ? "null" : value;

			return RedisSerializationContext.<String, String>newSerializationContext()
				.key(RedisSerializer.string())
				.value(customStringRedisSerializer(serializationFunction, deserializationFunction))
				.hashKey(RedisSerializer.java())
				.hashValue(RedisSerializer.java())
				.build();
		}

		StringRedisSerializer customStringRedisSerializer(Function<String, String> serializationFunction,
				Function<String, String> deserializationFunction) {

			return new StringRedisSerializer() {

				@Override
				public byte[] serialize(String value) {
					return super.serialize(serializationFunction.apply(value));
				}

				@Override
				public String deserialize(byte[] bytes) {
					return deserializationFunction.apply(super.deserialize(bytes));
				}
			};
		}
	}
}
