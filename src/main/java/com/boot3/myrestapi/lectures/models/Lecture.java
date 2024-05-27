package com.boot3.myrestapi.lectures.models;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    @Column(nullable = false)
    private LocalDateTime beginEnrollmentDateTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    @Column(nullable = false)
    private LocalDateTime closeEnrollmentDateTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    @Column(nullable = false)
    private LocalDateTime beginLectureDateTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
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
}  