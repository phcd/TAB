CREATE TABLE txtbl_sys_config(id int, name varchar(64)  NOT NULL, value  varchar(2048)  NOT NULL, description  varchar(255) ,  configtype   varchar(2) ,  need_notify  char, comment     varchar(255)  );

INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9981, 'receiver.mail.image.filter.width', '8', ' ', '5', '1', '');
INSERT INTO txtbl_sys_config (id, name, value, description, configtype, need_notify, comment) VALUES
  (9982, 'receiver.mail.image.filter.height', '8', '', '5', '1', '');
