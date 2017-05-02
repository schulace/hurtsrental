import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by schulace on 4/17/17.
 */
public class ManagerView extends myView {

    public ManagerView(Connection conn){
        super(conn);
    }

    @Override
    void run() {
        options.put("show inventory", () -> showInv(conn));
        options.put("show rentals", () -> showRentals(conn));
        options.put("add car type", () -> addCarType(conn));
        looper("manager");
    }




    private void showInv(Connection conn){
        try (Statement s = getStatement(conn)){
            ResultSet set = s.executeQuery(Queries.MANAGER_INV_VIEW);
            mainRunner.printSet(set);
        } catch (SQLException e) {
            System.out.println("something went wrong. oops");
        }
    }
    private void showRentals(Connection conn)
    {
        try (Statement s = getStatement(conn)){
            ResultSet set = s.executeQuery("select * from RENTAL"); //TODO flesh this out
            mainRunner.printSet(set);
        }
        catch(SQLException ex) {
            System.out.println("something went wrong. oops");
        }
    }

    private void addCarType(Connection conn){

    }

}
