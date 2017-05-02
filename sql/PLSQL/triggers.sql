create or replace TRIGGER ANTI_TIME_TRAVEL
BEFORE UPDATE OF C_D ON MY_TIME
for each row
BEGIN
  if :new.c_d < :old.c_d
  then
    RAISE_APPLICATION_ERROR(-20002, 'you may NOT set the fake new date to a date in the fake past. not cool');
  end if;
  NULL;
END;
\
create or replace trigger PICKUP
after update of pickup_occ on Rental
for each row
begin
  if :new.pickup_occ = 1
  then
    delete from at where car_id = :new.car_id;
  end if;
end;
\
create or replace TRIGGER DROPOFF
after UPDATE OF DROPOFF_OCC ON RENTAL
referencing new as changed
for each row
BEGIN
  if :changed.dropoff_occ = 1
  then
    insert into at values(:changed.car_id, :changed.pickup_loc);
  end if;
  NULL;
END;
