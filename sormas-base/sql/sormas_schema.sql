-- If a DB update was performed, insert a new line with a comment to the table SCHEMA_VERSION.
-- Example: INSERT INTO schema_version (version_number, comment) VALUES (1, 'Init database');

-- #1
CREATE TABLE schema_version
(
  version_number integer NOT NULL,
  changedate timestamp without time zone NOT NULL DEFAULT now(),
  comment character varying(255),
  CONSTRAINT schema_version_pkey PRIMARY KEY (version_number )
)
WITH (
  OIDS=FALSE
);


SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 483 (class 2612 OID 11574)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 142 (class 1259 OID 341910)
-- Name: cases; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE cases (
    id bigint NOT NULL,
    casestatus character varying(255),
    changedate timestamp without time zone NOT NULL,
    confirmeddate timestamp without time zone,
    creationdate timestamp without time zone NOT NULL,
    description character varying(512),
    disease character varying(255),
    investigateddate timestamp without time zone,
    negativedate timestamp without time zone,
    nocasedate timestamp without time zone,
    postivedate timestamp without time zone,
    recovereddate timestamp without time zone,
    reportdate timestamp without time zone,
    suspectdate timestamp without time zone,
    uuid character varying(36) NOT NULL,
    caseofficer_id bigint,
    casesupervisor_id bigint,
    contactofficer_id bigint,
    contactsupervisor_id bigint,
    healthfacility_id bigint,
    illlocation_id bigint,
    reportinguser_id bigint,
    surveillanceofficer_id bigint,
    surveillancesupervisor_id bigint,
    person_id bigint NOT NULL
);


ALTER TABLE cases OWNER TO sormas_user;

--
-- TOC entry 148 (class 1259 OID 341964)
-- Name: community; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE community (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    uuid character varying(36) NOT NULL,
    district_id bigint NOT NULL
);


ALTER TABLE community OWNER TO sormas_user;

--
-- TOC entry 147 (class 1259 OID 341957)
-- Name: district; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE district (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    uuid character varying(36) NOT NULL,
    region_id bigint NOT NULL
);


ALTER TABLE district OWNER TO sormas_user;

--
-- TOC entry 151 (class 1259 OID 342106)
-- Name: entity_seq; Type: SEQUENCE; Schema: public; Owner: sormas_user
--

CREATE SEQUENCE entity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entity_seq OWNER TO sormas_user;

--
-- TOC entry 149 (class 1259 OID 341971)
-- Name: facility; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE facility (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    publicownership boolean,
    type character varying(255),
    uuid character varying(36) NOT NULL,
    location_id bigint
);


ALTER TABLE facility OWNER TO sormas_user;

--
-- TOC entry 144 (class 1259 OID 341930)
-- Name: location; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE location (
    id bigint NOT NULL,
    address character varying(255),
    changedate timestamp without time zone NOT NULL,
    city character varying(255),
    creationdate timestamp without time zone NOT NULL,
    details character varying(255),
    latitude double precision,
    longitude double precision,
    uuid character varying(36) NOT NULL,
    community_id bigint,
    district_id bigint,
    region_id bigint
);


ALTER TABLE location OWNER TO sormas_user;

--
-- TOC entry 143 (class 1259 OID 341920)
-- Name: person; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE person (
    id bigint NOT NULL,
    approximateage integer,
    approximateagetype integer,
    birthdate date,
    burialconductor character varying(255),
    burialdate date,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    dead boolean,
    deathdate date,
    firstname character varying(255) NOT NULL,
    lastname character varying(255) NOT NULL,
    occupationdetails character varying(255),
    occupationtype character varying(255),
    phone character varying(255),
    presentcondition integer,
    sex character varying(255),
    uuid character varying(36) NOT NULL,
    address_id bigint,
    buriallocation_id bigint,
    deathlocation_id bigint,
    occupationfacility_id bigint
);


ALTER TABLE person OWNER TO sormas_user;

--
-- TOC entry 146 (class 1259 OID 341950)
-- Name: region; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE region (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    uuid character varying(36) NOT NULL
);


ALTER TABLE region OWNER TO sormas_user;

