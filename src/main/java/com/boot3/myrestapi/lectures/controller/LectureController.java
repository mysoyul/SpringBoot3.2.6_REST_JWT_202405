package com.boot3.myrestapi.lectures.controller;

import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import com.boot3.myrestapi.lectures.dto.LectureResDto;
import com.boot3.myrestapi.lectures.dto.hateoas.LectureResource;
import com.boot3.myrestapi.lectures.models.Lecture;
import com.boot3.myrestapi.lectures.models.LectureRepository;
import com.boot3.myrestapi.lectures.validator.LectureValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    //Constructor Injection 생성자주입
//    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }
    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto, Errors errors) {
        //Validation 어노테이션을 사용해서 입력항목 검증하기
        if(errors.hasErrors()) {
            return getErrors(errors);
        }
        //사용자정의 Validator를 사용해서 biz 로직의 입력항목 검증하기
        this.lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()) {
            return getErrors(errors);
        }
        // ReqDTO => Entity 변환
        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        //offline, free 필드의 값을 설정
        lecture.update();

        Lecture addedLecture = lectureRepository.save(lecture);
        // Entity => ResDTO 변환
        LectureResDto lectureResDto = modelMapper.map(addedLecture, LectureResDto.class);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(LectureController.class).slash(lectureResDto.getId());
        URI createUri = selfLinkBuilder.toUri();

        LectureResource lectureResource = new LectureResource(lectureResDto);
        //Rel 'query-lectures' link 생성
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        //self link 생성
        lectureResource.add(selfLinkBuilder.withSelfRel());
        //Rel 'update-lecture' link 생성
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private static ResponseEntity<Errors> getErrors(Errors errors) {
        return ResponseEntity.badRequest().body(errors);
    }
}
