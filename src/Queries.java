/**
 * Created by alex on 4/18/17.
 */
public class Queries {
    //language=Oracle
    public static final String MANAGER_INV_VIEW = "select car_id, plate_number, car.state, make, model, year, location.street_addr, location.state, location.zip from rental full outer join at using (car_id) inner join car using(car_id) inner join vehi_type using(type_id) left outer join location on at.loc_id = location.loc_id order by zip";

    //language=Oracle
    public static final String AVAILABLE_CARS =
            "select car_id, make, model, year, RATE, type from car natural join\n" +
            "(\n" +
            "    (\n" +
            "        select car_id\n" +
            "        from\n" +
            "        (\n" +
            "             select car_id, dropoff_loc as loc_id, max(end_date)\n" +
            "             from rental\n" +
            "             where rental.end_date < ? and dropoff_loc = ?\n" +
            "             group by car_id, dropoff_loc\n" +
            "        )\n" +
            "\n" +
            "       UNION\n" +
            "\n" +
            "        select car_id\n" +
            "        from at where car_id not in\n" +
            "        (\n" +
            "              select car_id\n" +
            "              from\n" +
            "              (\n" +
            "                  select car_id, dropoff_loc as loc_id, max(end_date)\n" +
            "                  from rental\n" +
            "                  where rental.end_date < ? and dropoff_loc = ?\n" +
            "                  group by car_id, dropoff_loc\n" +
            "              )\n" +
            "         ) and LOC_ID = ?\n" +
            "\n" +
            "\n" +
            "      MINUS\n" +
            "\n" +
            "      --all cars which will be out for rental during inputted timeframe\n" +
            "\n" +
            "        SELECT rental.car_id\n" +
            "        FROM rental inner JOIN at ON rental.car_id = at.car_id\n" +
            "        WHERE (start_date <= ? AND end_date >= ? OR (START_DATE >= ? AND END_DATE <= ?))\n" +
            "    )\n" +
            "\n" +
            "    MINUS\n" +
            "\n" +
            "    --cars reserved for pickup after specified dropoff time where the intended dropoff location doesn't match the reservation's pickup\n" +
            "    (\n" +
            "        SELECT car_id\n" +
            "        FROM\n" +
            "        (\n" +
            "            SELECT car_id, pickup_loc AS loc_id, min(start_date)\n" +
            "            FROM rental\n" +
            "            WHERE start_date >= ? AND pickup_loc != ?\n" +
            "            GROUP BY car_id, pickup_loc\n" +
            "        )\n" +
            "    )\n" +
            ") NATURAL join VEHI_TYPE order by rate, MODEL, make, year";

    //language=Oracle
    public static final String CITY_LIST = "select distinct city from location order by city";
    //language=Oracle
    public static final String CITY_FILTER = "select loc_id, street_addr, city, state, zip from location where LOWER(city) = LOWER(?)";
    //language=Oracle
    public static final String RENT = "insert into rental(start_date, end_date, cust_id, car_id, insurance_charge, pickup_loc, pickup_occ, dropoff_loc, dropoff_occ, end_fuel) values (?,?,?,?,?,?,0,?,0,0)";
    //language=Oracle
    public static final String MISC_CHARGE = "insert into MISC_CHARGE values(?,?,?,?,?)";
    //language=Oracle
    public static final String LAST_RENTAL = "select max(id) from rental";
    //language=Oracle
    public static final String ORG_LIST = "select org_name from organization";
    //language=Oracle
    public static final String ORG_DISCOUNT = "insert into org_discounts values(?,?)";
    //language=Oracle
    public static final String ORG_CHECK = "select * from organization where LOWER(org_name) = LOWER(?) and code = ?";

    //language=Oracle
    public static final String RANDOM_CUSTOMER = "select cust_id from (select cust_id from customer order by dbms_random.value()) where rownum = 1";
    //language=Oracle
    public static final String CUSTOMER_LOGIN_CHECK = "select cust_id, last_name from customer where cust_id = ? " +
            "and LOWER(last_name) = LOWER(?)";

    //language=Oracle
    public static final String CUSTOMER_ADD_APPT = "insert into CUSTOMER(FIRST_NAME, LAST_NAME, STREET_ADDR, CITY, STATE, LICENSE_NUMBER, LICENSE_ST, ZIP, APPT_NUMBER) values(?,?,?,?,?,?,?,?,?)";
    //language=Oracle
    public static final String CUSTOMER_ADD = "insert into CUSTOMER(FIRST_NAME, LAST_NAME, STREET_ADDR, CITY, STATE, LICENSE_NUMBER, LICENSE_ST, ZIP) values(?,?,?,?,?,?,?,?)";

