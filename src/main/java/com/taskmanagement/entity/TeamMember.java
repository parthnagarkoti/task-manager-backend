package com.taskmanagement.entity;

import com.taskmanagement.enums.TeamRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a User's membership in a Team, with a specific role.
 *
 * This is a proper @Entity (not a @JoinTable) because it carries
 * meaningful data: the role column (OWNER/MEMBER/VIEWER) and joinedAt.
 * This is what makes it one of the 8 meaningful tables.
 */
@Entity
@Table(
    name = "team_members",
    // Enforce that a user can only appear once per team
    uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // EnumType.STRING stores "OWNER" / "MEMBER" / "VIEWER" — not a number
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamRole role;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;
}
