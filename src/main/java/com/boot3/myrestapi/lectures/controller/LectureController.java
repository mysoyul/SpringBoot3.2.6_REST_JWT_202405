package com.boot3.myrestapi.lectures.controller;

import com.boot3.myrestapi.common.errors.ErrorsResource;
import com.boot3.myrestapi.common.exception.BusinessException;
import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import com.boot3.myrestapi.lectures.dto.LectureResDto;
import com.boot3.myrestapi.lectures.dto.hateoas.LectureResource;
import com.boot3.myrestapi.lectures.models.Lecture;
import com.boot3.myrestapi.lectures.models.LectureRepository;
import com.boot3.myrestapi.lectures.validator.LectureValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

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

    @PutMapping("/{id}")
    public ResponseEntity updateLecture(@PathVariable Integer id,
                                        @RequestBody @Valid LectureReqDto lectureReqDto,
                                        Errors errors) {
        String errMsg = String.format("Id = %d Lecture Not Found", id);
        Lecture existingLecture =
                this.lectureRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(errMsg, HttpStatus.NOT_FOUND));

        if (errors.hasErrors()) {
            return getErrors(errors);
        }
        lectureValidator.validate(lectureReqDto, errors);
        if (errors.hasErrors()) {
            return getErrors(errors);
        }

        this.modelMapper.map(lectureReqDto, existingLecture);
        existingLecture.update();
        Lecture savedLecture = this.lectureRepository.save(existingLecture);
        LectureResDto lectureResDto = modelMapper.map(savedLecture, LectureResDto.class);

        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity getLecture(@PathVariable Integer id) {
//        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
//        if(optionalLecture.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        Lecture lecture = optionalLecture.get();

        String errMsg = String.format("Id = %d Lecture Not Found", id);
        Lecture lecture = this.lectureRepository.findById(id)
                              .orElseThrow(() -> new BusinessException(errMsg, HttpStatus.NOT_FOUND));

        LectureResDto lectureResDto = modelMapper.map(lecture, LectureResDto.class);
        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }


    @GetMapping
    public ResponseEntity queryLectures(Pageable pageable, PagedResourcesAssembler<LectureResDto> assembler) {
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        // Page<Lecture> => Page<LectureResDto> 변환
        Page<LectureResDto> lectureResDtoPage =
                lecturePage.map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        
        // Page<LectureResDto> => PagedModel<EntityModel<LectureResDto>> 변환
        //PagedModel<EntityModel<LectureResDto>> pagedResources = assembler.toModel(lectureResDtoPage);

        // Page<LectureResDto> => PagedModel<LectureResource> 변환
        PagedModel<LectureResource> pagedResources =
                //assembler.toModel(lectureResDtoPage, resDto -> new LectureResource(resDto));
                assembler.toModel(lectureResDtoPage, LectureResource::new);
        return ResponseEntity.ok(pagedResources);
    }
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
        //Rel 'update-lecture' link 생성
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private static ResponseEntity<ErrorsResource> getErrors(Errors errors) {

        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
