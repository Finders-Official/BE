package com.finders.api.domain.photo.entity;

import com.finders.api.domain.photo.enums.print.FilmType;
import com.finders.api.domain.photo.enums.print.FrameType;
import com.finders.api.domain.photo.enums.print.PaperType;
import com.finders.api.domain.photo.enums.print.PrintMethod;
import com.finders.api.domain.photo.enums.print.PrintSize;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "print_order_item",
        indexes = {
                @Index(name = "idx_print_item_order", columnList = "print_order_id")
        }
)
public class PrintOrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "print_order_id", nullable = false)
    private PrintOrder printOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "film_type", length = 20, nullable = false)
    private FilmType filmType;

    @Enumerated(EnumType.STRING)
    @Column(name = "paper_type", length = 30, nullable = false)
    private PaperType paperType;

    @Enumerated(EnumType.STRING)
    @Column(name = "print_method", length = 20, nullable = false)
    private PrintMethod printMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", length = 20, nullable = false)
    private PrintSize size;

    @Enumerated(EnumType.STRING)
    @Column(name = "frame_type", length = 20, nullable = false)
    private FrameType frameType;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Builder
    private PrintOrderItem(
            PrintOrder printOrder,
            FilmType filmType,
            PaperType paperType,
            PrintMethod printMethod,
            PrintSize size,
            FrameType frameType,
            int unitPrice,
            int totalPrice
    ) {
        this.printOrder = printOrder;
        this.filmType = filmType;
        this.paperType = paperType;
        this.printMethod = printMethod;
        this.size = size;
        this.frameType = frameType;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public static PrintOrderItem create(
            PrintOrder printOrder,
            FilmType filmType,
            PaperType paperType,
            PrintMethod printMethod,
            PrintSize size,
            FrameType frameType,
            int unitPrice,
            int totalPrice
    ) {

        return PrintOrderItem.builder()
                .printOrder(printOrder)
                .filmType(filmType)
                .paperType(paperType)
                .printMethod(printMethod)
                .size(size)
                .frameType(frameType)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }
}
