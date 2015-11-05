CREATE DATABASE IF NOT EXISTS configdb;

USE configdb;

DROP TABLE IF EXISTS roles; 
CREATE TABLE roles (
  ROLE_ID INTEGER NOT NULL AUTO_INCREMENT,
  ROLE_NAME varchar(64) NOT NULL,
  PRIMARY KEY (ROLE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
DROP TABLE IF EXISTS objects;
CREATE TABLE objects (
  OBJECT_ID INTEGER NOT NULL AUTO_INCREMENT,
  OBJECT_NAME varchar(64) DEFAULT NULL,
  OBJECT_TYPE varchar(64) DEFAULT NULL,
  PRIMARY KEY (OBJECT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS roleprivileges;
CREATE TABLE roleprivileges (
  TENANT_ID INTEGER,
  ROLE_ID INTEGER NOT NULL,
  OBJECT_ID INTEGER NOT NULL,
  PERMISSION_LEVEL varchar(32) NOT NULL,
  KEY ROLE_ID_OBJECT_ID_idx (ROLE_ID,OBJECT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS user_login;
CREATE TABLE user_login (
  id INTEGER NOT NULL AUTO_INCREMENT,
  tenantid INTEGER NOT NULL,
  active char(1) NOT NULL DEFAULT 'Y',
  loginid varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  profile TEXT NOT NULL,
  touchtime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_key (loginid)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

DROP VIEW IF EXISTS sauthorization;
CREATE VIEW sauthorization as  
 select ROLE_NAME as rolename, OBJECT_TYPE as objecttype, 
OBJECT_NAME as objectname, PERMISSION_LEVEL as permission  
from roles r, objects o, roleprivileges rp  
where rp.ROLE_ID = r.ROLE_ID AND rp.OBJECT_ID = o.OBJECT_ID; 

DROP TABLE IF EXISTS app_config;
CREATE TABLE app_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  configtype char(1) NOT NULL,
  title varchar(64) NOT NULL,
  body text NOT NULL,
  variables text ,
  outvar varchar(64),
  status char(1) NOT NULL DEFAULT 'Y',
  system char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (id),
  UNIQUE KEY (title),
  KEY BYTYPE (configtype)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Application configuration';


DROP TABLE IF EXISTS idsequence;
CREATE TABLE idsequence (
  sequencename varchar(64) NOT NULL,
  sequenceno INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (sequencename)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Sequence Generator';

DROP TABLE IF EXISTS sqlfunctions;
CREATE TABLE sqlfunctions (
  status char(1) NOT NULL DEFAULT 'Y',
  funcId varchar(64) NOT NULL,
  funcBody text NOT NULL,
  PRIMARY KEY (funcId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Function Defination';

DROP TABLE IF EXISTS stored_proc_config;
CREATE TABLE `stored_proc_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sp_title` varchar(128) NOT NULL,
  `sp_body` text,
  `sp_poolname` varchar(64) NOT NULL,
  `sp_call_syntax` text NOT NULL,
  `sp_out_var` text,
  `sp_err_var` varchar(64) DEFAULT NULL,
  `status` char(1) NOT NULL DEFAULT 'Y',
  PRIMARY KEY (`id`),
  UNIQUE KEY `sp_title` (`sp_title`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COMMENT='Stored Procedure configuration';

DROP TABLE IF EXISTS db_config;
CREATE TABLE db_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  machineIp varchar(64) NOT NULL,
  dbPort varchar(8) NOT NULL,
  poolName varchar(64) NOT NULL,
  driverClass varchar(64) NOT NULL,
  connectionUrl varchar(64) NOT NULL,
  dbUser varchar(64) NOT NULL,
  dbUserPassword varchar(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (poolName,machineIp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Database pool configuration';