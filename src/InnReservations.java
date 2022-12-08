import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class InnReservations {
    public static void main(String[] args) {
        try {
            InnReservations inn = new InnReservations();
            int demoNum = Integer.parseInt(args[0]);
            inn.setUp();


        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        } catch (Exception e2) {
            System.err.println("Exception: " + e2.getMessage());
        }
    }

    public void setUp() throws SQLException {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded");
        } catch (ClassNotFoundException ex) {
            System.err.println("Unable to load JDBC Driver");
            System.exit(-1);
        }

        // Step 1: Establish connection to RDBMS
        try {
            Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                    System.getenv("HP_JDBC_USER"),
                    System.getenv("HP_JDBC_PW"));
            System.out.println("Connection made");
            conn.prepareStatement("grant all on ajone130.lab7_rooms to jrbarba@'%';");
            conn.prepareStatement("grant all on ajone130.lab7_rooms to afjacobs@'%';");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
