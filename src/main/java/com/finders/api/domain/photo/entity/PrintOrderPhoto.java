package com.finders.api.domain.photo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "print_order_photo",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pop_order_photo", columnNames = {"print_order_id", "scanned_photo_id"})
        }
)
public class PrintOrderPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "print_order_id", nullable = false)
    private PrintOrder printOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scanned_photo_id", nullable = false)
    private ScannedPhoto scannedPhoto;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Builder
    private PrintOrderPhoto(
            PrintOrder printOrder,
            ScannedPhoto scannedPhoto,
            int quantity
    ) {
        this.printOrder = printOrder;
        this.scannedPhoto = scannedPhoto;
        this.quantity = quantity;
    }

    public static PrintOrderPhoto create(
            PrintOrder order,
            ScannedPhoto photo,
            int quantity
    ) {
        return PrintOrderPhoto.builder()
                .printOrder(order)
                .scannedPhoto(photo)
                .quantity(quantity)
                .build();
    }
}
