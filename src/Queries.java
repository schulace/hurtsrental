/**
 * Created by schulace on 4/18/17.
 */
public class Queries {
    public static final String MANAGER_INV_VIEW =
        "select PLATE_NUMBER, make, MODEL, YEAR, STREET_ADDR, CITY, LOCATION.state from car natural join at  NATURAL join VEHI_TYPE left OUTER JOIN LOCATION on AT.LOC_ID = LOCATION.LOC_ID";
        //TODO fix above to include cars out for rental
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

    public static final String CITY_LIST = "select distinct city from location order by city";
    public static final String CITY_FILTER = "select loc_id, street_addr, city, state, zip from location where LOWER(city) = LOWER(?)";
    public static final String RENT = "insert into rental(start_date, end_date, cust_id, car_id, insurance_charge, pickup_loc, pickup_occ, dropoff_loc, dropoff_occ, start_fuel, end_fuel) values (?,?,?,?,?,?,0,?,0,1,NULL)";
    public static final String MISC_CHARGE = "insert into MISC_CHARGE values(?,?,?,?,?)";
    public static final String LAST_RENTAL = "select max(id) from rental";
    public static final String ORG_LIST = "select org_name from organization";
    public static final String ORG_DISCOUNT = "insert into org_discounts values(?,?)";
    public static final String ORG_CHECK = "select * from organization where LOWER(org_name) = LOWER(?) and code = ?";

    public static final String RANDOM_CUSTOMER = "select cust_id from (select cust_id from customer order by dbms_random.value()) where rownum = 1";
    public static final String CUSTOMER_LOGIN_CHECK = "select cust_id, last_name from customer where cust_id = ? " +
            "and LOWER(last_name) = LOWER(?)";

    public static final String CUSTOMER_ADD_APPT = "insert into CUSTOMER(FIRST_NAME, LAST_NAME, STREET_ADDR, CITY, STATE, LICENSE_NUMBER, LICENSE_ST, ZIP, APPT_NUMBER) values(?,?,?,?,?,?,?,?,?)";
    public static final String CUSTOMER_ADD = "insert into CUSTOMER(FIRST_NAME, LAST_NAME, STREET_ADDR, CITY, STATE, LICENSE_NUMBER, LICENSE_ST, ZIP) values(?,?,?,?,?,?,?,?)";

    public static final String MOST_RECENT_CUST = "select CUST_ID from CUSTOMER where CUST_ID = (select max(CUST_ID) from customer)";

    public static final String LIST_CUSTOMER_RENTALS = "select * from rental where cust_id = ?";
    public static final String LIST_CURRENT_CUSTOMER_RENTALS = "select * from rental where cust_id = ? and end_date >= (select c_d from my_time) and pickup_occ = 0";
    public static final String CAR_PICKUP = "update rental set pickup_occ = 1 where id = ?";
}
