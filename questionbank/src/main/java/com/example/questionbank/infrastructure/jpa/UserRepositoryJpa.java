package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.UserRepository;
import com.example.questionbank.infrastructure.jpa.mapper.UserMapper;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the {@link UserRepository} contract.
 */
@Repository
public class UserRepositoryJpa implements UserRepository {

    private final SpringUserDataRepository data;

    /**
     * Creates the adapter.
     *
     * @param data Spring Data user repository
     */
    public UserRepositoryJpa(SpringUserDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            return UserMapper.toDomain(data.save(UserMapper.toNewRecord(user)));
        }
        UserRecord record = data.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + user.getId()));
        record.update(user.getFullName(), user.getEmail(), user.getRole());
        return UserMapper.toDomain(data.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return data.findById(id).map(UserMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return data.findAll().stream().map(UserMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return data.existsById(id);
    }
}
