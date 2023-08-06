/*
 *  Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package example.chat.event;

import java.util.EventObject;

import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.lang.annotation.Nullable;

import example.chat.model.Chat;
import lombok.AccessLevel;
import lombok.Setter;

/**
 * {@link EventObject} encapsulating details of a {@link Chat} {@literal event}.
 *
 * @author John Blum
 * @param <T> {@link Class type} of {@link Object chat}.
 * @see java.util.EventObject
 * @see example.chat.model.Chat
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class ChatEvent<T> extends EventObject {

	public static @NotNull <T> ChatEvent<T> newChatEvent(@NotNull Object source) {
		return new ChatEvent<>(source);
	}

	@Setter(AccessLevel.PRIVATE)
	private T chat;

	protected ChatEvent(@NotNull Object source) {
		super(source);
	}

	public @Nullable T getChat() {
		return this.chat;
	}

	public @NotNull T requireChat() {
		return ObjectUtils.requireState(getChat(),"Chat was not set");
	}

	public @NotNull ChatEvent<T> with(@NotNull T chat) {
		setChat(ObjectUtils.requireObject(chat, "Chat is required"));
		return this;
	}
}
