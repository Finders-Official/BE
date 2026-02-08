package com.finders.api.domain.community.repository;

import com.finders.api.domain.community.entity.PostImage;
import com.finders.api.domain.community.enums.CommunityStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    @Query("""
            select pi
            from PostImage pi
            join pi.post p
            where p.photoLab.id = :labId
              and p.status = :status
            order by p.createdAt desc, pi.displayOrder asc, pi.id asc
            """)
    List<PostImage> findByPhotoLabIdAndPostStatus(
            @Param("labId") Long labId,
            @Param("status") CommunityStatus status,
            Pageable pageable
    );
}
