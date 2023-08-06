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
package example.chat.service.provider;

import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.service.annotation.Service;

import example.chat.event.ChatEvent;
import example.chat.event.ChatEventPublisher;
import example.chat.event.ChatEventListener;
import example.chat.model.Chat;
import example.chat.service.ChatService;

/**
 * {@link ChatService} implementation publishing {@link Chat Chats} in {@link ChatEvent ChatEvents}
 * to {@link ChatEventListener ChatListeners}.
 *
 * @author John Blum
 * @see example.chat.event.ChatEvent
 * @see example.chat.event.ChatEventPublisher
 * @see example.chat.model.Chat
 * @see example.chat.service.ChatService
 * @see org.springframework.stereotype.Service
 * @since 0.1.0
 */
@Service
@SuppressWarnings("unused")
public class EventDrivenChatService extends ChatEventPublisher<Chat> implements ChatService {

	@Override
	public void send(@NotNull Chat chat) {
		fire(ChatEvent.<Chat>newChatEvent(this).with(chat));
	}
}
