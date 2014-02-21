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
-- Name: dev_raw_data_52cfa00f000000ca; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dev_raw_data_52cfa00f000000ca (
    dev_raw_data_id bigint DEFAULT ((get_localip_num_func() << 32) + nextval('dev_raw_data_52cfa00f000000ca_seq'::regclass)) NOT NULL,
    registered_time timestamp with time zone DEFAULT now() NOT NULL,
    accept_id text NOT NULL,
    sub_accept_id bigint,
    dev_instance_id bigint NOT NULL,
    serial_id text NOT NULL,
    dev_request_id text,
    communication_status integer,
    datetime timestamp with time zone,
    ctrl_stts text,
    ope_mod_com integer,
    om_po_t integer,
    cur_val_com integer,
    cv_po_t integer,
    acc_val1_com integer,
    begin_date timestamp with time zone,
    ip_addr text,
    ctrl_fw_ver text,
    load_collect_interval integer,
    pcs_collect_interval integer,
    lib_collect_interval integer,
    grid_collect_interval integer,
    pv_collect_interval integer,
    contracted_ampacity integer,
    sys_param1_ovr_vol integer,
    sys_param1_ovr_interval numeric,
    sys_param2_uvr_vol integer,
    sys_param2_uvr_interval numeric,
    sys_param3_ofr_frq_50hz numeric,
    sys_param3_ofr_frq_60hz numeric,
    sys_param4_ufr_frq_50hz numeric,
    sys_param4_ufr_frq_60hz numeric,
    sys_param5_ofr_interval numeric,
    sys_param5_ufr_interval numeric,
    sys_param6_ovr_ctrl_vol integer,
    sys_param6_phjmp_deg integer,
    sys_param7_delay_recov integer,
    enable_log_send integer,
    log_send_type integer,
    log_send_time integer,
    enable_sch_errorlog integer,
    enable_sch_measurelog integer,
    enable_sch_stalog integer,
    enable_sch_acclog integer,
    enable_sch_cyclog integer,
    log_level integer,
    frequency integer,
    wait_time_self_chk integer,
    back_light_off_time integer,
    retry_interval_reset integer,
    retry_count_reset integer
);


ALTER TABLE public.dev_raw_data_52cfa00f000000ca OWNER TO postgres;

--
-- Name: dev_raw_data_52cfa00f000000ca_accept_id_sub_accept_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dev_raw_data_52cfa00f000000ca
    ADD CONSTRAINT dev_raw_data_52cfa00f000000ca_accept_id_sub_accept_id_key UNIQUE (accept_id, sub_accept_id);


--
-- Name: dev_raw_data_52cfa00f000000ca_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dev_raw_data_52cfa00f000000ca
    ADD CONSTRAINT dev_raw_data_52cfa00f000000ca_pkey PRIMARY KEY (dev_raw_data_id);


--
-- Name: dev_raw_data_52cfa00f000000ca_index1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX dev_raw_data_52cfa00f000000ca_index1 ON dev_raw_data_52cfa00f000000ca USING btree (registered_time);


--
-- PostgreSQL database dump complete
--

