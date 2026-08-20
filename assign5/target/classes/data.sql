INSERT INTO subjects (name, code, max_mse, max_ese)
SELECT * FROM (SELECT 'Data Structures' AS name, 'CS201' AS code, 30.0 AS max_mse, 70.0 AS max_ese) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'CS201') LIMIT 1;

INSERT INTO subjects (name, code, max_mse, max_ese)
SELECT * FROM (SELECT 'Database Management Systems' AS name, 'CS202' AS code, 30.0 AS max_mse, 70.0 AS max_ese) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'CS202') LIMIT 1;

INSERT INTO subjects (name, code, max_mse, max_ese)
SELECT * FROM (SELECT 'Operating Systems' AS name, 'CS203' AS code, 30.0 AS max_mse, 70.0 AS max_ese) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'CS203') LIMIT 1;

INSERT INTO subjects (name, code, max_mse, max_ese)
SELECT * FROM (SELECT 'Computer Networks' AS name, 'CS204' AS code, 30.0 AS max_mse, 70.0 AS max_ese) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'CS204') LIMIT 1;
