-- Add available_sizes to products
ALTER TABLE products ADD COLUMN available_sizes JSONB DEFAULT '[]'::jsonb;

-- Add size to order_items
ALTER TABLE order_items ADD COLUMN size VARCHAR(20);
