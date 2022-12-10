import com.mysql.cj.protocol.Resultset;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class InnReservations {
    public static void main(String[] args) {
        try {
            InnReservations inn = new InnReservations();
            int demoNum = Integer.parseInt(args[0]);
            //inn.setUp();
            inn.FR4();


        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        } catch (Exception e2) {
            System.err.println("Exception: " + e2.getMessage());
        }
    }

    public void setUp() throws SQLException {
        try {
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
            String createRooms = "CREATE TABLE IF NOT EXISTS lab7_rooms (\n" +
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
    public static void FR3() throws SQLException {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter your reservation code: ");
            int code = scanner.nextInt();
            scanner.nextLine();
            Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                    System.getenv("HP_JDBC_USER"),
                    System.getenv("HP_JDBC_PW"));
            PreparedStatement pstmt0 = conn.prepareStatement("SELECT room FROM lab7_reservations where code = ?");
            pstmt0.setInt(1, code);
            ResultSet rs0 = pstmt0.executeQuery();
            if (!rs0.next()){
                System.out.println("The reservation code entered does not exist. Please try again.");
                FR3();
                return;
            }
            Map<String, String> changes =  new HashMap<>();
            while(true){
                System.out.println("Select attribute to change:");
                System.out.println("0 - No change");
                System.out.println("1 - First name");
                System.out.println("2 - Last name");
                System.out.println("3 - Begin date");
                System.out.println("4 - End date");
                System.out.println("5 - Number of adults");
                System.out.println("6 - Number of children");

                //(Code, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids)
                String input = scanner.next();
                scanner.nextLine(); // flush
                if (input.equalsIgnoreCase("0")){
                    break;
                }
                switch (input){
                    case "1":
                        System.out.println("Enter new first name: ");
                        String firstName = scanner.next();
                        changes.put("FirstName", firstName);
                        break;
                    case "2":
                        System.out.println("Enter new last name: ");
                        String lastName = scanner.nextLine();
                        changes.put("LastName", lastName);
                        break;
                    case "3":
                        System.out.println("Enter new begin date: ");
                        String begin = scanner.next();

                        PreparedStatement pstmtA = conn.prepareStatement("SELECT room FROM lab7_reservations where code = ?");
                        pstmtA.setInt(1, code);
                        ResultSet rs = pstmtA.executeQuery();
                        String room = rs.getString("room");

                        PreparedStatement pstmtB = conn.prepareStatement("select * from lab7_reservations where room = ? and ? between checkin and checkout;");
                        pstmtB.setString(1, room);
                        pstmtB.setString(2, begin);
                        ResultSet check = pstmtB.executeQuery();

                        if (check.next()){
                            System.out.println("Update unsuccessful. The entered date conflicts with a current reservation.");
                        }
                        else{
                            changes.put("CheckIn", begin);
                        }
                        break;
                    case "4":
                        System.out.println("Enter new end date: ");
                        String end = scanner.next();

                        PreparedStatement pstmt1A = conn.prepareStatement("SELECT room FROM lab7_reservations where code = ?");
                        pstmt1A.setInt(1, code);
                        ResultSet rs1 = pstmt1A.executeQuery();
                        rs1.next();
                        String room1 = rs1.getString("room");

                        PreparedStatement pstmt1B = conn.prepareStatement("select * from lab7_reservations where room = ? and ? between checkin and checkout;");
                        pstmt1B.setString(1, room1);
                        pstmt1B.setString(2, end);
                        ResultSet check1 = pstmt1B.executeQuery();
                        if (check1.next()){
                            System.out.println("Update unsuccessful. The entered date conflicts with a current reservation.");
                        }
                        else{
                            changes.put("CheckOut", end);
                        }
                        break;

                    case "5":
                        System.out.println("Enter number of adults: ");
                        String adults = scanner.next();
                        changes.put("Adults", adults);
                        break;
                    case "6":
                        System.out.println("Enter number of kids: ");
                        String kids = scanner.next();
                        changes.put("Kids", kids);
                        break;
                }
            }
            if(changes.containsKey("FirstName")) {
                try(PreparedStatement pstmt = conn.prepareStatement("update lab7_reservations set firstname=? where code = ?;")){
                    pstmt.setString(1,changes.get("FirstName"));
                    pstmt.setInt(2, code);
                    pstmt.executeUpdate();
                    //
                    System.out.println("FirstName successfully updated.");
                } catch (SQLException e){
                    System.out.println("\nError updating FirstName");
                    System.out.println("\nPlease try again.\n");
                    e.printStackTrace();
                    conn.rollback();
                }
            }
            if(changes.containsKey("LastName")) {
                try(PreparedStatement pstmt = conn.prepareStatement("update lab7_reservations set LastName=? where code = ?;")){
                    pstmt.setString(1,changes.get("LastName"));
                    pstmt.setInt(2, code);
                    pstmt.executeUpdate();

                    System.out.println("LastName successfully updated.");
                } catch (SQLException e){
                    System.out.println("\nError updating LastName");
                    System.out.println("\nPlease try again.\n");
                    e.printStackTrace();
                    conn.rollback();
                }
            }
            if(changes.containsKey("Adults")) {
                try(PreparedStatement pstmt = conn.prepareStatement("update lab7_reservations set Adults=? where code = ?;")){
                    pstmt.setInt(1,Integer.parseInt(changes.get("Adults")));
                    pstmt.setInt(2, code);
                    pstmt.executeUpdate();

                    System.out.println("Adults successfully updated.");
                } catch (SQLException e){
                    System.out.println("\nError updating Adults");
                    System.out.println("\nPlease try again.\n");
                    e.printStackTrace();
                    conn.rollback();
                }
            }
            if(changes.containsKey("Kids")) {
                try(PreparedStatement pstmt = conn.prepareStatement("update lab7_reservations set Kids=? where code = ?;")){
                    pstmt.setInt(1, Integer.parseInt(changes.get("Kids")));
                    pstmt.setInt(2, code);
                    pstmt.executeUpdate();

                    System.out.println("Kids successfully updated.");
                } catch (SQLException e){
                    System.out.println("\nError updating Kids");
                    System.out.println("\nPlease try again.\n");
                    e.printStackTrace();
                    conn.rollback();
                }
            }

            if(changes.containsKey("CheckIn")) {
                try(PreparedStatement pstmt = conn.prepareStatement("update lab7_reservations set CheckIn=? where code = ?;")){
                    pstmt.setDate(1, Date.valueOf(changes.get("CheckIn")));
                    pstmt.setInt(2, code);
                    pstmt.executeUpdate();

                    System.out.println("CheckIn successfully updated.");
                } catch (SQLException e){
                    System.out.println("\nError updating CheckIn");
                    System.out.println("\nPlease try again.\n");
                    e.printStackTrace();
                    conn.rollback();
                }
            }
            if(changes.containsKey("CheckOut")) {
                try(PreparedStatement pstmt = conn.prepareStatement("update lab7_reservations set Checkout=? where code = ?;")){
                    pstmt.setDate(1, Date.valueOf(changes.get("CheckOut")));
                    pstmt.setInt(2, code);
                    pstmt.executeUpdate();

                    System.out.println("Checkout successfully updated.");
                } catch (SQLException e){
                    System.out.println("\nError updating CheckOut");
                    System.out.println("\nPlease try again.\n");
                    e.printStackTrace();
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }
    public void FR4() throws SQLException{
        try {
            Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                    System.getenv("HP_JDBC_USER"),
                    System.getenv("HP_JDBC_PW"));
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter your reservation code: ");
            int code = scanner.nextInt();
            scanner.nextLine();
            PreparedStatement pstmt0 = conn.prepareStatement("SELECT room FROM lab7_reservations where code = ?");
            pstmt0.setInt(1, code);
            ResultSet rs0 = pstmt0.executeQuery();
            if (!rs0.next()){
                System.out.println("The reservation code entered does not exist. Please try again.");
                FR4();
                return;
            }
            System.out.println("Are you sure you want to cancel your reservation? (Y/N): ");
            String confirmation = scanner.nextLine();
            if (confirmation.equals("Y") || confirmation.equals("y")) {
                try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM lab7_reservations WHERE Code = ?"))
                {
                    preparedStatement.setInt(1, code);
                    int outcome = preparedStatement.executeUpdate();
                    if (outcome == 1) {
                        System.out.println("Your reservation has been cancelled.");
                    }
                    else{
                        System.out.println("The reservation entered either does not exist or could not be found. Please try again.\n");
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                    conn.rollback();
                }
            }
            else{
                System.out.println("Cancellation terminated.");
            }
        }catch (Exception e){
            e.getMessage();
        }
    }
}
