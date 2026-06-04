-- V22: Create size_charts table
-- Stores size chart images linked to a product.
-- image_name : original uploaded filename (e.g. mens-tee-size-chart.png)
-- slug       : URL-friendly unique identifier generated via SlugUtils
-- url        : Cloudinary secure URL (wolf-ovrn/size-charts folder)

CREATE TABLE IF NOT EXISTS size_charts (
    id         UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    product_id UUID                     NOT NULL,
    image_name VARCHAR(255)             NOT NULL,
    slug       VARCHAR(255)             NOT NULL UNIQUE,
    url        VARCHAR(2048)            NOT NULL,
    alt_text   VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_size_chart_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_size_charts_product_id ON size_charts (product_id);
CREATE INDEX IF NOT EXISTS idx_size_charts_slug       ON size_charts (slug);
