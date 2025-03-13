package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class GameDAOSQL implements GameDAO {

    public GameDAOSQL() {
        try {
            DatabaseManager.createDatabase();
            createGameTableIfMissing();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize GameDAOSQL: " + e.getMessage());
        }
    }

    private void createGameTableIfMissing() throws DataAccessException {
        String sql = """
          CREATE TABLE IF NOT EXISTS chess_table (
              gameID INT NOT NULL PRIMARY KEY,
              whiteTeam VARCHAR(255),
              blackTeam VARCHAR(255),
              gameName VARCHAR(255),
              gameState TEXT
          )
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create 'chess_table': " + e.getMessage());
        }
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        String insertSQL = """
          INSERT INTO chess_table (gameID, whiteTeam, blackTeam, gameName, gameState)
          VALUES (?,?,?,?,?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, game.gameName());
            stmt.setString(5, serializeChessGame(game.game()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game record: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String selectSQL = """
          SELECT whiteTeam, blackTeam, gameName, gameState
          FROM chess_table
          WHERE gameID=?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {

            stmt.setInt(1, gameID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Game not found for ID: " + gameID);
                }
                String white = rs.getString("whiteTeam");
                String black = rs.getString("blackTeam");
                String name = rs.getString("gameName");
                String gameJson = rs.getString("gameState");
                ChessGame cg = deserializeChessGame(gameJson);

                return new GameData(gameID, white, black, name, cg);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game with ID=" + gameID + ": " + e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String selectSQL = """
          SELECT gameID, whiteTeam, blackTeam, gameName, gameState
          FROM chess_table
        """;

        var resultSet = new ArrayList<GameData>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int gameID = rs.getInt("gameID");
                String white = rs.getString("whiteTeam");
                String black = rs.getString("blackTeam");
                String gName = rs.getString("gameName");
                String stateJson = rs.getString("gameState");

                ChessGame cg = deserializeChessGame(stateJson);
                resultSet.add(new GameData(gameID, white, black, gName, cg));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return resultSet;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String updateSQL = """
          UPDATE chess_table
          SET whiteTeam=?, blackTeam=?, gameName=?, gameState=?
          WHERE gameID=?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, serializeChessGame(game.game()));
            stmt.setInt(5, game.gameID());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("No row found to update for gameID=" + game.gameID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String truncateSQL = "TRUNCATE chess_table";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(truncateSQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing chess_table: " + e.getMessage());
        }
    }

    private String serializeChessGame(ChessGame cg) {
        if (cg == null) return null;
        return new Gson().toJson(cg);
    }

    private ChessGame deserializeChessGame(String json) {
        if (json == null) return null;
        return new Gson().fromJson(json, ChessGame.class);
    }
}
