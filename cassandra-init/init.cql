CREATE KEYSPACE IF NOT EXISTS my_chat
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
