-- Add supervisor staff as Employee records (referenced via supervisor_id/emp_id
-- fields across M5/M10 entities). Idempotent — inserts only names not already
-- present, so it is safe to (re)run on any database.
INSERT INTO `employee_details` (`employeename`, `status`)
SELECT n, 'A' FROM (
  SELECT 'Manoj R' AS n UNION ALL
  SELECT 'Balakumar G' UNION ALL
  SELECT 'Amarovinappul M' UNION ALL
  SELECT 'Vijayakumar J' UNION ALL
  SELECT 'Praveen Kumar M' UNION ALL
  SELECT 'Jeevanantham S' UNION ALL
  SELECT 'Deepan Babu M' UNION ALL
  SELECT 'Edwin Devaraj S' UNION ALL
  SELECT 'Elaventhan V' UNION ALL
  SELECT 'Kaviraj P R' UNION ALL
  SELECT 'Muthu M'
) x
WHERE NOT EXISTS (
  SELECT 1 FROM `employee_details` e WHERE e.`employeename` = x.n
);
