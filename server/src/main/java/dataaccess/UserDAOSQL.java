package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOSQL implements UserDAO {

    public UserDAOSQL() {
        try {
            DatabaseManager.createDatabase();
            createUserTable();
        } catch (DataAccessException e) {
            throw new RuntimeException("Couldn't initialize SQLUserDAO: " + e.getMessage());
        }
    }

    private void createUserTable() throws DataAccessException {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_table (
                username VARCHAR(255) PRIMARY KEY,
                hashedPw VARCHAR(255) NOT NULL,
                email VARCHAR(255)
            )
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("user_table creation failed: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "INSERT INTO user_table (username, hashedPw, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, hashed);
            stmt.setString(3, user.email());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("User insertion failed or user exists: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT hashedPw, email FROM user_table WHERE username=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("User not found: " + username);
                }
                String hashedPw = rs.getString("hashedPw");
                String email = rs.getString("email");
                return new UserData(username, hashedPw, email);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not retrieve user: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        String sql = "TRUNCATE user_table";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
        }
    }
}
