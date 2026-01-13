Mono.fromRunnable() returns Mono<Void>


i understand why to use fromCallable() but explain to me why to use flatMap instead of then since thne is used to chain stream as well

> Great question! The difference is what they do with return values:

.then() - Discards the previous value, only signals completion:
java
.then(chatMessageRepository.save(message))  // Returns Mono<ChatMessage>
// Result: Mono<ChatMessage> (from save operation)
// Problem: You lose the original message object!


.flatMap() - Uses the previous value and can transform it:
java
.flatMap(msg -> chatMessageRepository.save(msg))  // Takes message, returns Mono<ChatMessage>
// Result: Mono<ChatMessage> (the saved message)
// Good: You keep the message flowing through the chain


> ah makes sense because you are not careting a new object in each step right. if you were createing an object in fromRunnable() step then this would be a problem btcause this will get lost. but since we have got our og message we don't need to worry abou that. or am i capping?


Your understanding is perfect:

With object mutation (what you're doing):
java
.then(Mono.fromRunnable(() -> message.setStatus(MessageStatus.DELIVERED)))  // ← Modifies SAME object
.then(chatMessageRepository.save(message))  // ← Saves SAME object (now modified)

✅ Works fine - you're always working with the same object reference

If you were creating new objects:
java
.then(Mono.fromCallable(() -> {
ChatMessage newMessage = message.copy();  // ← Creates NEW object
newMessage.setStatus(MessageStatus.DELIVERED);
return newMessage;  // ← This new object gets lost!
}))
.then(chatMessageRepository.save(message))  // ← Still saves ORIGINAL object

❌ Problem - the new object gets discarded by .then()

You're not "capping" at all - your analysis is spot on:

• **Same object reference** → fromRunnable() + .then() works fine
• **New object creation** → need fromCallable() + .flatMap() to pass the new object along

Your original approach was correct because you're mutating the existing object, not creating new ones. The mock was the only real issue!


# Why does expectNextMatches return a boolean?

# Better approaches:

Option 1: Pure predicate (cleaner):
java
.expectNextMatches(chatMessage ->
chatMessage.getSenderId().equals(senderId) &&
chatMessage.getContent().equals("Hello world!")
)


Option 2: Use expectNext() with exact object:
java
.expectNext(expectedChatMessage)


Option 3: Use assertNext() for assertions:
java
.assertNext(chatMessage -> {
assertEquals(senderId, chatMessage.getSenderId());
assertEquals("Hello world!", chatMessage.getContent());
// No return needed!
})


assertNext() is usually better for complex assertions - no boolean return required! 🎯

> what is a predicate?

> A Predicate is a function that takes an input and returns true or false.

## Simple Definition:
java
Predicate<String> = "Does this string meet my condition?"


## Examples:

Basic predicate:
java
Predicate<Integer> isEven = number -> number % 2 == 0;

isEven.test(4);  // returns true
isEven.test(5);  // returns false


String predicate:
java
Predicate<String> isLongName = name -> name.length() > 5;

isLongName.test("John");     // false
isLongName.test("Alexander"); // true


In your test:
java
.expectNextMatches(chatMessage -> chatMessage.getSenderId().equals(senderId))
//                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                 This is a Predicate<ChatMessage>
//                 Takes ChatMessage, returns boolean


## Think of it as:
"Give me a yes/no question about this object"

• Input: ChatMessage
• Question: "Does this message have the right senderId?"
• Output: true/false

Predicates are just boolean tests! 🎯


> what is consumer? that's what .assertNext() says it is

> A Consumer is a function that takes an input and does something with it (but returns nothing).

## Simple Definition:
java
Consumer<String> = "Do something with this string, but don't return anything"


## Examples:

Basic consumer:
java
Consumer<String> printer = message -> System.out.println(message);

printer.accept("Hello!");  // Prints "Hello!" but returns void


Testing consumer:
java
Consumer<ChatMessage> validator = chatMessage -> {
assertEquals("expected", chatMessage.getContent());
assertNotNull(chatMessage.getMessageId());
// Does assertions but returns nothing
};


## In your test:
java
.assertNext(chatMessage -> {
assertEquals(senderId, chatMessage.getSenderId());
assertEquals("Hello world!", chatMessage.getContent());
// This is a Consumer<ChatMessage>
// Takes ChatMessage, does assertions, returns void
})


## Key Difference:
• **Predicate:** Takes input → returns boolean (test)
• **Consumer:** Takes input → returns nothing (action)

Consumer = "Do something with this object"
Predicate = "Test this object and tell me true/false"

> what are these types of function called ie consumer, predicate etc etc? Do they have a collective name?

> They're called "Functional Interfaces" - part of Java's functional programming support.

## Common Functional Interfaces:

Consumer<T> - Takes input, returns nothing
java
Consumer<String> printer = s -> System.out.println(s);


Predicate<T> - Takes input, returns boolean
java
Predicate<Integer> isEven = n -> n % 2 == 0;


Function<T,R> - Takes input T, returns R
java
Function<String, Integer> getLength = s -> s.length();


Supplier<T> - Takes nothing, returns T
java
Supplier<String> getMessage = () -> "Hello World";


BiFunction<T,U,R> - Takes two inputs, returns R
java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;


## Collective Names:
• **"Functional Interfaces"** (Java term)
• **"Lambda Types"** (informal)
• **"Function Types"** (general programming term)

## They enable:
• **Lambda expressions** (x -> x * 2)
• **Method references** (String::length)
• **Functional programming** patterns

Java 8+ functional programming building blocks! 🎯



# AWS S3
> Here's what each object does:

1. PutObjectRequest - The actual S3 upload request
   • bucket: Which S3 bucket to upload to
   • key: The file path/name in S3 (like "chat-media/uuid-filename.jpg")
   • contentType: MIME type (image/jpeg, video/mp4, etc.)
   • This is what would normally be sent to S3 for a direct upload

2. PutObjectPresignRequest - Instructions for creating the pre-signed URL
   • signatureDuration: How long the URL stays valid (10 minutes)
   • putObjectRequest: Wraps the actual request from #1
   • This tells AWS "create a temporary URL for this specific upload request"

3. PresignedPutObjectRequest - The result containing the pre-signed URL
   • Contains the actual HTTPS URL with AWS signatures
   • Client can use this URL to upload directly to S3
   • No AWS credentials needed on client side
