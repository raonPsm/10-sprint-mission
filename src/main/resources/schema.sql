-- 테이블
-- User
CREATE TABLE users
(
    id         uuid PRIMARY KEY,
    created_at timestamp with time zone   NOT NULL,
    updated_at timestamp with time zone,
    username   varchar(50) UNIQUE         NOT NULL,
    email      varchar(100) UNIQUE        NOT NULL,
    password   varchar(60)                NOT NULL,
    profile_id uuid,
    role       varchar(20) DEFAULT 'USER' NOT NULL
);

-- BinaryContent
CREATE TABLE binary_contents
(
    id           uuid PRIMARY KEY,
    created_at   timestamp with time zone NOT NULL,
    file_name    varchar(255)             NOT NULL,
    size         bigint                   NOT NULL,
    content_type varchar(100)             NOT NULL
--     ,bytes        bytea        NOT NULL
);

-- -- UserStatus
-- CREATE TABLE user_statuses
-- (
--     id             uuid PRIMARY KEY,
--     created_at     timestamp with time zone NOT NULL,
--     updated_at     timestamp with time zone,
--     user_id        uuid UNIQUE              NOT NULL,
--     last_active_at timestamp with time zone NOT NULL
-- );

-- Channel
CREATE TABLE channels
(
    id          uuid PRIMARY KEY,
    created_at  timestamp with time zone NOT NULL,
    updated_at  timestamp with time zone,
    name        varchar(100),
    description varchar(500),
    type        varchar(10)              NOT NULL
);

-- Message
CREATE TABLE messages
(
    id         uuid PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    content    text,
    channel_id uuid                     NOT NULL,
    author_id  uuid
);

-- Message.attachments
CREATE TABLE message_attachments
(
    message_id    uuid,
    attachment_id uuid,
    PRIMARY KEY (message_id, attachment_id)
);

-- ReadStatus
CREATE TABLE read_statuses
(
    id           uuid PRIMARY KEY,
    created_at   timestamp with time zone NOT NULL,
    updated_at   timestamp with time zone,
    user_id      uuid                     NOT NULL,
    channel_id   uuid                     NOT NULL,
    last_read_at timestamp with time zone NOT NULL,
    UNIQUE (user_id, channel_id)
);


-- 제약 조건
-- User (1) -> BinaryContent (1)
ALTER TABLE users
    ADD CONSTRAINT fk_user_binary_content
        FOREIGN KEY (profile_id)
            REFERENCES binary_contents (id)
            ON DELETE SET NULL;

-- -- UserStatus (1) -> User (1)
-- ALTER TABLE user_statuses
--     ADD CONSTRAINT fk_user_status_user
--         FOREIGN KEY (user_id)
--             REFERENCES users (id)
--             ON DELETE CASCADE;

-- Message (N) -> Channel (1)
ALTER TABLE messages
    ADD CONSTRAINT fk_message_channel
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE;

-- Message (N) -> Author (1)
ALTER TABLE messages
    ADD CONSTRAINT fk_message_user
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE SET NULL;

-- MessageAttachment (1) -> BinaryContent (1)
ALTER TABLE message_attachments
    ADD CONSTRAINT fk_message_attachment_binary_content
        FOREIGN KEY (attachment_id)
            REFERENCES binary_contents (id)
            ON DELETE CASCADE;

-- ReadStatus (N) -> User (1)
ALTER TABLE read_statuses
    ADD CONSTRAINT fk_read_status_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- ReadStatus (N) -> User (1)
ALTER TABLE read_statuses
    ADD CONSTRAINT fk_read_status_channel
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE;

-- 마이그레이션
-- ALTER TABLE users
--     ADD role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Persistence Token
CREATE TABLE IF NOt EXISTS persistent_logins (
    username  VARCHAR(64) NOT NULL,    -- 누구의 토큰인지 식별하는 값
    series    VARCHAR(64) PRIMARY KEY, -- 쿠키 식별자 (고정) | 기기/브라우저를 식별하는 식별자
    token     VARCHAR(64) NOT NULL,    -- 매 요청마다 갱신 (인증에 사용되는 값)
    last_used TIMESTAMP   NOT NULL     -- 만료 체크, 쿠키 탈취 감지 보조
)
