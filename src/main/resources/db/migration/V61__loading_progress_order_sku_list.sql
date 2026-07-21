UPDATE page_definition
SET code = CASE code
    WHEN 'pico:order-dispatch-detail' THEN 'aria:order-dispatch-detail'
    WHEN 'pico:order-dispatch-detail-upload' THEN 'aria:order-dispatch-detail-upload'
END
WHERE code IN (
    'pico:order-dispatch-detail',
    'pico:order-dispatch-detail-upload'
);
