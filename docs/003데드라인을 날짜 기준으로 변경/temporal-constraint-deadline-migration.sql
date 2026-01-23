-- Task.deadline migration: ZonedDateTimeAttribute -> ZonedDateAttribute (date + offset)
-- Assumptions:
-- 1) Existing columns: deadline_time (DATETIME), deadline_zone_id (Region zone id).
-- 2) Offset is calculated at local midnight of deadline_date in the original region zone.
-- 3) MySQL timezone tables are loaded so CONVERT_TZ supports region zones.

-- 1. Add new columns (keep nullable during backfill).
ALTER TABLE task
    ADD COLUMN deadline_date DATE NULL AFTER deadline_zone_id,
    ADD COLUMN deadline_offset_id VARCHAR(10) NULL AFTER deadline_date;

-- 2. Backfill from legacy columns (local midnight offset).
UPDATE task
SET deadline_date      = DATE(deadline_time),
    deadline_offset_id = CONCAT(
            SUBSTR(DATE_FORMAT(CONVERT_TZ(CONCAT(DATE(deadline_time), ' 00:00:00'), deadline_zone_id, '+00:00'), '%z'),
                   1, 3),
            ':',
            SUBSTR(DATE_FORMAT(CONVERT_TZ(CONCAT(DATE(deadline_time), ' 00:00:00'), deadline_zone_id, '+00:00'), '%z'),
                   4, 2)
                         );

-- 3. Enforce NOT NULL after verifying backfill.
ALTER TABLE task
    MODIFY deadline_date DATE NOT NULL,
    MODIFY deadline_offset_id VARCHAR (10) NOT NULL;

-- 4. (Optional) Drop legacy columns after application rollout and data validation.
-- ALTER TABLE task DROP COLUMN deadline_time, DROP COLUMN deadline_zone_id;

-- Rollback plan:
-- - If needed, repopulate deadline_time from deadline_date using stored offset (time fixed at 00:00:00):
-- UPDATE task SET deadline_time = CONVERT_TZ(CONCAT(deadline_date, ' 00:00:00'), '+00:00', deadline_offset_id);
-- - Restore NOT NULL/column definitions accordingly.
