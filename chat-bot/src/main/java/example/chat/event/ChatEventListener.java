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

import java.util.EventListener;

/**
 * {@link EventListener} defining a contract for implementors to listen for {@link ChatEvent ChatEvents}.
 *
 * @author John Blum
 * @param <T> {@link Class type} of {@link Object chat}.
 * @see java.lang.FunctionalInterface
 * @see java.util.EventListener
 * @see example.chat.event.ChatEvent
 * @since 0.1.0
 */
@FunctionalInterface
public interface ChatEventListener<T> extends EventListener {

	void handle(ChatEvent<T> chatEvent);

}
