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

  
create or replace trigger PICKUP
after update of pickup_occ on Rental
for each row
begin
  if :new.pickup_occ = 1
  then
    delete from at where car_id = :new.car_id;
  end if;
end;

create or replace TRIGGER DROPOFF
after UPDATE OF DROPOFF_OCC ON RENTAL
referencing new as changed
for each row
  BEGIN
    if :changed.dropoff_occ = 1
    then
      insert into at values(:changed.car_id, :changed.DROPOFF_LOC);
    end if;
  END;
  
create or replace trigger RESERVE_CAR
before insert on rental
for each row
declare
cursor c is
(
    select car_id
    from
    (
         select car_id, dropoff_loc as loc_id, max(end_date)
         from rental
         where rental.end_date < :new.start_date and dropoff_loc = :new.pickup_loc
         group by car_id, dropoff_loc
    )

   UNION

    select car_id
    from at where car_id not in
    (
          select car_id
          from
          (
              select car_id, dropoff_loc as loc_id, max(end_date)
              from rental
              where rental.end_date < :new.start_date and dropoff_loc = :new.pickup_loc
              group by car_id, dropoff_loc
          )
     ) and LOC_ID = :new.pickup_loc


  MINUS

  --all cars which will be out for rental during inputted timeframe

    SELECT rental.car_id
    FROM rental inner JOIN at ON rental.car_id = at.car_id
    WHERE (start_date <= :new.end_date AND end_date >= :new.start_date OR (START_DATE >= :new.start_date AND END_DATE <= :new.end_date))
)

MINUS

--cars reserved for pickup after specified dropoff time where the intended dropoff location doesn't match the reservation's pickup
(
    SELECT car_id
    FROM
    (
        SELECT car_id, pickup_loc AS loc_id, min(start_date)
        FROM rental
        WHERE start_date >= :new.end_date AND pickup_loc != :new.dropoff_loc
        GROUP BY car_id, pickup_loc
    )
) ;
car_num number;
found boolean;
begin
    open c;
    fetch c into car_num;
    found := false;
    while c%found
    loop
        if car_num = :new.car_id
        then
            found := true;
            exit;
        end if;
        fetch c into car_num;
    end loop;
    if found = false
    then
        RAISE_APPLICATION_ERROR(-20006, 'unallowed insert');
    end if;
end;
