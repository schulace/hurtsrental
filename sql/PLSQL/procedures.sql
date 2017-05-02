CREATE PROCEDURE CREATEAT AS
BEGIN
  delete from at;
  for t in (select car_id from car)
  loop
    insert into at(car_id, loc_id) values (t.car_id, (select loc_id from (select loc_id, rownum, dbms_random.value(0,100) val from location order by val)where rownum = 1));
  end loop;
  NULL;
END CREATEAT;
/
CREATE PROCEDURE createcars AS
BEGIN
  for t in (select type_id from vehi_type sample(40))
  loop
    insert into car(state, plate_number, type_id) values((select state from (select state, rownum, dbms_random.value(0,100) val from states order by val)where rownum = 1), dbms_random.string('X', 7), t.type_id);
  end loop;
  for t in (select type_id from vehi_type sample(40))
  loop
    insert into car(state, plate_number, type_id) values((select state from (select state, rownum, dbms_random.value(0,100) val from states order by val)where rownum = 1), dbms_random.string('X', 7), t.type_id);
  end loop;
  NULL;
END createcars;
/
CREATE PROCEDURE CREATEMEMBEROF AS
BEGIN
    for t in (select * from customer sample(30))
    loop
      insert into member_of values (t.cust_id, (select org_name from (select org_name, rownum, dbms_random.value(0,100) val from organization order by val)where rownum = 1));
    end loop;
    for t in (select * from customer sample(30))
    loop
      begin
        insert into member_of values (t.cust_id, (select org_name from (select org_name, rownum, dbms_random.value(0,100) val from organization order by val)where rownum = 1));
        exception
        when others then
        goto end_loop;
      end;
    <<end_loop>>
    null;
    end loop;
  NULL;
END CREATEMEMBEROF;
/

--note: I ripped this one straight off of StackOverflow. However, it doesn't contribute to my codebase or do anything
--other than drop all my tables and their associated indices and squences and make my life easier
create PROCEDURE DROP_ALL_2 AS
BEGIN
  FOR i IN (SELECT   object_name
            ,        object_type
            FROM     user_objects
            ORDER BY object_type DESC) LOOP

    /* Drop types in descending order. */
    IF i.object_type = 'TYPE' THEN

      /* Drop type and force operation because dependencies may exist. Oracle 12c
         also fails to remove object types with dependents in pluggable databases
         (at least in release 12.1). Type evolution works in container database
         schemas. */
      EXECUTE IMMEDIATE 'DROP '||i.object_type||' '||i.object_name||' FORCE';

    /* Drop table tables in descending order. */
    ELSIF i.object_type = 'TABLE' THEN

      /* Drop table with cascading constraints to ensure foreign key constraints
         don't prevent the action. */
      EXECUTE IMMEDIATE 'DROP '||i.object_type||' '||i.object_name||' CASCADE CONSTRAINTS PURGE';

      /* Oracle 12c ONLY: Purge the recyclebin to dispose of system-generated
         sequence values because dropping the table doesn't automatically
         remove them from the active session.
         CRITICAL: Remark out the following when working in Oracle Database 11g. */
      EXECUTE IMMEDIATE 'PURGE RECYCLEBIN';

    ELSIF i.object_type = 'LOB' OR i.object_type = 'INDEX' THEN

      /* A system generated LOB column or INDEX will cause a failure in a
         generic drop of a table because it is listed in the cursor but removed
         by the drop of its table. This NULL block ensures there is no attempt
         to drop an implicit LOB data type or index because the dropping the
         table takes care of it. */
      NULL;

    ELSE

      /* Drop any other objects, like sequences, functions, procedures, and packages. */
      EXECUTE IMMEDIATE 'DROP '||i.object_type||' '||i.object_name;

    END IF;
  END LOOP;
END;
\
CREATE PROCEDURE REMOVEDATA AS
BEGIN
  delete from at;
  delete from car;
  delete from location;
  delete from member_of;
  delete from misc_charge;
  delete from org_discounts;
  delete from rental;
  delete from vehi_type;
  delete from organization;
  delete from customer;
  delete from sample_misc_charges;
  NULL;
END REMOVEDATA;
/

CREATE OR REPLACE FUNCTION RENTAL_COST
(
  RENTAL_ID IN NUMBER
) RETURN NUMBER AS
s_d Date;
e_d Date;
r_len number;
ins_c number;
total_cost number;
rate number;
calc_multiplier number;

--charges variables
pct number;
charge_cost number;
onetime number;
--discount percentages variables
disct number;

cursor charges is
  select percentage, cost, onetime
  from misc_charge
  where id = rental_id;

cursor discounts is
  select discount
  from organization natural join org_discounts
  where id =org_discounts.id;

BEGIN

  select start_date, end_date, insurance_charge, rate into s_d, e_d, ins_c, rate
  from rental natural join car natural join vehi_type where rental.id = rental_id;

  r_len := e_d - s_d;

  total_cost := r_len * (rate + ins_c);

  open charges;

  while charges%FOUND
  loop
    fetch charges into pct, charge_cost, onetime;
    if onetime = 1
    then
      total_cost := total_cost + pct * rate;
      total_cost := total_cost + charge_cost;
    else
      total_cost := total_cost + pct * rate * r_len;
      total_cost := total_cost + charge_cost * r_len;
    end if;
  end loop;

  calc_multiplier := 1;
  open discounts;
  while discounts%FOUND
  loop
    fetch discounts into disct;
    calc_multiplier := calc_multiplier - disct;
  end loop;

  if calc_multiplier < 0
  then
    calc_multiplier := 0;
  end if;

  return calc_multiplier * total_cost;


  RETURN NULL;
END RENTAL_COST;
