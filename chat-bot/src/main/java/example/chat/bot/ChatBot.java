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
package example.chat.bot;

import example.chat.model.Chat;
import example.chat.model.Person;

/**
 * Interface defining a contract for implementors to generate {@link Chat} from a given {@link Person}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see example.chat.model.Chat
 * @see example.chat.model.Person
 * @since 0.1.0
 */
@FunctionalInterface
public interface ChatBot {

	Chat chat(Person person);

}
