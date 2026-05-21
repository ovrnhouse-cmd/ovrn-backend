ALTER TABLE orders ADD COLUMN IF NOT EXISTS razorpay_order_id VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS razorpay_payment_id VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS paid_at TIMESTAMPTZ;

CREATE INDEX idx_orders_razorpay_order_id ON orders(razorpay_order_id);
