import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.Scanner;

import static java.time.temporal.ChronoUnit.DAYS;

public class InnReservations {
    private BufferedReader cnsl = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        try {
            InnReservations inn = new InnReservations();
            inn.optionSelect();
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

    public void optionSelect(){
        String command;

        try{
            Scanner scanner = new Scanner(System.in);
            this.printOptions();
            System.out.print("Input Command: ");

            while(scanner.hasNext())
            {
                String option_selected = scanner.next();
                option_selected = option_selected.replaceAll("\\s", "");

                if(option_selected.equals("1")){
                    System.out.println("\n1...");
                    this.fr1();
                    System.out.println();
                    this.printOptions();
                }
                else if(option_selected.equals("2")){
                    System.out.println("\n2...");
                    this.fr2();
                    System.out.println();
                    this.printOptions();
                }
                else if(option_selected.equals("3")){
                    System.out.println("\n3...");
                    System.out.println("Please add function");
                    System.out.println();
                    this.printOptions();
                }
                else if(option_selected.equals("4")){
                    System.out.println("\n4...");
                    System.out.println("Please add function");
                    System.out.println();
                    this.printOptions();
                }
                else if(option_selected.equals("5")){
                    System.out.println("\n5...");
                    System.out.println("Please add function");
                    System.out.println();
                    this.printOptions();
                }
                else if(option_selected.equals("M")||option_selected.equals("m")){
                    System.out.println("\nM...");
                    this.printOptions();
                }
                else if(option_selected.equals("0")){
                    System.out.println("\n0...");
                    System.out.println("\nExiting...");
                    System.out.println();
                    return;
                }

                //printOptions();
                System.out.println("Input Command: ");
            }
        }
        catch (InputMismatchException | SQLException | IOException ignored){
        }
    }

    public void printOptions(){
        System.out.println("\nMain Menu");
        System.out.println("[1]Rooms and Rates");
        System.out.println("[2]Book Resrvations");
        System.out.println("[3]Change Resrvations");
        System.out.println("[4]Cancel Resrvations");
        System.out.println("[5]Revenue Summary");
        System.out.println("[M]ain Menu");
        System.out.println("[0]Exit\n");
    }


    public void fr1() throws SQLException {
        Connection conn = this.createConnect();
        String roomCode = "";
        String roomName = "";
        int beds = 0;
        String bedType = "";
        int maxOcc = 0;
        float basePrice = 0;
        String decor = "";
        String NextAvailableCheckIn = "";
        double Popularity = 0.0;
        int lastLength = 0;
        String sql = """
                with popularity as (select room, count(room)/180 as p from lab7_reservations\s
                where DATE_ADD(CURDATE(), interval -180 day) between checkin AND CURDATE()
                group by room)

                select rooms.*, p, r3.lastLength as lastStayLength, r4.checkout as nextCheckInDate from lab7_rooms as rooms
                join popularity on room = roomcode
                join (select distinct r1.room, datediff(r2.checkout, r2.checkin) as lastLength \s
                from lab7_reservations r1 join lab7_reservations r2\s
                on r1.room = r2.room
                where datediff(curdate(), r2.checkout) = (select min(datediff(curdate(), checkout))
                from lab7_reservations where room = r1.room)) as r3
                on r3.room = roomcode
                join (select distinct r1.room , r2.checkout
                from lab7_reservations r1 join lab7_reservations r2\s
                on r1.room = r2.room where\s
                r2.checkout = (select max(checkout)
                from lab7_reservations where room = r2.room)) as r4
                on r4.room = roomcode
                order by p desc""";

        try(PreparedStatement prep_stm = conn.prepareStatement(sql))
        {
            try(ResultSet res_set = prep_stm.executeQuery())
            {

                //Output of Statement.
                System.out.format("\n|%-10s |%-25s |%-10s |%-10s |%-10s |%-10s |%-15s |%-25s |%-25s |%-20s\n", "RoomId", "roomName", "beds", "bedType", "maxOcc", "basePrice", "decor", "NextAvailableCheckIn", "lastLength", "Populariy");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------");

                while(res_set.next())
                {
                    roomCode = res_set.getString("roomcode");
                    roomName = res_set.getString("roomName");
                    beds = res_set.getInt("beds");
                    bedType = res_set.getString("bedType");
                    maxOcc = res_set.getInt("maxOcc");
                    basePrice = res_set.getFloat("basePrice");
                    decor = res_set.getString("decor");
                    NextAvailableCheckIn = res_set.getString("nextCheckInDate");
                    lastLength = res_set.getInt("laststaylength");
                    Popularity = res_set.getDouble("p");

                    if(NextAvailableCheckIn == null) { NextAvailableCheckIn = "Today"; }


                    System.out.format("|%-10s |%-25s |%-10s |%-10s |%-10s |%-10s |%-15s |%-25s |%-25s |%-20s\n", roomCode , roomName, beds, bedType, maxOcc, basePrice, decor, NextAvailableCheckIn, lastLength, Popularity);
                }
            }
        }
    }

    public void fr2() throws SQLException, IOException {
        Connection conn = this.createConnect();
        String firstName = "";
        String lastName = "";
        String roomCode = "";
        String startDate = "";
        String endDate = "";
        String childCount = "";
        String adultCount = "";
        String bedType = "";
        String roomName = "";

        int reserv_code = 0;
        double rate = 0.0;
        int room_occup = 0;


        try {
            System.out.print("First Name: ");
            firstName = cnsl.readLine();
            System.out.print("Last Name: ");
            lastName = cnsl.readLine();
            System.out.println("| Room Code | Room Name");
            System.out.println("| AOB       | Abscond or bolster");
            System.out.println("| CAS       | Convoke and sanguine");
            System.out.println("| FNA       | Frugal not apropos");
            System.out.println("| HBB       | Harbinger but bequest");
            System.out.println("| IBD       | Immutable before decorum");
            System.out.println("| IBS       | Interim but salutary");
            System.out.println("| MWC       | Mendicant with cryptic");
            System.out.println("| RND       | Recluse and defiance");
            System.out.println("| RTE       | Riddle to exculpate");
            System.out.println("| TAA       | Thrift and accolade");
            System.out.println("| Any       | No preference            | ");
            System.out.print("Desired Room Code: ");
            roomCode = cnsl.readLine();
            System.out.println("| King |");
            System.out.println("| Queen |");
            System.out.println("| Double |");
            System.out.print("Bed Type: ");
            bedType = cnsl.readLine();
            System.out.println("Reservations between");
            System.out.print("Start Date [YYYY-MM-DD]: ");
            startDate = cnsl.readLine();
            System.out.print("End Date [YYYY-MM-DD]: ");
            endDate = cnsl.readLine();
            System.out.print("Number of Children: ");
            childCount = cnsl.readLine();
            System.out.print("Number of Adults: ");
            adultCount = cnsl.readLine();
        } catch (Exception e) {
            System.out.println("\n" + e);
        }
        int maxOcc = Integer.parseInt(childCount) + Integer.parseInt(adultCount);
        boolean roomFound = false;
        while (!roomFound) {
            if (roomCode.equalsIgnoreCase("any")) {
                PreparedStatement preparedStatement = conn.prepareStatement("""
                        with Unavailable as (
                        select distinct room from lab7_reservations join lab7_rooms on room = roomcode WHERE\s
                         CheckIn between ? and ? and\s
                         CheckOut between ? and ?),
                        allRooms as (select distinct roomCode from lab7_rooms where bedType = ? and maxOcc >= ?)
                        select distinct roomCode from allRooms where not exists (select * from Unavailable\s
                        where roomCode = room)""");
                preparedStatement.setDate(1, Date.valueOf(startDate));
                preparedStatement.setDate(2, Date.valueOf(endDate));
                preparedStatement.setDate(3, Date.valueOf(startDate));
                preparedStatement.setDate(4, Date.valueOf(endDate));
                preparedStatement.setString(5, bedType);
                preparedStatement.setInt(6, maxOcc);
                ResultSet res_set = preparedStatement.executeQuery();
                if (!res_set.next()) {
                    System.out.println("Sorry, no rooms that meet your criteria are available");
                    return;
                } else {
                    roomCode = res_set.getString("roomCode");
                    roomFound = true;
                    System.out.println(roomCode + " is available");
                }
            } else {
                PreparedStatement preparedStatement = conn.prepareStatement("""
                        select * from lab7_reservations WHERE\s
                         CheckIn between ? and ? and\s
                         CheckOut between ? and ? and room = ?""");
                preparedStatement.setDate(1, Date.valueOf(startDate));
                preparedStatement.setDate(2, Date.valueOf(endDate));
                preparedStatement.setDate(3, Date.valueOf(startDate));
                preparedStatement.setDate(4, Date.valueOf(endDate));
                preparedStatement.setString(5, roomCode);
                ResultSet res_set = preparedStatement.executeQuery();
                if (res_set.next()) {
                    System.out.println("No Rooms Available");
                    System.out.print("Search for available room? Yes/Cancel: ");
                    String input = cnsl.readLine();
                    if (input.equalsIgnoreCase("Yes")) {
                        roomCode = "any";
                        continue;
                    } else {
                        return;
                    }

                } else {
                    roomFound = true;
                }
            }
            PreparedStatement maxOccQuery = conn.prepareStatement("select maxOcc, bedtype, roomname, baseprice from lab7_rooms where roomcode = ?");
            maxOccQuery.setString(1, roomCode);
            ResultSet rs = maxOccQuery.executeQuery();
            rs.next();
            int maxOccRoom = rs.getInt("maxOcc");
            String roomBedType = rs.getString("bedtype");
            roomName = rs.getString("roomname");
            rate = rs.getDouble("baseprice");
            if (maxOccRoom < maxOcc) {
                System.out.print("Room not big enough for your full party. Find a room that first? Yes/No: ");
                String input = cnsl.readLine();
                if (input.equalsIgnoreCase("Yes")) {
                    roomCode = "any";
                    roomFound = false;
                }
            }
            if (!roomBedType.equalsIgnoreCase(bedType)) {
                System.out.print("This room has a " + roomBedType + ", not the " + bedType + "" +
                        " you selected. Would you like us to find a room with your bed type? Yes/No: ");
                String input = cnsl.readLine();
                if (input.equalsIgnoreCase("Yes")) {
                    roomCode = "any";
                    roomFound = false;
                }
            }
        }
        try {


            //Date checker, make sure not before current date.
            LocalDate reserv_checkInDate = LocalDate.parse(startDate);
            LocalDate reserv_checkOutDate = LocalDate.parse(endDate);
            LocalDate currentDate = LocalDate.now();
            int date1 = currentDate.compareTo(reserv_checkInDate);
            int date2 = currentDate.compareTo(reserv_checkOutDate);
            if (date1 > 0 || date2 > 0) {
                System.out.println("Invalid dates");
                return;
            }
            double totalCost = 0;
            LocalDate checkInDate = LocalDate.parse(startDate);
            LocalDate checkOutDate = LocalDate.parse(endDate);
            ZoneId timezone = ZoneId.of("America/Los_Angeles");

            for (LocalDate date = checkInDate; date.isBefore(checkOutDate); date = date.plusDays(1)) {
                Calendar calender = Calendar.getInstance();
                ZonedDateTime zoned_time = date.atStartOfDay(timezone);
                Instant i = zoned_time.toInstant();

                java.util.Date d = java.util.Date.from(i);
                calender.setTime(d);

                int dayOfWeek = calender.get(Calendar.DAY_OF_WEEK);

                if (dayOfWeek >= 2 && dayOfWeek <= 6) {
                    totalCost += (rate);
                } else if (dayOfWeek == 0 || dayOfWeek == 7) {
                    totalCost += (rate * 1.10);
                }
            }
            double nightlyrate = totalCost / (DAYS.between(checkInDate, checkOutDate));
            try (Statement stm2 = conn.createStatement()) {
                //Get max code from reservations to generate potential future next code.
                String sql2 = "SELECT max(CODE) as CODE from lab7_reservations;";
                ResultSet res_set2 = stm2.executeQuery(sql2);

                while (res_set2.next()) {
                    String code = res_set2.getString("CODE");
                    reserv_code = Integer.parseInt(code) + 1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Would you to make a booking with the information: ");
            System.out.println("First Name: " + firstName);
            System.out.println("LastName: " + lastName);
            System.out.println("RoomCode: " + roomCode);
            System.out.println("RoomName: " + roomName);
            System.out.println("BedType: " + bedType);
            System.out.println("StartDate: " + startDate);
            System.out.println("EndDate: " + endDate);
            System.out.println("Number of Kids: " + childCount);
            System.out.println("Number of Adults: " + adultCount);
            System.out.println("Total Cost: " + totalCost);
            System.out.print("Confirm booking? Confirm/No: ");
            String input = cnsl.readLine();
            if(input.equalsIgnoreCase("No")){
                return;
            }
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO lab7_reservations (CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")) {

                pstmt.setInt(1, reserv_code);
                pstmt.setString(2, roomCode);
                pstmt.setDate(3, Date.valueOf(startDate));
                pstmt.setDate(4, Date.valueOf(endDate));
                pstmt.setDouble(5, rate);
                pstmt.setString(6, lastName);
                pstmt.setString(7, firstName);
                pstmt.setInt(8, Integer.parseInt(adultCount));
                pstmt.setInt(9, Integer.parseInt(childCount));
                pstmt.executeUpdate();
                System.out.println("Booking complete");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private Connection createConnect() {
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
            return conn;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
