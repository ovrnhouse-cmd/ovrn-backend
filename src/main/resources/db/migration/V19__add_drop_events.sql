-- DROP EVENTS TABLE
CREATE TABLE drop_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    drop_date TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- DROP EVENT PRODUCTS (MANY-TO-MANY)
CREATE TABLE drop_event_products (
    drop_event_id UUID NOT NULL REFERENCES drop_events(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (drop_event_id, product_id)
);

-- TRIGGER FOR updated_at
CREATE TRIGGER update_drop_events_modtime 
BEFORE UPDATE ON drop_events 
FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- INDEXES
CREATE INDEX idx_drop_events_slug ON drop_events(slug);
CREATE INDEX idx_drop_events_date ON drop_events(drop_date);