    //language=Oracle
    public static final String MOST_RECENT_CUST = "select CUST_ID from CUSTOMER where CUST_ID = (select max(CUST_ID) from customer)";

    //language=Oracle
    public static final String LIST_COMPLETED_RENTALS = "select RENTAL.id, RENTAL.START_DATE, RENTAL.END_DATE, RENTAL_COST(Rental.id) projected_cost, l1.city as pickup_city, l2.city as dropoff_city, VEHI_TYPE.make, VEHI_TYPE.model, VEHI_TYPE.year,  car.PLATE_NUMBER, car.STATE as license_state from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC inner join car on car.CAR_ID = rental.CAR_ID inner join VEHI_TYPE on VEHI_TYPE.TYPE_ID = car.TYPE_ID where cust_id = ? and DROPOFF_OCC = 1";
    //language=Oracle
    public static final String LIST_FUTURE_RENTALS = "select RENTAL.id, RENTAL.START_DATE, RENTAL.END_DATE, RENTAL_COST(Rental.id) projected_cost, l1.city as pickup_city, l2.city as dropoff_city, VEHI_TYPE.make, VEHI_TYPE.model, VEHI_TYPE.year,  car.PLATE_NUMBER, car.STATE as license_state from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC inner join car on car.CAR_ID = rental.CAR_ID inner join VEHI_TYPE on VEHI_TYPE.TYPE_ID = car.TYPE_ID where cust_id = ? and DROPOFF_OCC = 0 and PICKUP_OCC = 0";
    //language=Oracle
    public static final String LIST_AVAILABLE_PICKUPS = " select RENTAL.id, RENTAL.START_DATE, RENTAL.END_DATE, RENTAL_COST(Rental.id) projected_cost, l1.city as pickup_city, l2.city as dropoff_city, VEHI_TYPE.make, VEHI_TYPE.model, VEHI_TYPE.year,  car.PLATE_NUMBER, car.STATE as license_state from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC inner join car on car.CAR_ID = rental.CAR_ID inner join VEHI_TYPE on VEHI_TYPE.TYPE_ID = car.TYPE_ID where cust_id = ? and START_DATE <= (select c_d from my_time) and END_DATE >= (SELECT c_d from MY_TIME) and pickup_occ = 0 and rental.car_id in (select CAR_ID from AT)";
    //language=Oracle
    public static final String LIST_WOULD_BE_PICKUPS = "select id, START_DATE, END_DATE, l1.city as pickup_city, l2.city as dropoff_city, VEHI_TYPE.make, VEHI_TYPE.model, VEHI_TYPE.year,  car.PLATE_NUMBER, car.STATE as license_state from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC inner join car on car.CAR_ID = rental.CAR_ID inner join VEHI_TYPE on VEHI_TYPE.TYPE_ID = car.TYPE_ID where cust_id = ? and START_DATE <= (select c_d from my_time) and END_DATE >= (SELECT c_d from MY_TIME) and pickup_occ = 0 and RENTAL.CAR_ID not in (select CAR_ID from at)";
            //"select * from rental where cust_id = ? and START_DATE <= (select c_d from my_time) and END_DATE >= (SELECT c_d from MY_TIME) and pickup_occ = 0 and (CAR_ID,DROPOFF_LOC) in (select CAR_ID, loc_id from at)";

    //language=Oracle
    public static final String LIST_ALL_RENTALS_PAST = "select id, START_DATE, END_DATE, RENTAL_COST(Rental.id) projected_cost,l1.city as pickup_city, l2.city as dropoff_city from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC where DROPOFF_OCC = 1";

    //language=Oracle
    public static final String LIST_ALL_RENTALS_CURRENT = "select id, START_DATE, END_DATE, RENTAL_COST(Rental.id) projected_cost,l1.city as pickup_city, l2.city as dropoff_city from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC where DROPOFF_OCC = 0 and PICKUP_OCC = 1";

