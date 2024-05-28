package com.boot3.myrestapi.lectures.models;

import com.boot3.myrestapi.security.userinfo.UserInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
@Entity
@Table(name = "lectures")
public class Lecture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime beginEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime closeEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime beginLectureDateTime;

    @Column(nullable = false)
    private LocalDateTime endLectureDateTime;
    
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @Enumerated(EnumType.STRING)
    private LectureStatus lectureStatus = LectureStatus.DRAFT;

    @ManyToOne
    private UserInfo userInfo;

    public void update() {
        // Update free
        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        } else {
            this.free = false;
        }
        // Update offline
        if (this.location == null || this.location.isBlank()) {
            this.offline = false;
        } else {
            this.offline = true;
        }
    }

}  