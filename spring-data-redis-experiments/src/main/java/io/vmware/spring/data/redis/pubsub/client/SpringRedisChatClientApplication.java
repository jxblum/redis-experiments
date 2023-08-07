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
package io.vmware.spring.data.redis.pubsub.client;

import org.slf4j.Logger;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.RedisTemplate;

import example.chat.bot.config.EnableChatBot;
import example.chat.client.ConsoleChatClientApplication;
import example.chat.event.ChatEventListener;
import example.chat.event.ChatEventPublisher;
import example.chat.model.Chat;
import io.vmware.spring.data.redis.pubsub.client.config.RedisConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpringBootApplication} using Redis pub/sub messaging configured with Spring Data Redis
 * to implement and run the {@link example.chat.bot.ChatBot}.
 *
 * @author John Blum
 * @see example.chat.bot.config.EnableChatBot
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @since 0.1.0
 */
@Slf4j
@EnableChatBot
@SpringBootApplication
@ComponentScan(basePackageClasses = { example.chat.NonBeanType.class, RedisConfiguration.class },
	excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ConsoleChatClientApplication.class) }
)
@SuppressWarnings("unused")
public class SpringRedisChatClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRedisChatClientApplication.class);
	}

	@Bean
	ApplicationRunner redisChatPublishingRunner(RedisTemplate<Object, Object> redisTemplate,
			ChatEventPublisher<Chat> chatEventSourcePublisher) {

		return arguments -> chatEventSourcePublisher.register(newRedisPublishingChatEventListener(redisTemplate));
	}

	protected ChatEventListener<Chat> newRedisPublishingChatEventListener(RedisTemplate<Object, Object> redisTemplate) {

		return chatEvent -> {
			Chat chat = chatEvent.requireChat();
			logDebug("DEBUG [{}]", chat);
			redisTemplate.convertAndSend(RedisConfiguration.CHAT_REDIS_CHANNEL_NAME, chat);
		};
	}

	protected Logger getLogger() {
		return log;
	}

	protected void logDebug(String message, Object... args) {

		Logger logger = getLogger();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(message, args), args);
		}
	}
}
