import java.sql.*;
import java.util.*;

import static java.lang.System.exit;

/**
 * Created by schulace on 4/24/17.
 */
public class CustomerView extends myView {

    public int customer_id;

    public CustomerView(Connection conn) throws Exception {
        super(conn);
        customer_login();
    }

    public void customer_login() throws Exception {
        System.out.println("please log in:");
        String real_login = (String) mainRunner.getResponse(
                "for testing purposes, enter 'test' to get a random customer ID" +
                        ", 'login' to log in as a customer, or 'create' to make a new account", new ArrayList<String>() {{
                    add("test");
                    add("login");
                    add("create");
                }}, "hell", false); //ugly, but 1-line

        switch (real_login) {
            case "test":
                try (Statement s = conn.createStatement()) {
                    ResultSet set = s.executeQuery(Queries.RANDOM_CUSTOMER);
                    if (set.next()) {
                        customer_id = set.getInt(1);
                        System.out.println("logged in with ID = " + customer_id);
                        return;
                    } else {
                        System.out.println("no customers exist");
                        customer_login();
                        return;
                    }
                } catch (SQLException e) {
                    throw new Exception("lol");
                }
                //break; //unreachable when login is done recursively
            case "login":
                while (true) {
                    int uid = (Integer) mainRunner.getResponse("please enter your customer ID or -1 to exit", null,
                            7, false);
                    if (uid == -1) {
                        throw new Exception("lmao u couldn't log in");
                    }
                    String lname = (String) mainRunner.getResponse("please enter your last name",
                            null, "string", false);
                    try (PreparedStatement s = conn.prepareStatement(Queries.CUSTOMER_LOGIN_CHECK)) {
                        s.setInt(1, uid);
                        s.setString(2, lname);
                        ResultSet set = s.executeQuery();
                        if (set.next()) {
                            customer_id = set.getInt(1);
                            System.out.println("successfully logged in with customer id " + customer_id);
                            break;
                        }
                        System.out.println("incorrect login, try again");
                    } catch (SQLException e) {
                        e.printStackTrace(); //TODO handle
                    }
                }
                break;
            case "create":
                //TODO make these save answers
                String fName = mainRunner.getRegexResponse(
                        "what is your first name (up to 20 characters, no punctuation or spaces)",
                        "^[[a-zA-Z]]{1,20}$");
                String lNumber = mainRunner.getRegexResponse(
                        "enter your 9-digit license ID (may contain letters)",
                        "^.{9}$");
                String lState = mainRunner.getRegexResponse("what state is on your license? (enter an invalid state and you'll need to enter all of the above again)",
                        "^[a-zA-Z]{2}").toUpperCase();
                String lname = mainRunner.getRegexResponse(
                        "what is your last name (up to 20 characters, no punctuation or spaces)",
                        "^[[a-zA-Z]]{1,20}$");
                String street_addr = mainRunner.getRegexResponse(
                        "enter your street address (no city / state / zip) no punctuation",
                        "^[[a-zA-Z]|\\s|\\d]{1,20}$");
                String appt_number = mainRunner.getRegexResponse(
                        "appartment number (just press <enter> if you don't have one)",
                        "^\\d{0,7}$");
                String city = mainRunner.getRegexResponse(
                        "name of your city up to 25 characters, spaces are allowed",
                        "^[[a-zA-Z]|\\s]{1,25}$");
                String state = mainRunner.getRegexResponse("what state do you live in? (enter an invalid state and you'll need to enter all of the above again)",
                        "^[a-zA-Z]{2}").toUpperCase();
                int zip = Integer.parseInt(mainRunner.getRegexResponse(
                        "What is your 5-digit zip code?",
                        "^\\d{5}"));
                try (PreparedStatement stmnt = conn.prepareStatement(appt_number.equals("") ? Queries.CUSTOMER_ADD : Queries.CUSTOMER_ADD_APPT)) {
                    stmnt.setString(1, fName);
                    stmnt.setString(2, lname);
                    stmnt.setString(3, street_addr);
                    stmnt.setString(4, city);
                    stmnt.setString(5, state);
                    stmnt.setString(6, lNumber);
                    stmnt.setString(7, lState);
                    stmnt.setInt(8, zip);
                    if (!appt_number.equals("")) {
                        stmnt.setInt(9, Integer.parseInt(appt_number));
                    } else {
                        stmnt.setNull(9, Types.NUMERIC);
                    }
                    stmnt.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Database integrity constraint violated (one of the state abbreviations was inputted incorrectly, exiting");
                    customer_login();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("something went horribly wrong");
                    exit(2);
                }

                try (Statement stmnt = conn.createStatement()) {
                    ResultSet s = stmnt.executeQuery(Queries.MOST_RECENT_CUST);
                    s.next(); //now in first row (should be only row)
                    customer_id = s.getInt(1);
                    System.out.println("your unique customer ID is " + customer_id + ". Remember it, because Hurts Rent-a-Lemon doesn't support account recovery!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            default:
                customer_id = 0;
                break;
        }

    }


    @Override
    void run() {
        options.put("reserve car", () -> reserveCar());
        options.put("pickup car", () -> pickupCar());
        options.put("list rentals", () -> listRentals());
        options.put("drop off car", () -> dropoffCar());
        options.put("update info", () -> updateInfo());
        looper("customer");
    }

    public void reserveCar() {
        try (PreparedStatement availableCars = conn.prepareStatement(Queries.AVAILABLE_CARS);
             PreparedStatement rent = conn.prepareStatement(Queries.RENT);
             PreparedStatement misc_charge = conn.prepareStatement(Queries.MISC_CHARGE);
             PreparedStatement last_rental = conn.prepareStatement(Queries.LAST_RENTAL);
             Statement orglist = conn.createStatement();
             PreparedStatement orgAdd = conn.prepareStatement(Queries.ORG_DISCOUNT);
             PreparedStatement orgCheck = conn.prepareStatement(Queries.ORG_CHECK)
        ) {
            conn.setAutoCommit(false);
            //get pickup location
            confirmPrior("pickup location", () -> mainRunner.getLocation(conn, "where would you like to pick up the car from?"));
            int pickup_loc = (Integer) answers.get("pickup location");

            confirmPrior("dropoff location", () -> mainRunner.getLocation(conn, "where would you like to drop off the car?"));
            int dropoff_loc = (Integer) answers.get("dropoff location");


            //get start date
            confirmPrior("pickup date", () ->
            {
                System.out.println("when would you like to pick up the car?");
                return mainRunner.getDateResponse();
            });
            java.util.Date pickup_date = (java.util.Date) answers.get("pickup date");


            //get end date
            confirmPrior("dropoff date", () ->
            {
                System.out.println("when will you be returning the vehicle?");
                return mainRunner.getDateResponse();
            });
            java.util.Date dropoff_date = (java.util.Date) answers.get("dropoff date");

            if (dropoff_date.compareTo(pickup_date) <= 0) {
                System.out.println("you can't have a rental ending before it starts. try again");
                reserveCar();
                return;
            }

            availableCars.setDate(1, new java.sql.Date(pickup_date.getTime()));
            availableCars.setInt(2, pickup_loc);
            availableCars.setDate(3, new java.sql.Date(pickup_date.getTime()));
            availableCars.setInt(4, pickup_loc);
            availableCars.setInt(5, pickup_loc);
            availableCars.setDate(6, new java.sql.Date(dropoff_date.getTime()));
            availableCars.setDate(7, new java.sql.Date(pickup_date.getTime()));
            availableCars.setDate(8, new java.sql.Date(pickup_date.getTime()));
            availableCars.setDate(9, new java.sql.Date(dropoff_date.getTime()));
            availableCars.setDate(10, new java.sql.Date(dropoff_date.getTime()));
            availableCars.setInt(11, dropoff_loc);

            ResultSet set = availableCars.executeQuery();
            System.out.println("you must select from a list of the following cars, or enter 0 to cancel the reservation");
            mainRunner.printSet(set);
            ResultSet set2 = availableCars.executeQuery();
            ArrayList<Integer> car_options = new ArrayList<>();
            car_options.add(0);
            while (set2.next()) {
                car_options.add(set2.getInt(1));
            }
            confirmPrior("car id", () -> mainRunner.getResponse("select from car_id above", car_options, 0, false));
            int to_reserve = (int) answers.get("car id");
            if (to_reserve == 0) {
                answers.clear();
                System.out.println("rental aborted.");
                return;
            }

            int ins_cost = -1;
            while (ins_cost < 0) {
                confirmPrior("insurance charge", () -> (int) mainRunner.getResponse("what is the daily insurance charge for the vehicle", null, 0, false));
                ins_cost = (int) answers.get("insurance charge");
            }

            rent.setDate(1, new java.sql.Date(pickup_date.getTime()));
            rent.setDate(2, new java.sql.Date(dropoff_date.getTime()));
            rent.setInt(3, customer_id);
            rent.setInt(4, to_reserve);
            rent.setInt(5, ins_cost);
            rent.setInt(6, pickup_loc);
            rent.setInt(7, dropoff_loc);
            //a trigger will also process here to ensure that the rental doesn't go back in time
            rent.executeUpdate();
            int rental_id;
            ResultSet s = last_rental.executeQuery();
            s.next();
            rental_id = s.getInt(1);

            while (true) {
                String res;
                res = (String) mainRunner.getResponse("enter the name of a misc. charge, or 'done' when there are no more charges", null, "string", false);
                if (res.equals("done")) {
                    break;
                }
                int oneTime = ((String) mainRunner.getResponse(
                        "is this a one-time charge or daily charge?",
                        new ArrayList<String>() {{
                            add("one-time");
                            add("daily");
                        }},
                        "str",
                        true
                )).equals("one-time") ? 1 : 0;
                int cost = (int) mainRunner.getResponse("what is the dollar cost of this charge (put 0 if it's a percentage)", null, 0, false);
                double perct;
                while (true) {
                    perct = (double) mainRunner.getResponse("what is the % charge for this? (list as a decimal less than 1, or 0 if this charge doesn't have an associated percentage)", null, 0D, false);
                    if (perct <= 1) {
                        break;
                    }
                    System.out.println("percentage was >1");
                }
                misc_charge.setInt(1, rental_id);
                misc_charge.setString(2, res);
                misc_charge.setInt(3, cost);
                misc_charge.setInt(4, oneTime);
                misc_charge.setDouble(5, perct);
                misc_charge.executeUpdate();
            }

            ResultSet org_set = orglist.executeQuery(Queries.ORG_LIST);
            ArrayList<String> orgarr = new ArrayList<>();
            while (org_set.next()) {
                orgarr.add(org_set.getString(1));
            }
            orgarr.add("none");

            while (true) {
                System.out.println("almost done! we just need you to input some promo codes if you have any");
                String org = (String) mainRunner.getResponse("which organization do you belong to (enter 'none' to stop entering orgs)", orgarr, "str", true);
                if (org.equals("none")) {
                    break;
                }
                String code = mainRunner.getRegexResponse("enter your promo code (up to 20 characters, no special symbols or spaces)", "^\\w{0,20}$");
                orgCheck.setString(1, org);
                orgCheck.setString(2, code);
                ResultSet org_match = orgCheck.executeQuery();
                if (!org_match.next()) {
                    System.out.println("invalid code :(");
                    continue;
                }
                orgAdd.setInt(1, rental_id);
                orgAdd.setString(2, org);
                orgAdd.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println("something went wrong (it's possible that the dates you entered are before the current date (set this in manager view))");
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.out.println("something failed, so we tried rolling back, but even that failed. I believe 'screwed' is a good way to describe the state of the DB if you get here");
                e1.printStackTrace();
            }
            e.printStackTrace();
            reserveCar();
            return;
        }
        answers.clear();
    }

    public void listRentals() {
        try (PreparedStatement st = conn.prepareStatement(Queries.LIST_CUSTOMER_RENTALS)) {
            st.setInt(1, customer_id);
            ResultSet set = st.executeQuery();
            mainRunner.printSet(set);
        } catch (SQLException e) {
            System.out.println("couldn't connect to server :(");
        }
    }

    public void pickupCar() {
        try (PreparedStatement statement = conn.prepareStatement(Queries.LIST_AVAILABLE_PICKUPS);
             PreparedStatement update = conn.prepareStatement(Queries.CAR_PICKUP)) {
            conn.setAutoCommit(false);
            statement.setInt(1, customer_id);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                System.out.println("you don't have any current rentals to pick up");
                return;
            }
            ArrayList<Integer> rentalIDs = new ArrayList<>();
            do {
                rentalIDs.add(set.getInt(1));
            } while (set.next());

            System.out.println("select which rental you'd like to pick up from the following list by ID");
            ResultSet available_pups = statement.executeQuery();
            mainRunner.printSet(available_pups);
            confirmPrior("to update id", () -> mainRunner.getResponse("select from above", rentalIDs, 0, false));
            int id_to_update = (int) answers.get("to update id");
            update.setInt(1, id_to_update);
            update.executeUpdate();
            answers.clear();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("database failed, couldn't rollback transaction.");
            }
            e.printStackTrace();
        }
    }

    public void dropoffCar() {
        try (PreparedStatement statement = conn.prepareStatement(Queries.LIST_ACTIVE_RENTALS);
             PreparedStatement update = conn.prepareStatement(Queries.CAR_DROPOFF);
             PreparedStatement final_cost = conn.prepareStatement(Queries.COST_CALCULATION);
             PreparedStatement fix_dropoff_date = conn.prepareStatement(Queries.DROPOFF_FIX)) {
            conn.setAutoCommit(false);
            statement.setInt(1, customer_id);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                System.out.println("you don't have any current rentals out");
                return;
            }
            ArrayList<Integer> rentalIDs = new ArrayList<>();
            do {
                rentalIDs.add(set.getInt(1));
            } while (set.next());
            fix_dropoff_date.setInt(1, customer_id); //sets dropoff date to the max of either today's date or the reserved end date
            fix_dropoff_date.executeUpdate();
            System.out.println("select which rental you'd like to drop off from the following list by ID");
            listRentals();
            confirmPrior("to update id", () -> mainRunner.getResponse("select from above", rentalIDs, 0, false));
            int id_to_update = (int) answers.get("to update id");
            final_cost.setInt(1, id_to_update);
            ResultSet set1 = final_cost.executeQuery();
            update.setInt(1, id_to_update);
            if (!set1.next()) {
                System.out.println("shiitttttt. this shouldn't happen. aborting");
                conn.rollback();
                answers.clear();
                return;
            }
            int total = set1.getInt(1);
            System.out.println("you were 'charged' for $" + total + " for your rental. Have a nice day");
            update.executeUpdate();
            conn.commit();
            answers.clear();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                System.out.println("something went wrong. aborting transaction");
                conn.rollback();
            } catch (SQLException e1) {
                System.out.println("db write failed, and so did rollback. thanks oracle");
            }
        }
    }

    public void updateInfo() {
        try (PreparedStatement info_update = conn.prepareStatement(Queries.CUSTOMER_INFO_UPDATE);
             PreparedStatement get_current_info = conn.prepareStatement(Queries.GET_CUSTOMER)) {

            conn.setAutoCommit(false);
            get_current_info.setInt(1, customer_id);
            ResultSet res = get_current_info.executeQuery();
            if (!res.next()) {
                System.out.println("for some reason we think you don't exist. sorry :(");
                return;
            }

            ResultSetMetaData meta = res.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                answers.put(meta.getColumnLabel(i), res.getObject(i));
            }
            answers.remove("ID");
            String fname = (String) confirmPrior("FIRST_NAME", () -> mainRunner.getRegexResponse(
                    "what is your first name (up to 20 characters, no punctuation or spaces)",
                    "^[[a-zA-Z]]{1,20}$"));

            String lNumber = (String) confirmPrior("LICENSE_NUMBER", () -> mainRunner.getRegexResponse(
                    "enter your 9-digit license ID (may contain letters)",
                    "^.{9}$"));

            String lState = (String) confirmPrior("LICENSE_ST", () -> mainRunner.getRegexResponse("what state is on your license? (enter an invalid state and you'll need to enter all of the above again)",
                    "^[a-zA-Z]{2}").toUpperCase());

            String lname = (String) confirmPrior("LAST_NAME", () -> mainRunner.getRegexResponse(
                    "what is your last name (up to 20 characters, no punctuation or spaces)",
                    "^[[a-zA-Z]]{1,20}$"));

            String street_addr = (String) confirmPrior("STREET_ADDR", () -> mainRunner.getRegexResponse(
                    "enter your street address (no city / state / zip) no punctuation",
                    "^[[a-zA-Z]|\\s|\\d]{1,20}$"));

            String appt_number = (String) confirmPrior("APPT_NUMBER", () -> mainRunner.getRegexResponse(
                    "appartment number (just press <enter> if you don't have one)",
                    "^\\d{0,7}$"));

            String city = (String) confirmPrior("CITY", () -> mainRunner.getRegexResponse(
                    "name of your city up to 25 characters, spaces are allowed",
                    "^[[a-zA-Z]|\\s]{1,25}$"));

            String state = (String) confirmPrior("STATE", () -> mainRunner.getRegexResponse("what state do you live in? (enter an invalid state and you'll need to enter all of the above again)",
                    "^[a-zA-Z]{2}").toUpperCase());

            String zip = (String) confirmPrior("ZIP", () -> mainRunner.getRegexResponse(
                    "What is your 5-digit zip code?",
                    "^\\d{5}"));

            info_update.setString(1, fname);
            info_update.setString(2, lNumber);
            info_update.setString(3, lState);
            info_update.setString(4, lname);
            info_update.setString(5, street_addr);
            if (!appt_number.equals("")) {
                info_update.setInt(6, Integer.parseInt(appt_number));
            } else {
                info_update.setNull(6, Types.NUMERIC);
            }
            info_update.setString(7, city);
            info_update.setString(8, state);
            info_update.setString(9, zip);
            info_update.setInt(10, customer_id);
            info_update.executeUpdate();
            System.out.println("info updated successfully!");
            conn.commit();
            answers.clear();
        } catch (SQLIntegrityConstraintViolationException e1) {
            System.out.println("one of the states you entered is not actually a state. try again");
            try {
                conn.rollback();
                System.out.println("couldn't make things work out with the server :(");
            } catch (SQLException e) {
                System.out.println("transaction and rollback failed");
            }
            updateInfo();
        } catch (SQLException ex) {
            try {
                conn.rollback();
                System.out.println("couldn't make things work out with the server :(");
            } catch (SQLException e) {
                System.out.println("transaction and rollback failed");
            }
        }
    }

    public void addOrg() {

    }

}
