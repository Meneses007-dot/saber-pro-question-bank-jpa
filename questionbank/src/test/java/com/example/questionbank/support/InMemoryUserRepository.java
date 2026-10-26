package com.example.questionbank.support;

import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of the user contract.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> store = new LinkedHashMap<>();
    private long sequence;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.assignIdentity(++sequence);
        }
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
