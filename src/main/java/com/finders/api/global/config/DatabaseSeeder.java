package com.finders.api.global.config;

import com.finders.api.domain.member.entity.MemberAdmin;
import com.finders.api.domain.member.entity.MemberOwner;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.entity.PhotoLabBusinessHour;
import com.finders.api.domain.store.entity.PhotoLabImage;
import com.finders.api.domain.store.entity.Region;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.domain.store.repository.RegionRepository;
import com.finders.api.domain.reservation.entity.ReservationSlot;
import com.finders.api.domain.reservation.repository.ReservationSlotRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 개발 환경 Mock 데이터 자동 생성
 * dev 프로파일에서만 실행됩니다.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final PhotoLabRepository photoLabRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final EntityManager entityManager;

    private final Faker faker = new Faker(new Locale("ko"));

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (memberRepository.count() > 0) {
            log.info("Database already has data. Skipping seeding.");
            return;
        }

        log.info("========================================");
        log.info("Starting Database Seeding (Dev Environment)");
        log.info("========================================");

        try {
            // 1. Region 생성 (시/도 → 시/군/구)
            List<Region> regions = createRegions();
            log.info("Created {} regions", regions.size());

            // 2. Admin 생성
            MemberAdmin admin = createAdmin();
            log.info("Created admin: {}", admin.getEmail());

            // 3. Owner 생성
            List<MemberOwner> owners = createOwners(3);
            log.info("Created {} owners", owners.size());

            // 4. User 생성
            List<MemberUser> users = createUsers(10);
            log.info("Created {} users", users.size());

            // 5. PhotoLab 생성
            List<PhotoLab> photoLabs = createPhotoLabs(owners, regions);
            log.info("Created {} photo labs", photoLabs.size());

            // 6. ReservationSlot 생성
            createReservationSlots(photoLabs);
            log.info("Created reservation slots for all photo labs");

            log.info("========================================");
            log.info("Database Seeding Completed Successfully!");
            log.info("========================================");

        } catch (Exception e) {
            log.error("Database seeding failed", e);
            throw e;
        }
    }

    private List<Region> createRegions() {
        List<Region> regions = new ArrayList<>();

        // 시/도 생성 (sido = null)
        Region seoul = new Region(null, "서울");
        Region gyeonggi = new Region(null, "경기");
        Region busan = new Region(null, "부산");

        regionRepository.save(seoul);
        regionRepository.save(gyeonggi);
        regionRepository.save(busan);

        // 시/군/구 생성 (sido가 부모 지역)
        String[][] districtData = {
                {"강남구", "마포구", "성동구", "종로구", "용산구", "서초구"},  // 서울
                {"성남시", "수원시", "용인시"},  // 경기
                {"해운대구", "수영구"}  // 부산
        };

        Region[] sidoList = {seoul, gyeonggi, busan};

        for (int i = 0; i < sidoList.length; i++) {
            for (String district : districtData[i]) {
                Region region = new Region(sidoList[i], district);
                regions.add(regionRepository.save(region));
            }
        }

        return regions;
    }

    private MemberAdmin createAdmin() {
        MemberAdmin admin = MemberAdmin.builder()
                .name("관리자")
                .email("admin@finders.it.kr")
                .phone("010-0000-0000")
                .build();
        return memberRepository.save(admin);
    }

    private List<MemberOwner> createOwners(int count) {
        List<MemberOwner> owners = new ArrayList<>();

        String[] ownerNames = {"김사장", "이대표", "박점장"};
        String[] emails = {"owner1@finders.it.kr", "owner2@finders.it.kr", "owner3@finders.it.kr"};

        for (int i = 0; i < count; i++) {
            MemberOwner owner = MemberOwner.builder()
                    .name(ownerNames[i])
                    .email(emails[i])
                    .phone("010-" + faker.number().digits(4) + "-" + faker.number().digits(4))
                    .build();
            owners.add(memberRepository.save(owner));
        }

        return owners;
    }

    private List<MemberUser> createUsers(int count) {
        List<MemberUser> users = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            MemberUser user = MemberUser.builder()
                    .name(faker.name().fullName())
                    .email("user" + i + "@test.com")
                    .phone("010-" + faker.number().digits(4) + "-" + faker.number().digits(4))
                    .nickname("유저" + i + "_" + faker.internet().username().substring(0, 4))
                    .profileImage(null)
                    .build();
            users.add(memberRepository.save(user));
        }

        return users;
    }

    private List<PhotoLab> createPhotoLabs(List<MemberOwner> owners, List<Region> regions) {
        List<PhotoLab> photoLabs = new ArrayList<>();

        String[][] labData = {
                {"필름공방 강남점", "필름 현상 전문점입니다. 35mm, 중형, 대형 모든 포맷을 취급합니다.", "서울 강남구 테헤란로 123", "37.5012", "127.0396"},
                {"사진연구소 마포", "컬러/흑백 현상, 스캔, 인화까지 원스톱 서비스를 제공합니다.", "서울 마포구 홍대입구 45", "37.5563", "126.9220"},
                {"빛과필름 성수", "아날로그 감성을 담은 필름 현상소입니다.", "서울 성동구 성수이로 78", "37.5447", "127.0558"}
        };

        for (int i = 0; i < labData.length; i++) {
            MemberOwner owner = owners.get(i % owners.size());
            Region region = regions.get(i % regions.size());

            PhotoLab lab = PhotoLab.builder()
                    .owner(owner)
                    .region(region)
                    .name(labData[i][0])
                    .description(labData[i][1])
                    .phone("02-" + faker.number().digits(4) + "-" + faker.number().digits(4))
                    .zipcode("0" + faker.number().digits(4))
                    .address(labData[i][2])
                    .addressDetail(faker.number().numberBetween(1, 10) + "층")
                    .latitude(new BigDecimal(labData[i][3]))
                    .longitude(new BigDecimal(labData[i][4]))
                    .status(PhotoLabStatus.ACTIVE)
                    .isDeliveryAvailable(i % 2 == 0)
                    .maxReservationsPerHour(3)
                    .avgWorkTime(faker.number().numberBetween(3, 7))
                    .build();

            photoLabs.add(photoLabRepository.save(lab));
            entityManager.flush();

            // 영업시간 추가
            createBusinessHours(lab);

            // 이미지 추가
            createLabImages(lab);
        }

        return photoLabs;
    }

    private void createBusinessHours(PhotoLab lab) {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day == DayOfWeek.SUNDAY) continue; // 일요일 휴무

            PhotoLabBusinessHour hour = PhotoLabBusinessHour.builder()
                    .photoLab(lab)
                    .dayOfWeek(day)
                    .openTime(LocalTime.of(10, 0))
                    .closeTime(LocalTime.of(19, 0))
                    .isClosed(false)
                    .build();

            entityManager.persist(hour);
        }
    }

    private void createLabImages(PhotoLab lab) {
        for (int i = 0; i < 3; i++) {
            PhotoLabImage image = PhotoLabImage.builder()
                    .photoLab(lab)
                    .objectPath("labs/" + lab.getId() + "/sample_" + (i + 1) + ".jpg")
                    .displayOrder(i)
                    .isMain(i == 0) // 첫 번째 이미지가 메인
                    .build();

            entityManager.persist(image);
        }
    }

    private void createReservationSlots(List<PhotoLab> photoLabs) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(14); // 2주치 슬롯 생성

        for (PhotoLab lab : photoLabs) {
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                // 일요일 제외
                if (currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    // 10시 ~ 18시까지 1시간 단위 슬롯
                    for (int hour = 10; hour < 19; hour++) {
                        ReservationSlot slot = ReservationSlot.builder()
                                .photoLab(lab)
                                .reservationDate(currentDate)
                                .reservationTime(LocalTime.of(hour, 0))
                                .maxCapacity(lab.getMaxReservationsPerHour())
                                .reservedCount(0)
                                .build();

                        reservationSlotRepository.save(slot);
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
        }
    }
}
