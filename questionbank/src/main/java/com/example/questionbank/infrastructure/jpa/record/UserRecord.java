package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA representation of a user. Persistence annotations live only in this
 * package.
 */
@Entity
@Table(name = "users")
public class UserRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Required by JPA. */
    protected UserRecord() {
    }

    /**
     * Creates a user record.
     *
     * @param fullName full name
     * @param email unique email
     * @param role role in the system
     */
    public UserRecord(String fullName, String email, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    /**
     * Updates the mutable columns of the record.
     *
     * @param fullName full name
     * @param email unique email
     * @param role role in the system
     */
    public void update(String fullName, String email, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return full name */
    public String getFullName() {
        return fullName;
    }

    /** @return unique email */
    public String getEmail() {
        return email;
    }

    /** @return role in the system */
    public Role getRole() {
        return role;
    }
}