    //language=Oracle
    public static final String LIST_ALL_RENTALS_RESERVED = "select id, START_DATE, END_DATE, RENTAL_COST(Rental.id) projected_cost,l1.city as pickup_city, l2.city as dropoff_city from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC where PICKUP_OCC = 0";
    //language=Oracle
    public static final String DELETE_RENTAL = "delete from rental where id = ?";

    //language=Oracle
    public static final String LIST_ACTIVE_RENTALS = "select id, START_DATE, END_DATE, RENTAL_COST(Rental.id) projected_cost,l1.city as pickup_city, l2.city as dropoff_city from rental inner join LOCATION l1 on l1.LOC_ID = PICKUP_LOC inner join LOCATION l2 on l2.LOC_ID = DROPOFF_LOC where CUST_ID = ? and  DROPOFF_OCC = 0 and PICKUP_OCC = 1";
    //language=Oracle
    public static final String CAR_PICKUP = "update rental set pickup_occ = 1 where id = ?";
    //language=Oracle
    public static final String CAR_DROPOFF = "update rental set dropoff_occ = 1, END_FUEL = ? where id = ?";

    //language=Oracle
    public static final String SET_DATE = "update my_time set c_d = ?";
    //language=Oracle
    public static final String AUTO_PICK_UP = "update rental set pickup_occ = 1 where start_date <= (select C_D from my_time) and PICKUP_OCC = 0";
    //language=Oracle
    public static final String AUTO_DROP_OFF_1 = "update rental set dropoff_occ = 1 where end_date <= (select C_D from my_time) and DROPOFF_OCC = 0";

    //language=Oracle
    public static final String AUTO_DROP_OFF_2 ="INSERT INTO at(\n" +
            "SELECT t1.car_id, rental.dropoff_loc FROM \n" +
            "(\n" +
            "  SELECT car_id, max(end_date) max_d FROM rental GROUP BY car_id\n" +
            ") t1 INNER JOIN rental ON rental.car_id = t1.car_id AND rental.end_date = t1.max_d\n" +
            "WHERE rental.dropoff_occ = 1 and t1.CAR_ID not in (select CAR_ID from at))";
    //language=Oracle
    public static final String GET_DATE = "select c_d curr_date from my_time";

    //language=Oracle
    public static final String COST_CALCULATION = "select rental_cost(?) from dual";
    //language=Oracle
    public static final String DROPOFF_FIX = "update rental set END_DATE = case when (select c_d from my_time) > END_DATE then (select c_d from MY_TIME) else END_DATE END where id = ?";

    //language=Oracle
    public static final String CUSTOMER_INFO_UPDATE = "update customer set FIRST_NAME = ?, LICENSE_NUMBER = ?, LICENSE_ST = ?, LAST_NAME = ?, STREET_ADDR = ?, APPT_NUMBER = ?, CITY = ?, STATE = ?, ZIP = ? where cust_id = ?";
    //language=Oracle
    public static final String GET_CUSTOMER = "select * from customer where CUST_ID = ?";

    //language=Oracle
    public static final String CUSTOMER_BE_MEMBER = "insert into MEMBER_OF values (?,?)";

    //language=Oracle
    public static final String CREATE_CAR_TYPE = "insert into VEHI_TYPE(make, model, type, rate, year, FUEL_CAPACITY) VALUES (?,?,?,?,?,?)";

    //language=Oracle
    public static final String GET_VEHI_TYPES = "select * from VEHI_TYPE where LOWER(make) like LOWER(?) and LOWER(MODEL) like LOWER(?) and LOWER(TYPE) like LOWER(?) and (YEAR = ? or ? = 0)";

    //language=Oracle
    public static final String CAR_ADD = "INSERT into car(state, plate_number, TYPE_ID) VALUES (?,?,?)";

    //language=Oracle
    public static final String REVENUE = "SELECT sum(rental_cost(id)) revenue_$ FROM rental WHERE dropoff_occ = 1 AND end_date >= ? AND end_date <= ?";

    //language=Oracle
    public static final String ADD_ORG = "insert into ORGANIZATION(ORG_NAME, CODE, DISCOUNT) VALUES (?,?,?)";

    //language=Oracle
    public static final String REMOVE_ORG = "delete from ORGANIZATION where Lower(ORG_NAME) = Lower(?)";

    //language=Oracle
    public static final String CAR_REMOVE = "delete from car where CAR_ID = ?";

    //language=Oracle
    public static final String VIEW_ORGS  = "SELECT * from ORGANIZATION";
}
