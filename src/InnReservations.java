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
            Class.forName("com.mysql.cj.jdbc.Driver");
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
            String createRooms ="CREATE TABLE IF NOT EXISTS lab7_rooms (\n" +
                    "RoomCode char(5) PRIMARY KEY,\n" +
                    "RoomName varchar(30) NOT NULL,\n" +
                    "Beds int(11) NOT NULL,\n" +
                    "bedType varchar(8) NOT NULL,\n" +
                    "maxOcc int(11) NOT NULL,\n" +
                    "basePrice DECIMAL(6,2) NOT NULL,\n" +
                    "decor varchar(20) NOT NULL,\n" +
                    "UNIQUE (RoomName)\n" +
                    ");";
            String createReservations = "CREATE TABLE IF NOT EXISTS lab7_reservations (\n" +
                    "CODE int(11) PRIMARY KEY,\n" +
                    "Room char(5) NOT NULL,\n" +
                    "CheckIn date NOT NULL,\n" +
                    "Checkout date NOT NULL,\n" +
                    "Rate DECIMAL(6,2) NOT NULL,\n" +
                    "LastName varchar(15) NOT NULL,\n" +
                    "FirstName varchar(15) NOT NULL,\n" +
                    "Adults int(11) NOT NULL,\n" +
                    "Kids int(11) NOT NULL,\n" +
                    "FOREIGN KEY (Room) REFERENCES lab7_rooms (RoomCode)\n" +
                    ");";
            String addRoomData = "INSERT INTO lab7_rooms SELECT * FROM INN.rooms;";
            String addReservationData = "INSERT INTO lab7_reservations SELECT CODE, Room,\n" +
                    "DATE_ADD(CheckIn, INTERVAL 134 MONTH),\n" +
                    "DATE_ADD(Checkout, INTERVAL 134 MONTH),\n" +
                    "Rate, LastName, FirstName, Adults, Kids FROM INN.reservations;";
            String addAidenAccess = "grant all on ajone130.lab7_rooms to afjacobs@'%';";
            String addJasonAccess = "grant all on ajone130.lab7_rooms to jrbarba@'%';";
            try (Statement stmt = conn.createStatement()) {

                // Step 4: Send SQL statement to DBMS
                stmt.execute(createRooms);
                stmt.execute(createReservations);
                stmt.execute(addRoomData);
                stmt.execute(addReservationData);
                stmt.execute(addAidenAccess);
                stmt.execute(addJasonAccess);


                // Step 5: Handle results
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
