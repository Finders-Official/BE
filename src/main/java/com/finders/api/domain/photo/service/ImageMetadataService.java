package com.finders.api.domain.photo.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

/**
 * 이미지 메타데이터 추출 서비스
 * <p>
 * 이미지 바이트에서 width, height 등의 메타데이터를 추출합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageMetadataService {

    /**
     * 이미지 바이트에서 width/height 추출
     *
     * @param imageBytes 이미지 바이트 데이터
     * @return 이미지 크기 정보 (width, height)
     */
    public ImageDimensions extractDimensions(byte[] imageBytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(bis);

            // PNG 디렉토리 먼저 시도
            PngDirectory pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory.class);
            if (pngDirectory != null) {
                Integer width = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
                Integer height = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
                if (width != null && height != null) {
                    return new ImageDimensions(width, height);
                }
            }

            // JPEG 디렉토리 시도
            JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
            if (jpegDirectory != null) {
                Integer width = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
                Integer height = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
                if (width != null && height != null) {
                    return new ImageDimensions(width, height);
                }
            }

            log.warn("[ImageMetadataService.extractDimensions] Could not extract dimensions from metadata");
            return new ImageDimensions(null, null);

        } catch (Exception e) {
            log.error("[ImageMetadataService.extractDimensions] Failed to extract dimensions: {}", e.getMessage(), e);
            return new ImageDimensions(null, null);
        }
    }

    /**
     * 이미지 크기 정보
     *
     * @param width  이미지 너비 (픽셀)
     * @param height 이미지 높이 (픽셀)
     */
    public record ImageDimensions(Integer width, Integer height) {
    }
}
