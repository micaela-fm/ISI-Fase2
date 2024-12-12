package jdbc;

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

    static String inputData(String str) {
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
    static void addUser(jdbc.User userData, Card cardData) {

        final String INSERT_PERSON = "INSERT INTO person(email, taxnumber, name) VALUES (?,?,?) RETURNING id";
        final String INSERT_CARD = "INSERT INTO card(credit, typeof, client) VALUES (?,?,?)";
        final String INSERT_USER = "INSERT INTO client(person, dtregister) VALUES (?,?)";

        try (Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString()); PreparedStatement preparedStatementPerson = conn.prepareStatement(INSERT_PERSON, Statement.RETURN_GENERATED_KEYS); PreparedStatement preparedStatementCard = conn.prepareStatement(INSERT_CARD); PreparedStatement preparedStatementUser = conn.prepareStatement(INSERT_USER)) {
            conn.setAutoCommit(false);
            
            // Insert person
            preparedStatementPerson.setString(1, userData.getEmail());
            preparedStatementPerson.setInt(2, userData.getTaxNumber());
            preparedStatementPerson.setString(3, userData.getName());

            int affectedRows = preparedStatementPerson.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating person failed, no rows affected.");
            }

            int personId;
            try (ResultSet generatedKeys = preparedStatementPerson.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    personId = generatedKeys.getInt(1);
                } else {
                    throw new RuntimeException("Creating person failed, no ID obtained.");
                }
            }


            // CONTINUE


            conn.commit();
            if (preparedStatementUser != null) preparedStatementUser.close();
            if (preparedStatementCard != null) preparedStatementCard.close();
            if (preparedStatementPerson != null) preparedStatementPerson.close();
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
     *
     * @param values Array containing [operation, name, station, scooter]
     */
    public static void travel(String[] values) {

        // TO BE DONE
    }

    /**
     * Auxiliar method -- if you want
     * Gets client ID by name from the database
     *
     * @param name The name of the client
     * @return client ID or -1 if not found
     * @throws SQLException if database operation fails
     */
    public static int getClientId(String name) throws SQLException {

// TODO implement and replace the return
        return 0;
    }

    /**
     * Starts a new travel
     *
     * @param clientId  Client ID
     * @param scooterId Scooter ID
     * @param stationId Station ID
     * @throws SQLException if database operation fails
     */
    public static void startTravel(int clientId, int scooterId, int stationId) throws SQLException {

        System.out.print("EMPTY");
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

    public static void userSatisfaction(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("userSatisfaction()");
    }

    public static void occupationStation(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("occupationStation()");
    }
}
