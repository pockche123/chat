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

