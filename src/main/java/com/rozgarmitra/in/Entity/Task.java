package com.rozgarmitra.in.Entity;

import com.rozgarmitra.in.Enum.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_customer_created", columnList = "customer_id, created_at"),
        @Index(name = "idx_tasks_labour_created", columnList = "labour_id, created_at"),
        @Index(name = "idx_tasks_status_created", columnList = "status, created_at"),
        @Index(name = "idx_tasks_service_status", columnList = "service_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labour_id")
    private User labour;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "task_rejected_labours", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "labour_id"))
    @Builder.Default
    private Set<User> rejectedLabours = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceCategory serviceCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private TaskStatus status;

    @Embedded
    private GeoLocation taskLocation;

    @Column(nullable = false)
    private String addressText;

    @Column(nullable = false)
    private Double estimatedPrice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "start_otp")
    private String startOtp;

    private LocalDateTime startOtpGeneratedAt;
    private LocalDateTime startOtpVerifiedAt;

    @Column(name = "completion_otp")
    private String completionOtp;

    private LocalDateTime completionOtpGeneratedAt;
    private LocalDateTime completionOtpVerifiedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationMinutes;
    private Double finalAmount;
    private String paymentStatus;
    private LocalDateTime paidAt;
}