-- V23: Redesign size_charts as a global table (not product-scoped)
--
-- Changes:
--   1. Drop the old product-scoped size_charts table (created in V22)
--   2. Recreate it as a global table (no product_id FK, adds `name` column)
--   3. Add size_chart_id nullable FK on products table

-- Step 1: Drop old table if it exists (V22 created it with product_id)
DROP TABLE IF EXISTS size_charts;

-- Step 2: Create new global size_charts table
CREATE TABLE size_charts (
    id         UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name       VARCHAR(255)             NOT NULL,
    image_name VARCHAR(255)             NOT NULL,
    slug       VARCHAR(255)             NOT NULL UNIQUE,
    url        VARCHAR(2048)            NOT NULL,
    alt_text   VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_size_charts_slug ON size_charts (slug);

-- Step 3: Add nullable size_chart_id FK to products
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS size_chart_id UUID
        REFERENCES size_charts (id)
        ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_products_size_chart_id ON products (size_chart_id);
