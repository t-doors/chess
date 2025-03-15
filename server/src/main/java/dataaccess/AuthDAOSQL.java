package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AuthDAOSQL implements AuthDAO {

    public AuthDAOSQL() {
        try {
            DatabaseManager.createDatabase();
            createTableIfAbsent();
        } catch (DataAccessException e) {
            throw new RuntimeException("Could not init AuthDAOSQL: " + e.getMessage());
        }
    }

    private void createTableIfAbsent() throws DataAccessException {
        String sql = """
            CREATE TABLE IF NOT EXISTS auth_table (
                authToken VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
            )
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create auth_table: " + e.getMessage());
        }
    }

    @Override
    public void createAuth(AuthData authData) {
        String sql = "INSERT INTO auth_table (authToken, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authData.authToken());
            stmt.setString(2, authData.username());
            stmt.executeUpdate();

        } catch (SQLException | DataAccessException e) {
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth_table WHERE authToken=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Cannot delete - token not found: " + authToken);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting token: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT username FROM auth_table WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("No auth record for token: " + authToken);
                }
                String user = rs.getString("username");
                return new AuthData(user, authToken);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not retrieve auth token: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        String sql = "TRUNCATE auth_table";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
        }
    }
}
