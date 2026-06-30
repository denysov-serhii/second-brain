CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS log_entries (
    id UUID PRIMARY KEY,
    raw_content TEXT,
    extracted_text TEXT,
    summary TEXT,
    log_type VARCHAR(50) NOT NULL,
    embedding vector(1536),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source_device VARCHAR(20) NOT NULL
);
