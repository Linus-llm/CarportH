BEGIN;

DELETE FROM public.bills;
DELETE FROM public.offers;
DELETE FROM public.woods;
DELETE FROM public.wood_profiles;
DELETE FROM public.users;
DELETE FROM public.postalcodes;

ALTER SEQUENCE users_id_seq RESTART;
UPDATE users SET id = DEFAULT;
ALTER SEQUENCE wood_profiles_id_seq RESTART;
UPDATE wood_profiles SET id = DEFAULT;
ALTER SEQUENCE woods_id_seq RESTART;
UPDATE woods SET id = DEFAULT;
ALTER SEQUENCE offers_id_seq RESTART;
UPDATE offers SET id = DEFAULT;

INSERT INTO public.users (name, email, password, salt, role) VALUES
('ole', 'ole@customer.dk', decode('DEADBEAF', 'hex'), decode('DEADBEAF', 'hex'), 0),
('bob', 'bob@salesperson.dk', decode('DEADBEAF', 'hex'), decode('DEADBEAF', 'hex'), 1);

INSERT INTO public.wood_profiles (category, width, height, price) VALUES
-- RAFTERS
(0, 47, 195, 52.95),
-- PILLARS
(1, 97, 97, 45.95),
-- BEAMS
(2, 19, 100, 10.95),
(2, 25, 200, 20.95);
-- PLANKS
(3,19,100,8.95);

INSERT INTO public.woods (profile_id, length) VALUES
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
-- BEAMS
(3, 1800),
(3, 2100),
(3, 2400),
(3, 2700),
(3, 3000),
(3, 3600),
(3, 4200),
(3, 4800),
(3, 5400),
(3, 6000),
(4, 1800),
(4, 2100),
(4, 2400),
(4, 2700),
(4, 3000),
(4, 3600),
(4, 4200),
(4, 4800),
(4, 5400),
(4, 6000);

--INSERT INTO public.offers () VALUES
--();

INSERT INTO public.postalcodes (postalcode, city) VALUES
(4242, 'Bobville');

--INSERT INTO public.bills () VALUES
--();


END;
