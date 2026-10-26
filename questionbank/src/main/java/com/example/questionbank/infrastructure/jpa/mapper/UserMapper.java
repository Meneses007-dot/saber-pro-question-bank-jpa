package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.User;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;

/**
 * Translates users between the domain and the JPA model.
 */
public final class UserMapper {

    private UserMapper() {
    }

    /**
     * Converts a JPA record to a domain user.
     *
     * @param record JPA record
     * @return domain user
     */
    public static User toDomain(UserRecord record) {
        return new User(record.getId(), record.getFullName(), record.getEmail(), record.getRole());
    }

    /**
     * Creates a new JPA record from a domain user.
     *
     * @param user domain user
     * @return new JPA record
     */
    public static UserRecord toNewRecord(User user) {
        return new UserRecord(user.getFullName(), user.getEmail(), user.getRole());
    }
}
