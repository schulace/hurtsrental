execute DROP_ALL_2;
create table customer(
    cust_id numeric(12,0) generated always as identity,
    first_name varchar(20),
    last_name varchar(20),
    street_addr varchar(40),
    appt_number numeric(7),
    city varchar(25),
    state varchar(2) check (state in ('AK', 'AL', 'AR', 'AZ', 'CA', 'CO', 'CT', 'DC', 'DE', 'FL', 'GA', 'HI', 'IA', 'ID', 'IL', 'IN', 'KS', 'KY', 'LA', 'MA', 'MD', 'ME', 'MI', 'MN', 'MO', 'MS', 'MT', 'NC', 'ND', 'NE', 'NH', 'NJ', 'NM', 'NV', 'NY', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VA', 'VT', 'WA', 'WI', 'WV', 'WY')),
    zip varchar(5),
    license_number varchar(9),
    license_st varchar(2) check (license_st in ('AK', 'AL', 'AR', 'AZ', 'CA', 'CO', 'CT', 'DC', 'DE', 'FL', 'GA', 'HI', 'IA', 'ID', 'IL', 'IN', 'KS', 'KY', 'LA', 'MA', 'MD', 'ME', 'MI', 'MN', 'MO', 'MS', 'MT', 'NC', 'ND', 'NE', 'NH', 'NJ', 'NM', 'NV', 'NY', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VA', 'VT', 'WA', 'WI', 'WV', 'WY')),
    primary key(cust_id)
);

create table vehi_type(
    type_id numeric(12,0) generated always as identity,
    make varchar(20),
    model varchar(20),
    type varchar(20) check (type in ('SUV', 'minicar', 'subcompact', 'compact', 'fullsize', 'sports car', 'minivan', 'truck', 'other')),
    rate numeric(6,2),
    year numeric(4,0),
    primary key(type_id)
);


create table car(
    car_id numeric(12) generated always as identity,
    state varchar(2) check (state in ('AK', 'AL', 'AR', 'AZ', 'CA', 'CO', 'CT', 'DC', 'DE', 'FL', 'GA', 'HI', 'IA', 'ID', 'IL', 'IN', 'KS', 'KY', 'LA', 'MA', 'MD', 'ME', 'MI', 'MN', 'MO', 'MS', 'MT', 'NC', 'ND', 'NE', 'NH', 'NJ', 'NM', 'NV', 'NY', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VA', 'VT', 'WA', 'WI', 'WV', 'WY')),
    plate_number varchar(7),
    type_id numeric(12,0),
    foreign key (type_id) references vehi_type,
    primary key(car_id)
    
);

create table location(
    loc_id numeric(12,0) generated always as identity,
    street_addr varchar(40),
    city varchar(25),
    state varchar(2) check (state in ('AK', 'AL', 'AR', 'AZ', 'CA', 'CO', 'CT', 'DC', 'DE', 'FL', 'GA', 'HI', 'IA', 'ID', 'IL', 'IN', 'KS', 'KY', 'LA', 'MA', 'MD', 'ME', 'MI', 'MN', 'MO', 'MS', 'MT', 'NC', 'ND', 'NE', 'NH', 'NJ', 'NM', 'NV', 'NY', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VA', 'VT', 'WA', 'WI', 'WV', 'WY')),
    zip numeric(5,0),
    primary key(loc_id)
);

 create table my_time(
    c_d date
);

create table rental(
    id numeric(12,0) generated always as identity,
    start_date date,
    end_date date, check (end_date > start_date),
    cust_id numeric(12,0),
    car_id numeric(12,0),
    primary key(id),
    insurance_charge numeric(4,0),
    pickup_loc numeric(12,0),
    pickup_occ numeric(1,0),
    dropoff_loc numeric(12,0),
    dropoff_occ numeric(1,0),
    start_fuel numeric(4,3),
    end_fuel numeric(4,3),
    foreign key (car_id) references car on delete set null,
    foreign key (cust_id) references customer,
    foreign key (pickup_loc) references location,
    foreign key (dropoff_loc) references location
);
\
create table misc_charge(
    id numeric(12,0),
    charge_name varchar(20),
    cost numeric(12,0),
    onetime numeric(1,0), --boolean for whether to apply the fee once or on a daily basis
    primary key (id, charge_name),
    percentage numeric(3,3), --whether or not to tack on a percentage charge
    foreign key (id) references rental on delete cascade
);

create table sample_misc_charges(
    name varchar(20),
    cost numeric(5,2),
    onetime numeric(1,0), --boolean for whether to apply the fee once or on a daily basis
    percentage numeric(3,3), --whether or not to tack on a percentage charge
    primary key (name)
);

insert into sample_misc_charges VALUES ('car seat', 20, 1, 0);
insert into sample_misc_charges VALUES ('under 25', 25, 0, 0);
insert into sample_misc_charges VALUES ('collision coverage', 20, 0,0);
insert into sample_misc_charges VALUES ('licensing fee', 5, 0,0);
insert into sample_misc_charges VALUES ('roof rack', 15, 1,0);
insert into sample_misc_charges VALUES ('peak season', 10, 0,0);
insert into sample_misc_charges VALUES ('gps', 5, 1,0);
insert into sample_misc_charges VALUES ('freqent flier miles', 12, 1,0);
insert into sample_misc_charges VALUES ('sales tax', 0, 1, 0.06);
insert into sample_misc_charges VALUES ('extra driver', 12, 0, 0);


create table organization(
    org_name varchar(40),
    code varchar(20),
    discount decimal(3,3),
    primary key (org_name)
);

--relations
create table at(
    car_id numeric(12,0),
    loc_id numeric(12,0),
    primary key (car_id),
    foreign key (loc_id) references location,
    foreign key (car_id) references car
);

create table member_of(
    cust_id numeric(12,0),
    org_name varchar(40),
    primary key (cust_id, org_name),
    foreign key (cust_id) references customer,
    foreign key (org_name) references organization
);

create table org_discounts(
    id numeric(12,0),
    org_name varchar(20),
    primary key (id, org_name),
    foreign key (id) references rental on delete cascade,
    foreign key (org_name) references organization
);
 create table states
(
    state varchar(2)
);
 insert into states values('AK');
 insert into states values('AL');
 insert into states values('AR');
 insert into states values('AZ');
 insert into states values('CA');
 insert into states values('CO');
 insert into states values('CT');
 insert into states values('DC');
 insert into states values('DE');
 insert into states values('FL');
 insert into states values('GA');
 insert into states values('HI');
 insert into states values('IA');
 insert into states values('ID');
 insert into states values('IL');
 insert into states values('IN');
 insert into states values('KS');
 insert into states values('KY');
 insert into states values('LA');
 insert into states values('MA');
 insert into states values('MD');
 insert into states values('ME');
 insert into states values('MI');
 insert into states values('MN');
 insert into states values('MO');
 insert into states values('MS');
 insert into states values('MT');
 insert into states values('NC');
 insert into states values('ND');
 insert into states values('NE');
 insert into states values('NH');
 insert into states values('NJ');
 insert into states values('NM');
 insert into states values('NV');
 insert into states values('NY');
 insert into states values('OH');
 insert into states values('OK');
 insert into states values('OR');
 insert into states values('PA');
 insert into states values('RI');
 insert into states values('SC');
 insert into states values('SD');
 insert into states values('TN');
 insert into states values('TX');
 insert into states values('UT');
 insert into states values('VA');
 insert into states values('VT');
 insert into states values('WA');
 insert into states values('WI');
 insert into states values('WV');
 insert into states values('WY');


insert into my_time values(to_date('01/01/2001', 'mm/dd/yyyy'));

alter table vehi_type add constraint veh_type_unique unique (make, model, type, year);
alter table car add constraint car_plate_unique unique (plate_number, state);
alter table location add constraint location_unique_addr unique (street_addr, city, state, zip);
alter table customer add constraint cust_unique_license unique (license_st, license_number);
alter table organization add constraint org_unique_code unique (code);
