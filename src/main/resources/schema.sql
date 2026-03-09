
-- 연결 검증 SQL
SELECT current_database(), current_user, current_schema();

-- 기존 테이블 삭제 (초기화용)
-- 1. 가장 하위 자식 테이블 (다른 테이블들을 참조함)
DROP TABLE IF EXISTS message_attachments;
DROP TABLE IF EXISTS read_statuses;

-- 2. 중간 계층 테이블 (상위 부모를 참조함)
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS user_statuses;

-- 3. 최상위 부모 테이블 (참조를 당하는 입장이므로 가장 마지막에 삭제)
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS channels;
DROP TABLE IF EXISTS binary_contents;

--

CREATE TABLE binary_contents (
                                 id UUID PRIMARY KEY,
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                 file_name VARCHAR(255) NOT NULL,
                                 size BIGINT NOT NULL,
                                 content_type VARCHAR(100) NOT NULL
    -- bytes BYTEA NOT NULL
);

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITH TIME ZONE,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(60) NOT NULL,
                       profile_id UUID UNIQUE,

                       FOREIGN KEY (profile_id) REFERENCES binary_contents(id) ON DELETE SET NULL
);

CREATE TABLE user_statuses (
                               id UUID PRIMARY KEY,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               updated_at TIMESTAMP WITH TIME ZONE,
                               user_id UUID NOT NULL UNIQUE,
                               last_active_at TIMESTAMP WITH TIME ZONE NOT NULL,

                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE channels (
                          id UUID PRIMARY KEY,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE,
                          name VARCHAR(100),
                          description VARCHAR(500),
                          type VARCHAR(10) NOT NULL CHECK (type IN ('PUBLIC', 'PRIVATE'))
);

CREATE TABLE messages (
                          id UUID PRIMARY KEY,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE,
                          content TEXT,
                          channel_id UUID NOT NULL,
                          author_id UUID,

                          FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
                          FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE read_statuses (
                               id UUID PRIMARY KEY,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               updated_at TIMESTAMP WITH TIME ZONE,
                               user_id UUID NOT NULL,
                               channel_id UUID NOT NULL,
                               last_read_at TIMESTAMP WITH TIME ZONE NOT NULL,

                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
                               CONSTRAINT uk_user_channel UNIQUE (user_id, channel_id)
);

CREATE TABLE message_attachments (
                                     message_id UUID NOT NULL,
                                     attachment_id UUID NOT NULL,
                                     PRIMARY KEY (message_id, attachment_id),
                                     FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
                                     FOREIGN KEY (attachment_id) REFERENCES binary_contents(id) ON DELETE CASCADE
);