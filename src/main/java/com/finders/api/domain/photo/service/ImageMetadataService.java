package com.finders.api.domain.photo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

/**
 * 이미지 메타데이터 추출 서비스
 * <p>
 * Java 표준 ImageIO를 사용하여 이미지 크기를 빠르게 추출합니다.
 * 전체 이미지를 디코딩하지 않고 헤더 정보만 읽어 성능을 최적화했습니다.
 */
@Slf4j
@Service
public class ImageMetadataService {

    /**
     * 이미지 바이트에서 width/height 추출 (최적화)
     * <p>
     * ImageReader를 사용하여 전체 이미지를 디코딩하지 않고
     * 헤더 정보만 읽어 성능을 크게 향상시킵니다.
     *
     * @param imageBytes 이미지 바이트 데이터
     * @return 이미지 크기 정보 (width, height)
     */
    public ImageDimensions extractDimensions(byte[] imageBytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
             ImageInputStream iis = ImageIO.createImageInputStream(bis)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                log.warn("[ImageMetadataService.extractDimensions] No image reader found for the provided bytes");
                return new ImageDimensions(null, null);
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                return new ImageDimensions(width, height);
            } finally {
                reader.dispose();
            }

        } catch (Exception e) {
            log.error("[ImageMetadataService.extractDimensions] Failed to extract dimensions: {}", e.getMessage());
            return new ImageDimensions(null, null);
        }
    }

    /**
     * 이미지 바이트에서 width/height 추출 (폴백: 전체 디코딩)
     * <p>
     * ImageReader가 실패하는 경우 BufferedImage로 폴백합니다.
     * 이 방법은 전체 이미지를 메모리에 로드하므로 더 느립니다.
     *
     * @param imageBytes 이미지 바이트 데이터
     * @return 이미지 크기 정보 (width, height)
     * @deprecated ImageReader 방식을 사용하세요
     */
    @Deprecated
    public ImageDimensions extractDimensionsWithFullDecode(byte[] imageBytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                log.warn("[ImageMetadataService.extractDimensionsWithFullDecode] Could not decode image");
                return new ImageDimensions(null, null);
            }
            return new ImageDimensions(image.getWidth(), image.getHeight());
        } catch (Exception e) {
            log.error("[ImageMetadataService.extractDimensionsWithFullDecode] Failed to extract dimensions: {}", e.getMessage());
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
