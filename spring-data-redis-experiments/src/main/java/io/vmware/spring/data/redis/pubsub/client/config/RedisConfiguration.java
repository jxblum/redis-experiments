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
package io.vmware.spring.data.redis.pubsub.client.config;

import org.cp.elements.lang.Renderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.ErrorHandler;

import example.chat.model.Chat;
import io.vmware.spring.data.redis.pubsub.client.event.ChatMessageListener;
import io.vmware.spring.data.redis.pubsub.client.support.ConsoleToStringChatRenderer;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring {@link Configuration} for Redis.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Configuration
 * @since 0.1.0
 */
@Slf4j
@Configuration
@SuppressWarnings("unused")
public class RedisConfiguration {

	public static final String CHAT_REDIS_CHANNEL_NAME = "chatroom";

	@Bean
	Renderer<Chat> chatClientRenderer() {
		return new ConsoleToStringChatRenderer();
	}

	@Bean
	ChatMessageListener chatMessageListener(Renderer<Chat> chatRenderer) {
		return chat -> chat.render(chatRenderer);
	}

	@Bean
	MessageListenerAdapter messageListenerAdapter(ChatMessageListener chatMessageListener) {

		MessageListenerAdapter messageListener =
			new MessageListenerAdapter(chatMessageListener, "receive");

		messageListener.setSerializer(RedisSerializer.java());

		return messageListener;
	}

	@Bean
	RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter messageListener) {

		RedisMessageListenerContainer messageListenerContainer = new RedisMessageListenerContainer();

		messageListenerContainer.setConnectionFactory(connectionFactory);
		messageListenerContainer.setErrorHandler(redisMessageListenerContainerErrorHandler());
		messageListenerContainer.addMessageListener(messageListener, ChannelTopic.of(CHAT_REDIS_CHANNEL_NAME));

		return messageListenerContainer;
	}

	private ErrorHandler redisMessageListenerContainerErrorHandler() {
		return throwable -> log.warn("RedisMessageListenerContainer ERROR [{}]", throwable.getMessage(), throwable);
	}
}
