import java.sql.*;

import static java.lang.System.exit;

/**
 * Created by schulace on 4/10/17.
 */
public class testing {
    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("could not find required libraries");
            exit(1);
        }
        System.out.println("hello tehre");
    }


    public static void test2(Connection conn) throws SQLException {
        //oracle class required has been loaded
        try (Connection connection = mainRunner.login(); Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            PreparedStatement statement = connection.prepareStatement(Queries.AVAILABLE_CARS);
            int i = 0;
            statement.setString(1, "22-JAN-01");
            statement.setInt(2, 1);
            statement.setString(3, "22-JAN-01");
            statement.setInt(4, 1);
            statement.setInt(5, 1);
            statement.setString(6, "31-JAN-01");
            statement.setString(7, "22-JAN-01");
            statement.setString(8, "22-JAN-01");
            statement.setString(9, "31-JAN-01");
            statement.setString(10, "31-JAN-01");
            statement.setInt(11, 16);

            ResultSet set = statement.executeQuery();

            mainRunner.printSet(set);
        }

    }

    public static void regexTest(){
        System.out.println(Integer.parseInt(mainRunner.getRegexResponse("test", "^\\d{0,9}")));
        System.out.println("hello world");
    }

    public static void whatever(Connection conn) {

        int customer_id = 0;
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
            e.printStackTrace();
        }

        System.out.println(customer_id);
    }

}
