
DELETE FROM txtbl_sys_config ;


INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES 
  (1, 'recivemail-jndi', 'receivemail', 'send message to receive mail ejb for  taskfactory ejb', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (2, 'recisdomsg-jndi', 'backaccount', 'send message to taskfactory ejb for receive email ejb(call back)', '3', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (3, 'sending-jndi', 'sendmail', 'send message to sendmail ejb for web', '4', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (4, 'notify-jndi', 'iduaccount', 'send message of add/update/delete to taskfactory for web', '3', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (5, 'bodySize', '524288', '', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (6, 'attachSize', '10485760', '', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (7, 'attachType', 'jpg,jpeg,txt,bmp,gif,png', '', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (8, 'maxlength.databody', '2048', '', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9, 'maxnums.maillist', '30', 'for contact download. the number of contacts each frame.', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10, 'loginName', '', '', '4', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (11, 'loginPassword', '', '', '4', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (17, 'recisdomsg.provider.url', '192.168.10.42:1099', '', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (18, 'sending.provider.url', '192.168.10.42:1100', 'required,point to sendmail module', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (19, 'notify.provider.url', '192.168.10.42:1099', 'required,taskfactory address', '1', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (20, 'recivemail.provider.url', '192.168.10.42:1099', '', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (23, 'sendFailureTimes', '3', '', '4', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (24, 'senderBeginTotal', '100', '', '4', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (25, 'senderEndTotal', '1000', '', '4', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (26, 'senderLimit', '0.05', '', '4', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (31, 'subjectSize', '509', '', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (33, 'receiveLimit', '0.05', '', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (34, 'crmadvice.name', 'archermind', 'crm advice pattern inerface (url)', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (35, 'crmadvice.password', 'duyK8Wp', 'crm advice pattern passowrd', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (36, 'crmadvice.url', 'http://67.228.63.114/web.svc', 'crm advice pattern name', '1', '0', 'old : http://txtbl.webalgorithm.com/web.svc');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (37, 'maxnums.emailaccount', '3', 'max numbers of email accounts per peek account', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (38, 'notify.taskfactory.IDU.host', '192.168.10.42:1099', '', '1', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (39, 'notify.taskfactory.IDU.jndi', 'iduaccount', '', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (40, 'notify.taskfactory.mailserver.host', '192.168.10.42:1099', '', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (41, 'notify.taskfactory.mailserver.jndi', 'mailserver', '', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (47, 'imageHeight', '195', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (48, 'imageWidth', '320', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (52, 'resiger.withCRM', 'true', 'control if register with crm', '1', '0', 'true or false');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (60, 'hostPrefixSMTP', 'smtp;pop;mail;mailhost;outgoing;smtp-server;smtpauth;authsmtp;smtp.mail;smtp.email;smtp.isp;plus.smtp.mail;mx', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (62, 'hostPrefixPOP3', 'pop;pop3;pop.3;mail;pop.mail;pop.email;pop3.mail;pop3.email;incoming;pop-server;mail-server;pop.3.isp;plus.pop.mail;postoffice;postoffice.isp;pop.business', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (64, 'hostPrefixIMAP', 'imap;mail;imap.mail;imap.email;incoming;imap-server;mail-server;imap.isp;plus.imap.mail;postoffice;postoffice.isp', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (66, 'mailFrom', '', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (70, 'peek.address', 'http://10.150.9.5/txtbl/servlet/RequestProcesser', 'Address of Peek Server', '6', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (71, 'peek.timer_inquire', '300', 'How often the Peek checks for mail in polling method', '6', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (72, 'peek.sleep_continuely', '1800', 'How many seconds before the peek enters sleep mode', '6', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (73, 'peek.sleep_timer_inquire', '1200', 'How long between mail checks on a Peek in Sleep Mode', '6', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (74, 'peek.timer_flash', '500', 'How long to flash the LED on a new mail', '6', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (75, 'peek.timer_vibrate', '5000', 'How long to vibrate (in ms) on a new mail', '6', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (79, 'peek.del_days_trash', '30', NULL, '6', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (80, 'peek.del_days_boxes', '180', NULL, '6', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (81, 'peek.del_amount', '300', 'How many emails to delete on auto-delete', '6', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (82, 'maxlength.databody.total', '12288', 'response max body length to client ; 12K', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (88, 'reload.web.config.host', '192.168.10.42:1099', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (89, 'reload.web.config.jndi', 'reloadwebconfig', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (90, 'reload.taskfactory.config.host', '192.168.10.42:1099', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (91, 'reload.taskfactory.config.jndi', 'reloadtaskfactoryconfig', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (92, 'reload.sendmail.config.host', '192.168.10.42:1100', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (93, 'reload.sendmail.config.jndi', 'reloadsendconfig', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (94, 'reload.receivemail.config.host', '192.168.10.42:1100', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (95, 'reload.receivemail.config.jndi', 'reloadreceiveconfig', '', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (98, 'proxyFlag', '1', '', '5', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (99, 'yahooContatctsFlag', 'webscraping', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (183, 'web.simbypass.flag', 'true', 'if enanble Sim ByPass', '1', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (186, 'shortTimeOut', '240', '', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (196, 'largeMailSize', '209715200', '', '5', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (199, 'destination.protocol.group', 'pop3,gmailpop3;imap,yahooimap,yahoo;hotmail;exchange;imapidle;newimap;newgmailimap;newimapidle', '', '3', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (200, 'destination.target.group.default', '2;192.168.10.42:1100;8', 'required', '3', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (201, 'destination.target.group.imap', '4;192.168.10.42:1100;8', 'required', '3', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (202, 'destination.target.group.hotmail', '5;192.168.10.42:1100;8', 'required', '3', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (203, 'txtbl.ws.address', 'http://192.168.10.42:8080/txtbl/CommandService?wsdl', NULL, '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (210, 'notification.email.alias', 'Peek', 'notification email alias', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (211, 'notification.email.from', 'care@getpeek.com', 'from email name', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (212, 'notification.email.subject.start', 'Download Started', 'email subject', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (213, 'notification.email.subject.interrupt', 'Download Interrupted', 'email subject', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (214, 'notification.email.subject.complete', 'Download Completed', 'email subject', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (215, 'notification.email.body.start', 'Dear Peekster,\r\n\r\nYour contact list download has started successfully. We''ll keep you updated as it progresses. \r\n\r\nThanks and Happy Peeking,\r\nThe Peek Team\r\n\r\n877-677-PEEK\r\nwww.getpeek.com', 'email body  when start downloading contacts', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (216, 'notification.email.body.interrupt', 'Dear Peekster,\r\n\r\nSorry about this, but your contact list download was interrupted before we could finish.\r\n\r\nFeel free to give it another shot by selecting ''Download Contacts'' from the Contacts section of your Peek.\r\n\r\nThanks and Happy Peeking,\r\nThe Peek Team\r\n\r\n877-677-PEEK\r\nwww.getpeek.com', 'email body  when interrupt downloading contacts', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (217, 'notification.email.body.complete', 'Dear Peekster,\r\n\r\nYour contact list download was completed successfully! You can access all of the info by selecting ''Contacts'' from the Peek menu.\r\n\r\nThanks and Happy Peeking,\r\nThe Peek Team\r\n\r\n877-677-PEEK\r\nwww.getpeek.com', 'email body  when complete downloading contacts', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (218, 'notification.timer.interval', '120000', 'timeout  ', '1', '0', 'NULL');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (219, 'receiver.search.new.mail.sizessss', '10', NULL, '1', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (220, 'web.maillist.maildate.utc', 'true', NULL, '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (221, 'taskfactory.subscribe.protocol', 'msp:8', NULL, '3', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (222, 'notification.10102', 'Sorry, we couldn''t find this email or attachment on the system.', 'Returned when EmailID or attachment ID does not exist.', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (223, 'notification.10103', 'Can not find this Email and attachements. Please check Email id :', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (224, 'notification.10007', 'Sorry,have no IMEI(DeviceCode) and SIM.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (225, 'notification.10812', 'Sorry,Wrong request.Cannot find peek user account.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (226, 'notification.10813', 'CRM havn''t update sim/imei. Invalidate User.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (227, 'notification.10814', 'This Peek Account is overdue, Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (228, 'notification.10815', 'Peek account isnot same as one CRM returned.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (229, 'notification.10816', 'Local database cannot modify device sim/imei.please check DAL.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (230, 'notification.10817', 'Soory,cannot find device of this peek user.Mobile is not sycn.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (231, 'notification.10818', 'Invalidate user.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (232, 'notification.20012', 'Fail to reset device,Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (233, 'notification.20060', 'Sorry,but alias of  email account must have two portion,first name and last name.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (234, 'notification.20050', 'Your peek account has no the email account,or its status is inactivate.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (235, 'notification.50042', 'Peek accout CRM return is not same as ESServer,Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (236, 'notification.10517', 'Sorry, but that email account is already in use with Peek.Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (237, 'notification.10518', 'Sorry.It''s fail to register because of modifying device code.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (238, 'notification.20048', 'We''re sorry, but that email account is already in use with Peek.Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (239, 'notification.20049', 'We''re sorry, but that email account is already in use with Peek.Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (240, 'notification.10031', 'You already have 3 email accounts on your Peek.Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (241, 'notification.50015', 'Cannot save the detail server cfg  client submitted,please check DAL.log.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (242, 'notification.50021', 'That email & password combination isn''t valid. Please try again.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (243, 'notification.50031', 'Cannot be success to Delete  Email account:', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (244, 'notification.30000', 'Sorry,We''re having trouble adding email account.Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (245, 'notification.10519', 'Sorry, but that email account is already in use with Peek.Give us a call at 877-677-PEEK for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (246, 'notification.10504', 'Sorry, but we don''t currently support that email provider.Visit us at www.getpeek.com for help.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (247, 'notification.10205', 'Cannot download contact,no such account.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (248, 'notification.10206', 'Sorry,we have trouble restoring contacts from', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (249, 'notification.30001', 'No any contacts,please check.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (250, 'notification.40041', 'To send mail with your Peek, please make sure you have an active email account in Peek Manager.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (251, 'notification.40042', 'To send mail with your Peek, please make sure you have an active email account in Peek Manager.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (252, 'notification.50006', 'Fail to activity PeekUser. modifyStatus ERROR.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (253, 'notification.10001', 'No support this command.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (254, 'notification.10101', 'No new email.please wait.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (255, 'notification.10215', 'Sorry, no new emails. Please wait.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (256, 'notification.20051', 'Wrong ''From'' address.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (257, 'notification.50007', 'Fail to activity PeekUser. modifyStatus ERROR.', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (258, 'notification.50008', 'Fail to payment active this time,because response code:  CRM return message', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (278, 'notification.20055', 'Sorry,we have a trouble in network.Cannot be success to send messages to destination,please check.', 'JMS exception', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (300, '10digit.convert.email', '@text.getpeek.net', '10digit.convert.email', '1', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9973, 'sender.mail.failure.limit', '0.05', '', '4', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9974, 'sender.mail.failure.repeat.times', '3', '', '4', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9975, 'sender.mail.end.statistic.count', '10000', '', '4', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9976, 'sender.mail.begin.statistic.count', '100', '', '4', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9977, 'sender.mail.local.smtp.mail.from', '', '', '4', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9978, 'sender.mail.local.smtp.login.password', '', '', '4', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9979, 'sender.mail.local.smtp.login.name', '', '', '4', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9981, 'receiver.mail.image.filter.width', '8', ' ', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9982, 'receiver.mail.image.filter.height', '8', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9983, 'receiver.mail.parse.error.replace.body', 'Parsing Failure For This Message', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9984, 'receiver.mail.version', 'US', '', '', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9985, 'receiver.mail.filter.size', '0', '', '', '1', 'Not change');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9986, 'receiver.mail.repeat.login.switch', 'true', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9987, 'receiver.mail.thread.max.pool.size', '10', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9988, 'receiver.mail.thread.min.pool.size', '5', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9989, 'receiver.mail.thread.create.size', '5', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9990, 'receiver.mail.thread.keep.alive.time', '-1', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9991, 'receiver.large.mail.size', '2097152', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9992, 'receiver.mail.thread.timeout', '240', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9993, 'receiver.search.new.mail.size', '10', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9994, 'receiver.mail.image.width', '320', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9995, 'receiver.mail.image.height', '195', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9996, 'receiver.mail.subject.size', '509', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9997, 'receiver.mail.attach.size', '10485760', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9998, 'receiver.mail.attach.type', 'jpg,jpeg,bmp,png,gif,txt,doc,pdf,rtf', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9999, 'receiver.mail.body.size', '524288', '', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10000, 'validator.guess.smtp.prefix.match', 'smtp;pop;mail;mailhost;outgoing;smtp-server;smtpauth;authsmtp;smtp.mail;smtp.email;smtp.isp;plus.smtp.mail;mx', '', '0', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10001, 'validator.guess.pop3.prefix.match', 'pop;pop3;pop.3;mail;pop.mail;pop.email;pop3.mail;pop3.email;incoming;pop-server;mail-server;pop.3.isp;plus.pop.mail;postoffice;postoffice.isp;pop.business', '', '0', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10002, 'validator.guess.imap.prefix.match', 'imap;mail;imap.mail;imap.email;incoming;imap-server;mail-server;imap.isp;plus.imap.mail;postoffice;postoffice.isp', '', '0', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10003, 'validator.guess.mx.match', 'yahoo.com=yahoo,rockmail,ymail;gmail.com=google,gmail,getpeek;hotmail.com=hotmail', '', '0', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10004, 'validator.guess.failure.delivery.to', 'pengfei.zhu@archermind.com', '', '0', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10005, 'validator.guess.failure.delivery.cc', 'yongjun.xu@archermind.com', '', '0', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10006, 'validator.guess.failure.delivery.bcc', '', '', '0', '0', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10007, 'msp.warnbefore', 'Your  account will invalid in ', NULL, '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10008, 'msp.warnafter', 'You account  is invalid please login again', NULL, '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10009, 'first.waring.time.msp', '432000000', '', '5', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10010, 'first.waring.subject.msp', 'Update your password in Peek Manager', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10011, 'first.waring.content.msp', 'Hey Peekster,\r\n\r\nYour email account password will expire in 5 days. To ensure that your Peek service continues uninterrupted, just follow these steps:\r\n\r\n1.\tGo to your Peek?s ?Peek Manager? section and select ?Email Accounts? \r\n2.\tSelect your e-mail account and re-submit your existing password. Keep in mind that it?s the same password that you enter to access your account from a computer.\r\n\r\nAs an added security precaution, every 90 days we?re now be requiring you to update your Hotmail / MSN / Live email account password from your Peek. Thanks for your help in ensuring the security of your mobile email!\r\n\r\nHappy Peeking,\r\nThe Peek Team\r\n\r\n877-677-PEEK (7335)\r\nwww.getpeek.com', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10012, 'second.waring.time.msp', '86400000', '', '5', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10013, 'second.waring.subject.msp', 'Update your password in Peek Manager!', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10014, 'second.waring.content.msp', 'Hey Peekster,\r\n\r\nYour email account password will expire in just 1 day! To ensure that your Peek service continues uninterrupted, just follow these steps:\r\n\r\n1.\tGo to your Peek�s �Peek Manager� section and select �Email Accounts� \r\n2.\tSelect your e-mail account and re-submit your existing password. Keep in mind that it�s the same password that you enter to access your account from a computer.\r\n\r\nIf you have any questions, please give us a call right away at 877-677-PEEK and we�d be happy to help you out.\r\n\r\nHappy Peeking,\r\nThe Peek Team\r\n\r\n877-677-PEEK (7335)\r\nwww.getpeek.com\r\n', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10015, 'third.waring.time.msp', '3600000', '', '5', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10016, 'third.waring.subject.msp', 'URGENT - Update your password in Peek Manager!!!!!!!11', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10017, 'third.waring.content.msp', 'Hey Peekster,\r\n\r\nHeads up! Your email account password will expire in just 1 HOUR! To ensure that your Peek service continues uninterrupted, just follow these easy steps:\r\n\r\n1.\tGo to your Peek�s �Peek Manager� section and select �Email Accounts� \r\n2.\tSelect your e-mail account and re-submit your existing password. Keep in mind that it�s the same password that you enter to access your account from a computer.\r\n\r\nIf you have any questions, please give us a call right away at 877-677-PEEK and we�d be happy to help you out.\r\n\r\nThanks,\r\nThe Peek Team\r\n\r\n877-677-PEEK (7335)\r\nwww.getpeek.com\r\n', '', '5', '1', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10018, 'after.invalid.msp.subject', 'ffffffffffffffffff', NULL, '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10019, 'after.invalid.msp.content', 'Hey Peekster,\r\n\r\nHeads up! Your email account password will expire in just 1 HOUR! To ensure that your Peek service continues uninterrupted, just follow these easy steps:\r\n\r\n1.\tGo to your Peek?s ?Peek Manager? section and select ?Email Accounts? \r\n2.\tSelect your e-mail account and re-submit your existing password. Keep in mind that it?s the same password that you enter to access your account from a computer.\r\n\r\nIf you have any questions, please give us a call right away at 877-677-PEEK and we?d be happy to help you out.\r\n\r\nThanks,\r\nThe Peek Team\r\n\r\n877-677-PEEK (7335)\r\nwww.getpeek.com', NULL, '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10020, 'archermind.com', '10', 'Archermind check timer', '3', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10021, 'subscribe.url', 'http://66.114.70.60:8080/txtbl-tf-servlet/subscribe', NULL, '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10022, 'taskfactory.subscribe.timeout', '4', NULL, '3', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10023, 'MSP_REG_BODY', 'Hey Peekster,\r\n\r\nA quick word about your account. Like most mobile email services, we send and fetch email on your behalf by automatically logging into your account behind the scenes. Together with our friends at Microsoft, we''ve now worked out a way to do this while never storing your username or password! \r\n\r\nAs an added security precaution, every 90 days we?ll now be requiring you to update your Hotmail / MSN / Live email account password from your Peek. It couldn''t be easier ? all you need to do is go to ?Peek Manager,? select the specific account, re-type your existing password. Keep in mind that it?s the same password that you enter to access your account from a computer, and there are no changes in passwords required.\r\n\r\nThere''s no need to write yourself any reminders. We?ll send you reminder emails with these same instructions when the time is right to make sure that your Peek service continues uninterrupted. Thanks for your help in ensuring the security of your mobile email!\r\n\r\nHappy Peeking,\r\nThe Peek Team\r\n\r\n877-677-PEEK (7335)\r\nwww.getpeek.com', 'Sent after registering an MSP email account', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10024, 'MSP_REG_SUBJECT', 'Your email security', '', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10025, 'taskfactory.pop3CollectionClearTime', '1', NULL, '3', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10026, 'MSP_REG_From', 'Peek Customer Care', NULL, NULL, '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10027, 'after.invalid.msp.subect', 'URGENT - Update your password in Peek Manage', '', '5', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10028, 'pushmail.topic.jndi', 'topicpushmail', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10029, 'pushmail.topic.url', '192.168.10.42:1099', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10031, 'isPushMailOn', 'true', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10033, 'timesForRetry', '10', 'retry', '8', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10034, 'aws.s3.access.key', '0GJDTF53000365MMQKR2', 'AWS S3 Access Key', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10035, 'aws.s3.secret.key', 'hwy2k72KuyMxVuqH5AopYCm45Qe+hJSFU8OxEdSh', 'AWS S3 Secret Key', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10036, 'aws.s3.bucket.suffix', 'staging', 'Suffix used in naming the S3 buckets used. Options are- Prod, Staging, Integration, or Dev', '1', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10037, 'validator.guess.exchange.prefix.match', 'mex07a', 'Microsoft Exchange Server', '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10038, 'hostPrefixExchange', 'mex07a', NULL, '0', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10039, 'UUIDIpMapping.queue.url', '192.168.10.42:1099', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10040, 'UUIDIpMapping.queue.jndi', 'pushmailmapping', 'pushmailmapping', '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10041, 'push.device.time', '5', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10042, 'intervalForPushOff', '300', NULL, '8', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10043, 'push.heartbeat.count', '2', NULL, '8', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10044, 'intervalForTimer', '30000', 'in millis', '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10045, 'intervalForRetry', '60', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10046, 'intervalToDiscard', '3600000', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10047, 'UDPport', '7', NULL, '8', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10048, 'destination.target.group.imapidle', '5;192.168.10.42:1100;8', NULL, '3', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10049, 'imapidle.start.date', '2008-03-25 00:01:00', NULL, '3', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10050, 'imapidle.end.date', '2010-03-25 23:59:59', NULL, '3', '0', NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10051, 'idleMessagePause', '1000', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10052, 'idleCatchupDestinationUrl', '192.168.10.42:1100', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10053, 'idleConnectionTtl', '9', 'time to live of an imap idle connection', '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10054, 'idleConnectionRefreshInterval', '3', 'interval with which we are refreshing our idle connection', NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10055, 'mspMigrationCutoffDate', '2009-06-16 10:00:00', 'cutoff date for migration of pop accounts to msp to prevent duplicate deliveries', NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10058, 'attachmentsvc.queue.url', '192.168.10.42:1099', NULL, NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10059, 'attachmentsvc.queue.jndi', 'attachmentsvc', NULL, NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10060, 'messageid.store.url', 'http://127.0.0.1/uid', NULL, NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10061, 'reconsvc.queue.jndi', 'reconsvc', NULL, NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10062, 'reconsvc.queue.url', '192.168.10.42:1099', NULL, NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10063, 'eventsvc.queue.jndi', 'eventsvc', NULL, NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10064, 'eventsvc.queue.url', '192.168.10.42:1099', NULL, NULL, NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10065, 'destination.target.group.newgmailimap', '5;192.168.10.42:1100;8', '', '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10066, 'destination.target.group.newimapidle', '2;192.168.10.42:1100;8', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10067, 'newIdleCatchupDestinationUrl', '192.168.10.42:1100', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10068, 'newimapidle.start.date', '2008-03-25 00:01:00', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10069, 'newimapidle.end.date', '2010-03-25 00:01:00', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10070, 'maxMessageSize', '1024000', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10071, 'newimap.hoursToBackInSearch', '262800', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10073, 'imapMigrationDaysToGoBack', '3650', NULL, '3', NULL, NULL);
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (10074, 'destination.target.group.googlevoice', '5;localhost:1100;8', NULL, '3', '0', NULL);



