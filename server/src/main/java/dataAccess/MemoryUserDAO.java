package dataAccess;

import model.UserData;

import java.util.HashMap;

/**
 * In-memory implementation of UserDAO
 * using a HashMap to store users keyed by username.
 */
public class MemoryUserDAO implements dataAccess.UserDAO {

    private final HashMap<String, UserData> userMap;

    public MemoryUserDAO() {
        userMap = new HashMap<>();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (userMap.containsKey(user.username())) {
            throw new DataAccessException("User already exists: " + user.username());
        }
        userMap.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (!userMap.containsKey(username)) {
            throw new DataAccessException("User not found: " + username);
        }
        return userMap.get(username);
    }

    @Override
    public void clear() throws DataAccessException {
        userMap.clear();
    }
}