--
-- TOC entry 150 (class 1259 OID 341981)
-- Name: userroles; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE userroles (
    user_id bigint NOT NULL,
    userrole character varying(255) NOT NULL
);


ALTER TABLE userroles OWNER TO sormas_user;

--
-- TOC entry 145 (class 1259 OID 341940)
-- Name: users; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace: 
--

CREATE TABLE users (
    id bigint NOT NULL,
    aktiv boolean NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    firstname character varying(255) NOT NULL,
    lastname character varying(255) NOT NULL,
    password character varying(64) NOT NULL,
    phone character varying(255),
    seed character varying(16) NOT NULL,
    useremail character varying(255),
    username character varying(255) NOT NULL,
    uuid character varying(36) NOT NULL,
    address_id bigint,
    associatedofficer_id bigint,
    region_id bigint
);


ALTER TABLE users OWNER TO sormas_user;

--
-- TOC entry 1732 (class 2606 OID 341917)
-- Name: cases_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT cases_pkey PRIMARY KEY (id);


--
-- TOC entry 1734 (class 2606 OID 341919)
-- Name: cases_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT cases_uuid_key UNIQUE (uuid);


--
-- TOC entry 1756 (class 2606 OID 341968)
-- Name: community_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY community
    ADD CONSTRAINT community_pkey PRIMARY KEY (id);


--
-- TOC entry 1758 (class 2606 OID 341970)
-- Name: community_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY community
    ADD CONSTRAINT community_uuid_key UNIQUE (uuid);


--
-- TOC entry 1752 (class 2606 OID 341961)
-- Name: district_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY district
    ADD CONSTRAINT district_pkey PRIMARY KEY (id);


--
-- TOC entry 1754 (class 2606 OID 341963)
-- Name: district_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY district
    ADD CONSTRAINT district_uuid_key UNIQUE (uuid);


--
-- TOC entry 1760 (class 2606 OID 341978)
-- Name: facility_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY facility
    ADD CONSTRAINT facility_pkey PRIMARY KEY (id);


--
-- TOC entry 1762 (class 2606 OID 341980)
-- Name: facility_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY facility
    ADD CONSTRAINT facility_uuid_key UNIQUE (uuid);


--
-- TOC entry 1740 (class 2606 OID 341937)
-- Name: location_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY location
    ADD CONSTRAINT location_pkey PRIMARY KEY (id);


--
-- TOC entry 1742 (class 2606 OID 341939)
-- Name: location_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY location
    ADD CONSTRAINT location_uuid_key UNIQUE (uuid);


--
-- TOC entry 1736 (class 2606 OID 341927)
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- TOC entry 1738 (class 2606 OID 341929)
-- Name: person_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_uuid_key UNIQUE (uuid);


--
-- TOC entry 1748 (class 2606 OID 341954)
-- Name: region_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_pkey PRIMARY KEY (id);


--
-- TOC entry 1750 (class 2606 OID 341956)
-- Name: region_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_uuid_key UNIQUE (uuid);


--
-- TOC entry 1764 (class 2606 OID 341985)
-- Name: unq_userroles_0; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY userroles
    ADD CONSTRAINT unq_userroles_0 UNIQUE (user_id, userrole);


--
-- TOC entry 1744 (class 2606 OID 341947)
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 1746 (class 2606 OID 341949)
-- Name: users_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_uuid_key UNIQUE (uuid);


--
-- TOC entry 1765 (class 2606 OID 341986)
-- Name: fk_cases_caseofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_caseofficer_id FOREIGN KEY (caseofficer_id) REFERENCES users(id);


--
-- TOC entry 1769 (class 2606 OID 342006)
-- Name: fk_cases_casesupervisor_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_casesupervisor_id FOREIGN KEY (casesupervisor_id) REFERENCES users(id);


--
-- TOC entry 1770 (class 2606 OID 342011)
-- Name: fk_cases_contactofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_contactofficer_id FOREIGN KEY (contactofficer_id) REFERENCES users(id);


--
-- TOC entry 1768 (class 2606 OID 342001)
-- Name: fk_cases_contactsupervisor_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_contactsupervisor_id FOREIGN KEY (contactsupervisor_id) REFERENCES users(id);


