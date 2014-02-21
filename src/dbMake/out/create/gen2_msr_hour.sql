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
-- Name: wf_proc_data_52cfa02a000005cf; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE wf_proc_data_52cfa02a000005cf (
    wf_proc_data_id bigint DEFAULT ((get_localip_num_func() << 32) + nextval('wf_proc_data_52cfa02a000005cf_seq'::regclass)) NOT NULL,
    wf_definition_id bigint,
    registered_time timestamp with time zone DEFAULT now() NOT NULL,
    wf_proc_updated_time timestamp with time zone DEFAULT now() NOT NULL,
    wf_proc_status integer DEFAULT 0,
    deleted_time timestamp with time zone,
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    place_id text,
    ctrl_id text NOT NULL,
    msr_time timestamp with time zone NOT NULL,
    bat_chg_engy numeric,
    bat_dischg_engy numeric,
    pv_engy numeric,
    load_engy numeric,
    rect_engy_in numeric,
    rect_engy_out numeric,
    add_count integer
);


ALTER TABLE public.wf_proc_data_52cfa02a000005cf OWNER TO postgres;

--
-- Name: wf_proc_data_52cfa02a000005cf_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY wf_proc_data_52cfa02a000005cf
    ADD CONSTRAINT wf_proc_data_52cfa02a000005cf_pkey PRIMARY KEY (wf_proc_data_id);


--
-- Name: wf_proc_data_52cfa02a000005cf_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX wf_proc_data_52cfa02a000005cf_index ON wf_proc_data_52cfa02a000005cf USING btree (ctrl_id, msr_time);


--
-- Name: wf_proc_data_52cfa02a000005cf_index1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX wf_proc_data_52cfa02a000005cf_index1 ON wf_proc_data_52cfa02a000005cf USING btree (registered_time);


--
-- PostgreSQL database dump complete
--

