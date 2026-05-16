-- SEED CATEGORIES
INSERT INTO categories (name, slug, description) VALUES
('Apparel', 'apparel', 'High-quality minimal clothing'),
('Accessories', 'accessories', 'Essential daily carry items'),
('Lifestyle', 'lifestyle', 'Objects for your living space')
ON CONFLICT (slug) DO NOTHING;

-- SEED PRODUCTS
-- Apparel
INSERT INTO products (category_id, name, slug, description, base_price, compare_at_price, stock_quantity)
SELECT id, 'OVRN Essential Tee', 'ovrn-essential-tee', 'The perfect everyday t-shirt, crafted from premium organic cotton.', 2400.00, 2900.00, 100
FROM categories WHERE slug = 'apparel'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (category_id, name, slug, description, base_price, compare_at_price, stock_quantity)
SELECT id, 'Stealth Oversized Hoodie', 'stealth-oversized-hoodie', 'Heavyweight fleece hoodie with a modern oversized fit.', 5500.00, null, 50
FROM categories WHERE slug = 'apparel'
ON CONFLICT (slug) DO NOTHING;

-- Accessories
INSERT INTO products (category_id, name, slug, description, base_price, compare_at_price, stock_quantity)
SELECT id, 'Vanguard Card Holder', 'vanguard-card-holder', 'Slim leather card holder with RFID protection.', 1800.00, 2200.00, 75
FROM categories WHERE slug = 'accessories'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (category_id, name, slug, description, base_price, compare_at_price, stock_quantity)
SELECT id, 'OVRN Signature Cap', 'ovrn-signature-cap', 'Classic 6-panel cap with embroidered branding.', 1200.00, null, 120
FROM categories WHERE slug = 'accessories'
ON CONFLICT (slug) DO NOTHING;

-- Lifestyle
INSERT INTO products (category_id, name, slug, description, base_price, compare_at_price, stock_quantity)
SELECT id, 'Monolith Ceramic Vase', 'monolith-ceramic-vase', 'Hand-crafted ceramic vase with a matte black finish.', 3200.00, 4000.00, 30
FROM categories WHERE slug = 'lifestyle'
ON CONFLICT (slug) DO NOTHING;

-- SEED PRODUCT IMAGES (Placeholders)
INSERT INTO product_images (product_id, url, alt_text, is_primary, display_order)
SELECT id, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=1000', 'Essential Tee Front', true, 0
FROM products WHERE slug = 'ovrn-essential-tee';

INSERT INTO product_images (product_id, url, alt_text, is_primary, display_order)
SELECT id, 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?q=80&w=1000', 'Oversized Hoodie', true, 0
FROM products WHERE slug = 'stealth-oversized-hoodie';

INSERT INTO product_images (product_id, url, alt_text, is_primary, display_order)
SELECT id, 'https://images.unsplash.com/photo-1627123424574-724758594e93?q=80&w=1000', 'Card Holder', true, 0
FROM products WHERE slug = 'vanguard-card-holder';

INSERT INTO product_images (product_id, url, alt_text, is_primary, display_order)
SELECT id, 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?q=80&w=1000', 'Signature Cap', true, 0
FROM products WHERE slug = 'ovrn-signature-cap';

INSERT INTO product_images (product_id, url, alt_text, is_primary, display_order)
SELECT id, 'https://images.unsplash.com/photo-1581783898377-1c85bf937427?q=80&w=1000', 'Ceramic Vase', true, 0
FROM products WHERE slug = 'monolith-ceramic-vase';

-- SEED DUMMY ADMIN USER (For testing, though OAuth is preferred)
INSERT INTO users (email, first_name, last_name, provider, provider_id, role)
VALUES ('admin@ovrn.in', 'Admin', 'User', 'INTERNAL', 'admin_001', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
