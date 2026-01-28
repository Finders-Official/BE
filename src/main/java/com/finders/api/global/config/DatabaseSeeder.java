package com.finders.api.global.config;

import com.finders.api.domain.community.entity.Comment;
import com.finders.api.domain.community.entity.Post;
import com.finders.api.domain.community.entity.PostImage;
import com.finders.api.domain.community.entity.PostLike;
import com.finders.api.domain.inquiry.entity.Inquiry;
import com.finders.api.domain.inquiry.entity.InquiryImage;
import com.finders.api.domain.inquiry.entity.InquiryReply;
import com.finders.api.domain.member.entity.*;
import com.finders.api.domain.member.enums.SocialProvider;
import com.finders.api.domain.member.enums.TokenHistoryType;
import com.finders.api.domain.member.enums.TokenRelatedType;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.domain.payment.entity.Payment;
import com.finders.api.domain.payment.enums.OrderType;
import com.finders.api.domain.photo.entity.*;
import com.finders.api.domain.photo.enums.DeliveryStatus;
import com.finders.api.domain.photo.enums.DevelopmentOrderStatus;
import com.finders.api.domain.photo.enums.ReceiptMethod;
import com.finders.api.domain.photo.enums.print.*;
import com.finders.api.domain.community.entity.SearchHistory;
import com.finders.api.domain.reservation.entity.Reservation;
import com.finders.api.domain.reservation.entity.ReservationSlot;
import com.finders.api.domain.reservation.enums.ReservationStatus;
import com.finders.api.domain.reservation.repository.ReservationSlotRepository;
import com.finders.api.domain.store.entity.*;
import com.finders.api.domain.store.enums.DocumentType;
import com.finders.api.domain.store.enums.NoticeType;
import com.finders.api.domain.store.enums.PhotoLabStatus;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.domain.store.repository.RegionRepository;
import com.finders.api.domain.terms.entity.MemberAgreement;
import com.finders.api.domain.terms.entity.Terms;
import com.finders.api.domain.terms.enums.TermsType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 개발 환경 Mock 데이터 자동 생성
 * dev 프로파일에서만 실행됩니다.
 */
