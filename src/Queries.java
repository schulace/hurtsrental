/**
 * Created by schulace on 4/18/17.
 */
public class Queries {
    public static final String MANAGER_INV_VIEW =
        "select PLATE_NUMBER, make, MODEL, YEAR, STREET_ADDR, CITY, LOCATION.state from car natural join at  NATURAL join VEHI_TYPE left OUTER JOIN LOCATION on AT.LOC_ID = LOCATION.LOC_ID";
        //TODO fix above to include cars out for rental
    public static final String AVAILABLE_CARS =
    "        select car_id\n" +
    "        from(\n" +
    "          select car_id, dropoff_loc as loc_id, max(end_date)\n" +
    "          from rental\n" +
    "          where rental.end_date < ? and dropoff_loc = ? \n" +
    "          group by car_id, dropoff_loc\n" +
    "        )\n" +
    "\n" +
    "        UNION\n" +
    "\n" +
    "        select car_id\n" +
    "        from at where car_id not in\n" +
    "          (select car_id\n" +
    "          from(\n" +
    "            select car_id, dropoff_loc as loc_id, max(end_date)\n" +
    "            from rental\n" +
    "            where rental.end_date < ? and dropoff_loc = ? \n" +
    "            group by car_id, dropoff_loc\n" +
    "          )\n" +
    "        ) and LOC_ID = ? \n" +
    "\n" +
    "        MINUS\n" +
    "\n" +
    "        --all cars which will be out for rental during inputted timeframe\n" +
    "        (select rental.car_id\n" +
    "        from rental join at on rental.car_id = at.car_id\n" +
    "        where (start_date < ? and end_date > ? OR\n" +
    "        (START_DATE > ? and END_DATE < ? ))\n" +
    "\n" +
    "        MINUS\n" +
    "\n" +
    "        --cars reserved for pickup after specified dropoff time where the intended dropoff location doesn't match the reservation's pickup\n" +
    "        (select car_id\n" +
    "        from (\n" +
    "          select car_id, pickup_loc as loc_id, min(start_date)\n" +
    "          from rental\n" +
    "          where start_date > ? and pickup_loc = ?\n" +
    "          group by car_id, pickup_loc\n" +
    "          )\n" +
    "        ))";

    public static final String CITY_LIST = "select distinct city from location order by city";
    public static final String CITY_FILTER = "select loc_id, street_addr, city, state, zip from location where LOWER(city) = LOWER(?)";

    public static final String RANDOM_CUSTOMER = "select cust_id from (select cust_id from customer order by dbms_random.value()) where rownum = 1";

    public static final String CUSTOMER_LOGIN_CHECK = "select cust_id, last_name from customer where cust_id = ? " +
            "and LOWER(last_name) = LOWER(?)";

    public static final String CUSTOMER_ADD_APPT = "insert into CUSTOMER(FIRST_NAME, LAST_NAME, STREET_ADDR, CITY, STATE, LICENSE_NUMBER, LICENSE_ST, ZIP, APPT_NUMBER) values(?,?,?,?,?,?,?,?,?)";
    public static final String CUSTOMER_ADD = "insert into CUSTOMER(FIRST_NAME, LAST_NAME, STREET_ADDR, CITY, STATE, LICENSE_NUMBER, LICENSE_ST, ZIP) values(?,?,?,?,?,?,?,?)";

    public static final String MOST_RECENT_CUST = "select CUST_ID from CUSTOMER where CUST_ID = (select max(CUST_ID) from customer)";
}
