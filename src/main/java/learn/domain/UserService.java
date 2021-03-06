package learn.domain;

import learn.models.User;
import learn.repository.DataAccessException;

public interface UserService {
    Result<User> findByEmail(String email) throws DataAccessException;

    Result<User> addUser(User user) throws DataAccessException;

    Result<User> editUser(User user) throws DataAccessException;

    Result<User> deleteUser(User user) throws DataAccessException;
}
