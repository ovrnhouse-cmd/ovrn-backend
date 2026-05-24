CREATE TABLE product_categories (
    product_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Copy existing categories into the mapping table
INSERT INTO product_categories (product_id, category_id)
SELECT id, category_id
FROM products
WHERE category_id IS NOT NULL;

-- Remove category_id from products
ALTER TABLE products DROP CONSTRAINT IF EXISTS products_category_id_fkey;
DROP INDEX IF EXISTS idx_products_category;
ALTER TABLE products DROP COLUMN category_id;
