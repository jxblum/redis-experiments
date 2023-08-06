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
package example.chat.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import example.chat.NonBeanType;
import example.chat.event.ChatEventListener;
import example.chat.event.ChatEventPublisher;
import example.chat.model.Chat;

/**
 * {@link SpringBootApplication} serving as a simple {@link example.chat.bot.ChatBot} client.
 *
 * @author John Blum
 * @see example.chat.model.Chat
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.ComponentScan
 * @since 0.1.0
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = NonBeanType.class)
@SuppressWarnings("unused")
public class ConsoleChatClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsoleChatClientApplication.class);
	}

	@Bean
	ChatEventListener<Chat> consoleChatEventHandler(ChatEventPublisher<Chat> chatEventPublisher) {
		ChatEventListener<Chat> console = chatEvent -> logChat(chatEvent.getChat());
		chatEventPublisher.register(console);
		return console;
	}

	private void logChat(Chat chat) {
		logChatToSystemOut(chat);
	}

	private void logChatToSystemOut(Chat chat) {
		System.out.println(chat);
		System.out.flush();
	}
}
