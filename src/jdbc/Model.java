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
     */
    static void addUser(User userData, Card cardData) {

        final String INSERT_PERSON = "INSERT INTO person(email, taxnumber, name) VALUES (?,?,?) RETURNING id";
        final String INSERT_CARD = "INSERT INTO card(credit, typeof, client) VALUES (?,?,?)";
        final String INSERT_USER = "INSERT INTO client(person, dtregister) VALUES (?,?)";

        try (
                Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString());
                PreparedStatement preparedStatementPerson = conn.prepareStatement(INSERT_PERSON, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement preparedStatementCard = conn.prepareStatement(INSERT_CARD);
                PreparedStatement preparedStatementUser = conn.prepareStatement(INSERT_USER)) {

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

            // Insert client
            preparedStatementUser.setInt(1, personId);
            preparedStatementUser.setTimestamp(2, userData.getRegistrationDate());

            affectedRows = preparedStatementUser.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating user failed, no rows affected.");
            }

            // Insert card
            cardData.setClient(personId);
            preparedStatementCard.setDouble(1, cardData.getCredit());
            preparedStatementCard.setString(2, cardData.getReference());
            preparedStatementCard.setInt(3, cardData.getClient());

            affectedRows = preparedStatementCard.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating card failed, no rows affected.");
            }

            conn.commit();
            preparedStatementUser.close();
            preparedStatementCard.close();
            preparedStatementPerson.close();
        } catch (SQLException e) {
            System.out.println("Error on insert values");
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Lists orders based on specified criteria
     *
     * @param orders Criteria for listing orders
     */
    static void listOrders(String[] orders) {
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
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Processes a travel operation (start or stop)
     *
     * @param values Array containing [operation, name, station, scooter]
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

    /**
     * Auxiliary method -- if you want
     * Gets client ID by name from database
     *
     * @param name The name of the client
     * @return client ID or -1 if not found
     */
    public static int getClientId(String name) {

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
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean scooterInStation(Connection conn, int scooterId, int stationId) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM dock WHERE scooter = ? AND station = ?) AS result";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, scooterId);
            preparedStatement.setInt(2, stationId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next() && rs.getBoolean("result");
            }
        }
    }

    private static boolean hasOngoingTravel(Connection conn, String type, int id) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM travel WHERE " + type + " = ? AND dtfinal IS NULL) AS result";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next() && rs.getBoolean("result");
            }
        }
    }

    private static double getCredit(Connection conn, int clientId) throws SQLException {
        String query = "SELECT credit FROM card WHERE client = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, clientId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next() ? rs.getDouble("credit") : 0;
            }
        }
    }

    private static double getUnlockValue(Connection conn) throws SQLException {
        String query = "SELECT unlock FROM servicecost LIMIT 1";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {
            return rs.next() ? rs.getDouble("unlock") : 0;
        }
    }

    private static void insertTravelRecord(Connection conn, int clientId, int scooterId, int stationId) throws SQLException {
        String query = "INSERT INTO travel(dtinitial, client, scooter, stinitial) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(2, clientId);
            preparedStatement.setInt(3, scooterId);
            preparedStatement.setInt(4, stationId);
            preparedStatement.executeUpdate();
        }
    }

    private static void updateCredit(Connection conn, int clientId, double amount) throws SQLException {
        String query = "UPDATE card SET credit = credit - ? WHERE client = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, clientId);
            preparedStatement.executeUpdate();
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
        try (Connection conn = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString())) {
            conn.setAutoCommit(false);

            // Check if client has an ongoing travel
            if (!hasOngoingTravel(conn, "client", clientId) ) {
                throw new Exception("Client has no ongoing travel");
            }

            // Check if scooter has ongoing travel with the client
            if (!hasOngoingTravelWithClient(conn, clientId, scooterId)) {
                throw new Exception("Scooter has no ongoing travel with client");
            }

            // TODO() As viagens com mesmo clientID e scootter ID entram em conflito ao calcular o custo da viagem entao usamos o dtfinal para garantir que estamos a finalizar a viagem correta

            Timestamp dtFinal = new Timestamp(System.currentTimeMillis());
            // update travel record
            updateTravelRecord(conn, clientId, scooterId, stationId, dtFinal);

            // calculate travel cost
            double cost = calculateTravelCost(conn, clientId, scooterId, dtFinal);

            System.out.println("Cost: " + cost);

            // TODO() if cost > credit nao deixa finalizar a viagem

            double credit = getCredit(conn, clientId);

            System.out.println("Credit: " + credit);

            if (cost > credit) {
                throw new Exception("Insufficient credit to pay for travel");
            }
            // update credit
            updateCredit(conn, clientId, cost);


            // atualizar dock da scooter

            conn.commit();
            System.out.println("Success!");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateTravelRecord(Connection conn, int clientId, int scooterId, int stationId, Timestamp dtFinal) throws SQLException {
        String query = "UPDATE travel SET dtfinal = ?, stfinal = ? WHERE client = ? AND scooter = ? AND dtfinal IS NULL";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setTimestamp(1, dtFinal);
            preparedStatement.setInt(2, stationId);
            preparedStatement.setInt(3, clientId);
            preparedStatement.setInt(4, scooterId);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Viagem finalizada com sucesso.");
            } else {
                System.out.println("Nenhuma viagem encontrada para o cliente e scooter especificado.");
            }
        }
    }

    private static double calculateTravelCost(Connection conn, int clientId, int scooterId, Timestamp dtFinal) throws Exception {
        String query = """
                          SELECT
                              ROUND(
                                  (SELECT sc.usable FROM servicecost sc) *
                                  EXTRACT(EPOCH FROM (t.dtfinal - t.dtinitial)) / 60,
                                  2
                              ) AS travel_cost
                          FROM
                              travel t
                          WHERE
                              t.client = ?
                              AND t.scooter = ?
                              AND t.dtfinal = ?;
                      """;
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, clientId);
            preparedStatement.setInt(2, scooterId);
            preparedStatement.setTimestamp(3, dtFinal);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("travel_cost");
                } else {
                    throw new Exception("Error calculating travel cost");
                }
            }
        }
    }

    private static boolean hasOngoingTravelWithClient(Connection conn, int clientId, int scooterId) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM travel WHERE client = ? AND scooter = ? AND dtfinal IS NULL) AS result";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, clientId);
            preparedStatement.setInt(2, scooterId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next() && rs.getBoolean("result");
            }
        }
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
        try (Connection connection = DriverManager.getConnection(jdbc.UI.getInstance().getConnectionString());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(cmd)) {
            UI.printResults(resultSet);
        } catch (SQLException e) {
            System.out.println("Error on insert values");
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void occupationStation(/*FILL WITH PARAMETERS */) {
        // TODO
        System.out.println("occupationStation()");
    }
}

