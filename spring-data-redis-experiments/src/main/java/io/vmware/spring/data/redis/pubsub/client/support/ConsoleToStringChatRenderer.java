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
package io.vmware.spring.data.redis.pubsub.client.support;

import org.cp.elements.lang.Renderer;

import example.chat.model.Chat;

/**
 * {@link Renderer} that renders a {@link Chat} to {@link System#out}.
 *
 * @author John Blum
 * @see example.chat.model.Chat
 * @see org.cp.elements.lang.Renderer
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class ConsoleToStringChatRenderer implements Renderer<Chat> {

	@Override
	public String render(Chat chat) {

		String chatString = chat.toString();

		System.out.printf("REDIS PUB/SUB [%s]%n", chatString);
		System.out.flush();

		return chatString;
	}
}
