-- Rename RETURN enum to EXCHANGE
ALTER TYPE faq_category RENAME VALUE 'RETURN' TO 'EXCHANGE';

-- Update the FAQ entry for Exchange policy
UPDATE faqs 
SET question = 'What is your exchange policy?', 
    answer = 'We offer a 7-day exchange policy. There is no return policy. Items must have their tags uncut and be unworn to be eligible for exchange.'
WHERE category = 'EXCHANGE';

-- Update the FAQ entry for Shipping policy
UPDATE faqs 
SET answer = 'Most orders are processed within 2-3 business days. Shopping above ₹ 1,499 qualifies for free shipping. For orders below this amount, standardized shipping fees will apply at checkout based on the distance.'
WHERE category = 'SHIPPING' AND question = 'How long does shipping take?';

-- Update Banner text for Free shipping
UPDATE banner_texts
SET text = 'FREE SHIPPING · ORDERS OVER ₹ 1,499'
WHERE text LIKE '%FREE SHIPPING%';
