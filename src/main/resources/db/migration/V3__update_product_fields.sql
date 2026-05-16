-- Robustly update product fields
DO $$
BEGIN
    -- Rename base_price to selling_price if it exists
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='base_price') THEN
        ALTER TABLE products RENAME COLUMN base_price TO selling_price;
    END IF;

    -- Rename compare_at_price to marked_price if it exists
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='compare_at_price') THEN
        ALTER TABLE products RENAME COLUMN compare_at_price TO marked_price;
    END IF;

    -- Drop stock_quantity if it exists
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='stock_quantity') THEN
        ALTER TABLE products DROP COLUMN stock_quantity;
    END IF;

    -- Add in_stock if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='in_stock') THEN
        ALTER TABLE products ADD COLUMN in_stock BOOLEAN NOT NULL DEFAULT true;
    END IF;
END $$;

-- Update indexes
DROP INDEX IF EXISTS idx_products_stock;
DROP INDEX IF EXISTS idx_products_in_stock;
CREATE INDEX idx_products_in_stock ON products(in_stock);
