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
package example.chat.bot.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.annotation.NotNull;
import org.cp.elements.lang.annotation.Nullable;
import org.cp.elements.util.CollectionUtils;
import org.cp.elements.util.stream.StreamUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import example.chat.bot.ChatBot;
import example.chat.model.Chat;
import example.chat.model.Person;
import example.chat.service.ChatService;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link ChatBot} implementation generating {@link Chat Chats} containing {@literal famous quotes}
 * from various {@link Person people}.
 *
 * @author John Blum
 * @see example.chat.bot.ChatBot
 * @see example.chat.model.Chat
 * @see example.chat.model.Person
 * @see example.chat.service.ChatService
 * @see org.springframework.beans.factory.annotation.Qualifier
 * @see org.springframework.stereotype.Service
 * @since 1.0.0
 */
@Service
@Qualifier("FamousQuotes")
@SuppressWarnings("unused")
public class FamousQuotesChatBot implements ChatBot {

	protected static final long SCHEDULE_INITIAL_DELAY = 5_000L;

	private static final Iterable<Person> people = Set.of(
		Person.newPerson("Aristotle", "(Greek)"),
		Person.newPerson("Benjamin", "Franklin"),
		Person.newPerson("Arthur", "Ashe"),
		Person.newPerson("Bertrand", "Russel"),
		Person.newPerson("Bill", "Waterson"),
		Person.newPerson("Bruce", "Lee"),
		Person.newPerson("Charles", "Dudley Warner"),
		Person.newPerson("Confucius", "(Chinese)"),
		Person.newPerson("David", "Lee Roth"),
		Person.newPerson("Don", "Marquis"),
		Person.newPerson("Eleanor", "Roosevelt"),
		Person.newPerson("George", "Moore"),
		Person.newPerson("Groucho", "Marx"),
		Person.newPerson("H. G.", "Wells"),
		Person.newPerson("Henry", "Youngman"),
		Person.newPerson("Isaac", "Asimov"),
		Person.newPerson("Jack", "Welch"),
		Person.newPerson("Jean-Luc", "Godard"),
		Person.newPerson("Jim", "Rohn"),
		Person.newPerson("Johann", "Wolfgang von Goethe"),
		Person.newPerson("John", "Burroughs"),
		Person.newPerson("Jules", "Renard"),
		Person.newPerson("Katherine", "Hepburn"),
		Person.newPerson("Ken", "Kesey"),
		Person.newPerson("Lana", "Turner"),
		Person.newPerson("Mark", "Twain"),
		Person.newPerson("Martin", "Luther King Jr."),
		Person.newPerson("Maya", "Angelou"),
		Person.newPerson("Milton", "Berle"),
		Person.newPerson("Mitch", "Hedberg"),
		Person.newPerson("Nelson", "Mandela"),
		Person.newPerson("Nikos", "Kazantzakis"),
		Person.newPerson("Oliver", "Herford"),
		Person.newPerson("Reba", "McEntire"),
		Person.newPerson("Reinhold", "Niebuhr"),
		Person.newPerson("Robert", "Benchley"),
		Person.newPerson("Robin", "Williams"),
		Person.newPerson("Samuel", "Beckett"),
		Person.newPerson("Stephan", "Hawking"),
		Person.newPerson("Theodore", "Roosevelt"),
		Person.newPerson("Thomas", "Edison"),
		Person.newPerson("Unknown", "Unknown"),
		Person.newPerson("Virat", "Kohli"),
		Person.newPerson("Walt", "Disney"),
		Person.newPerson("W. C.", "Fields"),
		Person.newPerson("W. H.", "Auden"),
		Person.newPerson("Will", "Rogers"),
		Person.newPerson("William", "Lyon Phelps"),
		Person.newPerson("William", "Shakespear"),
		Person.newPerson("Winston", "Churchill")
	);

