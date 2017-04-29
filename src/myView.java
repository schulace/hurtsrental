import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by schulace on 4/26/17.
 */
public abstract class myView {
    public boolean leave = false;
    public HashMap<String, Runnable> options;
    public Connection conn;
    public HashMap<String, Object> answers;

    public myView(Connection conn)
    {
        this.conn = conn;
        this.options = new HashMap<String, Runnable>();
        options.put("exit", () -> leave = true);
        this.answers = new HashMap<String, Object>();
    }

    public Statement getStatement(Connection conn)
    {
        try{
            return conn.createStatement();
        } catch (SQLException e){
            System.out.println("there was an error connecting to the database (check your connection)");
            return null;
        }
    }

    abstract void run();

    public void looper(String viewName)
    {
        StringBuilder builder = new StringBuilder(100);
        builder.append("\nyou may enter one of the following\n");
        for(String key : options.keySet()) {
            builder.append(key).append('\n');
        }
        System.out.println("welcome to the " + viewName + " view");
        while (!leave) {
            Runnable r = options.get((String) mainRunner.getResponse(builder.toString(), new ArrayList<String>(options.keySet()), "string", false));
            if (r != null) {
                r.run();
            }
            if (leave) {
                leave = false;
                break;
            }
        }

    }

    /**
     *
     * this method tries to use some new java 8 features of callables. basically, the
     * idea is that responses are stored in a hash map (answers), and in the event that
     * some exception is thrown, we can recover the customer's answers and ask if they
     * want to re-input or use the old value
     *
     * @param option which option to set
     * @param r the callable to execute
     */
    public void confirmPrior(String option, Callable<Object> r){
        Object s = answers.get(option);
        if(s != null) {
            String res = (String) mainRunner.getResponse(
                    "you had previously entered " + s + " as " + option + ". enter y to reuse, or n to input it again",
                    new ArrayList<String>() {{
                        add("y");
                        add("n");
                    }},
                    "string",
                    false
            );
            if (res.equals("y")) {
                return;
            }
        }
        try {
            answers.put(option, r.call());
        }
        catch(Exception e){
            System.err.println("java needs better functional methods");
        }
    }
}
