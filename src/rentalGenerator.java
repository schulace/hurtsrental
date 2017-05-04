import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static java.lang.System.exit;

/**
 * Created by schulace on 5/1/17.
 */
public class rentalGenerator {

    public static void run(Connection connection) {
        try(Statement st1 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ){
            ResultSet set = st1.executeQuery("select cust_id from customer sample(5)");
            ArrayList<Integer> temp = new ArrayList<>();
            System.out.println("about to insert into arrlist");
            while(set.next()){
                temp.add(set.getInt(1));
            }
            Random r = new Random();
            connection.setAutoCommit(false);
            for(Integer cust_id:temp)
            {
                //this only half works. the actual interface does it fine though.
                try(PreparedStatement stmnt = connection.prepareStatement(Queries.AVAILABLE_CARS, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    PreparedStatement insert = connection.prepareStatement(Queries.RENT)) {
                        LocalDate start = LocalDate.of(2001, r.nextInt(10) + 1, r.nextInt(27) + 1);
                        LocalDate end = LocalDate.MIN;
                        while (end.compareTo(start) <= 0) {
                            end = LocalDate.of(2001, r.nextInt(10) + 1, r.nextInt(27) + 1);
                        }
                        int startloc = r.nextInt(40) + 1;
                        int endloc = r.nextInt(40) + 1;

                        stmnt.setDate(1, new java.sql.Date(Date.valueOf(start).getTime()));
                        stmnt.setInt(2, startloc);
                        stmnt.setDate(3, new java.sql.Date(Date.valueOf(start).getTime()));
                        stmnt.setInt(4, startloc);
                        stmnt.setInt(5, startloc);
                        stmnt.setDate(6, new java.sql.Date(Date.valueOf(end).getTime()));
                        stmnt.setDate(7, new java.sql.Date(Date.valueOf(start).getTime()));
                        stmnt.setDate(8, new java.sql.Date(Date.valueOf(start).getTime()));
                        stmnt.setDate(9, new java.sql.Date(Date.valueOf(end).getTime()));
                        stmnt.setDate(10,new java.sql.Date(Date.valueOf(end).getTime()));
                        stmnt.setInt(11, endloc);
                        ResultSet set2 = stmnt.executeQuery();

                        if(!set2.next()){
                            System.out.println("continued");
                            continue;
                        }
                        int car = set2.getInt(1);
                        insert.setDate(1, new java.sql.Date(Date.valueOf(start).getTime()));
                        insert.setDate(2, new java.sql.Date(Date.valueOf(end).getTime()));
                        insert.setInt(3, cust_id);
                        insert.setInt(4, car);
                        insert.setInt(5, 10);
                        insert.setInt(6, startloc);
                        insert.setInt(7, endloc);
                        System.out.printf("given car %d, startingD %s, endingD %s, starting at %d, ending at %d\n", car, start.toString(), end.toString(), startloc, endloc);

                        insert.executeUpdate();
                        System.out.println("updated 'yay'?");
                        connection.commit();
                    }
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