	private static final List<Chat> chats = Arrays.asList(
		Chat.newChat(findPersonBy("Aristotle (Greek)"), "Quality is not an act, it is a habit."),
		Chat.newChat(findPersonBy("Arthur Ashe"), "Start where you are. Use what you have. Do what you can."),
		Chat.newChat(findPersonBy("Benjamin Franklin"), "Well done is better than well said."),
		Chat.newChat(findPersonBy("Bertrand Russel"), "I would never die for my beliefs because I might be wrong."),
		Chat.newChat(findPersonBy("Bill Waterson"), "Reality continues to ruin my life."),
		Chat.newChat(findPersonBy("Bruce Lee"), "Mistakes are always forgivable, if one has the courage to admit them."),
		Chat.newChat(findPersonBy("Charles Dudley Warner"), "Everybody talks about the weather, but nobody does anything about it."),
		Chat.newChat(findPersonBy("Confucius (Chinese)"), "It does not matter how slowly you go as long as you do not stop."),
		Chat.newChat(findPersonBy("David Lee Roth"), "I used to jog but the ice cubes kept falling out of my glass."),
		Chat.newChat(findPersonBy("Don Marquis"), "Procrastination is the art of keeping up with yesterday."),
		Chat.newChat(findPersonBy("Eleanor Roosevelt"), "It is better to light a candle than curse the darkness."),
		Chat.newChat(findPersonBy("H. G. Wells"), "If you fell down yesterday, stand up today."),
		Chat.newChat(findPersonBy("Henry Youngman"), "If you're going to do something tonight that you'll be sorry for tomorrow morning, sleep late."),
		Chat.newChat(findPersonBy("Isaac Asimov"), "People who think they know everything are a great annoyance to those of us who do."),
		Chat.newChat(findPersonBy("George Moore"), "A man travels the world over in search of what he needs and returns home to find it."),
		Chat.newChat(findPersonBy("Groucho Marx"), "Anyone who says he can see through women is missing a lot."),
		Chat.newChat(findPersonBy("Groucho Marx"), "I refuse to join any club that would have me as a member."),
		Chat.newChat(findPersonBy("Jack Welch"), "Change before you have to."),
		Chat.newChat(findPersonBy("Jean-Luc Godard"), "To be or not to be. That's not really a question."),
		Chat.newChat(findPersonBy("Jim Rohn"), "Either you run the day or the day runs you."),
		Chat.newChat(findPersonBy("Johann Wolfgang von Goethe"), "Knowing is not enough; we must apply. Willing is not enough; we must do."),
		Chat.newChat(findPersonBy("John Burroughs"), "The smallest deed is better than the greatest intention."),
		Chat.newChat(findPersonBy("Jules Renard"), "Laziness is nothing more than the habit of resting before you get tired."),
		Chat.newChat(findPersonBy("Katherine Hepburn"), "Life is hard. After all, it kills you."),
		Chat.newChat(findPersonBy("Ken Kesey"), "You can't really be strong until you see a funny side to things."),
		Chat.newChat(findPersonBy("Lana Turner"), "A successful man is one who makes more money than his wife can spend. A successful woman is one who can find such a man."),
		Chat.newChat(findPersonBy("Maya Angelou"), "We may encounter many defeats but we must not be defeated."),
		Chat.newChat(findPersonBy("Mark Twain"), "All generalizations are false, including this one."),
		Chat.newChat(findPersonBy("Mark Twain"), "Don't let schooling interfere with your education."),
		Chat.newChat(findPersonBy("Mark Twain"), "Go to Heaven for the climate, Hell for the company."),
		Chat.newChat(findPersonBy("Martin Luther King Jr."), "Love is the only force capable of transforming an enemy into a friend."),
		Chat.newChat(findPersonBy("Milton Berle"), "A committee is a group that keeps minutes and loses hours."),
		Chat.newChat(findPersonBy("Mitch Hedberg"), "My fake plants died because I did not pretend to water them."),
		Chat.newChat(findPersonBy("Nelson Mandela"), "It always seems impossible until it's done."),
		Chat.newChat(findPersonBy("Nikos Kazantzakis"), "In order to succeed, we must first believe that we can."),
		Chat.newChat(findPersonBy("Oliver Herford"), "A woman's mind is cleaner than a man's; She changes it more often."),
		Chat.newChat(findPersonBy("Reinhold Niebuhr"), "God grant me the serenity to accept the things I cannot change, the courage to change the things I can, and the wisdom to know the difference."),
		Chat.newChat(findPersonBy("Reba McEntire"), "To succeed in life, you need three things: a wishbone, a backbone and a funny bone."),
		Chat.newChat(findPersonBy("Robert Benchley"), "Drawing on my fine command of the English language, I said nothing."),
		Chat.newChat(findPersonBy("Robin Williams"), "I'm sorry, if you were right, I'd agree with you."),
		Chat.newChat(findPersonBy("Samuel Beckett"), "Ever tried. Ever failed. No matter. Try Again. Fail again. Fail better."),
		Chat.newChat(findPersonBy("Samuel Beckett"), "We are all born mad. Some remain so."),
		Chat.newChat(findPersonBy("Stephan Hawking"), "Life would be tragic if it weren't funny."),
		Chat.newChat(findPersonBy("Theodore Roosevelt"), "Keep your eyes on the stars, and your feet on the ground."),
		Chat.newChat(findPersonBy("Thomas Edison"), "The chief function of the body is to carry the brain around."),
		Chat.newChat(findPersonBy("Unknown Unknown"), "A leader is one who knows the way, goes the way, and shows the way."),
		Chat.newChat(findPersonBy("Unknown Unknown"), "A lie gets halfway around the world before the truth has time to get its pants on."),
		Chat.newChat(findPersonBy("Unknown Unknown"), "Be happy for this moment. This moment is your life."),
		Chat.newChat(findPersonBy("Unknown Unknown"), "Give me a lever long enough and a fulcrum on which to place it and I shall move the world."),
		Chat.newChat(findPersonBy("Unknown Unknown"), "The greater danger for most of us lies not in setting our aim too high and falling short, but in setting our aim too low and achieving our mark."),
		Chat.newChat(findPersonBy("Unknown Unknown"), "The only thing worse than being blind is having sight but no vision."),
		Chat.newChat(findPersonBy("Virat Kohli"), "Self-belief and hard work will always earn you success."),
		Chat.newChat(findPersonBy("Walt Disney"), "The way to get started is to quit talking and begin doing."),
		Chat.newChat(findPersonBy("W. C. Fields"), "I cook with wine, sometimes I even add it to the food."),
		Chat.newChat(findPersonBy("W. H. Auden"), "We are all here on earth to help others; what on earth the others are here for I don't know."),
		Chat.newChat(findPersonBy("Will Rogers"), "Be thankful we're not getting all the government we're paying for."),
		Chat.newChat(findPersonBy("Will Rogers"), "Everything is funny, as long as it's happening to somebody else."),
		Chat.newChat(findPersonBy("William Lyon Phelps"), "If at first you don't succeed, find out if the loser gets anything."),
		Chat.newChat(findPersonBy("William Shakespear"), "We know what we are, but know not what we may be."),
		Chat.newChat(findPersonBy("Winston Churchill"), "I may be drunk, Miss, but in the morning I will be sober and you will still be ugly.")
	);

