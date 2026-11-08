package at.salzburg.hackathon.hackathontourism03.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "Report")
public class ReportEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String placeId;

    @Column(name = "report_type", nullable = false)
    private String type;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastConfirmedAt;

    @Column(nullable = false)
    private Integer confirmations;

    @Column
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private Long reporterId;

    @ElementCollection
    @CollectionTable(name = "report_confirmed_by", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "user_id")
    private Set<Long> confirmedBy = new HashSet<>();
}
