-- =====================================================================
-- V21_1 — Delete the broken camelCase entity_field_registry rows that
-- V21 failed to UPDATE because snake_case duplicates already existed.
-- =====================================================================
DELETE FROM entity_field_registry WHERE entity_code='Driver' AND field_code IN
  ('dlNumber','dlExpiry','primaryMobile','altMobile','preferredLanguage','aadhaarLast4');
DELETE FROM entity_field_registry WHERE entity_code='Vehicle' AND field_code IN
  ('vehicleType','tankerCompartments','tankCleaningCertNo','tankCleaningCertExpiry',
   'tareWeightKg','rcExpiry','insuranceExpiry','pucExpiry','previousLoad','transporterId');
DELETE FROM entity_field_registry WHERE entity_code='TankLocation' AND field_code IN
  ('tankCapacityKL','currentOilGrade','batchDensity','lastCleanedOn','minHeadspaceKL','cleaningCertExpiry');
-- Just in case the V19 widget hint UPDATE created `id` row on Driver
DELETE FROM entity_field_registry WHERE entity_code='Driver' AND field_code='id';
