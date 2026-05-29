ALTER TABLE drop_events
  ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;

UPDATE drop_events
  SET expires_at = drop_date + INTERVAL '48 hours'
  WHERE expires_at IS NULL;

ALTER TABLE drop_events
  ALTER COLUMN expires_at SET NOT NULL;

CREATE INDEX idx_drop_events_dates
  ON drop_events (is_active, drop_date, expires_at);
