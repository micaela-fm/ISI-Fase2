package jdbc;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Scanner;
import java.io.IOException;
import java.sql.*;

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

    static void addUser(User userData, Card cardData) {
        /**
         * Adds a new user with associated card to the database
         *
         * @param userData User information
         * @param cardData Card information
         * @throws SQLException if database operation fails
         */
        final String INSERT_PERSON = "INSERT INTO person(email, taxnumber, name) VALUES (?,?,?) RETURNING id";
        final String INSERT_CARD = "INSERT INTO card(credit, typeof, client) VALUES (?,?,?)";
        final String INSERT_USER = "INSERT INTO client(person, dtregister) VALUES (?,?)";

        try (
                Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString());
                PreparedStatement pstmtPerson = conn.prepareStatement(INSERT_PERSON, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement pstmtCard = conn.prepareStatement(INSERT_CARD);
                PreparedStatement pstmtUser = conn.prepareStatement(INSERT_USER);) {
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

    public static void travel(String[] values){
        /**
         * Processes a travel operation (start or stop)
         * @param values Array containing [operation, name, station, scooter]
         * @throws SQLException if database operation fails
         */
        int clientId = Integer.parseInt(values[0]);
        int scooterId = Integer.parseInt(values[1]);
        int stationId = Integer.parseInt(values[2]);
//        boolean start = false;
//        boolean stop = false;
        switch (values[3].toLowerCase()) {
            case "start":
//                start = true;
                startTravel(clientId, scooterId, stationId);
                break;
            case "stop":
//                stop = true;
                stopTravel(clientId, scooterId, stationId);
                break;
            default:
                throw new IllegalArgumentException("Invalid value: " + values[3]);
        }

        }
    }

    public static int getClientId(String name) throws SQLException {
        /** Auxiliar method -- if you want
         * Gets client ID by name from database
         * @param name The name of the client
         * @return client ID or -1 if not found
         * @throws SQLException if database operation fails
         */
// TODO implement and replace the return
        return 0;
    }

    public static void startTravel(int clientId, int scooterId, int stationId) throws SQLException {
        /**
         * Starts a new travel
         * @param clientId Client ID
         * @param scooterId Scooter ID
         * @param stationId Station ID
         * @throws SQLException if database operation fails
         */
    try {
        Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString());

        // Get credit value
        String getCredit = "SELECT credit FROM card WHERE client = ?";
        PreparedStatement pstmtCredit = conn.prepareStatement(getCredit);
        pstmtCredit.setInt(1, clientId);
        ResultSet rsCredit = pstmtCredit.executeQuery();
        double credit = 0;
        if (rsCredit.next()) {
            credit = rsCredit.getDouble("credit");
        }

        // Get unlock value
        String getUnlock = "SELECT unlock FROM servicecost LIMIT 1";
        PreparedStatement pstmtUnlock = conn.prepareStatement(getUnlock);
        ResultSet rsUnlock = pstmtUnlock.executeQuery();
        double unlock = 0;
        if (rsUnlock.next()) {
            unlock = rsUnlock.getDouble("unlock");
        }

        if (credit < unlock) {
            System.out.print("Insufficient credit to unlock scooter");
            return;
        }

        // Insert travel record
        String insertTravel = "INSERT INTO travel(dtinitial, client, scooter, stinitial) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmtTravel = conn.prepareStatement(insertTravel);
        pstmtTravel.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        pstmtTravel.setInt(2, clientId);
        pstmtTravel.setInt(3, scooterId);
        pstmtTravel.setInt(4, stationId);
        pstmtTravel.executeUpdate();

        // Deduct unlock cost from card balance
        String updateCredit = "UPDATE card SET credit = credit - ? WHERE client = ?";
        PreparedStatement pstmtUpdateCredit = conn.prepareStatement(updateCredit);
        pstmtUpdateCredit.setDouble(1, unlock);
        pstmtUpdateCredit.setInt(2, clientId);
        pstmtUpdateCredit.executeUpdate();

        conn.commit();
        pstmtCredit.close();
        pstmtUnlock.close();
        pstmtTravel.close();
        pstmtUpdateCredit.close();
        conn.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
        System.out.print("EMPTY");
    }


    public static void stopTravel(int clientId, int scooterId, int stationId) throws SQLException {
        /**
         * Stops an ongoing travel
         * @param clientId Client ID
         * @param scooterId Scooter ID
         * @param stationId Destination station ID
         * @throws SQLException if database operation fails
         */
        System.out.print("EMPTY");
    }

    public static void updateDocks(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("updateDocks()");
    }

    public static void userSatisfaction(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("userSatisfaction()");
    }

    public static void occupationStation(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("occupationStation()");
    }
}
