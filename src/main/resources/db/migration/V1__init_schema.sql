-- ==========================================================
-- GigCampus Database Schema Migration V1
-- Hyperlocal Student Gig Marketplace (Erode, Tamil Nadu)
-- ==========================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- 2. Student Profiles Table
CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    college_name VARCHAR(150),
    department VARCHAR(100),
    year_of_study VARCHAR(20),
    skills TEXT,
    location VARCHAR(100),
    bio TEXT,
    portfolio_url VARCHAR(255),
    github_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    rating_average DECIMAL(3, 2) DEFAULT 0.00,
    reviews_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_student_user ON student_profiles(user_id);

-- 3. Business Profiles Table
CREATE TABLE IF NOT EXISTS business_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    business_category VARCHAR(100),
    location VARCHAR(150),
    description TEXT,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    website_url VARCHAR(255),
    gstin_or_reg_number VARCHAR(50),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verification_notes TEXT,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_business_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_business_user ON business_profiles(user_id);
CREATE INDEX idx_business_status ON business_profiles(verification_status);

-- 4. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_class VARCHAR(50),
    display_order INT DEFAULT 0
);

-- 5. Gigs Table
CREATE TABLE IF NOT EXISTS gigs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    required_skills VARCHAR(255),
    budget DECIMAL(10, 2) NOT NULL,
    deadline DATE NULL,
    work_mode VARCHAR(30) NOT NULL DEFAULT 'REMOTE',
    location VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    views_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gig_business FOREIGN KEY (business_id) REFERENCES business_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_gig_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_gigs_business ON gigs(business_id);
CREATE INDEX idx_gigs_category ON gigs(category_id);
CREATE INDEX idx_gigs_status ON gigs(status);

-- 6. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gig_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    proposal TEXT NOT NULL,
    estimated_days INT NOT NULL,
    proposed_budget DECIMAL(10, 2),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    business_notes TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_gig FOREIGN KEY (gig_id) REFERENCES gigs(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uq_gig_student UNIQUE (gig_id, student_id)
);

CREATE INDEX idx_app_gig ON applications(gig_id);
CREATE INDEX idx_app_student ON applications(student_id);
CREATE INDEX idx_app_status ON applications(status);

-- 7. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    link_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_user ON notifications(recipient_user_id);
CREATE INDEX idx_notification_read ON notifications(is_read);

-- 8. Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gig_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_gig FOREIGN KEY (gig_id) REFERENCES gigs(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_review_gig ON reviews(gig_id);
CREATE INDEX idx_review_recipient ON reviews(recipient_id);

-- 9. Reports Table
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    gig_id BIGINT NULL,
    reported_user_id BIGINT NULL,
    reason VARCHAR(255) NOT NULL,
    details TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_gig FOREIGN KEY (gig_id) REFERENCES gigs(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_report_status ON reports(status);
