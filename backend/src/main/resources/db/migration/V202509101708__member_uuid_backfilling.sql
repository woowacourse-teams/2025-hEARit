UPDATE member
SET uuid = UUID()
WHERE uuid IS NULL;
