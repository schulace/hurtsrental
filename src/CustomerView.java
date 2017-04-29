import java.sql.*;
import java.sql.Date;
import java.util.*;

import static java.lang.System.exit;

/**
 * Created by schulace on 4/24/17.
 */
public class CustomerView extends myView {

    private int customer_id;

    public CustomerView(Connection conn) throws Exception {
        super(conn);
        customer_login(conn);
    }

    private void customer_login(Connection conn) throws Exception{
        System.out.println("please log in:");
        String real_login = (String) mainRunner.getResponse(
                "for testing purposes, enter 'test' to get a random customer ID" +
                        ", 'login' to log in as a customer, or 'create' to make a new account", new ArrayList<String>()
                {{
                    add("test");
                    add("login");
                    add("create");
                }},  "hell", false); //ugly, but 1-line

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
                            exit(1);
                        }
                    } catch (SQLException e) {
                        throw new Exception("lol");
                    }
                    break;
                case "login":
                    while(true) {
                        int uid = (Integer) mainRunner.getResponse("please enter your customer ID or -1 to exit", null,
                                7, false);
                        if(uid == -1){
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
                    try(PreparedStatement stmnt = conn.prepareStatement(appt_number.equals("") ? Queries.CUSTOMER_ADD: Queries.CUSTOMER_ADD_APPT)){
                        stmnt.setString(1, fName);
                        stmnt.setString(2, lname);
                        stmnt.setString(3, street_addr);
                        stmnt.setString(4, city);
                        stmnt.setString(5, state);
                        stmnt.setString(6, lNumber);
                        stmnt.setString(7, lState);
                        stmnt.setInt(8, zip);
                        if(!appt_number.equals("")) {
                            stmnt.setInt(9, Integer.parseInt(appt_number));
                        }
                        stmnt.executeUpdate();
                    } catch (SQLIntegrityConstraintViolationException e){
                        System.out.println("Database integrity constraint violated (one of the state abbreviations was inputted incorrectly, exiting");
                        throw new Exception("im lazy AF");
                    } catch(SQLException e){
                        e.printStackTrace(); //TODO you fucked up on the state bro
                        exit(2);
                    }

                    try(Statement stmnt = conn.createStatement()){
                        ResultSet s = stmnt.executeQuery(Queries.MOST_RECENT_CUST);
                        s.next(); //now in first row (should be only row)
                        customer_id = s.getInt(1);
                        System.out.println("your unique customer ID is " + customer_id + ". Remember it, because Hurts Rent-a-Lemon doesn't support account recovery!");
                    }

                default:
                    customer_id = 0;
                    break;
            }

    }


    @Override
    void run() {
        options.put("reserve car", () -> reserveCar(this.conn));
        options.put("pickup car", () -> pickupCar(this.conn));
        looper("customer");
    }

    private void reserveCar(Connection conn) {
        try(PreparedStatement availableCars = conn.prepareStatement(Queries.AVAILABLE_CARS)){
            //get pickup location
            confirmPrior("pickup location", () -> mainRunner.getLocation(conn, "where would you like to pick up the car from?"));
            int pickup_loc = (Integer) answers.get("pickup location");

            confirmPrior("dropoff location", () -> mainRunner.getLocation(conn, "where would you like to drop off the car?"));
            int dropoff_loc = (Integer) answers.get("dropoff location");


            //get start date
            confirmPrior("pickup date", () ->
                {System.out.println("when would you like to pick up the car?");
                return mainRunner.getDateResponse();});
            java.util.Date pickup_date = (java.util.Date)answers.get("pickup date");


            //get end date
            confirmPrior("dropoff date", () ->
                {System.out.println("when will you be returning the vehicle?");
                return mainRunner.getDateResponse();});
            java.util.Date dropoff_date = (java.util.Date)answers.get("dropoff date");

            if(dropoff_date.compareTo(pickup_date) <= 0){
                System.out.println("you can't have a rental ending before it starts. try again");
                reserveCar(conn);
                return;
            }

            availableCars.setDate(1, new java.sql.Date(pickup_date.getTime()));
            availableCars.setInt(2, pickup_loc);
            availableCars.setDate(3, new java.sql.Date(pickup_date.getTime()));
            availableCars.setInt(4, pickup_loc);
            availableCars.setInt(5, pickup_loc);
            availableCars.setDate(6, new java.sql.Date(pickup_date.getTime()));
            availableCars.setDate(7, new java.sql.Date(pickup_date.getTime()));
            availableCars.setDate(8, new java.sql.Date(pickup_date.getTime()));
            availableCars.setDate(9, new java.sql.Date(pickup_date.getTime()));
            availableCars.setDate(10, new java.sql.Date(pickup_date.getTime()));
            availableCars.setInt(11, dropoff_loc);

            ResultSet set = availableCars.executeQuery();
            mainRunner.printSet(set);

        } catch (Exception e){
            reserveCar(conn);
            return;
        }
        answers.clear();
    }

    private void pickupCar(Connection conn){

    }

}
