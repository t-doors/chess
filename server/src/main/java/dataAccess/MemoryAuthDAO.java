package dataAccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {

    private final HashMap<String, AuthData> authMap;

    public MemoryAuthDAO() {
        authMap = new HashMap<>();
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (authMap.containsKey(auth.authToken())) {
            throw new DataAccessException("Auth token already exists: " + auth.authToken());
        }
        authMap.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (!authMap.containsKey(authToken)) {
            throw new DataAccessException("Auth token not found: " + authToken);
        }
        return authMap.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (!authMap.containsKey(authToken)) {
            throw new DataAccessException("Cannot delete missing token: " + authToken);
        }
        authMap.remove(authToken);
    }

    @Override
    public void clear() {
        authMap.clear();
    }
}
