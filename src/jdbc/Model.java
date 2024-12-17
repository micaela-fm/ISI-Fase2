package jdbc;

import java.io.IOException;
import java.sql.*;
import java.util.AbstractMap;
import java.util.Scanner;

/*
 *
 * @author MP
 * @version 1.0
 * @since 2024-11-07
 */
public class Model {

    static String inputData(String str) throws IOException {
        // IMPLEMENTED
        /*
         * Gets input data from user
         *
         * @param str Description of required input values
         *
         * @return String containing comma-separated values
         */
        Scanner key = new Scanner(System.in); // Scanner closes System.in if you call close(). Don't do it
        System.out.println("Enter corresponding values, separated by commas of:");
        System.out.println(str);
        return key.nextLine();
    }

    /**
     * Adds a new user with associated card to the database
     *
     * @param userData User information
     * @param cardData Card information
     * @throws SQLException if database operation fails
     */
    static void addUser(User userData, Card cardData) {

        final String INSERT_PERSON = "INSERT INTO person(email, taxnumber, name) VALUES (?,?,?) RETURNING id";
        final String INSERT_CARD = "INSERT INTO card(credit, typeof, client) VALUES (?,?,?)";
        final String INSERT_USER = "INSERT INTO client(person, dtregister) VALUES (?,?)";

        try (
                Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString());
                PreparedStatement pstmtPerson = conn.prepareStatement(INSERT_PERSON, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement pstmtCard = conn.prepareStatement(INSERT_CARD);
                PreparedStatement pstmtUser = conn.prepareStatement(INSERT_USER)) {

            conn.setAutoCommit(false);

            // Insert person
            pstmtPerson.setString(1, userData.getEmail());
            pstmtPerson.setInt(2, userData.getTaxNumber());
            pstmtPerson.setString(3, userData.getName());

            int affectedRows = pstmtPerson.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating person failed, no rows affected.");
            }

            int personId;
            try (ResultSet generatedKeys = pstmtPerson.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    personId = generatedKeys.getInt(1);
                } else {
                    throw new RuntimeException("Creating person failed, no ID obtained.");
                }
            }

            // Insert client
            pstmtUser.setInt(1, personId);
            pstmtUser.setTimestamp(2, userData.getRegistrationDate());

