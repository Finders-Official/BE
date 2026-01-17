package com.finders.api.domain.photo.dto;

import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.photo.enums.print.FilmType;
import com.finders.api.domain.photo.enums.print.FrameType;
import com.finders.api.domain.photo.enums.print.PaperType;
import com.finders.api.domain.photo.enums.print.PrintMethod;
import com.finders.api.domain.photo.enums.print.PrintSize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Builder;

public class PhotoRequest {

    @Builder
    @Schema(name = "PrintQuoteRequest", description = "회원 - 인화 실시간 견적 요청(/print/quote)")
    public record PrintQuote(

            @Schema(description = "현상 주문 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "developmentOrderId는 필수입니다.")
            Long developmentOrderId,

            @Schema(description = "수령 방식", example = "DELIVERY", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "receiptMethod는 필수입니다.")
            ReceiptMethod receiptMethod,

            // ===== 선택 옵션 (null 허용) =====

            @Schema(description = "필름 종류", example = "COLOR_NEG")
            FilmType filmType,

            @Schema(description = "인화 방식", example = "INKJET")
            PrintMethod printMethod,

            @Schema(description = "인화지 종류", example = "ECO_LUSTER_255")
            PaperType paperType,

            @Schema(description = "인화 사이즈", example = "SIZE_6x8")
            PrintSize size,

            @Schema(description = "인화 유형(프레임)", example = "NO_FRAME")
            FrameType frameType,

            // ===== 필수 =====

            @Schema(description = "선택한 스캔 사진 및 수량 목록", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty(message = "photos는 최소 1개 이상이어야 합니다.")
            List<@Valid SelectedPhoto> photos
    ) { }

    @Builder
    @Schema(name = "SelectedPhoto", description = "견적 계산에 포함할 스캔 사진 + 수량")
    public record SelectedPhoto(

            @Schema(description = "스캔 사진 ID", example = "501", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "scannedPhotoId는 필수입니다.")
            Long scannedPhotoId,

            @Schema(description = "해당 사진 인화 수량(1 이상)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
            @Positive(message = "quantity는 1 이상이어야 합니다.")
            int quantity
    ) { }

    @Builder
    @Schema(name = "CreatePrintOrderRequest", description = "회원 - 인화 주문 생성 요청(/print-orders)")
    public record CreatePrintOrder(

            @NotNull Long developmentOrderId,
            @NotNull ReceiptMethod receiptMethod,

            @NotNull FilmType filmType,
            @NotNull PrintMethod printMethod,
            @NotNull PaperType paperType,
            @NotNull PrintSize size,
            @NotNull FrameType frameType,

            @NotEmpty List<@Valid SelectedPhoto> photos
    ) { }

    public record DepositReceiptConfirm(
            @NotBlank(message = "objectPath는 필수입니다.")
            String objectPath,

            @NotBlank(message = "입금자명은 필수입니다.")
            String depositorName,

            @NotBlank(message = "입금 은행은 필수입니다.")
            String depositBankName
    ) {}
}
