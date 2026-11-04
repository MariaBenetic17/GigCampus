-- ==========================================================
-- GigCampus Seed Migration V2
-- Initial Categories and Fictional Development Data
-- (Clearly marked as fictional for local evaluation)
-- ==========================================================

-- Categories
INSERT INTO categories (id, name, slug, description, icon_class, display_order) VALUES
(1, 'Website Development', 'website-development', 'Modern business websites, landing pages, and portfolio sites', 'bi-globe', 1),
(2, 'Graphic & Poster Design', 'graphic-design', 'Promotional posters, social media banners, and shop branding', 'bi-palette', 2),
(3, 'Social Media & Marketing', 'social-media-marketing', 'Instagram Reels, Google Maps setup, and digital promotions', 'bi-megaphone', 3),
(4, 'Excel & Data Organization', 'data-organization', 'Inventory sheets, billing spreadsheets, and data cleaning', 'bi-file-earmark-spreadsheet', 4),
(5, 'Software & Billing Assistance', 'software-billing', 'Basic desktop tools, GST billing setup, and barcode labels', 'bi-cpu', 5),
(6, 'Content & Translation', 'content-translation', 'Tamil & English content, product descriptions, and catalogs', 'bi-translate', 6);

-- Sample Users (Password is 'Password@123' hashed with BCrypt)
-- Hash: $2a$10$wKkS1Y4gZl2w1ZJqE99Hl.v7eTfA0M7Yw7pX0vXvj6H6bM8m2d2uO
-- We will use a standard BCrypt hash: $2a$10$e8Z4Jk69wYJ61fGzE8d7be8N2k6jGvU0N5Y0kQ6n6R0q7O4yW2s32
-- Note: Spring Boot DataSeeder / AdminBootstrap will also ensure safe runtime bootstrap.
