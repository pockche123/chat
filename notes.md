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