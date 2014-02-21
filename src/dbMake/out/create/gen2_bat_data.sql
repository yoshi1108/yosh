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
-- Name: dev_raw_data_52cfa015000000ce; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dev_raw_data_52cfa015000000ce (
    dev_raw_data_id bigint DEFAULT ((get_localip_num_func() << 32) + nextval('dev_raw_data_52cfa015000000ce_seq'::regclass)) NOT NULL,
    registered_time timestamp with time zone DEFAULT now() NOT NULL,
    accept_id text NOT NULL,
    sub_accept_id bigint,
    dev_instance_id bigint NOT NULL,
    serial_id text NOT NULL,
    dev_request_id text,
    communication_status integer,
    datetime timestamp with time zone,
    bat_pw_in numeric,
    bat_pw_out numeric,
    bat_temp numeric,
    bat_soc numeric,
    bat_learning_cap integer,
    bat_stts text,
    acc_interval integer,
    bat_chg_engy numeric,
    bat_dischg_engy numeric,
    bat_cap numeric,
    dev_fw_ver text,
    dev_fw_ver_2nd text,
    dev_fw_ver_3rd text,
    dev_fw_ver_4th text,
    begin_date timestamp with time zone,
    chg_st_time integer,
    dischg_st_time integer,
    enable_init_test integer,
    bat_ope_mode integer,
    pw_sply_priori_mode integer,
    peakcut_pw integer,
    const_cur numeric,
    const_vol integer,
    const_chg_pw_u integer,
    const_chg_pw_v integer,
    const_dischg_pw_u integer,
    const_dischg_pw_v integer,
    dischg_end_vol integer,
    low_bat_rate_normal integer,
    low_bat_rate_emergency integer,
    guard_lev_over_vol integer,
    guard_lev_low_vol integer,
    bat_correct_interval integer,
    prechg_vol integer,
    bat_ope_cmd integer,
    bat_chg_cmd integer,
    bat_chg_cap integer,
    bat_chg_rate integer,
    bat_chg_tim integer,
    bat_dischg_cmd integer,
    bat_dischg_cap integer,
    bat_dischg_rate integer,
    bat_dischg_tim integer
);


ALTER TABLE public.dev_raw_data_52cfa015000000ce OWNER TO postgres;

--
-- Name: dev_raw_data_52cfa015000000ce_accept_id_sub_accept_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dev_raw_data_52cfa015000000ce
    ADD CONSTRAINT dev_raw_data_52cfa015000000ce_accept_id_sub_accept_id_key UNIQUE (accept_id, sub_accept_id);


--
-- Name: dev_raw_data_52cfa015000000ce_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dev_raw_data_52cfa015000000ce
    ADD CONSTRAINT dev_raw_data_52cfa015000000ce_pkey PRIMARY KEY (dev_raw_data_id);


--
-- Name: dev_raw_data_52cfa015000000ce_index1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX dev_raw_data_52cfa015000000ce_index1 ON dev_raw_data_52cfa015000000ce USING btree (registered_time);


--
-- PostgreSQL database dump complete
--

