-- =============================================================================
-- audit_site_misplacement.sql  (READ-ONLY -- does NOT modify any data)
--
-- Purpose: detect data that was silently mis-attributed to SuZhou by the old
--          MySQL column default, even though the record clearly belongs to
--          Penang. Report-only; the operator decides whether to fix.
--
-- All comparisons use HEX literals so the file is charset-safe:
--   'SuZhou'  (Chinese)  = 0xE88B8FE5B79E
--   'Penang'             = 0x50656E616E67
--   double-encoded SuZhou mojibake = 0xE991BBE5BF93E7AA9E
--
-- Target schema: it_asset_manage
-- =============================================================================

USE `it_asset_manage`;

SET @site_sz       = 0xE88B8FE5B79E;          -- 'SuZhou'
SET @site_penang   = 0x50656E616E67;          -- 'Penang'
SET @site_mojibake = 0xE991BBE5BF93E7AA9E;    -- mojibake of 'SuZhou'

-- -----------------------------------------------------------------------------
-- A. asset_inbound: site = SuZhou but storage_location mentions Penang
-- -----------------------------------------------------------------------------
SELECT 'A. asset_inbound' AS audit, COUNT(*) AS hits
FROM asset_inbound
WHERE site = @site_sz
  AND storage_location LIKE CONCAT('%', @site_penang, '%');
-- samples
SELECT inbound_id, inbound_no, asset_name, serial_number, category_id,
       storage_location, site, applicant, apply_time
FROM asset_inbound
WHERE site = @site_sz
  AND storage_location LIKE CONCAT('%', @site_penang, '%');

-- -----------------------------------------------------------------------------
-- B. asset_info: site = SuZhou but category belongs to Penang
-- -----------------------------------------------------------------------------
SELECT 'B. asset_info' AS audit, COUNT(*) AS hits
FROM asset_info a
JOIN asset_category c ON a.category_id = c.category_id
WHERE a.site = @site_sz
  AND c.site = @site_penang;
-- samples
SELECT a.asset_id, a.asset_code, a.asset_name, a.category_id,
       c.category_name, c.site AS category_site, a.site AS asset_site,
       a.storage_location
FROM asset_info a
JOIN asset_category c ON a.category_id = c.category_id
WHERE a.site = @site_sz
  AND c.site = @site_penang
LIMIT 50;

-- -----------------------------------------------------------------------------
-- C. asset_use_record: site = SuZhou but asset_id points to a Penang asset
-- -----------------------------------------------------------------------------
SELECT 'C. asset_use_record' AS audit, COUNT(*) AS hits
FROM asset_use_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang;
-- samples
SELECT r.record_id, r.asset_id, a.asset_code, a.asset_name,
       r.site AS record_site, a.site AS asset_site, r.user_id, r.use_date
FROM asset_use_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang
LIMIT 50;

-- -----------------------------------------------------------------------------
-- D. asset_scrap_record: site = SuZhou but asset_id points to a Penang asset
-- -----------------------------------------------------------------------------
SELECT 'D. asset_scrap_record' AS audit, COUNT(*) AS hits
FROM asset_scrap_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang;
-- samples
SELECT r.scrap_id, r.asset_id, a.asset_code, a.asset_name,
       r.site AS record_site, a.site AS asset_site, r.create_time
FROM asset_scrap_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang
LIMIT 50;

-- -----------------------------------------------------------------------------
-- E. asset_repair_record: site = SuZhou but asset_id points to a Penang asset
-- -----------------------------------------------------------------------------
SELECT 'E. asset_repair_record' AS audit, COUNT(*) AS hits
FROM asset_repair_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang;
-- samples
SELECT r.repair_id, r.asset_id, a.asset_code, a.asset_name,
       r.site AS record_site, a.site AS asset_site, r.create_time
FROM asset_repair_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang
LIMIT 50;

-- -----------------------------------------------------------------------------
-- F. asset_transfer_record: site = SuZhou but asset_id points to a Penang asset
-- -----------------------------------------------------------------------------
SELECT 'F. asset_transfer_record' AS audit, COUNT(*) AS hits
FROM asset_transfer_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang;
-- samples
SELECT r.transfer_id, r.transfer_no, r.asset_id, a.asset_code, a.asset_name,
       r.site AS record_site, a.site AS asset_site, r.to_location, r.create_time
FROM asset_transfer_record r
JOIN asset_info a ON r.asset_id = a.asset_id
WHERE r.site = @site_sz
  AND a.site = @site_penang
LIMIT 50;

-- -----------------------------------------------------------------------------
-- G. Extra: rows still holding the double-encoded 'SuZhou' mojibake value
--    (asset_model / status_label were created with a mojibake DEFAULT).
-- -----------------------------------------------------------------------------
SELECT 'G. asset_model mojibake' AS audit, COUNT(*) AS hits
FROM asset_model WHERE site = @site_mojibake;
SELECT 'G. status_label mojibake' AS audit, COUNT(*) AS hits
FROM status_label WHERE site = @site_mojibake;

-- -----------------------------------------------------------------------------
-- H. Extra: site = SuZhou but storage_location mentions Penang at asset level
-- -----------------------------------------------------------------------------
SELECT 'H. asset_info by location' AS audit, COUNT(*) AS hits
FROM asset_info
WHERE site = @site_sz
  AND storage_location LIKE CONCAT('%', @site_penang, '%');

-- -----------------------------------------------------------------------------
-- I. NULL-site sweep across EVERY table that has a `site` column.
--    Purpose: reveal write paths that already store NULL (and, more importantly,
--    any that would newly store NULL once the DB column default is removed).
--    Result rows are tables whose site IS NULL for >=1 row; an empty result is
--    the expected/healthy outcome.
--    Built dynamically (single UNION ALL statement) so no table is hard-coded.
-- -----------------------------------------------------------------------------
SET SESSION group_concat_max_len = 1000000;
SET @nullsql = (
  SELECT GROUP_CONCAT(
    CONCAT('SELECT ''', TABLE_NAME, ''' AS table_name, COUNT(*) AS null_site_rows FROM `',
           TABLE_NAME, '` WHERE `site` IS NULL')
    SEPARATOR ' UNION ALL ')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND COLUMN_NAME = 'site'
);
SET @nullsql = CONCAT('SELECT * FROM (', @nullsql, ') x WHERE null_site_rows > 0 ORDER BY table_name');
PREPARE stmt_null FROM @nullsql;
EXECUTE stmt_null;
DEALLOCATE PREPARE stmt_null;
