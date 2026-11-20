CREATE TABLE applications (
                              id UUID PRIMARY KEY,
                              client_id UUID NOT NULL,
                              lender_id UUID NOT NULL,
                              status TEXT NOT NULL,
                              requested_amount NUMERIC NOT NULL,
                              lender_block_id TEXT
);

CREATE TABLE outbox_messages (
                                 id UUID PRIMARY KEY,
                                 topic TEXT NOT NULL,
                                 message_key TEXT NOT NULL,
                                 payload_json TEXT NOT NULL,
                                 created_at TIMESTAMPTZ NOT NULL,
                                 published_at TIMESTAMPTZ
);