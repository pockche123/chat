User flow:
1. User A sends a chat message to Chat server 1.
2. Chat server 1 obtains a message ID from the ID generator.
3. Chat server 1 sends the message to the message sync queue.
4. The message is stored in a key-value store.
5.a. If User B is online, the message is forwarded to Chat server 2 where User B is connected.
5.b. If User B is offline, a push notification is sent from push notification (PN) servers.
6. Chat server 2 forwards the message to User B. There is a persistent WebSocket connection between User B and Chat server 2.


## CURRENT FLOW  (What We're Adding):
Server 1: User A → WebSocket → ChatMessageService → Kafka
Server 2: Kafka → ChatMessageListener → MessageDeliveryService → User B

Features:
- User authentication  for secure access.
- Real-time message broadcasting using reactive streams.
- Save chat history in a database for future reference.


Things to work on: 
- MULTISERVER features 
- using redis for online status
- using Kafka for multi-server message sync queue. 
- kubernetes config
- sonar checks

## Next Logical Step:
Enhance your existing services to actually use Redis and Kafka properly.

Currently your services are basic stubs:
• RedisServerRegistryService - Only reads, doesn't write user status
• KafkaMessageQueueService - Only sends, doesn't receive

Which would you like to tackle first?
1. Redis: Make it track online users properly (login/logout)
2. Kafka: Add a consumer to receive messages from other servers