	private static final Person ANONYMOUS_PERSON = Person.newPerson("Anonymous", "Person");

	private static List<Chat> findChatsBy(Person person) {

		return chats.stream()
			.filter(chat -> chat.getPerson().equals(person))
			.toList();
	}

	private static @Nullable Person findPersonBy(String name) {

		return StreamUtils.stream(people)
			.filter(person -> person.getName().equals(name))
			.findFirst()
			.orElse(null);
	}

	@Getter(AccessLevel.PROTECTED)
	private final ChatService chatService;

	private final Random randomIndex;

	public FamousQuotesChatBot(@NotNull ChatService chatService) {
		this.chatService = ObjectUtils.requireObject(chatService, "ChatService is required");
		this.randomIndex = new Random(System.currentTimeMillis());
	}

	@Override
	public Chat chat(Person person) {

		List<Chat> personChats = findChatsBy(person);

		return personChats.isEmpty()
			? Chat.newChat(ANONYMOUS_PERSON, Chat.DEFAULT_MESSAGE)
			: personChats.get(randomIndex(personChats.size()));
	}


	private int randomIndex(int bound) {
		return this.randomIndex.nextInt(bound);
	}

	private Person randomPerson() {

		List<Person> personList = CollectionUtils.asList(people);

		return personList.get(randomIndex(personList.size()));
	}

	@Scheduled(initialDelay = SCHEDULE_INITIAL_DELAY, fixedRateString = "${example.chat.bot.schedule.rate:3000}")
	public void sendChat() {
		getChatService().send(chat(randomPerson()));
	}
}
