# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table task (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  done                      tinyint(1) default 0,
  due_date                  datetime,
  constraint pk_task primary key (id))
;

create table test_result (
  id                        bigint auto_increment not null,
  value                     bigint,
  test_name                 varchar(255),
  back_end                  varchar(255),
  add_date                  datetime,
  constraint pk_test_result primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table task;

drop table test_result;

SET FOREIGN_KEY_CHECKS=1;

