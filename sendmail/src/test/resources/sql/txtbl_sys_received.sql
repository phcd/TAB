DELETE FROM txtbl_sys_received ;
DELETE FROM txtbl_sys_sent;

INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, level, save_time) VALUES (1, 'webalgorithm.com',1, 'pop.emailsrvr.com', 110, 'newpop3',5, now() );
INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, level, save_time) VALUES (2, 'webalgorithm.com',1, 'imap.emailsrvr.com', 143, 'newimap',5, now() );
INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, receive_ts, level, save_time) VALUES (3, 'gmail.com',1, 'imap.gmail.com', 993, 'newimap','ssl',5, now() );
INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, level, save_time) VALUES (4, 'aol.com',1, 'imap.aol.com', 143, 'newimap', 5, now() );
INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, receive_ts,level, save_time) VALUES (5, 'hotmail.com',1, 'pop3.live.com', 995, 'newpop3','ssl',5, now() );
INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, receive_ts, level, save_time) VALUES (6, 'gmail.com',1, 'mail.aircel.co.in/Microsoft-Server-ActiveSync', 995, 'activeSync','ssl',5, now() );
INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, level, save_time) VALUES (7, 'yahoo.com',1, 'imap.mail.yahoo.com', 143, 'newimap',5, now() );
INSERT INTO txtbl_sys_received (id, name, status ,receive_host, receive_port, receive_protocol_type, level, save_time) VALUES (8, 'googlevoice.com',1, 'mail.googlevoice.com', 143, 'googlevoice',5, now() );

INSERT INTO txtbl_sys_sent (id, name, status ,send_host, send_port, send_protocol_type, send_ts,need_auth,level, save_time) VALUES (3, 'gmail.com',1, 'smtp.gmail.com', 25, 'localsmtp','ssl',1,5, now() );


