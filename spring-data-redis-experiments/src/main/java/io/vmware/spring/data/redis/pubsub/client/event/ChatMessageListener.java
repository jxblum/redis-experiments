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
package io.vmware.spring.data.redis.pubsub.client.event;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.NonNull;

import example.chat.model.Chat;

/**
 * Interface defining a contract for implementors to listen for {@link Chat Chats}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see example.chat.model.Chat
 * @since 0.1.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ChatMessageListener extends MessageListener {

	@Override
	default void onMessage(@NonNull Message message, byte[] pattern) {
		Chat chat = (Chat) RedisSerializer.java().deserialize(message.getBody());
		receive(chat);
	}

	void receive(Chat chat);

}
