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
package example.chat.service;

import example.chat.model.Chat;

/**
 * Interface defining a contract for implementors to send {@link Chat Chats}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see example.chat.model.Chat
 * @since 1.0.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ChatService {

	void send(Chat chat);

}
