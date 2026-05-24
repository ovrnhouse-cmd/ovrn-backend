CREATE TABLE banner_texts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    text VARCHAR(500) NOT NULL,
    is_highlight BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_banner_texts_modtime
    BEFORE UPDATE ON banner_texts
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();
