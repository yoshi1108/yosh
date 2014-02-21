--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: dev_raw_data_52cfa024000000d6; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dev_raw_data_52cfa024000000d6 (
    dev_raw_data_id bigint DEFAULT ((get_localip_num_func() << 32) + nextval('dev_raw_data_52cfa024000000d6_seq'::regclass)) NOT NULL,
    registered_time timestamp with time zone DEFAULT now() NOT NULL,
    accept_id text NOT NULL,
    sub_accept_id bigint,
    dev_instance_id bigint NOT NULL,
    serial_id text NOT NULL,
    dev_request_id text,
    communication_status integer,
    datetime timestamp with time zone,
    rect_wat_in numeric,
    rect_wat_out numeric,
    rect_stts text,
    acc_interval integer,
    rect_engy_in numeric,
    rect_engy_out numeric
);


ALTER TABLE public.dev_raw_data_52cfa024000000d6 OWNER TO postgres;

--
-- Name: dev_raw_data_52cfa024000000d6_accept_id_sub_accept_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dev_raw_data_52cfa024000000d6
    ADD CONSTRAINT dev_raw_data_52cfa024000000d6_accept_id_sub_accept_id_key UNIQUE (accept_id, sub_accept_id);


--
-- Name: dev_raw_data_52cfa024000000d6_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dev_raw_data_52cfa024000000d6
    ADD CONSTRAINT dev_raw_data_52cfa024000000d6_pkey PRIMARY KEY (dev_raw_data_id);


--
-- Name: dev_raw_data_52cfa024000000d6_index1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX dev_raw_data_52cfa024000000d6_index1 ON dev_raw_data_52cfa024000000d6 USING btree (registered_time);


--
-- PostgreSQL database dump complete
--

