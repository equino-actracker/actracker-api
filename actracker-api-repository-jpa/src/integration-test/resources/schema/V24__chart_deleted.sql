ALTER TABLE IF EXISTS chart ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
