package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for users, independent of the persistence technology.
 */
public interface UserRepository {

    /**
     * Inserts or updates a user.
     *
     * @param user user to persist
     * @return the persisted user
     */
    User save(User user);

    /**
     * Finds a user by identity.
     *
     * @param id user identity
     * @return the user when it exists
     */
    Optional<User> findById(Long id);

    /**
     * Lists every user.
     *
     * @return all users
     */
    List<User> findAll();

    /**
     * Indicates whether a user exists.
     *
     * @param id user identity
     * @return {@code true} when the user exists
     */
    boolean existsById(Long id);
}
