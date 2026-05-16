-- Rename price columns
ALTER TABLE products RENAME COLUMN base_price TO selling_price;
ALTER TABLE products RENAME COLUMN compare_at_price TO marked_price;

-- Replace stock_quantity with in_stock
ALTER TABLE products DROP COLUMN stock_quantity;
ALTER TABLE products ADD COLUMN in_stock BOOLEAN NOT NULL DEFAULT true;

-- Update indexes
DROP INDEX IF EXISTS idx_products_stock;
CREATE INDEX idx_products_in_stock ON products(in_stock);
