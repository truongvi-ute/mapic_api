-- Add reason_category column to reports table
ALTER TABLE reports 
ADD COLUMN reason_category VARCHAR(50);

-- Categorize existing reports based on reason text
UPDATE reports 
SET reason_category = CASE
    WHEN LOWER(reason) LIKE '%spam%' OR LOWER(reason) LIKE '%quảng cáo%' THEN 'SPAM'
    WHEN LOWER(reason) LIKE '%quấy rối%' OR LOWER(reason) LIKE '%bắt nạt%' THEN 'HARASSMENT'
    WHEN LOWER(reason) LIKE '%bạo lực%' OR LOWER(reason) LIKE '%violence%' THEN 'VIOLENCE'
    WHEN LOWER(reason) LIKE '%sai lệch%' OR LOWER(reason) LIKE '%giả mạo%' OR LOWER(reason) LIKE '%fake%' THEN 'FAKE_NEWS'
    WHEN LOWER(reason) LIKE '%thù ghét%' OR LOWER(reason) LIKE '%phân biệt%' THEN 'HATE_SPEECH'
    WHEN LOWER(reason) LIKE '%không phù hợp%' OR LOWER(reason) LIKE '%inappropriate%' OR LOWER(reason) LIKE '%nude%' OR LOWER(reason) LIKE '%khỏa thân%' THEN 'INAPPROPRIATE'
    ELSE 'OTHER'
END
WHERE reason_category IS NULL;

-- Add index for better query performance
CREATE INDEX idx_reports_reason_category ON reports(reason_category);

-- Add index for filtering by status and category
CREATE INDEX idx_reports_status_category ON reports(status, reason_category);
