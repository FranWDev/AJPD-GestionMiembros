MERGE INTO user_permissions (EMAIL, CAN_MANAGE_PERMISSIONS, CAN_MANAGE_ORGANIZATION, CAN_MANAGE_WEB, CAN_MANAGE_FINANCES) KEY(EMAIL) VALUES 
('presidencia@proyectodubini.org', true, true, true, true),
('fran.hernandez@proyectodubini.org', true, true, true, true),
('secretaria@proyectodubini.org', false, true, false, false),
('comunicacion@proyectodubini.org', false, false, true, false),
('tesoreria@proyectodubini.org', false, false, false, true);
