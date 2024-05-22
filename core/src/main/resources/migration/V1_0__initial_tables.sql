CREATE SCHEMA IF NOT EXISTS "db_schema";

CREATE TABLE "db_schema".feature
(
    id   serial      NOT NULL,
    name varchar(20) NOT NULL,
    path varchar(30) NOT NULL,
    CONSTRAINT pk_feature PRIMARY KEY (id)
);

CREATE TABLE "db_schema"."role"
(
    id         serial      NOT NULL,
    name       varchar(20) NOT NULL,
    created_by integer     NOT NULL,
    CONSTRAINT unique_role UNIQUE (name),
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE INDEX idx_role ON "db_schema"."role" (id);

CREATE TABLE "db_schema".role_feature_mapping
(
    id         serial  NOT NULL,
    role_id    integer NOT NULL,
    feature_id integer NOT NULL,
    CONSTRAINT pk_role_feature_mapping PRIMARY KEY (id),
    CONSTRAINT unique_role_feature_mapping UNIQUE (role_id, feature_id),
    CONSTRAINT fk_role_feature_mapping_role FOREIGN KEY (role_id) REFERENCES "db_schema"."role" (id),
    CONSTRAINT fk_role_feature_mapping_feature FOREIGN KEY (feature_id) REFERENCES "db_schema".feature (id)
);

CREATE INDEX idx_role_feature_mapping ON "db_schema".role_feature_mapping (id);

CREATE TABLE "db_schema".area
(
    id           serial      NOT NULL,
    name         varchar(30) NOT NULL,
    zone_id      integer     NOT NULL,
    created_by   integer     NOT NULL,
    created_time timestamp   NOT NULL,
    polygon      text        NOT NULL,
    CONSTRAINT pk_area PRIMARY KEY (id)
);

CREATE INDEX idx_area ON "db_schema".area (id ASC);

CREATE TABLE "db_schema".area_device_mapping
(
    id        serial  NOT NULL,
    area_id   integer NOT NULL,
    device_id integer NOT NULL,
    CONSTRAINT pk_area_device_mapping PRIMARY KEY (id),
    CONSTRAINT unique_area_device_mapping UNIQUE (area_id, device_id)
);

CREATE INDEX idx_area_device_mapping ON "db_schema".area_device_mapping (id ASC);

CREATE INDEX idx_area_device_mapping_0 ON "db_schema".area_device_mapping (device_id ASC);

CREATE INDEX idx_area_device_mapping_1 ON "db_schema".area_device_mapping (area_id ASC);

CREATE TABLE "db_schema".area_user_mapping
(
    id      serial  NOT NULL,
    area_id integer NOT NULL,
    user_id integer NOT NULL,
    CONSTRAINT pk_area_user_mapping PRIMARY KEY (id),
    CONSTRAINT unique_area_user_mapping UNIQUE (area_id, user_id)
);

CREATE INDEX idx_area_user_mapping ON "db_schema".area_user_mapping (id ASC);

CREATE INDEX idx_area_user_mapping_0 ON "db_schema".area_user_mapping (area_id ASC, user_id ASC);

CREATE INDEX idx_area_user_mapping_1 ON "db_schema".area_user_mapping (user_id ASC, area_id ASC);

CREATE TABLE "db_schema".comments
(
    id           serial    NOT NULL,
    comment      text      NOT NULL,
    created_time timestamp NOT NULL,
    created_by   integer   NOT NULL,
    ticket_id    integer   NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

CREATE INDEX idx_comments ON "db_schema".comments (id ASC);

CREATE INDEX idx_comments_0 ON "db_schema".comments (ticket_id ASC);

CREATE TABLE "db_schema".comments_file
(
    id          serial       NOT NULL,
    file_path   varchar(200) NOT NULL,
    file_type   varchar(10),
    comments_id integer      NOT NULL,
    CONSTRAINT pk_comments_file PRIMARY KEY (id)
);

CREATE INDEX idx_comments_file ON "db_schema".comments_file (id ASC);

CREATE INDEX idx_comments_file_0 ON "db_schema".comments_file (comments_id ASC);

CREATE TABLE "db_schema".customer
(
    id         serial      NOT NULL,
    name       varchar(50) NOT NULL,
    state      varchar(30) NOT NULL,
    city       varchar(30) NOT NULL,
    created_by integer,
    CONSTRAINT pk_customer PRIMARY KEY (id),
    CONSTRAINT unique_customer_name UNIQUE (name)
);

CREATE INDEX idx_customer ON "db_schema".customer (id ASC);

CREATE TABLE "db_schema".customer_product_mapping
(
    id          serial  NOT NULL,
    customer_id integer NOT NULL,
    product_id  integer NOT NULL,
    CONSTRAINT pk_customer_product_mapping PRIMARY KEY (id),
    CONSTRAINT unique_customer_product_mapping UNIQUE (customer_id, product_id)
);

CREATE INDEX idx_customer_product_mapping ON "db_schema".customer_product_mapping (id ASC);

CREATE INDEX idx_customer_product_mapping_0 ON "db_schema".customer_product_mapping (customer_id ASC);

CREATE INDEX idx_customer_product_mapping_1 ON "db_schema".customer_product_mapping (product_id ASC);

CREATE TABLE "db_schema".customer_product_mapping_device
(
    id                          serial  NOT NULL,
    customer_product_mapping_id integer NOT NULL,
    device_id                   integer NOT NULL,
    CONSTRAINT pk_customer_product_mapping_device PRIMARY KEY (id),
    CONSTRAINT unq_customer_product_mapping_device UNIQUE (customer_product_mapping_id, device_id)
);

CREATE INDEX idx_customer_product_mapping_device ON "db_schema".customer_product_mapping_device (id ASC);

CREATE INDEX idx_customer_product_mapping_device_0 ON "db_schema".customer_product_mapping_device (device_id ASC);

CREATE TABLE "db_schema".device
(
    id          serial                     NOT NULL,
    name        varchar(30)                NOT NULL,
    lat         double precision DEFAULT 0 NOT NULL,
    lon         double precision DEFAULT 0 NOT NULL,
    description varchar(250),
    created_by  integer,
    CONSTRAINT pk_device PRIMARY KEY (id),
    CONSTRAINT unique_device UNIQUE (name)
);

CREATE INDEX idx_device ON "db_schema".device (id ASC);

CREATE TABLE "db_schema".product
(
    id           serial      NOT NULL,
    name         varchar(30) NOT NULL,
    description  varchar(250),
    product_type varchar(100),
    created_by   integer,
    CONSTRAINT unique_product_name UNIQUE (name),
    CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE INDEX idx_product ON "db_schema".product (id ASC);

CREATE TABLE "db_schema".ticket
(
    id                                 serial      NOT NULL,
    subject                            varchar(100),
    type                               varchar(10) NOT NULL,
    issue_related                      varchar(10) NOT NULL,
    priority                           varchar(10) NOT NULL,
    status                             varchar(10) NOT NULL,
    description                        text        NOT NULL,
    created_time                       timestamp   NOT NULL,
    created_by                         integer     NOT NULL,
    assigned_to                        integer     NOT NULL,
    customer_product_mapping_id        integer     NOT NULL,
    customer_product_mapping_device_id integer,
    CONSTRAINT pk_ticket PRIMARY KEY (id)
);

CREATE INDEX idx_ticket ON "db_schema".ticket (id ASC);

CREATE INDEX idx_ticket_0 ON "db_schema".ticket (assigned_to ASC);

CREATE INDEX idx_ticket_1 ON "db_schema".ticket (issue_related ASC);

CREATE INDEX idx_ticket_2 ON "db_schema".ticket (status ASC);

CREATE TABLE "db_schema".ticket_files
(
    id        serial       NOT NULL,
    subject   varchar(100) NOT NULL,
    file_path varchar(200) NOT NULL,
    file_type varchar(10),
    ticket_id integer      NOT NULL,
    CONSTRAINT pk_ticket_files PRIMARY KEY (id)
);

CREATE INDEX idx_ticket_files ON "db_schema".ticket_files (id ASC);

CREATE INDEX idx_ticket_files_0 ON "db_schema".ticket_files (ticket_id ASC);

CREATE TABLE "db_schema".ticket_updation_history
(
    id           serial      NOT NULL,
    updated_time timestamp   NOT NULL,
    updated_by   integer     NOT NULL,
    change_in    varchar(10) NOT NULL,
    change_from  varchar(15) NOT NULL,
    change_to    varchar(15) NOT NULL,
    ticket_id    integer     NOT NULL,
    CONSTRAINT pk_ticket_updation_history PRIMARY KEY (id)
);

CREATE INDEX idx_ticket_updation_history ON "db_schema".ticket_updation_history (id ASC);

CREATE INDEX idx_ticket_updation_history_0 ON "db_schema".ticket_updation_history (ticket_id ASC);

CREATE TABLE "db_schema".user_product_mapping
(
    id                          serial  NOT NULL,
    user_id                     integer NOT NULL,
    customer_product_mapping_id integer NOT NULL,
    CONSTRAINT pk_user_product_mapping PRIMARY KEY (id),
    CONSTRAINT unique_user_product_mapping UNIQUE (user_id, customer_product_mapping_id)
);

CREATE INDEX idx_user_product_mapping ON "db_schema".user_product_mapping (id ASC);

CREATE INDEX idx_user_product_mapping_0 ON "db_schema".user_product_mapping (user_id ASC);

CREATE TABLE "db_schema".user_role_mapping
(
    id      serial  NOT NULL,
    user_id integer NOT NULL,
    role_id integer NOT NULL,
    CONSTRAINT pk_user_role_mapping PRIMARY KEY (id),
    CONSTRAINT unique_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE "db_schema".users
(
    id           serial                NOT NULL,
    username     varchar(30)           NOT NULL,
    name         varchar(30)           NOT NULL,
    email        varchar(100)          NOT NULL,
    password     varchar(100)          NOT NULL,
    ldap_user    boolean DEFAULT false NOT NULL,
    created_time timestamp,
    login_time   timestamp,
    customer_id  integer,
    created_by   integer,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT unique_username UNIQUE (username),
    CONSTRAINT unique_user_email UNIQUE (email)
);

CREATE INDEX idx_users ON "db_schema".users (id ASC);

CREATE TABLE "db_schema"."zone"
(
    id           serial      NOT NULL,
    name         varchar(30) NOT NULL,
    created_by   integer     NOT NULL,
    created_time timestamp   NOT NULL,
    customer_id  integer     NOT NULL,
    polygon      text        NOT NULL,
    CONSTRAINT pk_zone1 PRIMARY KEY (id),
    CONSTRAINT unique_zone_customer_zone UNIQUE (customer_id, name)
);

CREATE INDEX idx_zone ON "db_schema"."zone" (id);

ALTER TABLE "db_schema".area
    ADD CONSTRAINT fk_area_zone FOREIGN KEY (zone_id) REFERENCES "db_schema"."zone" (id);

ALTER TABLE "db_schema".area
    ADD CONSTRAINT fk_area_users FOREIGN KEY (created_by) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".area_device_mapping
    ADD CONSTRAINT fk_area_device_mapping_area FOREIGN KEY (area_id) REFERENCES "db_schema".area (id);

ALTER TABLE "db_schema".area_device_mapping
    ADD CONSTRAINT fk_area_device_mapping_device FOREIGN KEY (device_id) REFERENCES "db_schema".device (id);

ALTER TABLE "db_schema".area_user_mapping
    ADD CONSTRAINT fk_area_user_mapping_area FOREIGN KEY (area_id) REFERENCES "db_schema".area (id);

ALTER TABLE "db_schema".area_user_mapping
    ADD CONSTRAINT fk_area_user_mapping_users FOREIGN KEY (user_id) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".comments
    ADD CONSTRAINT fk_comments_ticket FOREIGN KEY (ticket_id) REFERENCES "db_schema".ticket (id);

ALTER TABLE "db_schema".comments_file
    ADD CONSTRAINT fk_comments_file_comments FOREIGN KEY (comments_id) REFERENCES "db_schema".comments (id);

ALTER TABLE "db_schema".customer
    ADD CONSTRAINT fk_customer_users FOREIGN KEY (created_by) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".customer_product_mapping
    ADD CONSTRAINT fk_customer_product_mapping_customer FOREIGN KEY (customer_id) REFERENCES "db_schema".customer (id);

ALTER TABLE "db_schema".customer_product_mapping
    ADD CONSTRAINT fk_customer_product_mapping_product FOREIGN KEY (product_id) REFERENCES "db_schema".product (id);

ALTER TABLE "db_schema".customer_product_mapping_device
    ADD CONSTRAINT fk_customer_product_mapping_device_customer_product_mapping FOREIGN KEY (customer_product_mapping_id) REFERENCES "db_schema".customer_product_mapping (id);

ALTER TABLE "db_schema".customer_product_mapping_device
    ADD CONSTRAINT fk_customer_product_mapping_device_device FOREIGN KEY (device_id) REFERENCES "db_schema".device (id);

ALTER TABLE "db_schema".device
    ADD CONSTRAINT fk_device_users FOREIGN KEY (created_by) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".product
    ADD CONSTRAINT fk_product_users FOREIGN KEY (created_by) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".ticket
    ADD CONSTRAINT fk_ticket_users_created_by FOREIGN KEY (created_by) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".ticket
    ADD CONSTRAINT fk_ticket_users_assigned_to FOREIGN KEY (assigned_to) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".ticket
    ADD CONSTRAINT fk_ticket_customer_product_mapping_device FOREIGN KEY (customer_product_mapping_device_id) REFERENCES "db_schema".customer_product_mapping_device (id);

ALTER TABLE "db_schema".ticket
    ADD CONSTRAINT fk_ticket_customer_product_mapping FOREIGN KEY (customer_product_mapping_id) REFERENCES "db_schema".customer_product_mapping (id);

ALTER TABLE "db_schema".ticket_files
    ADD CONSTRAINT fk_ticket_files_ticket FOREIGN KEY (ticket_id) REFERENCES "db_schema".ticket (id);

ALTER TABLE "db_schema".ticket_updation_history
    ADD CONSTRAINT fk_ticket_updation_history_ticket FOREIGN KEY (ticket_id) REFERENCES "db_schema".ticket (id);

ALTER TABLE "db_schema".user_product_mapping
    ADD CONSTRAINT fk_user_product_mapping_users FOREIGN KEY (user_id) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".user_product_mapping
    ADD CONSTRAINT fk_user_product_mapping_customer_product_mapping FOREIGN KEY (customer_product_mapping_id) REFERENCES "db_schema".customer_product_mapping (id);

ALTER TABLE "db_schema".user_role_mapping
    ADD CONSTRAINT fk_user_role_mapping_user FOREIGN KEY (user_id) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema".user_role_mapping
    ADD CONSTRAINT fk_user_role_mapping_role FOREIGN KEY (role_id) REFERENCES "db_schema"."role" (id);

ALTER TABLE "db_schema".users
    ADD CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES "db_schema".customer (id);

ALTER TABLE "db_schema".users
    ADD CONSTRAINT fk_users_users FOREIGN KEY (created_by) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema"."zone"
    ADD CONSTRAINT fk_zone_users FOREIGN KEY (created_by) REFERENCES "db_schema".users (id);

ALTER TABLE "db_schema"."zone"
    ADD CONSTRAINT fk_zone_customer FOREIGN KEY (customer_id) REFERENCES "db_schema".customer (id);