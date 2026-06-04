-- V21: Add image_name and slug columns to product_images
-- These columns store the original filename and a URL-friendly slug
-- generated via SlugUtils for each uploaded product image.

ALTER TABLE product_images
    ADD COLUMN IF NOT EXISTS image_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS slug       VARCHAR(255) UNIQUE;
