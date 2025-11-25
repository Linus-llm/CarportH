BEGIN;

DELETE FROM test.users;
DELETE FROM test.wood_profiles;
DELETE FROM test.woods;
DELETE FROM test.offers;
DELETE FROM test.postalcodes;
DELETE FROM test.bills;

SELECT setval('test.users_id_seq', 1);
SELECT setval('test.wood_profiles_id_seq', 1);
SELECT setval('test.woods_id_seq', 1);
SELECT setval('test.offers_id_seq', 1);

INSERT INTO test.users (name, email, password, salt, role) VALUES
('ole', 'ole@customer.dk', decode('DEADBEAF', 'hex'), decode('DEADBEAF', 'hex'), 0),
('bob', 'bob@salesperson.dk', decode('DEADBEAF', 'hex'), decode('DEADBEAF', 'hex'), 1);

INSERT INTO test.wood_profiles (category, width, height, price) VALUES
-- RAFTERS
(0, 47, 195, 52.95),
-- PILLARS
(1, 97, 97, 45.95),
-- BOARDS
(2, 19, 100, 10.95),
(2, 25, 200, 20.95);

INSERT INTO test.woods (profile_id, length) VALUES
-- RAFTERS
(1, 3000),
(1, 3600),
(1, 4200),
(1, 4800),
(1, 6000),
-- PILLARS
(2, 1800),
(2, 2100),
(2, 2400),
(2, 2700),
(2, 3000),
(2, 3600),
(2, 4200),
(2, 4800),
-- BOARDS
(3, 1800),
(3, 2100),
(3, 2400),
(3, 2700),
(3, 3000),
(3, 3600),
(3, 4200),
(4, 1800),
(4, 2100),
(4, 2400),
(4, 2700),
(4, 3000),
(4, 3600),
(4, 4200);

--INSERT INTO test.offers () VALUES
--();

INSERT INTO test.postalcodes (postalcode, city) VALUES
(4242, 'Bobville');

--INSERT INTO test.bills () VALUES
--();

END;
