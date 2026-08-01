-- Sidebar/page label rename per user request: "Loading Supervisor Allocation"
-- -> "Supervisor Allocation" for aria:loading-allocation.

UPDATE page_definition
SET title = 'Supervisor Allocation'
WHERE code = 'aria:loading-allocation' AND title = 'Loading Supervisor Allocation';
