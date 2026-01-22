USE my_chat;
DROP TABLE IF EXISTS audit_logs;
CREATE TABLE IF NOT Exists audit_logs
(
    auditId uuid,
    userId uuid,
    action text,
    status text,
    timestamp timestamp,
    ipAddress text,
    details text,
    primary key (auditId)
);
