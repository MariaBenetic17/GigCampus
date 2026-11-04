-- ==========================================================
-- GigCampus Database Schema Migration V3
-- Real-Time Live Chat, Saved Gigs, and Admin Audit Logs
-- ==========================================================

-- 10. Conversations Table
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gig_id BIGINT NULL,
    application_id BIGINT NULL,
    title VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conv_gig FOREIGN KEY (gig_id) REFERENCES gigs(id) ON DELETE SET NULL,
    CONSTRAINT fk_conv_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE SET NULL
);

CREATE INDEX idx_conv_gig ON conversations(gig_id);
CREATE INDEX idx_conv_app ON conversations(application_id);

-- 11. Conversation Participants Table
CREATE TABLE IF NOT EXISTS conversation_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cp_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_conv_user UNIQUE (conversation_id, user_id)
);

CREATE INDEX idx_cp_conv ON conversation_participants(conversation_id);
CREATE INDEX idx_cp_user ON conversation_participants(user_id);

-- 12. Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_msg_conv ON messages(conversation_id);
CREATE INDEX idx_msg_sender ON messages(sender_id);
CREATE INDEX idx_msg_sent_at ON messages(sent_at);

-- 13. Saved Gigs Table
CREATE TABLE IF NOT EXISTS saved_gigs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    gig_id BIGINT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_gig FOREIGN KEY (gig_id) REFERENCES gigs(id) ON DELETE CASCADE,
    CONSTRAINT uq_student_saved_gig UNIQUE (student_id, gig_id)
);

CREATE INDEX idx_saved_student ON saved_gigs(student_id);
CREATE INDEX idx_saved_gig ON saved_gigs(gig_id);

-- 14. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NULL,
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_admin FOREIGN KEY (admin_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_admin ON audit_logs(admin_user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
