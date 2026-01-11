package com.finders.api.domain.store.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PhotoLab_USER", description = "현상소 API")
@RestController
@RequestMapping("/user/photo-labs")
@RequiredArgsConstructor
public class UserPhotoLabController {
}
