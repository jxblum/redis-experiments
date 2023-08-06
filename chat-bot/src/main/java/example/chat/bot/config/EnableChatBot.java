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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import example.chat.bot.ChatBot;

/**
 * Java {@link Annotation} used to enable a {@link ChatBot} in a Spring application context.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see example.chat.bot.ChatBot
 * @see example.chat.bot.config.ChatBotConfiguration
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(ChatBotConfiguration.class)
@SuppressWarnings("unused")
public @interface EnableChatBot {

}
