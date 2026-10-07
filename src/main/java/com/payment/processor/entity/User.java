package com.payment.processor.entity;

import com.payment.processor.enums.Role;
import com.payment.processor.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.USERS_TABLE_NAME;

@Entity
@Table(
        name = USERS_TABLE_NAME,
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        },
        indexes = {
                @Index(
                        name = "idx_users_email",
                        columnList = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Role role = Role.USER;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "source_version", nullable = false)
    private long sourceVersion;

//    public void addAccount(Account account) {
//        accounts.add(account);
//        account.setUser(this);
//    }
//
//    public void removeAccount(Account account) {
//        accounts.remove(account);
//        account.setUser(null);
//    }

    public boolean applyUpdateIfNewer(
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String displayName,
            UserStatus status,
            boolean emailVerified,
            Role role,
            Instant updatedAt,
            long sourceVersion
    ) {
        if (sourceVersion <= this.sourceVersion) {
            return false;
        }

        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.status = status;
        this.emailVerified = emailVerified;
        this.role = role;
        this.updatedAt = updatedAt;
        this.sourceVersion = sourceVersion;

        return true;
    }

}