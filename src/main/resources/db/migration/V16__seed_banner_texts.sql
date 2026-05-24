-- Seed banner texts from the initial banner strip design.
-- "▌" prefix stripped as per design revision.
-- is_highlight = true renders with the accent/highlight CSS class on the frontend.

INSERT INTO banner_texts (id, text, is_highlight, sort_order, is_active) VALUES
    (gen_random_uuid(), 'DROP 002 — HOWL SEASON // LOADING',      false, 0, true),
    (gen_random_uuid(), 'T-MINUS 14 : 07 : 42 : 18',              true,  1, true),
    (gen_random_uuid(), 'GINTAMA X OVRN CREWNECK — RESTOCK 05.11',false, 2, true),
    (gen_random_uuid(), 'FREE SHIPPING · ORDERS OVER ₹ 4,999',    false, 3, true),
    (gen_random_uuid(), 'OVER AND BEYOND',                         false, 4, true);
