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
package example.chat.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import org.cp.elements.lang.Renderable;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.lang.annotation.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract Data Type (ADT) modeling a {@literal chat}.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see java.time.LocalDateTime
 * @see org.cp.elements.lang.Renderable
 * @see example.chat.model.Person
 * @since 0.1.0
 */
@Getter
@RequiredArgsConstructor(staticName = "newChat")
@SuppressWarnings("unused")
public class Chat implements Renderable, Serializable {

	public static final String DEFAULT_MESSAGE = "What?";

	protected static final String CHAT_TO_STRING = "%1$s - %2$s: \"%3$s\"";
	protected static final String TIMESTAMP_PATTERN = "yyyy-MM-dd-HH-mm-ss";

	protected static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

	protected static final Supplier<LocalDateTime> TIMESTAMP_SUPPLIER = LocalDateTime::now;

	private final Person person;

	private final String message;

	public @Nullable String getMessage(@Nullable String defaultMessage) {
		return StringUtils.defaultIfBlank(getMessage(), defaultMessage);
	}

	public @NotNull LocalDateTime getTimestamp() {
		return TIMESTAMP_SUPPLIER.get();
	}

	@Override
	public String toString() {
		return String.format(CHAT_TO_STRING, toString(getTimestamp()), getPerson(), getMessage());
	}

	protected @NotNull String toString(@NotNull LocalDateTime dateTime) {
		return dateTime.format(TIMESTAMP_FORMATTER);
	}
}
