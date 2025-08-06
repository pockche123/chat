-- important for integration tests
CREATE KEYSPACE IF NOT EXISTS chatapp_test
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE chatapp_test;

CREATE TABLE IF NOT EXISTS chat_messages (
                                             message_id uuid,
                                             conversation_id uuid,
                                             timestamp timestamp,
                                             sender_id uuid,
                                             receiver_id uuid,
                                             content text,
                                             status text,
                                             PRIMARY KEY ((conversation_id), timestamp, message_id)
) WITH CLUSTERING ORDER BY (timestamp DESC);