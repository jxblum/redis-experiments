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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.cp.elements.lang.annotation.NotNull;

/**
 * Abstract base class defining a contract for implementors allowing {@link ChatEventListener ChatListeners}
 * to be registered and unregistered as well as to fire {@link ChatEvent ChatEvents}.
 *
 * @author John Blum
 * @param <T> {@link Class type} of {@link Object chat}.
 * @see example.chat.event.ChatEvent
 * @see example.chat.event.ChatEventListener
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class ChatEventPublisher<T> {

	private final Set<ChatEventListener<T>> chatEventListeners = new CopyOnWriteArraySet<>();

	protected void fire(@NotNull ChatEvent<T> chatEvent) {
		if (chatEvent != null) {
			this.chatEventListeners.forEach(chatEventListener -> chatEventListener.handle(chatEvent));
		}
	}

	public boolean register(@NotNull ChatEventListener<T> chatEventListener) {
		return chatEventListener != null && this.chatEventListeners.add(chatEventListener);
	}

	public boolean unregister(@NotNull ChatEventListener<T> chatEventListener) {
		return chatEventListener != null && this.chatEventListeners.remove(chatEventListener);
	}
}
