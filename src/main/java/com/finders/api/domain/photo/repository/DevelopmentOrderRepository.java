package com.finders.api.domain.photo.repository;

import com.finders.api.domain.photo.entity.DevelopmentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DevelopmentOrderRepository extends JpaRepository<DevelopmentOrder, Long> {
    /**
     * 주문 코드 중복 여부 확인
     */
    boolean existsByOrderCode(String orderCode);

    boolean existsByReservationId(Long reservationId);

    @Query(
            value = """
        select d
        from DevelopmentOrder d
        join fetch d.photoLab pl
        where d.user.id = :memberId
        """,
            countQuery = """
        select count(d)
        from DevelopmentOrder d
        where d.user.id = :memberId
        """
    )
    Page<DevelopmentOrder> findMyOrdersWithPhotoLab(@Param("memberId") Long memberId, Pageable pageable);

    boolean existsByIdAndUser_Id(Long id, Long memberId);
}
