package com.finders.api.domain.photo.entity;

import com.finders.api.domain.photo.enums.DeliveryStatus;
import com.finders.api.domain.photo.entity.PrintOrder;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "delivery",
        indexes = {
                @Index(name = "idx_delivery_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_delivery_order", columnNames = "print_order_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "print_order_id", nullable = false)
    private PrintOrder printOrder;

    @Column(name = "recipient_name", nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(name = "address_detail", length = 100)
    private String addressDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(length = 50)
    private String carrier;

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee = 0;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Builder
    private Delivery(
            PrintOrder printOrder,
            String recipientName,
            String phone,
            String zipcode,
            String address,
            String addressDetail,
            DeliveryStatus status,
            String trackingNumber,
            String carrier,
            Integer deliveryFee,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt
    ) {
        this.printOrder = printOrder;
        this.recipientName = recipientName;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.status = (status != null) ? status : DeliveryStatus.PENDING;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.deliveryFee = (deliveryFee != null) ? deliveryFee : 0;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    /**
     * 배송 주문 생성 시점에 배송지 정보를 기반으로 delivery row를 만든다.
     * (DDL상 배송지 필드가 NOT NULL이라서 이 방식이 가장 자연스럽다)
     */
    public static Delivery toEntity(
            PrintOrder printOrder,
            String recipientName,
            String phone,
            String zipcode,
            String address,
            String addressDetail,
            int deliveryFee
    ) {
        return Delivery.builder()
                .printOrder(printOrder)
                .recipientName(recipientName)
                .phone(phone)
                .zipcode(zipcode)
                .address(address)
                .addressDetail(addressDetail)
                .deliveryFee(deliveryFee)
                .status(DeliveryStatus.PENDING)
                .build();
    }

    public static Delivery create(PrintOrder printOrder) {
        Delivery delivery = new Delivery();
        delivery.printOrder = printOrder;
        delivery.status = DeliveryStatus.PENDING;
        return delivery;
    }

    public static Delivery create(
            PrintOrder printOrder,
            String recipientName,
            String phone,
            String zipcode,
            String address,
            String addressDetail,
            int deliveryFee
    ) {
        return Delivery.builder()
                .printOrder(printOrder)
                .recipientName(recipientName)
                .phone(phone)
                .zipcode(zipcode)
                .address(address)
                .addressDetail(addressDetail)
                .deliveryFee(deliveryFee)
                .status(DeliveryStatus.PENDING)
                .build();
    }

    public void updateCarrier(String carrier) {
        this.carrier = carrier;
    }

    public void updateTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void markShipped(LocalDateTime shippedAt) {
        this.shippedAt = (shippedAt != null) ? shippedAt : LocalDateTime.now();
        this.status = DeliveryStatus.SHIPPED;
    }

    /**
     * 배송 완료 처리
     */
    public void markDelivered(LocalDateTime deliveredAt) {
        this.deliveredAt = (deliveredAt != null) ? deliveredAt : LocalDateTime.now();
        this.status = DeliveryStatus.DELIVERED;
    }

}