            affectedRows = pstmtUser.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating user failed, no rows affected.");
            }

            // Insert card
            cardData.setClient(personId);
            pstmtCard.setDouble(1, cardData.getCredit());
            pstmtCard.setString(2, cardData.getReference());
            pstmtCard.setInt(3, cardData.getClient());

            affectedRows = pstmtCard.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating card failed, no rows affected.");
            }

            conn.commit();
            if (pstmtUser != null)
                pstmtUser.close();
            if (pstmtCard != null)
                pstmtCard.close();
            if (pstmtPerson != null)
                pstmtPerson.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Error on insert values");
            // e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * To implement from this point forward. Do not need to change the code above.
     * -------------------------------------------------------------------------------
     * IMPORTANT:
     * --- DO NOT MOVE IN THE CODE ABOVE. JUST HAVE TO IMPLEMENT THE METHODS BELOW
     * ---
     * -------------------------------------------------------------------------------
     **/

    /**
     * Lists orders based on specified criteria
     *
     * @param orders Criteria for listing orders
     * @throws SQLException if database operation fails
     */
    static void listOrders(String[] orders) throws SQLException {
        final String val1 = orders[0];
        final String val2 = orders[1];
        final String val3 = orders[2];

        int stationId;
        Timestamp startDate;
        Timestamp endDate;

        if (!val1.contains(":")) {
            stationId = Integer.parseInt(val1);
            AbstractMap.SimpleEntry<Timestamp, Timestamp> pair = getTimeOrdered(val2, val3);
            startDate = pair.getKey();
            endDate = pair.getValue();
        } else if (!val2.contains(":")) {
            stationId = Integer.parseInt(val2);
            AbstractMap.SimpleEntry<Timestamp, Timestamp> pair = getTimeOrdered(val1, val3);
            startDate = pair.getKey();
            endDate = pair.getValue();
        } else {
            stationId = Integer.parseInt(val3);
            AbstractMap.SimpleEntry<Timestamp, Timestamp> pair = getTimeOrdered(val1, val2);
            startDate = pair.getKey();
            endDate = pair.getValue();
        }
        listReplacementOrders(stationId, startDate, endDate);
    }

    private static AbstractMap.SimpleEntry<Timestamp, Timestamp> getTimeOrdered(String val1, String val2) {
        Timestamp tmp1 = Timestamp.valueOf(val1);
        Timestamp tmp2 = Timestamp.valueOf(val2);
        if (tmp1.before(tmp2)) {
            return new AbstractMap.SimpleEntry<>(tmp1, tmp2);
        } else {
            return new AbstractMap.SimpleEntry<>(tmp2, tmp1);
        }
    }

    /**
     * Lists replacement orders for a specific station in a given time period
     *
     * @param stationId Station ID
     * @param startDate Start date for a period
     * @param endDate   End date for a period
     */
    public static void listReplacementOrders(int stationId, Timestamp startDate, Timestamp endDate) {
        final String VALUE_CMD = "select * from replacementorder where station = ? and dtorder between ? and ?";
        try {
            Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString());
            PreparedStatement preparedStatement = conn.prepareStatement(VALUE_CMD);

            preparedStatement.setInt(1, stationId);
            preparedStatement.setTimestamp(2, startDate);
            preparedStatement.setTimestamp(3, endDate);

            ResultSet res = preparedStatement.executeQuery();

            UI.printResults(res);

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error on insert values");
            // e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Processes a travel operation (start or stop)
     * @param values Array containing [operation, name, station, scooter]
     * @throws SQLException if database operation fails
     */
    public static void travel(String[] values) {

        int clientId = Integer.parseInt(values[0]);
        int scooterId = Integer.parseInt(values[1]);
        int stationId = Integer.parseInt(values[2]);
        try {
            switch (values[3].toLowerCase()) {
                case "start":
                    startTravel(clientId, scooterId, stationId);
                    break;
                case "stop":
                    stopTravel(clientId, scooterId, stationId);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid value: " + values[3]);
            }
        } catch (SQLException e) {
            System.out.println("Error on insert values");
            // e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /** Auxiliar method -- if you want
     * Gets client ID by name from database
     * @param name The name of the client
     * @return client ID or -1 if not found
     * @throws SQLException if database operation fails
     */
    public static int getClientId(String name) throws SQLException {

// TODO implement and replace the return
        return 0;
    }

    // Try With resources automatically closes the connections
    public static void startTravel(int clientId, int scooterId, int stationId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString())) {
            conn.setAutoCommit(false);

            // Check if client has an ongoing travel
            if (hasOngoingTravel(conn, "client", clientId)) {
                throw new Exception("Client has an ongoing travel");
            }

            if (!scooterInStation(conn, scooterId, stationId)) {
                throw new Exception("Scooter is not in the station");
            }

            // Check if scooter has an ongoing travel
            if (hasOngoingTravel(conn, "scooter", scooterId)) {
                throw new Exception("Scooter has an ongoing travel");
            }

            // Get credit value
            double credit = getCredit(conn, clientId);

            // Get unlock value
            double unlock = getUnlockValue(conn);

            if (credit < unlock) {
                throw new Exception("Insufficient credit to unlock scooter");
            }

            // Insert travel record
            insertTravelRecord(conn, clientId, scooterId, stationId);

            // Deduct unlock cost from card balance
            updateCredit(conn, clientId, unlock);

            conn.commit();
            System.out.println("Success!");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean scooterInStation(Connection conn, int scooterId, int stationId) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM dock WHERE scooter = ? AND station = ?) AS result";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, scooterId);
            pstmt.setInt(2, stationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getBoolean("result");
            }
        }
    }

    private static boolean hasOngoingTravel(Connection conn, String type, int id) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM travel WHERE " + type + " = ? AND dtfinal IS NULL) AS result";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getBoolean("result");
            }
        }
    }

    private static double getCredit(Connection conn, int clientId) throws SQLException {
        String query = "SELECT credit FROM card WHERE client = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getDouble("credit") : 0;
            }
        }
    }

    private static double getUnlockValue(Connection conn) throws SQLException {
        String query = "SELECT unlock FROM servicecost LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getDouble("unlock") : 0;
        }
    }

    private static void insertTravelRecord(Connection conn, int clientId, int scooterId, int stationId) throws SQLException {
        String query = "INSERT INTO travel(dtinitial, client, scooter, stinitial) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, clientId);
            pstmt.setInt(3, scooterId);
            pstmt.setInt(4, stationId);
            pstmt.executeUpdate();
        }
    }

    private static void updateCredit(Connection conn, int clientId, double amount) throws SQLException {
        String query = "UPDATE card SET credit = credit - ? WHERE client = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, clientId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Stops an ongoing travel
     *
     * @param clientId  Client ID
     * @param scooterId Scooter ID
     * @param stationId Destination station ID
     * @throws SQLException if database operation fails
     */
    public static void stopTravel(int clientId, int scooterId, int stationId) throws SQLException {


        System.out.print("EMPTY");
    }

    public static void updateDocks(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("updateDocks()");
    }

    public static void userSatisfaction() {
        String cmd = """
                select
                    t1.scooter,
                    round(avg(t1.evaluation),2) as avg_ratings,
                    count(t1.scooter) as travels,
                    round((coalesce (t2.high_ratings, 0) * 100.0 / count(t1.scooter)),2) as high_rating_percentage
                from
                    travel t1
                left join
                    (select scooter, count(scooter) as high_ratings
                     from travel
                     where evaluation >= 4
                     group by scooter) t2
                on
                    t1.scooter = t2.scooter
                where
                    t1.evaluation is not null
                group by
                    t1.scooter, t2.high_ratings
                order by
                    t1.scooter asc ;
                """;
        try {
            Connection connection = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(cmd);
            UI.printResults(resultSet);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error on insert values");
            // e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void occupationStation(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("occupationStation()");
    }
}

