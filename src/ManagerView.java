
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by schulace on 4/17/17.
 */
public class ManagerView extends myView {

    public ManagerView(Connection conn){
        super(conn);
    }

    @Override
    void run() {
        options.put("show inventory", () -> showInv());
        options.put("show rentals", () -> showRentals());
        options.put("add bulk car type", () -> bulkAddCarType());
        options.put("add car type", () -> addCarType());
        options.put("add new car", () -> addCar());
        options.put("get revenue", () ->revenue());
        options.put("add organization", () ->addOrg());
        options.put("remove organization", () ->removeOrg());
        options.put("remove car", () ->removeCar());
        options.put("list organizations", () ->viewOrgs());
        looper("manager");
    }




    private void showInv(){
        try (Statement s = getStatement(conn)){
            ResultSet set = s.executeQuery(Queries.MANAGER_INV_VIEW);
            mainRunner.printSet(set);
        } catch (SQLException e) {
            System.out.println("something went wrong. oops");
        }
    }

    private void showRentals() {
        try (Statement s = getStatement(conn)){
            ResultSet set = s.executeQuery(Queries.LIST_ALL_RENTALS_PAST);
            System.out.println("completed rentals");
            mainRunner.printSet(set);
            System.out.println("---------------------------------------------------");

            ResultSet set2 = s.executeQuery(Queries.LIST_ALL_RENTALS_CURRENT);
            System.out.println("current rentals");
            mainRunner.printSet(set2);
            System.out.println("---------------------------------------------------");

            ResultSet set3 = s.executeQuery(Queries.LIST_ALL_RENTALS_RESERVED);
            System.out.println("reservations");
            mainRunner.printSet(set3);
            System.out.println("---------------------------------------------------");
        }
        catch(SQLException ex) {
            System.out.println("something went wrong. oops");
        }
    }

