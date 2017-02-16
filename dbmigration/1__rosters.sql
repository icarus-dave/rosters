-- apply changes
create table duty (
  id                            bigint auto_increment not null,
  date                          date not null,
  state                         varchar(9) not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_duty_state check ( state in ('SCHEDULED','DRAFT','CONFIRMED','COMPLETED')),
  constraint pk_duty primary key (id)
);

create table stand_downs (
  duty_id                       bigint not null,
  operator_id                   bigint not null,
  constraint pk_stand_downs primary key (duty_id,operator_id)
);

create table replacements (
  duty_id                       bigint not null,
  operator_id                   bigint not null,
  constraint pk_replacements primary key (duty_id,operator_id)
);

create table operator (
  id                            bigint auto_increment not null,
  active                        boolean,
  first_name                    varchar(255) not null,
  last_name                     varchar(255) not null,
  email                         varchar(255) not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint uq_operator_email unique (email),
  constraint pk_operator primary key (id)
);

create table shift (
  id                            bigint auto_increment not null,
  duty_id                       bigint,
  start                         timestamp not null,
  finish                        timestamp not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_shift primary key (id)
);

create table team (
  id                            bigint auto_increment not null,
  name                          varchar(255) not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_team primary key (id)
);

create table team_members (
  operator_id                   bigint,
  team_id                       bigint,
  roster_weighting              bigint
);

alter table stand_downs add constraint fk_stand_downs_duty foreign key (duty_id) references duty (id) on delete restrict on update restrict;
create index ix_stand_downs_duty on stand_downs (duty_id);

alter table stand_downs add constraint fk_stand_downs_operator foreign key (operator_id) references operator (id) on delete restrict on update restrict;
create index ix_stand_downs_operator on stand_downs (operator_id);

alter table replacements add constraint fk_replacements_duty foreign key (duty_id) references duty (id) on delete restrict on update restrict;
create index ix_replacements_duty on replacements (duty_id);

alter table replacements add constraint fk_replacements_operator foreign key (operator_id) references operator (id) on delete restrict on update restrict;
create index ix_replacements_operator on replacements (operator_id);

alter table shift add constraint fk_shift_duty_id foreign key (duty_id) references duty (id) on delete restrict on update restrict;
create index ix_shift_duty_id on shift (duty_id);

alter table team_members add constraint fk_team_members_operator_id foreign key (operator_id) references operator (id) on delete restrict on update restrict;
create index ix_team_members_operator_id on team_members (operator_id);

alter table team_members add constraint fk_team_members_team_id foreign key (team_id) references team (id) on delete restrict on update restrict;
create index ix_team_members_team_id on team_members (team_id);