@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final String LOG_PREFIX = "[DatabaseSeeder.run]";

    // ===== Constants: Admin Data =====
    private static final String ADMIN_PASSWORD = "admin1234!";

    // ===== Constants: Owner Data =====
    private static final String[] OWNER_NAMES = {"김사장", "이대표", "박점장"};
    private static final String[] OWNER_EMAILS = {"owner1@finders.it.kr", "owner2@finders.it.kr", "owner3@finders.it.kr"};
    private static final String[] OWNER_PHONES = {"010-1111-0001", "010-1111-0002", "010-1111-0003"};
    private static final String OWNER_PASSWORD = "owner1234!";

    // ===== Constants: User Data =====
    private static final String[] USER_NAMES = {
            "김민수", "이영희", "박지훈", "최서연", "정우성",
            "강미나", "윤도현", "한소희", "조현우", "신예은"
    };
    private static final String[] USER_PHONES = {
            "010-2222-0001", "010-2222-0002", "010-2222-0003", "010-2222-0004", "010-2222-0005",
            "010-2222-0006", "010-2222-0007", "010-2222-0008", "010-2222-0009", "010-2222-0010"
    };
    private static final String[] USER_NICKNAMES = {
            "민수_film", "영희_photo", "지훈_cam", "서연_snap", "우성_lens",
            "미나_shot", "도현_roll", "소희_dev", "현우_print", "예은_scan"
    };

    // ===== Constants: PhotoLab Phone/Address =====
    private static final String[] LAB_PHONES = {"02-1234-5678", "02-2345-6789", "02-3456-7890"};
    private static final String[] LAB_ZIPCODES = {"06234", "04157", "04779"};
    private static final String[] LAB_ADDRESS_DETAILS = {"1층", "2층", "3층"};
    private static final int[] LAB_AVG_WORK_TIMES = {3, 5, 4};

    // ===== Constants: Region Data =====
    private static final String[] SIDO_NAMES = {"서울", "경기", "부산"};
    private static final String[][] DISTRICT_DATA = {
            {"강남구", "마포구", "성동구", "종로구", "용산구", "서초구"},
            {"성남시", "수원시", "용인시"},
            {"해운대구", "수영구"}
    };

    // ===== Constants: Address Data =====
    private static final String[][] ADDRESS_DATA = {
            {"집", "06234", "서울 강남구 테헤란로 123", "아파트 101호"},
            {"회사", "04157", "서울 마포구 와우산로 45", "오피스빌딩 5층"},
            {"부모님댁", "48058", "부산 해운대구 해운대로 789", "해운대아파트 301호"}
    };

    // ===== Constants: Tag Data =====
    private static final String[] TAG_NAMES = {
            "필름현상", "스캔", "인화", "중형카메라", "대형카메라",
            "흑백필름", "컬러네거티브", "슬라이드필름", "당일현상", "배송가능"
    };

    // ===== Constants: PhotoLab Data =====
    private static final String[][] LAB_DATA = {
            {"필름공방 강남점", "필름 현상 전문점입니다. 35mm, 중형, 대형 모든 포맷을 취급합니다.", "서울 강남구 테헤란로 123", "37.5012", "127.0396"},
            {"사진연구소 마포", "컬러/흑백 현상, 스캔, 인화까지 원스톱 서비스를 제공합니다.", "서울 마포구 홍대입구 45", "37.5563", "126.9220"},
            {"빛과필름 성수", "아날로그 감성을 담은 필름 현상소입니다.", "서울 성동구 성수이로 78", "37.5447", "127.0558"}
    };

    // ===== Constants: Post Data =====
    private static final String[][] POST_DATA = {
            {"첫 필름 현상 후기", "처음으로 필름 카메라를 사용해봤는데 정말 감동적이었어요! 현상소에서 친절하게 설명해주셨습니다."},
            {"성수동 필름 현상소 추천", "성수동에 있는 빛과필름 현상소 다녀왔어요. 스캔 퀄리티가 정말 좋네요!"},
            {"흑백 필름의 매력", "컬러도 좋지만 흑백 필름만의 감성이 있는 것 같아요. 여러분은 어떻게 생각하세요?"},
            {"필름 보관 팁 공유", "필름 보관할 때 냉장고에 넣어두면 좋다고 하더라고요. 실제로 해보니 효과가 있는 것 같습니다."},
            {"주말 출사 사진 공유", "주말에 한강에서 찍은 사진들입니다. Kodak Portra 400으로 촬영했어요."}
    };

    // ===== Constants: Comment Data =====
    private static final String[] COMMENT_CONTENTS = {
            "좋은 사진이네요!",
            "저도 가보고 싶어요~",
            "필름 감성 최고!",
            "어떤 카메라로 찍으셨나요?",
            "정보 감사합니다!"
    };

    // ===== Constants: Inquiry Data =====
    private static final String[][] INQUIRY_DATA = {
            {"현상 소요 시간 문의", "일반 컬러 필름 현상 시 얼마나 걸리나요?"},
            {"배송 문의", "스캔본을 택배로 받을 수 있나요?"},
            {"가격 문의", "중형 필름 현상 가격이 어떻게 되나요?"}
    };

    // ===== Constants: Terms Data =====
    private static final LocalDate TERMS_EFFECTIVE_DATE = LocalDate.of(2025, 1, 1);

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final PhotoLabRepository photoLabRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final EntityManager entityManager;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // 생성된 데이터를 저장할 리스트들
    private List<Terms> termsList = new ArrayList<>();
    private List<MemberUser> users = new ArrayList<>();
    private List<MemberOwner> owners = new ArrayList<>();
    private MemberAdmin admin;
    private List<PhotoLab> photoLabs = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    private List<ReservationSlot> slots = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();
    private List<DevelopmentOrder> developmentOrders = new ArrayList<>();
    private List<ScannedPhoto> scannedPhotos = new ArrayList<>();
    private List<Post> posts = new ArrayList<>();

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (memberRepository.count() > 0) {
            log.info("{} Database already has data. Skipping seeding.", LOG_PREFIX);
            return;
        }

        log.info("{} ========================================", LOG_PREFIX);
        log.info("{} Starting Database Seeding (Dev Environment)", LOG_PREFIX);
        log.info("{} ========================================", LOG_PREFIX);

        try {
            // 1. Terms (약관)
            createTerms();
            log.info("{} Created {} terms", LOG_PREFIX, termsList.size());

            // 2. Region (지역)
            List<Region> regions = createRegions();
            log.info("{} Created {} regions", LOG_PREFIX, regions.size());

            // 3. Admin 생성
            admin = createAdmin();
            log.info("{} Created admin: {}", LOG_PREFIX, admin.getEmail());

            // 4. Owner 생성
            owners = createOwners(OWNER_NAMES.length);
            log.info("{} Created {} owners", LOG_PREFIX, owners.size());

            // 5. User 생성
            users = createUsers(10);
            log.info("{} Created {} users", LOG_PREFIX, users.size());

            // 6. MemberAgreement (약관 동의)
            createMemberAgreements();
            log.info("{} Created member agreements for all members", LOG_PREFIX);

            // 7. SocialAccount (소셜 계정)
            createSocialAccounts();
            log.info("{} Created social accounts for users", LOG_PREFIX);

            // 8. MemberAddress (배송지)
            createMemberAddresses();
            log.info("{} Created member addresses", LOG_PREFIX);

            // 9. Tags 생성
            createTags();
            log.info("{} Created {} tags", LOG_PREFIX, tags.size());

            // 10. PhotoLab 생성 (영업시간, 이미지, 공지, 문서, 태그 포함)
            photoLabs = createPhotoLabs(owners, regions);
            log.info("{} Created {} photo labs with business hours, images, notices, documents, tags", LOG_PREFIX, photoLabs.size());

            // 11. FavoritePhotoLab (찜한 현상소)
            createFavoritePhotoLabs();
            log.info("{} Created favorite photo labs", LOG_PREFIX);

            // 12. ReservationSlot (예약 슬롯)
            createReservationSlots(photoLabs);
            log.info("{} Created reservation slots for all photo labs", LOG_PREFIX);

            // 13. Reservation (예약)
            createReservations();
            log.info("{} Created {} reservations", LOG_PREFIX, reservations.size());

            // 14. DevelopmentOrder (현상 주문)
            createDevelopmentOrders();
            log.info("{} Created {} development orders", LOG_PREFIX, developmentOrders.size());

            // 15. ScannedPhoto (스캔 사진)
            createScannedPhotos();
            log.info("{} Created {} scanned photos", LOG_PREFIX, scannedPhotos.size());

            // 16. PrintOrder, PrintOrderItem, PrintOrderPhoto, Delivery (인화 주문)
            createPrintOrders();
            log.info("{} Created print orders with items and delivery info", LOG_PREFIX);

            // 17. Post, PostImage (커뮤니티 게시글)
            createPosts();
            log.info("{} Created {} posts with images", LOG_PREFIX, posts.size());

            // 18. PostLike, Comment (좋아요, 댓글)
            createPostInteractions();
            log.info("{} Created post likes and comments", LOG_PREFIX);

            // 19. Inquiry, InquiryReply, InquiryImage (문의)
            createInquiries();
            log.info("{} Created inquiries with replies and images", LOG_PREFIX);

            // 20. PhotoRestoration (AI 복원)
            createPhotoRestorations();
            log.info("{} Created photo restorations", LOG_PREFIX);

            // 21. Payment (결제)
            createPayments();
            log.info("{} Created payments", LOG_PREFIX);

            // 22. TokenHistory (토큰 이력)
            createTokenHistory();
            log.info("{} Created token history", LOG_PREFIX);

            // 23. SearchHistory (검색 기록)
            createSearchHistory();
            log.info("{} Created search history", LOG_PREFIX);

            log.info("{} ========================================", LOG_PREFIX);
            log.info("{} Database Seeding Completed Successfully!", LOG_PREFIX);
            log.info("{} ========================================", LOG_PREFIX);

        } catch (Exception e) {
            log.error("{} Database seeding failed", LOG_PREFIX, e);
            throw e;
        }
    }

    // ===== Terms =====
    private void createTerms() {
        List<Terms> termsToSave = new ArrayList<>();

        Terms service = Terms.builder()
                .type(TermsType.SERVICE)
                .version("v1.0")
                .title("서비스 이용약관")
                .content("서비스 이용약관의 상세 내용입니다. 본 약관은 Finders 서비스 이용에 관한 조건 및 절차, 회사와 회원의 권리, 의무 및 책임사항 등을 규정함을 목적으로 합니다.")
                .isRequired(true)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        Terms privacy = Terms.builder()
                .type(TermsType.PRIVACY)
                .version("v1.0")
                .title("개인정보 처리방침")
                .content("개인정보 처리방침의 상세 내용입니다. Finders는 이용자의 개인정보를 중요시하며, 개인정보보호법을 준수하고 있습니다.")
                .isRequired(true)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        Terms location = Terms.builder()
                .type(TermsType.LOCATION)
                .version("v1.0")
                .title("위치정보 이용약관")
                .content("위치정보 이용약관의 상세 내용입니다. 본 약관은 위치기반서비스 이용에 관한 조건을 규정합니다.")
                .isRequired(false)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        Terms notification = Terms.builder()
                .type(TermsType.NOTIFICATION)
                .version("v1.0")
                .title("마케팅 정보 수신 동의")
                .content("마케팅 정보 수신 동의입니다. 이벤트, 프로모션 등의 정보를 받아보실 수 있습니다.")
                .isRequired(false)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        termsToSave.add(service);
        termsToSave.add(privacy);
        termsToSave.add(location);
        termsToSave.add(notification);

        // Batch persist
        for (Terms terms : termsToSave) {
            entityManager.persist(terms);
        }
        entityManager.flush();

        termsList.addAll(termsToSave);
    }

    // ===== Regions =====
    private List<Region> createRegions() {
        List<Region> regions = new ArrayList<>();

        // 시/도 생성 (parentRegion = null)
        List<Region> sidoList = new ArrayList<>();
        for (String sidoName : SIDO_NAMES) {
            Region sido = Region.builder()
                    .parentRegion(null)
                    .regionName(sidoName)
                    .build();
            sidoList.add(sido);
        }
        regionRepository.saveAll(sidoList);

        // 시/군/구 생성 (parentRegion이 상위 지역)
        List<Region> districtsToSave = new ArrayList<>();
        for (int i = 0; i < sidoList.size(); i++) {
            for (String district : DISTRICT_DATA[i]) {
                Region region = Region.builder()
                        .parentRegion(sidoList.get(i))
                        .regionName(district)
                        .build();
                districtsToSave.add(region);
            }
        }
        regions.addAll(regionRepository.saveAll(districtsToSave));

        return regions;
    }

    // ===== Admin =====
    private MemberAdmin createAdmin() {
        MemberAdmin admin = MemberAdmin.builder()
                .name("관리자")
                .email("admin@finders.it.kr")
                .phone("010-0000-0000")
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .build();
        return memberRepository.save(admin);
    }

    // ===== Owners =====
    private List<MemberOwner> createOwners(int count) {
        List<MemberOwner> ownersToSave = new ArrayList<>();
        String encodedOwnerPassword = passwordEncoder.encode(OWNER_PASSWORD);

        for (int i = 0; i < count; i++) {
            MemberOwner owner = MemberOwner.builder()
                    .name(OWNER_NAMES[i])
                    .email(OWNER_EMAILS[i])
                    .phone(OWNER_PHONES[i])
                    .passwordHash(encodedOwnerPassword)
                    .build();
            ownersToSave.add(owner);
        }

        return memberRepository.saveAll(ownersToSave).stream()
                .filter(m -> m instanceof MemberOwner)
                .map(m -> (MemberOwner) m)
                .toList();
    }

    // ===== Users =====
    private List<MemberUser> createUsers(int count) {
        List<MemberUser> usersToSave = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            MemberUser user = MemberUser.builder()
                    .name(USER_NAMES[i])
                    .email("user" + (i + 1) + "@test.com")
                    .phone(USER_PHONES[i])
                    .nickname(USER_NICKNAMES[i])
                    .profileImage(null)
                    .build();
            usersToSave.add(user);
        }

        return memberRepository.saveAll(usersToSave).stream()
                .filter(m -> m instanceof MemberUser)
                .map(m -> (MemberUser) m)
                .toList();
    }

    // ===== MemberAgreement =====
    private void createMemberAgreements() {
        Terms serviceTerms = termsList.stream().filter(t -> t.getType() == TermsType.SERVICE).findFirst().orElse(null);
        Terms privacyTerms = termsList.stream().filter(t -> t.getType() == TermsType.PRIVACY).findFirst().orElse(null);

        List<Member> allMembers = new ArrayList<>();
        allMembers.add(admin);
        allMembers.addAll(owners);
        allMembers.addAll(users);

        List<MemberAgreement> agreementsToSave = new ArrayList<>();
        for (int idx = 0; idx < allMembers.size(); idx++) {
            Member member = allMembers.get(idx);
            int daysAgo = 7 + idx;

            if (serviceTerms != null) {
                MemberAgreement agreement = MemberAgreement.builder()
                        .member(member)
                        .terms(serviceTerms)
                        .isAgreed(true)
                        .agreedAt(LocalDateTime.now().minusDays(daysAgo))
                        .build();
                agreementsToSave.add(agreement);
            }

            if (privacyTerms != null) {
                MemberAgreement agreement = MemberAgreement.builder()
                        .member(member)
                        .terms(privacyTerms)
                        .isAgreed(true)
                        .agreedAt(LocalDateTime.now().minusDays(daysAgo))
                        .build();
                agreementsToSave.add(agreement);
            }
        }

        for (MemberAgreement agreement : agreementsToSave) {
            entityManager.persist(agreement);
        }
        entityManager.flush();
    }

    // ===== SocialAccount =====
    private void createSocialAccounts() {
        List<SocialAccount> accountsToSave = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            MemberUser user = users.get(i);

            if (i % 2 == 0) {
                SocialAccount kakaoAccount = SocialAccount.builder()
                        .user(user)
                        .provider(SocialProvider.KAKAO)
                        .providerId("kakao_" + String.format("%010d", 1000000000L + i))
                        .socialEmail(user.getEmail())
                        .build();
                accountsToSave.add(kakaoAccount);
            }

            if (i % 3 == 0) {
                SocialAccount appleAccount = SocialAccount.builder()
                        .user(user)
                        .provider(SocialProvider.APPLE)
                        .providerId("apple_user_" + (i + 1))
                        .socialEmail(user.getEmail())
                        .build();
                accountsToSave.add(appleAccount);
            }
        }

        for (SocialAccount account : accountsToSave) {
            entityManager.persist(account);
        }
        entityManager.flush();
    }

    // ===== MemberAddress =====
    private void createMemberAddresses() {
        List<MemberAddress> addressesToSave = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            MemberUser user = users.get(i);
            int addressCount = (i % 3) + 1; // 1~3개의 주소

            for (int j = 0; j < addressCount && j < ADDRESS_DATA.length; j++) {
                MemberAddress address = MemberAddress.builder()
                        .user(user)
                        .addressName(ADDRESS_DATA[j][0])
                        .zipcode(ADDRESS_DATA[j][1])
                        .address(ADDRESS_DATA[j][2])
                        .addressDetail(ADDRESS_DATA[j][3])
                        .isDefault(j == 0)
                        .build();
                addressesToSave.add(address);
            }
        }

        // Batch persist
        for (MemberAddress address : addressesToSave) {
            entityManager.persist(address);
        }
        entityManager.flush();
    }

    // ===== Tags =====
    private void createTags() {
        List<Tag> tagsToSave = new ArrayList<>();

        for (String name : TAG_NAMES) {
            Tag tag = Tag.builder().name(name).build();
            tagsToSave.add(tag);
        }

        // Batch persist
        for (Tag tag : tagsToSave) {
            entityManager.persist(tag);
        }
        entityManager.flush();

        tags.addAll(tagsToSave);
    }

    // ===== PhotoLabs =====
    private List<PhotoLab> createPhotoLabs(List<MemberOwner> owners, List<Region> regions) {
        List<PhotoLab> photoLabsResult = new ArrayList<>();

        for (int i = 0; i < LAB_DATA.length; i++) {
            MemberOwner owner = owners.get(i % owners.size());
            Region region = regions.get(i % regions.size());

            PhotoLab lab = PhotoLab.builder()
                    .owner(owner)
                    .region(region)
                    .name(LAB_DATA[i][0])
                    .description(LAB_DATA[i][1])
                    .phone(LAB_PHONES[i])
                    .zipcode(LAB_ZIPCODES[i])
                    .address(LAB_DATA[i][2])
                    .addressDetail(LAB_ADDRESS_DETAILS[i])
                    .latitude(new BigDecimal(LAB_DATA[i][3]))
                    .longitude(new BigDecimal(LAB_DATA[i][4]))
                    .status(PhotoLabStatus.ACTIVE)
                    .isDeliveryAvailable(i % 2 == 0)
                    .maxReservationsPerHour(3)
                    .avgWorkTime(LAB_AVG_WORK_TIMES[i])
                    .build();

            photoLabsResult.add(photoLabRepository.save(lab));
            entityManager.flush();

            createBusinessHours(lab);
            createLabImages(lab);
            createLabNotices(lab);
            createLabDocuments(lab);
            createLabTags(lab, i);
        }

        return photoLabsResult;
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
                    .isMain(i == 0)
                    .build();

            entityManager.persist(image);
        }
    }

    private void createLabNotices(PhotoLab lab) {
        // 일반 공지
        PhotoLabNotice notice1 = PhotoLabNotice.builder()
                .photoLab(lab)
                .title("영업시간 안내")
                .content("평일 10:00 ~ 19:00, 토요일 10:00 ~ 17:00, 일요일 휴무입니다.")
                .noticeType(NoticeType.GENERAL)
                .isActive(true)
                .build();
        entityManager.persist(notice1);

        // 이벤트 공지
        PhotoLabNotice notice2 = PhotoLabNotice.builder()
                .photoLab(lab)
                .title("신규 오픈 이벤트")
                .content("첫 주문 시 스캔 무료! 2025년 2월까지 진행됩니다.")
                .noticeType(NoticeType.EVENT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .isActive(true)
                .build();
        entityManager.persist(notice2);
    }

    private void createLabDocuments(PhotoLab lab) {
        // 사업자등록증
        PhotoLabDocument doc1 = PhotoLabDocument.builder()
                .photoLab(lab)
                .documentType(DocumentType.BUSINESS_LICENSE)
                .objectPath("labs/" + lab.getId() + "/documents/business_license.pdf")
                .fileName("사업자등록증.pdf")
                .verifiedAt(LocalDateTime.now().minusDays(30))
                .build();
        entityManager.persist(doc1);
    }

    private void createLabTags(PhotoLab lab, int labIndex) {
        // 각 현상소에 3~5개의 태그 할당
        int tagCount = 3 + (labIndex % 3);
        for (int i = 0; i < tagCount && i < tags.size(); i++) {
            int tagIdx = (labIndex + i) % tags.size();
            PhotoLabTag labTag = PhotoLabTag.builder()
                    .photoLab(lab)
                    .tag(tags.get(tagIdx))
                    .build();
            entityManager.persist(labTag);
        }
    }

    // ===== FavoritePhotoLab =====
    private void createFavoritePhotoLabs() {
        for (int i = 0; i < users.size(); i++) {
            MemberUser user = users.get(i);
            // 일부 유저에게 찜한 현상소 추가
            if (i % 2 == 0 && !photoLabs.isEmpty()) {
                PhotoLab favLab = photoLabs.get(i % photoLabs.size());
                FavoritePhotoLab favorite = new FavoritePhotoLab(user, favLab);
                entityManager.persist(favorite);
            }
        }
    }

    // ===== ReservationSlots =====
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

                        ReservationSlot savedSlot = reservationSlotRepository.save(slot);
                        slots.add(savedSlot);
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
        }
    }

    // ===== Reservations =====
    private void createReservations() {
        int reservationCount = 0;
        int[] rollCounts = {2, 1, 3, 2, 4, 1, 3, 2, 1, 2};

        for (int i = 0; i < slots.size() && reservationCount < 10; i += 10) {
            ReservationSlot slot = slots.get(i);
            MemberUser user = users.get(reservationCount % users.size());

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .slot(slot)
                    .photoLab(slot.getPhotoLab())
                    .status(ReservationStatus.RESERVED)
                    .isDevelop(true)
                    .isScan(true)
                    .isPrint(reservationCount % 2 == 0)
                    .rollCount(rollCounts[reservationCount])
                    .requestMessage("테스트 예약입니다. 조심히 다뤄주세요.")
                    .build();

            entityManager.persist(reservation);
            reservations.add(reservation);

            slot.increaseReservedCountOrThrow();
            reservationCount++;
        }
    }

    // ===== DevelopmentOrders =====
    private void createDevelopmentOrders() {
        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);

            String orderCode = generateOrderCode("DO");
            DevelopmentOrderStatus status = i < 3 ? DevelopmentOrderStatus.COMPLETED : DevelopmentOrderStatus.RECEIVED;

            DevelopmentOrder order = DevelopmentOrder.builder()
                    .reservation(reservation)
                    .photoLab(reservation.getPhotoLab())
                    .user(reservation.getUser())
                    .orderCode(orderCode)
                    .status(status)
                    .isDevelop(reservation.isDevelop())
                    .isScan(reservation.isScan())
                    .isPrint(reservation.isPrint())
                    .rollCount(reservation.getRollCount())
                    .totalPhotos(reservation.getRollCount() * 36)
                    .totalPrice(15000 * reservation.getRollCount())
                    .completedAt(status == DevelopmentOrderStatus.COMPLETED ? LocalDateTime.now().minusDays(1) : null)
                    .build();

            entityManager.persist(order);
            developmentOrders.add(order);
        }
    }

    // ===== ScannedPhotos =====
    private void createScannedPhotos() {
        for (DevelopmentOrder order : developmentOrders) {
            if (order.getStatus() == DevelopmentOrderStatus.COMPLETED) {
                // 완료된 주문에 스캔 사진 추가
                int photoCount = Math.min(order.getTotalPhotos(), 10); // 최대 10장
                for (int i = 0; i < photoCount; i++) {
                    ScannedPhoto photo = ScannedPhoto.create(
                            order,
                            "orders/" + order.getId() + "/scans/" + UUID.randomUUID() + ".jpg",
                            "scan_" + (i + 1) + ".jpg",
                            i
                    );
                    entityManager.persist(photo);
                    scannedPhotos.add(photo);
                }
            }
        }
    }

    // ===== PrintOrders =====
    private void createPrintOrders() {
        int orderIndex = 0;
        for (DevelopmentOrder devOrder : developmentOrders) {
            if (devOrder.getStatus() == DevelopmentOrderStatus.COMPLETED && devOrder.hasPrintTask()) {
                boolean isDeliveryOrder = (orderIndex % 2 == 1);
                ReceiptMethod receiptMethod = isDeliveryOrder ? ReceiptMethod.DELIVERY : ReceiptMethod.PICKUP;

                PrintOrder printOrder = PrintOrder.builder()
                        .developmentOrder(devOrder)
                        .photoLab(devOrder.getPhotoLab())
                        .user(devOrder.getUser())
                        .orderCode(generateOrderCode("PO"))
                        .status(com.finders.api.domain.photo.enums.PrintOrderStatus.PENDING)
                        .totalPrice(isDeliveryOrder ? 8000 : 5000)
                        .receiptMethod(receiptMethod)
                        .build();

                entityManager.persist(printOrder);
                entityManager.flush();

                if (isDeliveryOrder) {
                    MemberUser user = devOrder.getUser();
                    Delivery delivery = Delivery.builder()
                            .printOrder(printOrder)
                            .recipientName(user.getName())
                            .phone(user.getPhone())
                            .zipcode("06234")
                            .address("서울 강남구 테헤란로 123")
                            .addressDetail("아파트 101호")
                            .status(DeliveryStatus.PENDING)
                            .deliveryFee(3000)
                            .build();
                    entityManager.persist(delivery);
                }

                PrintOrderItem item = PrintOrderItem.builder()
                        .printOrder(printOrder)
                        .filmType(FilmType.COLOR_NEG)
                        .paperType(PaperType.ECO_GLOSSY_260)
                        .printMethod(PrintMethod.CPRINT)
                        .size(PrintSize.SIZE_5x7)
                        .frameType(FrameType.NO_FRAME)
                        .unitPrice(1400)
                        .totalPrice(5000)
                        .build();
                entityManager.persist(item);

                List<ScannedPhoto> orderPhotos = scannedPhotos.stream()
                        .filter(p -> p.getOrder().getId().equals(devOrder.getId()))
                        .limit(3)
                        .toList();

                for (ScannedPhoto photo : orderPhotos) {
                    PrintOrderPhoto pop = PrintOrderPhoto.builder()
                            .printOrder(printOrder)
                            .scannedPhoto(photo)
                            .quantity(1)
                            .build();
                    entityManager.persist(pop);
                }

                orderIndex++;
            }
        }
    }

    // ===== Posts =====
    private void createPosts() {
        for (int i = 0; i < POST_DATA.length; i++) {
            MemberUser user = users.get(i % users.size());
            PhotoLab lab = (i % 2 == 0) ? photoLabs.get(i % photoLabs.size()) : null;

            Post post = Post.builder()
                    .memberUser(user)
                    .photoLab(lab)
                    .isSelfDeveloped(lab == null)
                    .title(POST_DATA[i][0])
                    .content(POST_DATA[i][1])
                    .labReview(lab != null ? "현상 퀄리티가 매우 좋았습니다!" : null)
                    .build();

            entityManager.persist(post);
            posts.add(post);
            entityManager.flush();

            // 이미지 추가 (배치)
            List<PostImage> imagesToSave = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                PostImage image = PostImage.builder()
                        .post(post)
                        .objectPath("posts/" + post.getId() + "/image_" + (j + 1) + ".jpg")
                        .displayOrder(j)
                        .width(1920)
                        .height(1280)
                        .build();
                imagesToSave.add(image);
            }
            for (PostImage image : imagesToSave) {
                entityManager.persist(image);
            }
        }
        entityManager.flush();
    }

    // ===== Post Interactions (Likes, Comments) =====
    private void createPostInteractions() {
        List<PostLike> likesToSave = new ArrayList<>();
        List<Comment> commentsToSave = new ArrayList<>();
        int[] likeCounts = {3, 2, 4, 2, 3};
        int[] commentCounts = {2, 1, 3, 2, 1};

        for (int postIdx = 0; postIdx < posts.size(); postIdx++) {
            Post post = posts.get(postIdx);
            int likeCount = likeCounts[postIdx % likeCounts.length];

            for (int i = 0; i < likeCount && i < users.size(); i++) {
                MemberUser user = users.get(i);
                if (!user.getId().equals(post.getMemberUser().getId())) {
                    PostLike like = PostLike.builder()
                            .post(post)
                            .memberUser(user)
                            .build();
                    likesToSave.add(like);
                    post.increaseLikeCount();
                }
            }

            int commentCount = commentCounts[postIdx % commentCounts.length];
            for (int i = 0; i < commentCount; i++) {
                MemberUser commenter = users.get((i + 1) % users.size());
                Comment comment = Comment.builder()
                        .post(post)
                        .memberUser(commenter)
                        .content(COMMENT_CONTENTS[(postIdx + i) % COMMENT_CONTENTS.length])
                        .build();
                commentsToSave.add(comment);
                post.increaseCommentCount();
            }
        }

        for (PostLike like : likesToSave) {
            entityManager.persist(like);
        }
        for (Comment comment : commentsToSave) {
            entityManager.persist(comment);
        }
        entityManager.flush();
    }

    // ===== Inquiries =====
    private void createInquiries() {
        for (int i = 0; i < INQUIRY_DATA.length; i++) {
            MemberUser user = users.get(i % users.size());
            PhotoLab lab = (i % 2 == 0) ? photoLabs.get(i % photoLabs.size()) : null;

            Inquiry inquiry = Inquiry.builder()
                    .member(user)
                    .photoLab(lab)
                    .title(INQUIRY_DATA[i][0])
                    .content(INQUIRY_DATA[i][1])
                    .build();

            entityManager.persist(inquiry);
            entityManager.flush();

            // 이미지 추가
            if (i % 2 == 0) {
                InquiryImage image = InquiryImage.builder()
                        .inquiry(inquiry)
                        .objectPath("inquiries/" + inquiry.getId() + "/image_1.jpg")
                        .displayOrder(0)
                        .build();
                entityManager.persist(image);
            }

            // 답변 추가
            if (i < 2) {
                Member replier = (lab != null) ? lab.getOwner() : admin;
                InquiryReply reply = InquiryReply.builder()
                        .inquiry(inquiry)
                        .replier(replier)
                        .content("안녕하세요! 문의 주셔서 감사합니다. " + (i == 0 ? "일반적으로 2~3일 정도 소요됩니다." : "네, 배송 서비스 이용 가능합니다."))
                        .build();
                entityManager.persist(reply);
            }
        }
        entityManager.flush();
    }

    // ===== PhotoRestorations =====
    private void createPhotoRestorations() {
        List<PhotoRestoration> restorationsToSave = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            MemberUser user = users.get(i % users.size());

            PhotoRestoration restoration = PhotoRestoration.builder()
                    .memberId(user.getId())
                    .originalPath("restorations/" + user.getId() + "/original/" + UUID.randomUUID() + ".jpg")
                    .maskPath("restorations/" + user.getId() + "/mask/" + UUID.randomUUID() + ".png")
                    .tokenUsed(1)
                    .build();

            // 일부는 완료 상태로
            if (i == 0) {
                restoration.complete(
                        "restorations/" + user.getId() + "/restored/" + UUID.randomUUID() + ".jpg",
                        1920,
                        1280
                );
            }

            restorationsToSave.add(restoration);
        }

        // Batch persist
        for (PhotoRestoration restoration : restorationsToSave) {
            entityManager.persist(restoration);
        }
        entityManager.flush();
    }

    // ===== Payments =====
    private void createPayments() {
        List<Payment> paymentsToSave = new ArrayList<>();

        for (int i = 0; i < 3 && i < users.size(); i++) {
            MemberUser user = users.get(i);

            Payment payment = Payment.builder()
                    .member(user)
                    .orderType(OrderType.TOKEN_PURCHASE)
                    .relatedOrderId(null)
                    .paymentId("payment_" + UUID.randomUUID().toString().substring(0, 8))
                    .orderName("토큰 10개 구매")
                    .amount(10000)
                    .tokenAmount(10)
                    .build();

            paymentsToSave.add(payment);
        }

        // Batch persist
        for (Payment payment : paymentsToSave) {
            entityManager.persist(payment);
        }
        entityManager.flush();
    }

    // ===== TokenHistory =====
    private void createTokenHistory() {
        List<TokenHistory> historiesToSave = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            MemberUser user = users.get(i);

            // 초기 토큰 지급
            TokenHistory history = TokenHistory.builder()
                    .member(user)
                    .type(TokenHistoryType.PURCHASE)
                    .amount(5)
                    .balanceAfter(5)
                    .relatedType(TokenRelatedType.PAYMENT)
                    .relatedId(null)
                    .description("회원가입 보너스 토큰")
                    .build();

            historiesToSave.add(history);

            // 일부 유저는 토큰 사용 이력 추가
            if (i % 3 == 0) {
                TokenHistory useHistory = TokenHistory.builder()
                        .member(user)
                        .type(TokenHistoryType.USE)
                        .amount(-1)
                        .balanceAfter(4)
                        .relatedType(TokenRelatedType.PHOTO_RESTORATION)
                        .relatedId(null)
                        .description("AI 복원 서비스 이용")
                        .build();
                historiesToSave.add(useHistory);
            }
        }

        for (TokenHistory history : historiesToSave) {
            entityManager.persist(history);
        }
        entityManager.flush();
    }

    // ===== SearchHistory =====
    private void createSearchHistory() {
        String[] keywords = {"필름 현상", "흑백 필름", "스캔", "인화", "강남 현상소"};

        for (int i = 0; i < 5; i++) {
            MemberUser user = users.get(i % users.size());
            SearchHistory history = SearchHistory.builder()
                    .memberUser(user)
                    .keyword(keywords[i])
                    .build();
            entityManager.persist(history);
        }
        entityManager.flush();
    }

    // ===== Utility =====
    private String generateOrderCode(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + "-" + date + "-" + random;
    }
}