    private void bulkAddCarType(){
        try(PreparedStatement iCar = conn.prepareStatement(Queries.CREATE_CAR_TYPE)){
            conn.setAutoCommit(false);
            String make = (String) confirmPrior("car make", () -> mainRunner.getResponse("what is the make of this car?", null, "st", false));
            String model = (String) confirmPrior("car model", () -> mainRunner.getResponse("what is the model of this car?", null, "s", false));
            String type = (String) confirmPrior("type", () -> mainRunner.getResponse("What is the type of this car?", new ArrayList<String>(){{
                add("SUV");
                add("minicar");
                add("subcompact");
                add("compact");
                add("fullsize");
                add("sports car");
                add("minivan");
                add("truck");
                add("other");
            }}, "s", true));
            double rate = (Double) confirmPrior("rate", () -> mainRunner.getResponse("what is the daily cost for this car?", null, 0.3D, false));
            int fuel_cap = (Integer) confirmPrior("fuel capacity", () -> mainRunner.getResponse("what is the  fuel capacity for this car?", null, 3, false));
            int yearStart = (Integer) confirmPrior("start year", () -> mainRunner.getResponse("waht is the year this car started to be made", null, 3, false));
            int yearEnd =  (Integer) confirmPrior("end year", () -> mainRunner.getResponse("waht is the year this car stopped being made", null, 3, false));
            iCar.setString(1, make);
            iCar.setString(2, model);
            iCar.setString(3, type);
            iCar.setDouble(4, rate);
            iCar.setInt(6, fuel_cap);
            while(yearStart <= yearEnd){
                iCar.setInt(5, yearStart);
                try {
                    iCar.executeUpdate();
                    conn.commit();
                } catch (SQLIntegrityConstraintViolationException e){
                    System.out.println("car already in db, stopping insertion, got up to year " + yearStart);
                    break;
                }
            }
        }
        catch (SQLException e){
            System.out.println("type already exists in database");
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("not allowed to change state of autocommit. thanks oracle");
            }
        }
    }

    private void addCarType(){
        try(PreparedStatement statement = conn.prepareStatement(Queries.CREATE_CAR_TYPE)){
            conn.setAutoCommit(false);
            String make = (String) confirmPrior("car make", () -> mainRunner.getResponse("what is the make of this car?", null, "st", false));
            String model = (String) confirmPrior("car model", () -> mainRunner.getResponse("what is the model of this car?", null, "s", false));
            String type = (String) confirmPrior("type", () -> mainRunner.getResponse("What is the type of this car?", new ArrayList<String>(){{
                add("SUV");
                add("minicar");
                add("subcompact");
                add("compact");
                add("fullsize");
                add("sports car");
                add("minivan");
                add("truck");
                add("other");
            }}, "s", true));
            double rate = (Double) confirmPrior("rate", () -> mainRunner.getResponse("what is the daily cost for this car?", null, 0.3D, false));
            int fuel_cap = (Integer) confirmPrior("fuel capacity", () -> mainRunner.getResponse("what is the  fuel capacity for this car?", null, 3, false));
            int year = (Integer) confirmPrior("year", () -> mainRunner.getResponse("what is the year this car was made", null, 3, false));
            statement.setString(1, make);
            statement.setString(2, model);
            statement.setString(3, type);
            statement.setDouble(4, rate);
            statement.setInt(5, year);
            statement.setInt(6, fuel_cap);
            statement.executeUpdate();
            conn.commit();
            System.out.println("successfully added type");
        } catch (SQLIntegrityConstraintViolationException ex){
            System.out.println("already exists, aborting");
        } catch (SQLException e){
            System.out.println("problem occurred talking to database");
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addCar(){
        try(PreparedStatement listTypes = conn.prepareStatement(Queries.GET_VEHI_TYPES);
            PreparedStatement insCar = conn.prepareStatement(Queries.CAR_ADD)){
            conn.setAutoCommit(false);
            System.out.println("You will first need to select which type this car is of");
            System.out.println("for the following, enter part of the name or just press <enter> to get all matches");
            String make = (String) confirmPrior("make", () -> mainRunner.getRegexResponse("what is the make of this car?", "^\\w*$"));
            String model = (String) confirmPrior("model", () -> mainRunner.getResponse("what is the model of this car?", null, "s", false));
            String type = (String) confirmPrior("type", () -> mainRunner.getResponse("What is the type of this car?", new ArrayList<String>(){{
                add("SUV");
                add("minicar");
                add("subcompact");
                add("compact");
                add("fullsize");
                add("sports car");
                add("minivan");
                add("truck");
                add("other");
            }}, "s", false));
            System.out.println("enter a year, or 0 to get all years");
            int year = (Integer) confirmPrior("year", () -> mainRunner.getResponse("what is the year this car was made", null, 3, false));
            listTypes.setString(1, "%" + make + "%");
            listTypes.setString(2, "%" + model + "%");
            listTypes.setString(3, "%" + type + "%");
            listTypes.setInt(4, year);
            listTypes.setInt(5, year);
            ResultSet s = listTypes.executeQuery();
            mainRunner.printSet(s);
            ResultSet q = listTypes.executeQuery();
            ArrayList<Integer> options = new ArrayList<Integer>();
            if(!q.next()){
                System.out.println("no car types match your query, exiting. (you can add a car type afterwards)");
                return;
            }
            do{
                options.add(q.getInt(1));
            } while(q.next());
            int id = (Integer) mainRunner.getResponse("select an ID from the above for the car you'd like to insert", options, 4, false);
            String license = (String) confirmPrior("license plate", () -> mainRunner.getRegexResponse("enter the license plate of the car (7 characters)", "\\w{7}"));
            String state = (String) confirmPrior("car state", () -> mainRunner.getRegexResponse("enter the 2-letter uppercase abbreviation for the car's state (e.g. PA)", "[A-Z]{2}"));
            insCar.setString(1, state);
            insCar.setString(2, license);
            insCar.setInt(3, id);
            insCar.executeQuery();
            conn.commit();
            System.out.println("successfully added a car");

            System.out.println();
            switch ((String)mainRunner.getResponse("to add another car of this type, enter 'continue', just press <enter> to exit", new ArrayList<String>(){{
                add("continue");
                add("");
            }}, "st", false)){
                case "continue":
                    addCar();
                    return;
            }
            answers.clear();
        } catch (SQLIntegrityConstraintViolationException ex){
                System.out.println("this car already exists in the database");
        }
        catch (SQLException e){
            System.out.println("something went wrong. aborting transaction");
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeCar(){
        try(PreparedStatement bye = conn.prepareStatement(Queries.CAR_REMOVE)){
            System.out.println("there are a lot of cars in the database. would you like to list them out?");
            if((Boolean)mainRunner.getResponse("enter true or false", new ArrayList<Boolean>(){{add(true);add(false);}},false, false)){
                showInv();
            }
            int id = (Integer) confirmPrior("id to delete", () -> mainRunner.getResponse("enter the ID of the car to be deleted", null, 4, false));
            bye.setInt(1, id);
            if(bye.executeUpdate() == 0){
                System.out.println("no such car exists. nothing was deleted");
                return;
            }
            answers.clear();
        } catch (SQLException e){
            System.out.println("something unexpected happened");
            e.printStackTrace();
        }
    }

    private void revenue(){
        try(PreparedStatement stmnt = conn.prepareStatement(Queries.REVENUE)){
            System.out.println("enter a start date");
            java.sql.Date startDate = (java.sql.Date)confirmPrior("start date", () -> new java.sql.Date(mainRunner.getDateResponse().getTime()));
            System.out.println("enter an end date");
            java.sql.Date endDate = (java.sql.Date)confirmPrior("end date", () -> new java.sql.Date(mainRunner.getDateResponse().getTime()));
            stmnt.setDate(1, startDate);
            stmnt.setDate(2, endDate);
            ResultSet set = stmnt.executeQuery();
            mainRunner.printSet(set);
            answers.clear();
        } catch (SQLException ex) {
            System.out.println("couldn't connect to server properly");
        }
    }

    private void addOrg(){
        try(PreparedStatement org_add = conn.prepareStatement(Queries.ADD_ORG)){
            conn.setAutoCommit(true);
            String org_name = (String) confirmPrior("organization name", () -> mainRunner.getRegexResponse("what is the name of your org?", "^[A-Za-z\\s\\d]{0,40}$"));
            String code = (String) confirmPrior("code", () -> mainRunner.getRegexResponse("discount code? (no spaces)", "^[\\w]{0,20}"));
            double disct_pct = (Double) confirmPrior("discount percent", () -> mainRunner.getResponse("what is the discount % (as decimal < 1)", null, 0.4D, false));
            org_add.setString(1, org_name);
            org_add.setString(2, code);
            org_add.setDouble(3, disct_pct);
            org_add.executeUpdate();
            System.out.println("organization successfully added");
        } catch (SQLException e){
            System.out.println("couldn't properly communicate with the server. sorry :(");
        }
    }

    private void removeOrg(){
        try(PreparedStatement org_del = conn.prepareStatement(Queries.REMOVE_ORG);
            PreparedStatement org_l = conn.prepareStatement(Queries.ORG_LIST)) {

            ResultSet r1 = org_l.executeQuery();
            ArrayList<String> resps = new ArrayList<String>();
            while(r1.next()){
                resps.add(r1.getString(1).toLowerCase());
            }

            String org_name = (String) confirmPrior("organization name", () -> mainRunner.getResponse("what's the org name you're removing?", resps, "st", true));
            org_del.setString(1, org_name);
            org_del.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    private void viewOrgs(){
        try(PreparedStatement st = conn.prepareStatement(Queries.VIEW_ORGS)){
            ResultSet set = st.executeQuery();
            mainRunner.printSet(set);
        } catch (SQLException e){
            System.out.println("couldn't properly communicate with the database");
        }
    }
}
