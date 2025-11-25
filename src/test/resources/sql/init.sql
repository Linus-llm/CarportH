BEGIN;

DROP TABLE IF EXISTS test.users CASCADE;
DROP TABLE IF EXISTS test.wood_profiles CASCADE;
DROP TABLE IF EXISTS test.woods CASCADE;
DROP TABLE IF EXISTS test.offers CASCADE;
DROP TABLE IF EXISTS test.postalcodes CASCADE;
DROP TABLE IF EXISTS test.bills CASCADE;

DROP SEQUENCE IF EXISTS test.users_id_seq CASCADE;
DROP SEQUENCE IF EXISTS test.wood_profiles_id_seq CASCADE;
DROP SEQUENCE IF EXISTS test.woods_id_seq CASCADE;
DROP SEQUENCE IF EXISTS test.offers_id_seq CASCADE;

CREATE TABLE test.users AS (SELECT * from public.users) WITH NO DATA;
CREATE TABLE test.wood_profiles AS (SELECT * from public.wood_profiles) WITH NO DATA;
CREATE TABLE test.woods AS (SELECT * from public.woods) WITH NO DATA;
CREATE TABLE test.offers AS (SELECT * from public.offers) WITH NO DATA;
CREATE TABLE test.postalcodes AS (SELECT * from public.postalcodes) WITH NO DATA;
CREATE TABLE test.bills AS (SELECT * from public.bills) WITH NO DATA;

CREATE SEQUENCE test.users_id_seq;
ALTER TABLE test.users ALTER COLUMN id SET DEFAULT nextval('test.users_id_seq');
CREATE SEQUENCE test.wood_profiles_id_seq;
ALTER TABLE test.wood_profiles ALTER COLUMN id SET DEFAULT nextval('test.wood_profiles_id_seq');
CREATE SEQUENCE test.woods_id_seq;
ALTER TABLE test.woods ALTER COLUMN id SET DEFAULT nextval('test.woods_id_seq');
CREATE SEQUENCE test.offers_id_seq;
ALTER TABLE test.offers ALTER COLUMN id SET DEFAULT nextval('test.offers_id_seq');


END;