--
-- TOC entry 1772 (class 2606 OID 342021)
-- Name: fk_cases_healthfacility_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_healthfacility_id FOREIGN KEY (healthfacility_id) REFERENCES facility(id);


--
-- TOC entry 1773 (class 2606 OID 342026)
-- Name: fk_cases_illlocation_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_illlocation_id FOREIGN KEY (illlocation_id) REFERENCES location(id);


--
-- TOC entry 1767 (class 2606 OID 341996)
-- Name: fk_cases_person_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_person_id FOREIGN KEY (person_id) REFERENCES person(id);


--
-- TOC entry 1766 (class 2606 OID 341991)
-- Name: fk_cases_reportinguser_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);


--
-- TOC entry 1774 (class 2606 OID 342031)
-- Name: fk_cases_surveillanceofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_surveillanceofficer_id FOREIGN KEY (surveillanceofficer_id) REFERENCES users(id);


--
-- TOC entry 1771 (class 2606 OID 342016)
-- Name: fk_cases_surveillancesupervisor_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_surveillancesupervisor_id FOREIGN KEY (surveillancesupervisor_id) REFERENCES users(id);


--
-- TOC entry 1786 (class 2606 OID 342091)
-- Name: fk_community_district_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY community
    ADD CONSTRAINT fk_community_district_id FOREIGN KEY (district_id) REFERENCES district(id);


--
-- TOC entry 1785 (class 2606 OID 342086)
-- Name: fk_district_region_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY district
    ADD CONSTRAINT fk_district_region_id FOREIGN KEY (region_id) REFERENCES region(id);


--
-- TOC entry 1787 (class 2606 OID 342096)
-- Name: fk_facility_location_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY facility
    ADD CONSTRAINT fk_facility_location_id FOREIGN KEY (location_id) REFERENCES location(id);


--
-- TOC entry 1779 (class 2606 OID 342056)
-- Name: fk_location_community_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY location
    ADD CONSTRAINT fk_location_community_id FOREIGN KEY (community_id) REFERENCES community(id);


--
-- TOC entry 1781 (class 2606 OID 342066)
-- Name: fk_location_district_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY location
    ADD CONSTRAINT fk_location_district_id FOREIGN KEY (district_id) REFERENCES district(id);


--
-- TOC entry 1780 (class 2606 OID 342061)
-- Name: fk_location_region_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY location
    ADD CONSTRAINT fk_location_region_id FOREIGN KEY (region_id) REFERENCES region(id);


--
-- TOC entry 1778 (class 2606 OID 342051)
-- Name: fk_person_address_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_address_id FOREIGN KEY (address_id) REFERENCES location(id);


--
-- TOC entry 1776 (class 2606 OID 342041)
-- Name: fk_person_buriallocation_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_buriallocation_id FOREIGN KEY (buriallocation_id) REFERENCES location(id);


--
-- TOC entry 1775 (class 2606 OID 342036)
-- Name: fk_person_deathlocation_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_deathlocation_id FOREIGN KEY (deathlocation_id) REFERENCES location(id);


--
-- TOC entry 1777 (class 2606 OID 342046)
-- Name: fk_person_occupationfacility_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_occupationfacility_id FOREIGN KEY (occupationfacility_id) REFERENCES facility(id);


--
-- TOC entry 1788 (class 2606 OID 342101)
-- Name: fk_userroles_user_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY userroles
    ADD CONSTRAINT fk_userroles_user_id FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 1783 (class 2606 OID 342076)
-- Name: fk_users_address_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_users_address_id FOREIGN KEY (address_id) REFERENCES location(id);


--
-- TOC entry 1784 (class 2606 OID 342081)
-- Name: fk_users_associatedofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_users_associatedofficer_id FOREIGN KEY (associatedofficer_id) REFERENCES users(id);


--
-- TOC entry 1782 (class 2606 OID 342071)
-- Name: fk_users_region_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_users_region_id FOREIGN KEY (region_id) REFERENCES region(id);


--
-- TOC entry 1881 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;

ALTER TABLE person ADD COLUMN phoneowner character varying(255);
  
INSERT INTO schema_version (version_number, comment) VALUES (1, 'Init database');
