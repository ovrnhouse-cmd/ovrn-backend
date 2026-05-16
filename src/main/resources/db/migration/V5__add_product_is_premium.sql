-- Add is_premium column to products table
ALTER TABLE products ADD COLUMN is_premium BOOLEAN NOT NULL DEFAULT false;
