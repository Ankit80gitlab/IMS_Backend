-- user : ImsAdmin
-- password : ImsAdmin@002

INSERT INTO "db_schema"."users" (username, name, password, email, ldap_user, created_time, login_time, customer_id, created_by,type)
VALUES ('ImsAdmin', 'Admin', '$2a$10$84UiDGTyyHwkFz08OPOuUu4J8zMbca4O8DBxAQpkaebuV0ijyhU4a', 'admin@ims.com', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, NULL,NULL);

INSERT INTO "db_schema"."role" (name, created_by,customer_id)
VALUES ('ROLE_ADMIN', (SELECT id FROM "db_schema"."users" WHERE username = 'ImsAdmin'),NULL);

INSERT INTO "db_schema"."user_role_mapping" (user_id, role_id)
VALUES ((SELECT id FROM "db_schema"."users" WHERE username = 'ImsAdmin'), (SELECT id FROM "db_schema"."role" WHERE name = 'ROLE_ADMIN'));

---------------------------------------------------------------------------------------

INSERT INTO "db_schema".feature (name, "path") VALUES ('Dashboard', '/dashboard');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'Dashboard'));

INSERT INTO "db_schema".feature (name, path) VALUES ('User Management', '/userManagement');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'User Management'));

INSERT INTO "db_schema".feature (name, path) VALUES ('Role Management', '/roleManagement');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'Role Management'));

INSERT INTO "db_schema".feature (name, "path") VALUES ('Product Management', '/productManagement');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'Product Management'));

INSERT INTO "db_schema".feature (name, "path") VALUES ('Customer Management', '/customerManagement');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'Customer Management'));

INSERT INTO "db_schema".feature (name, "path") VALUES ('MapView', '/mapView');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'MapView'));

INSERT INTO "db_schema".feature (name, "path") VALUES ('Device Configuration', '/deviceConfiguration');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'Device Configuration'));

INSERT INTO "db_schema".feature (name, path) VALUES ('Ticket Management', '/ticketManagement');
INSERT INTO "db_schema".role_feature_mapping (role_id, feature_id)
VALUES ((SELECT id FROM "db_schema".role WHERE name = 'ROLE_ADMIN'), (SELECT id FROM "db_schema".feature WHERE name = 'Ticket Management'));

---------------------------------------------------------------------------------------
INSERT INTO "db_schema"."user_type" (name) VALUES ('Field Supervisor');
INSERT INTO "db_schema"."user_type" (name) VALUES ('Field Technicians');
INSERT INTO "db_schema"."user_type" (name) VALUES ('Safety Officers');
INSERT INTO "db_schema"."user_type" (name) VALUES ('Incident Coordinators');;