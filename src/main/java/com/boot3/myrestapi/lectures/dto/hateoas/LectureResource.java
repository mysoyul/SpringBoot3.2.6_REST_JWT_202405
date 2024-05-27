package com.boot3.myrestapi.lectures.dto.hateoas;

import com.boot3.myrestapi.lectures.controller.LectureController;
import com.boot3.myrestapi.lectures.dto.LectureResDto;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class LectureResource extends RepresentationModel<LectureResource> {
    @JsonUnwrapped
    private LectureResDto lectureResDto;
    public LectureResource(LectureResDto resDto) {
        this.lectureResDto = resDto;
        //self link 생성
        add(linkTo(LectureController.class).slash(resDto.getId()).withSelfRel());
    }
    public LectureResDto getLectureResDto() {
        return lectureResDto;
    }

}