package com.boot3.myrestapi.lectures.controller;

import com.boot3.myrestapi.common.errors.ErrorsResource;
import com.boot3.myrestapi.common.exception.BusinessException;
import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import com.boot3.myrestapi.lectures.dto.LectureResDto;
import com.boot3.myrestapi.lectures.dto.hateoas.LectureResource;
import com.boot3.myrestapi.lectures.models.Lecture;
import com.boot3.myrestapi.lectures.models.LectureRepository;
import com.boot3.myrestapi.lectures.validator.LectureValidator;
import com.boot3.myrestapi.security.annot.CurrentUser;
import com.boot3.myrestapi.security.userinfo.UserInfo;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
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
                                        Errors errors,
                                        @CurrentUser UserInfo currentUser) {
        Lecture existingLecture = getExistingLecture(id);
        //Lecture가 참조하는 UserInfo 객체와 인증한 UserInfo 객체가 다르면 401 인증 오류
        if((existingLecture.getUserInfo() != null) && (!existingLecture.getUserInfo().equals(currentUser))) {
            throw new BadCredentialsException("등록한 User와 수정을 요청한 User가 다릅니다.");
            //return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

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

        //Lecture 객체와 연관된 UserInfo 객체가 있다면 LectureResDto에 email을 set
        if(savedLecture.getUserInfo() != null)
            lectureResDto.setEmail(savedLecture.getUserInfo().getEmail());

        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    private Lecture getExistingLecture(Integer id) {
        String errMsg = String.format("Id = %d Lecture Not Found", id);
        Lecture existingLecture =
                this.lectureRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(errMsg, HttpStatus.NOT_FOUND));
        return existingLecture;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity getLecture(@PathVariable Integer id,
                                     @CurrentUser UserInfo currentUser) {
//        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
//        if(optionalLecture.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        Lecture lecture = optionalLecture.get();

        Lecture lecture = getExistingLecture(id);

        LectureResDto lectureResDto = modelMapper.map(lecture, LectureResDto.class);
        //Lecture가 참조하는 UserInfo 가 있으면
        if (lecture.getUserInfo() != null)
            lectureResDto.setEmail(lecture.getUserInfo().getEmail());

        LectureResource lectureResource = new LectureResource(lectureResDto);
        //인증토큰의 email과 Lecture가 참조하는 email주소가 같으면 update 링크를 제공하기
        if ((lecture.getUserInfo() != null) && (lecture.getUserInfo().equals(currentUser))) {
            lectureResource.add(linkTo(LectureController.class)
                    .slash(lecture.getId()).withRel("update-lecture"));
        }
        return ResponseEntity.ok(lectureResource);
    }


    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity queryLectures(Pageable pageable,
                                        PagedResourcesAssembler<LectureResDto> assembler,
                                        @CurrentUser UserInfo currentUser) {
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        // Page<Lecture> => Page<LectureResDto> 변환
//        Page<LectureResDto> lectureResDtoPage =
//                lecturePage.map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        Page<LectureResDto> lectureResDtoPage =
                lecturePage.map(lecture -> {
                    LectureResDto lectureResDto = new LectureResDto();
                    if (lecture.getUserInfo() != null) {
                        lectureResDto.setEmail(lecture.getUserInfo().getEmail());
                    }
                    modelMapper.map(lecture, lectureResDto);
                    return lectureResDto;
                });
        
        // Page<LectureResDto> => PagedModel<EntityModel<LectureResDto>> 변환
        //PagedModel<EntityModel<LectureResDto>> pagedResources = assembler.toModel(lectureResDtoPage);

        // Page<LectureResDto> => PagedModel<LectureResource> 변환
        PagedModel<LectureResource> pagedResources =
                //assembler.toModel(lectureResDtoPage, resDto -> new LectureResource(resDto));
                assembler.toModel(lectureResDtoPage, LectureResource::new);
        return ResponseEntity.ok(pagedResources);
    }
    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto,
                                           Errors errors,
                                           @CurrentUser UserInfo currentUser) {
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

        //Lecture와 UserInfo 연관관계 설정
        lecture.setUserInfo(currentUser);

        Lecture addedLecture = lectureRepository.save(lecture);
        // Entity => ResDTO 변환
        LectureResDto lectureResDto = modelMapper.map(addedLecture, LectureResDto.class);

        //LectureResDto 에 UserInfo 객체의 email set
        lectureResDto.setEmail(addedLecture.getUserInfo().getEmail());

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
