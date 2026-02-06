package com.finders.api.infra.storage;

import com.finders.api.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoragePathTest {

    @Nested
    class FromObjectPath {

        @ParameterizedTest
        @CsvSource({
                "restorations/65/original/07e3951b92cd40f5ada52981df5e0fb6_photo.png, RESTORATION_ORIGINAL",
                "restorations/65/mask/07e3951b92cd40f5ada52981df5e0fb6_mask.png, RESTORATION_MASK",
                "restorations/65/restored/07e3951b92cd40f5ada52981df5e0fb6_restored.png, RESTORATION_RESTORED",
        })
        void 복원_경로_original_mask_restored를_정확히_구분한다(String objectPath, StoragePath expected) {
            assertThat(StoragePath.fromObjectPath(objectPath)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "restorations/65/mask/07e3951b92cd40f5ada52981df5e0fb6_mask.png, RESTORATION_MASK",
                "restorations/65/mask/e83981fef70246fab84bc3ac2a9f2690_mask.png, RESTORATION_MASK",
                "restorations/65/mask/5388985cd87643cea056c0adb96f9543_mask.png, RESTORATION_MASK",
                "restorations/65/mask/1f5dff8a4ba5431b9c150ef20ac5a122_mask.png, RESTORATION_MASK",
                "restorations/65/mask/234e08115a4d4a59afa7d92c6cf1f6ae_mask.png, RESTORATION_MASK",
                "restorations/65/mask/b59f95a079994d4b8d60f3ebd3347405_mask.png, RESTORATION_MASK",
        })
        void 프로덕션_로그에서_실패했던_mask_경로가_올바르게_매칭된다(String objectPath, StoragePath expected) {
            assertThat(StoragePath.fromObjectPath(objectPath)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "restorations/1/original/uuid.png, RESTORATION_ORIGINAL",
                "restorations/999/original/uuid.png, RESTORATION_ORIGINAL",
                "restorations/1/mask/uuid.png, RESTORATION_MASK",
                "restorations/999/mask/uuid.png, RESTORATION_MASK",
                "restorations/1/restored/uuid.png, RESTORATION_RESTORED",
                "restorations/999/restored/uuid.png, RESTORATION_RESTORED",
        })
        void 다양한_memberId에서도_복원_경로를_구분한다(String objectPath, StoragePath expected) {
            assertThat(StoragePath.fromObjectPath(objectPath)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "photo-labs/1/images/uuid.jpg, LAB_IMAGE",
                "photo-labs/1/documents/business/uuid.pdf, LAB_DOCUMENT",
        })
        void 현상소_이미지와_서류_경로를_구분한다(String objectPath, StoragePath expected) {
            assertThat(StoragePath.fromObjectPath(objectPath)).isEqualTo(expected);
        }

        @Test
        void 현상소_QR코드_경로를_매칭한다() {
            assertThat(StoragePath.fromObjectPath("photo-labs/1/qr.png")).isEqualTo(StoragePath.LAB_QR);
        }

        @ParameterizedTest
        @CsvSource({
                "profiles/1/uuid.jpg, PROFILE",
                "posts/1/uuid.jpg, POST_IMAGE",
                "promotions/1/uuid.jpg, PROMOTION",
                "inquiries/1/uuid.jpg, INQUIRY",
                "temp/1/uuid.jpg, TEMP_PUBLIC",
                "temp/orders/1/scans/uuid.jpg, SCANNED_PHOTO",
        })
        void 기타_경로를_올바르게_매칭한다(String objectPath, StoragePath expected) {
            assertThat(StoragePath.fromObjectPath(objectPath)).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "unknown/path/file.jpg", "invalid"})
        void 유효하지_않은_경로는_예외를_던진다(String objectPath) {
            assertThatThrownBy(() -> StoragePath.fromObjectPath(objectPath))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void fromObjectPath와_extractId를_조합하면_올바른_ID를_추출한다() {
            String maskPath = "restorations/65/mask/uuid_mask.png";

            StoragePath type = StoragePath.fromObjectPath(maskPath);
            Long memberId = type.extractId(maskPath);

            assertThat(type).isEqualTo(StoragePath.RESTORATION_MASK);
            assertThat(memberId).isEqualTo(65L);
        }
    }

    @Nested
    class ExtractId {

        @ParameterizedTest
        @CsvSource({
                "restorations/65/original/uuid.png, 65",
                "restorations/123/mask/uuid.png, 123",
                "restorations/7/restored/uuid.png, 7",
                "profiles/42/uuid.jpg, 42",
                "posts/100/uuid.jpg, 100",
                "inquiries/88/uuid.jpg, 88",
        })
        void 경로에서_도메인ID를_추출한다(String objectPath, Long expectedId) {
            StoragePath type = StoragePath.fromObjectPath(objectPath);
            assertThat(type.extractId(objectPath)).isEqualTo(expectedId);
        }

        @Test
        void 스캔사진_경로에서_주문ID를_추출한다() {
            assertThat(StoragePath.SCANNED_PHOTO.extractId("temp/orders/42/scans/uuid.jpg"))
                    .isEqualTo(42L);
        }

        @ParameterizedTest
        @ValueSource(strings = {"profiles/abc/uuid.jpg", "profiles//uuid.jpg", "restorations"})
        void ID가_숫자가_아니거나_세그먼트가_부족하면_예외를_던진다(String objectPath) {
            assertThatThrownBy(() -> StoragePath.PROFILE.extractId(objectPath))
                    .isInstanceOf(CustomException.class);
        }
    }
}
