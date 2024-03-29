/**
 * main class
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class mainRunner {
    public static final Scanner scan = new Scanner(System.in);

    /**
     * @param args
     * @throws SQLException if it can't close the connection by the end
     */
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("could not find required libraries");
            exit(1);
        }
        //oracle class required has been loaded
        //scan = new Scanner(System.in);
        try (Connection connection = login()) {
            String res = (String)getResponse("type 'manager' or 'customer' to begin",
                Arrays.asList("manager", "customer", "generator"), "str", false);
            switch(res){
                case "manager":
                    ManagerView manView = new ManagerView(connection);
                    manView.run();
                    break;
                case "customer":
                    CustomerView custView = new CustomerView(connection);
                    custView.run();
                    break;
                case "generator":
                    rentalGenerator.run(connection);
            }
        }
        scan.close();
    }

    public static Object getResponse(String prompt, List responses, Object sample, boolean list_options) {
        System.out.println(prompt);
        if(responses != null && list_options) {
            for (Object var: responses){
                System.out.println(var);
            }

        }
        Object retval = null;
        if (sample instanceof String) {
            while(retval == null) {
                retval = scan.nextLine();
            }
        } else if (sample instanceof Integer) {
            while(!scan.hasNextInt()){
                System.out.println(prompt);
                scan.nextLine();
            }
            retval = scan.nextInt();
            scan.nextLine();
        } else if (sample instanceof Double) {
            while(!scan.hasNextDouble()){
                System.out.println(prompt);
                scan.nextLine();
            }
            retval = scan.nextDouble();
            scan.nextLine();
        } else if (sample instanceof Boolean){
            while(!scan.hasNextBoolean()){
                System.out.println(prompt);
                scan.nextLine();
            }
            retval = scan.nextBoolean();
            scan.nextLine();
        }
        else {
            System.err.println("what the fuck");
            exit(2);
        }
        if (responses != null && !responses.contains(retval)) {
            System.out.println("invalid response.");
            return getResponse(prompt, responses, sample, list_options);
        }
        return retval;
    }

    public static String getRegexResponse(String prompt, String re) {
        System.out.println(prompt);
        Pattern p = Pattern.compile(re);
        String retval = scan.nextLine();
        Matcher m = p.matcher(retval);
        if(m.matches()){
            return retval;
        }
        System.out.println("your input did not match the specified format (" + re + " if you understand regex)");
        return getRegexResponse(prompt, re);
    }

    public static java.util.Date getDateResponse(){
        System.out.println("enter a date according to the following pattern: dd-MMM-yy, \n" +
            "(i.e. 02-JAN-12 would indicate Jan 2, 2012");
        String userIn = scan.nextLine();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        sdf.setLenient(false);
        try{
            return sdf.parse(userIn);
        }catch (ParseException pE){
            return getDateResponse();
        }

    }
    /**
     * @param q a scroll-insensitive concur_read_only resultSet containing instr. ids and names. should be the one
     *          generated by doSearch so that only ids matching ones in the list returned will work. having this
     *          prevents expensive requests to the SQL server each time to check for an ID
     * @throws SQLException throws when errors occur
     */
    public static void printSet(ResultSet q) throws SQLException {
        if (!q.next()) {
            System.out.println("no data :(");
            return;
        }
        ResultSetMetaData meta = q.getMetaData();
        int[] columnWidths = new int[meta.getColumnCount()];
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            columnWidths[i - 1] = meta.getColumnDisplaySize(i);
            String raw_res = meta.getColumnLabel(i);
            System.out.print(String.format("%-" + (columnWidths[i - 1] + 4) + "s", raw_res.substring(0, Math.min((columnWidths[i-1] + 2), raw_res.length()))));
            //above prints out column headers
        }
        System.out.println();
        do {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String myString = meta.getColumnType(i) != 93 ? q.getString(i) : q.getString(i).substring(0,10);
                //above line is for truncating dates
                System.out.print(String.format("%-" + (columnWidths[i - 1] + 4) + "s", myString));
            }
            System.out.println();
        } while (q.next());
    }

    public static int getLocation(Connection conn, String prompt){
        try(Statement st = conn.createStatement(); PreparedStatement prepSt = conn.prepareStatement(Queries.CITY_FILTER, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){
            ResultSet s = st.executeQuery(Queries.CITY_LIST);
            ArrayList<String> options = new ArrayList<>();
            while(s.next()){
                options.add(s.getString(1).toLowerCase());
            }
            String start_loc_str = (String)mainRunner.getResponse(prompt, options, "string", true);
            System.out.println("starting city is: " + start_loc_str);

            prepSt.setString(1, start_loc_str);
            ResultSet intSet = prepSt.executeQuery();
            ArrayList<Integer> intOpts = new ArrayList<>();
            System.out.println("please input the ID number of one of the below listed locations");
            printSet(intSet);
            intSet.beforeFirst();
            ResultSet set2 = prepSt.executeQuery();
            while(set2.next()){
                intOpts.add(set2.getInt(1));
            }
            return (Integer) mainRunner.getResponse("you must enter one of the IDs above", intOpts, 0, false);
        } catch (SQLException e) {
            e.printStackTrace();
            exit(2);
        }
        return -1;
    }

    /**
     * logs the user into the database
     */
    public static Connection login() throws SQLException {
        System.out.print("Please enter your userName:");
        String name = scan.nextLine();
        System.out.print("Please enter your password:");
        String pass = scan.nextLine();
        try {
            //fairly certain I'm fine not closing here because failed connections return null;
            Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@edgar0.cse.lehigh.edu:1521:cse241",
                    name, pass);
            System.out.println("successfully authenticated and connected to database\n");
            return connection;
        } catch (Exception e) {
            System.out.println("invalid login, try again\n");
            return login();
        }
    }

    /**
     * replaces all single quotes with a double quote.
     *
     * @param s the string to sanitize
     */
    private static String sanitizeString(String s) {
        //replaces all single quotes with 2 single quotes (escaping them) and removes all semicolons
        return s.replaceAll("'", "''").replaceAll(";", "");
    }
}
