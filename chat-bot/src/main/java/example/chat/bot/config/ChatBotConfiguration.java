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
package example.chat.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

import example.chat.bot.ChatBot;
import example.chat.bot.provider.DespairDotComChatBot;
import example.chat.bot.provider.FamousQuotesChatBot;
import example.chat.service.ChatService;

/**
 * Spring {@link Configuration} class used to configure, register and enable a {@link ChatBot}
 * in a Spring application context.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.scheduling.annotation.EnableScheduling
 * @see example.chat.bot.ChatBot
 * @since 0.1.0
 */
@Configuration
@EnableScheduling
@SuppressWarnings("unused")
public class ChatBotConfiguration {

	@Bean
	@Profile("despair-dot-com")
	public DespairDotComChatBot despairDotComChatBot(ChatService chatService) {
		return new DespairDotComChatBot(chatService);
	}

	@Bean
	@Profile("famous-quotes")
	public FamousQuotesChatBot famousQuotesChatBot(ChatService chatService) {
		return new FamousQuotesChatBot(chatService);
	}
}
