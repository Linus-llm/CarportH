BEGIN;

DROP TABLE IF EXISTS public.users CASCADE;
DROP TABLE IF EXISTS public.wood_profiles CASCADE;
DROP TABLE IF EXISTS public.woods CASCADE;
DROP TABLE IF EXISTS public.offers CASCADE;
DROP TABLE IF EXISTS public.postalcodes CASCADE;
DROP TABLE IF EXISTS public.bills CASCADE;

CREATE TABLE IF NOT EXISTS public.users
(
    id serial NOT NULL,
    name character varying NOT NULL,
    email character varying NOT NULL,
    password bytea NOT NULL,
    salt bytea NOT NULL,
    role integer NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.wood_profiles
(
    id serial NOT NULL,
    category integer NOT NULL,
    width integer NOT NULL,
    height integer NOT NULL,
    price double precision NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.woods
(
    id serial NOT NULL,
    profile_id integer NOT NULL,
    length integer NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.postalcodes
(
    postalcode integer NOT NULL,
    city character varying NOT NULL,
    PRIMARY KEY (postalcode)
);

CREATE TABLE IF NOT EXISTS public.offers
(
    id serial NOT NULL,
    customer_id integer NOT NULL,
    salesperson_id integer,
    address character varying NOT NULL,
    postalcode integer NOT NULL,
    width integer NOT NULL,
    height integer NOT NULL,
    length integer NOT NULL,
    shed_width integer NOT NULL,
    shed_length integer NOT NULL,
    price double precision,
    text character varying,
    status integer NOT NULL,
    PRIMARY KEY (id)
);



CREATE TABLE IF NOT EXISTS public.bills
(
    id serial NOT NULL,
    offer_id integer NOT NULL,
    wood_id integer NOT NULL,
    helptext character varying NOT NULL,
    count integer NOT NULL,
    price double precision NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.woods
    ADD CONSTRAINT profile_id FOREIGN KEY (profile_id)
    REFERENCES public.wood_profiles (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;


ALTER TABLE IF EXISTS public.offers
    ADD CONSTRAINT customer_id FOREIGN KEY (customer_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;


ALTER TABLE IF EXISTS public.offers
    ADD CONSTRAINT salesperson_id FOREIGN KEY (salesperson_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;


ALTER TABLE IF EXISTS public.offers
    ADD CONSTRAINT postalcode FOREIGN KEY (postalcode)
    REFERENCES public.postalcodes (postalcode) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;


ALTER TABLE IF EXISTS public.bills
    ADD CONSTRAINT offer_id FOREIGN KEY (offer_id)
    REFERENCES public.offers (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;


ALTER TABLE IF EXISTS public.bills
    ADD CONSTRAINT wood_id FOREIGN KEY (wood_id)
    REFERENCES public.woods (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    NOT VALID;

END;
