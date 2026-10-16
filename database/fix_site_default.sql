-- =============================================================================
-- fix_site_default.sql
--
-- Purpose : remove the implicit DEFAULT of the `site` column on EVERY table.
--
-- Root cause (production incident 2026-10-16, "Penang inbound not uploaded"):
--   1) MyBatis-Plus default insert strategy is FieldStrategy.NOT_NULL, so a
--      NULL field is OMITTED from the generated INSERT statement.
--   2) When the inbound payload carried no `site`, the INSERT omitted the
--      `site` column, and MySQL silently filled the column DEFAULT ('SuZhou').
--   3) Penang records were therefore stored under SuZhou and disappeared from
--      the Penang site list, which looked like "the upload failed".
--   Dropping the column DEFAULT removes the silent fallback at the DB layer,
--   turning a silent mis-attribution into an explicit NULL.
--
-- Design notes:
--   * ASCII-ONLY on purpose: avoids file/connection charset pitfalls.
--   * IDEMPOTENT: tables whose `site` column already has no default are skipped.
--     Safe to run more than once.
--   * Tables are DISCOVERED dynamically via information_schema (no hard-coded
--     list, so no table can be missed).
--   * The data fix uses the HEX literal 0x50656E616E67 = 'Penang', again to
--     avoid any charset issue on the wire.
--
-- Target schema : it_asset_manage (shared by production and local dev).
-- Run as        : mysql --default-character-set=utf8mb4 -u<user> -p < fix_site_default.sql
-- =============================================================================

USE `it_asset_manage`;

-- -----------------------------------------------------------------------------
-- 0. BEFORE: report the tables that still carry a `site` default.
--    Expected on first run: 19 rows (2 of them with a mojibake default).
-- -----------------------------------------------------------------------------
SELECT 'BEFORE' AS phase,
       TABLE_NAME,
       COLUMN_DEFAULT,
       HEX(COLUMN_DEFAULT) AS hex_default
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND COLUMN_NAME = 'site'
  AND COLUMN_DEFAULT IS NOT NULL
ORDER BY TABLE_NAME;

-- -----------------------------------------------------------------------------
-- 1. Dynamically DROP DEFAULT on every `site` column.
--    A stored procedure with a cursor is used so the table list is discovered
--    at runtime from information_schema.COLUMNS.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_drop_site_defaults`;
DELIMITER $$
CREATE PROCEDURE `sp_drop_site_defaults`()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE v_table VARCHAR(64);
  DECLARE cur CURSOR FOR
    SELECT TABLE_NAME
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND COLUMN_NAME = 'site'
      AND COLUMN_DEFAULT IS NOT NULL;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_table;
    IF done = 1 THEN
      LEAVE read_loop;
    END IF;
    -- No non-ASCII literal here: the ALTER carries only identifiers.
    SET @ddl = CONCAT('ALTER TABLE `', v_table, '` ALTER COLUMN `site` DROP DEFAULT');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;

CALL `sp_drop_site_defaults`();
DROP PROCEDURE IF EXISTS `sp_drop_site_defaults`;

-- -----------------------------------------------------------------------------
-- 2. Data fix: inbound_id = 3 belongs to Penang but was stored as 'SuZhou'.
--    0x50656E616E67 = 'Penang' (HEX, charset-safe).
--    NOTE: on a fresh/local dev DB this row may not exist; the UPDATE is then a
--    harmless no-op (0 rows affected).
-- -----------------------------------------------------------------------------
UPDATE `asset_inbound`
SET `site` = 0x50656E616E67
WHERE `inbound_id` = 3;

-- -----------------------------------------------------------------------------
-- 3. AFTER: verification. `remaining_site_defaults` MUST be 0.
--    The second result set lists every `site` column with a NULL default.
-- -----------------------------------------------------------------------------
SELECT 'AFTER' AS phase,
       COUNT(*) AS remaining_site_defaults
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND COLUMN_NAME = 'site'
  AND COLUMN_DEFAULT IS NOT NULL;

SELECT TABLE_NAME, COLUMN_DEFAULT, HEX(COLUMN_DEFAULT) AS hex_default
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND COLUMN_NAME = 'site'
ORDER BY TABLE_NAME;
