ALTER TABLE activity ADD COLUMN creator_id VARCHAR(36);

UPDATE activity SET creator_id='0' WHERE creator_id IS NULL;

ALTER TABLE activity ALTER COLUMN creator_id SET NOT NULL;
