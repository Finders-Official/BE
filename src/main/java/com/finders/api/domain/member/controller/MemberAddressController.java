package com.finders.api.domain.member.controller;

import com.finders.api.domain.member.dto.request.MemberAddressRequest;
import com.finders.api.domain.member.dto.response.MemberAddressResponse;
import com.finders.api.domain.member.service.command.MemberAddressCommandService;
import com.finders.api.global.response.ApiResponse;
import com.finders.api.global.response.SuccessCode;
import com.finders.api.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 주소(User Address)", description = "유저 배송지 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/addresses")
public class MemberAddressController {

    private final MemberAddressCommandService memberAddressCommandService;

    @Operation(
            summary = "배송지 추가",
            description = "새 배송지를 등록하고 상세 정보를 반환합니다."
    )
    @PostMapping
    public ApiResponse<MemberAddressResponse.AddressDetail> createAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid MemberAddressRequest.Create request
    ) {
        MemberAddressResponse.AddressDetail response = memberAddressCommandService.createAddress(authUser.memberId(), request);
        return ApiResponse.success(SuccessCode.MEMBER_ADDRESS_CREATED, response);
    }
}
