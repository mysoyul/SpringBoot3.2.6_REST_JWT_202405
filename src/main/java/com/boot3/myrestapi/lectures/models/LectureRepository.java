package com.boot3.myrestapi.lectures.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Integer> {
    Optional<Lecture> findByName(String name);

}