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
import com.finders.api.domain.terms.entity.TermsSocialMapping;
import com.finders.api.domain.terms.enums.TermsType;
import com.finders.api.domain.terms.repository.TermsRepository;
import com.finders.api.domain.terms.repository.TermsSocialMappingRepository;
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
    private static final String[] OWNER_NAMES = {
            "김사장", "이대표", "박점장", "최관장", "정원장",
            "강대표", "윤사장", "한점장", "조관장", "신대표",
            "임사장", "오점장", "서대표", "안관장", "유사장"
    };
    private static final String[] OWNER_EMAILS = {
            "owner1@finders.it.kr", "owner2@finders.it.kr", "owner3@finders.it.kr",
            "owner4@finders.it.kr", "owner5@finders.it.kr", "owner6@finders.it.kr",
            "owner7@finders.it.kr", "owner8@finders.it.kr", "owner9@finders.it.kr",
            "owner10@finders.it.kr", "owner11@finders.it.kr", "owner12@finders.it.kr",
            "owner13@finders.it.kr", "owner14@finders.it.kr", "owner15@finders.it.kr"
    };
    private static final String[] OWNER_PHONES = {
            "010-1111-0001", "010-1111-0002", "010-1111-0003", "010-1111-0004", "010-1111-0005",
            "010-1111-0006", "010-1111-0007", "010-1111-0008", "010-1111-0009", "010-1111-0010",
            "010-1111-0011", "010-1111-0012", "010-1111-0013", "010-1111-0014", "010-1111-0015"
    };
    private static final String OWNER_PASSWORD = "owner1234!";

    // ===== Constants: User Data =====
    private static final String[] USER_NAMES = {
            "김민수", "이영희", "박지훈", "최서연", "정우성",
            "강미나", "윤도현", "한소희", "조현우", "신예은",
            "권태호", "송지안", "황민지", "전승현", "백서윤",
            "남궁현", "류지원", "문예진", "장하늘", "심유나"
    };
    private static final String[] USER_PHONES = {
            "010-2222-0001", "010-2222-0002", "010-2222-0003", "010-2222-0004", "010-2222-0005",
            "010-2222-0006", "010-2222-0007", "010-2222-0008", "010-2222-0009", "010-2222-0010",
            "010-2222-0011", "010-2222-0012", "010-2222-0013", "010-2222-0014", "010-2222-0015",
            "010-2222-0016", "010-2222-0017", "010-2222-0018", "010-2222-0019", "010-2222-0020"
    };
    private static final String[] USER_NICKNAMES = {
            "민수_film", "영희_photo", "지훈_cam", "서연_snap", "우성_lens",
            "미나_shot", "도현_roll", "소희_dev", "현우_print", "예은_scan",
            "태호_analog", "지안_35mm", "민지_color", "승현_bw", "서윤_kodak",
            "현_fuji", "지원_ilford", "예진_portra", "하늘_ektachrome", "유나_cinestill"
    };

    // ===== Constants: PhotoLab Phone/Address =====
    // 50개 현상소를 위한 확장 데이터 (순환 사용)
    private static final String[] LAB_PHONES = {
            "02-1234-5678", "02-2345-6789", "02-3456-7890", "02-4567-8901", "02-5678-9012",
            "031-111-2222", "031-222-3333", "031-333-4444", "031-444-5555", "031-555-6666",
            "051-111-1111", "051-222-2222", "051-333-3333", "032-111-1111", "032-222-2222",
            "053-111-1111", "053-222-2222", "02-6789-0123", "02-7890-1234", "02-8901-2345"
    };
    private static final String[] LAB_ZIPCODES = {
            "06234", "04157", "04779", "06035", "04104", "13487", "16514", "17015", "10391", "14055",
            "48058", "48301", "47285", "21554", "21318", "42180", "42703", "04542", "07236", "08390"
    };
    private static final String[] LAB_ADDRESS_DETAILS = {"1층", "2층", "3층", "지하1층", "4층", "5층", "101호", "201호", "301호", "B1층"};
    private static final int[] LAB_AVG_WORK_TIMES = {3, 5, 4, 2, 7, 3, 4, 5, 6, 2};

    // ===== Constants: Print Order Pricing =====
    private static final int PRINT_ORDER_PICKUP_PRICE = 5000;
    private static final int PRINT_ORDER_DELIVERY_PRICE = 8000;
    private static final int DELIVERY_FEE = 3000;

    // ===== Constants: Region Data =====
    private static final String[] SIDO_NAMES = {"서울", "경기", "부산", "인천", "대구"};
    private static final String[][] DISTRICT_DATA = {
            {"강남구", "마포구", "성동구", "종로구", "용산구", "서초구", "송파구", "영등포구", "강서구", "관악구"},
            {"성남시", "수원시", "용인시", "고양시", "안양시", "부천시"},
            {"해운대구", "수영구", "부산진구", "동래구"},
            {"남동구", "부평구", "연수구"},
            {"수성구", "달서구", "중구"}
    };

    // ===== Constants: Address Data =====
    private static final String[][] ADDRESS_DATA = {
            {"집", "06234", "서울 강남구 테헤란로 123", "아파트 101호"},
            {"회사", "04157", "서울 마포구 와우산로 45", "오피스빌딩 5층"},
            {"부모님댁", "48058", "부산 해운대구 해운대로 789", "해운대아파트 301호"}
    };

    // ===== Constants: Tag Data =====
    private static final String[] TAG_NAMES = {
            "따뜻한 색감", "청량한", "빈티지한", "영화용 필름", "택배 접수"
    };

    // ===== Constants: PhotoLab Data =====
    private static final String[][] LAB_DATA = {
            // 서울 (20개)
            {"필름공방 강남점", "필름 현상 전문점입니다. 35mm, 중형, 대형 모든 포맷을 취급합니다.", "서울 강남구 테헤란로 123", "37.5012", "127.0396"},
            {"사진연구소 마포", "컬러/흑백 현상, 스캔, 인화까지 원스톱 서비스를 제공합니다.", "서울 마포구 홍대입구 45", "37.5563", "126.9220"},
            {"빛과필름 성수", "아날로그 감성을 담은 필름 현상소입니다.", "서울 성동구 성수이로 78", "37.5447", "127.0558"},
            {"아날로그랩 종로", "40년 전통의 필름 현상 장인이 운영합니다.", "서울 종로구 인사동길 25", "37.5729", "126.9850"},
            {"필름스토리 용산", "빈티지 카메라 수리와 현상을 함께 합니다.", "서울 용산구 이태원로 55", "37.5344", "126.9879"},
            {"컬러웍스 서초", "프리미엄 스캔 전문점입니다. 고해상도 드럼스캔 가능.", "서울 서초구 강남대로 421", "37.4979", "127.0276"},
            {"모노크롬 송파", "흑백 필름 전문 현상소. 수작업 프린팅 가능.", "서울 송파구 올림픽로 300", "37.5145", "127.1058"},
            {"필름애비뉴 영등포", "즉석 현상과 당일 스캔 서비스 제공.", "서울 영등포구 여의대로 108", "37.5259", "126.9250"},
            {"스냅랩 강서", "인스타그램 감성의 따뜻한 톤 보정 전문.", "서울 강서구 화곡로 264", "37.5418", "126.8493"},
            {"필름메이트 관악", "학생 할인 프로모션 상시 진행.", "서울 관악구 관악로 1", "37.4784", "126.9516"},
            {"포토아틀리에 홍대", "예술가들이 사랑하는 감성 현상소.", "서울 마포구 와우산로 94", "37.5536", "126.9266"},
            {"레트로필름 연남", "레트로 감성 가득한 동네 현상소.", "서울 마포구 연남로 35", "37.5667", "126.9234"},
            {"필름프렌즈 합정", "친절한 상담과 꼼꼼한 현상으로 유명.", "서울 마포구 양화로 45", "37.5496", "126.9137"},
            {"아날로그타운 이태원", "외국인 손님도 많이 찾는 글로벌 현상소.", "서울 용산구 이태원로27가길 8", "37.5346", "126.9924"},
            {"필름버스터 건대", "젊은 감성의 트렌디한 현상소.", "서울 광진구 아차산로 243", "37.5407", "127.0688"},
            {"라이카랩 청담", "프리미엄 서비스와 고품질 현상.", "서울 강남구 도산대로 521", "37.5242", "127.0402"},
            {"필름하우스 잠실", "롯데월드 인근 편리한 위치.", "서울 송파구 올림픽로 240", "37.5130", "127.1025"},
            {"오렌지필름 신촌", "대학가 학생들의 아지트.", "서울 서대문구 연세로 50", "37.5584", "126.9388"},
            {"필름마스터 역삼", "IT업계 직장인들이 많이 찾는 곳.", "서울 강남구 역삼로 180", "37.5012", "127.0365"},
            {"블랙앤화이트 을지로", "을지로 골목 숨은 흑백 전문점.", "서울 중구 을지로 157", "37.5668", "126.9918"},
            // 경기 (12개)
            {"성남필름랩", "분당 지역 대표 현상소.", "경기 성남시 분당구 판교로 255", "37.3947", "127.1112"},
            {"수원아날로그", "수원역 인근 접근성 좋은 현상소.", "경기 수원시 팔달구 인계로 123", "37.2636", "127.0286"},
            {"용인필름스튜디오", "넓은 공간과 편안한 분위기.", "경기 용인시 수지구 죽전로 144", "37.3254", "127.1065"},
            {"고양필름공장", "대량 현상 주문 가능한 전문 시설.", "경기 고양시 일산서구 중앙로 1455", "37.6584", "126.7515"},
            {"안양포토랩", "안양예술공원 근처 예술적 공간.", "경기 안양시 만안구 예술공원로 131", "37.4028", "126.9181"},
            {"부천필름클럽", "동호회 회원 할인 혜택.", "경기 부천시 원미구 길주로 210", "37.5035", "126.7660"},
            {"판교필름아트", "판교 IT밸리 직장인 단골집.", "경기 성남시 분당구 판교역로 192", "37.3947", "127.1112"},
            {"일산사진관", "30년 전통 사진관의 필름 현상 서비스.", "경기 고양시 일산동구 무궁화로 42", "37.6590", "126.7735"},
            {"광교필름스테이션", "광교호수공원 뷰 멋진 현상소.", "경기 수원시 영통구 광교호수로 77", "37.2901", "127.0462"},
            {"동탄아날로그하우스", "신도시 젊은 층 인기.", "경기 화성시 동탄중심상가1길 25", "37.2065", "127.0738"},
            {"평택필름센터", "평택역 5분 거리 편리한 위치.", "경기 평택시 평택로 89", "36.9921", "127.0857"},
            {"김포필름스토어", "공항 가기 전 급한 현상 가능.", "경기 김포시 김포한강2로 76", "37.6324", "126.7156"},
            // 부산 (8개)
            {"해운대필름", "바다가 보이는 현상소.", "부산 해운대구 해운대해변로 264", "35.1587", "129.1604"},
            {"광안리포토랩", "광안대교 야경 스팟 인근.", "부산 수영구 광안해변로 225", "35.1531", "129.1187"},
            {"서면아날로그", "서면 중심가 접근성 최고.", "부산 부산진구 중앙대로 680", "35.1579", "129.0600"},
            {"동래필름마을", "온천장 근처 아늑한 공간.", "부산 동래구 온천장로 107", "35.2050", "129.0785"},
            {"센텀필름", "센텀시티 내 프리미엄 서비스.", "부산 해운대구 센텀중앙로 90", "35.1695", "129.1316"},
            {"부산역포토", "부산역 KTX 이용객 인기.", "부산 동구 중앙대로 206", "35.1152", "129.0403"},
            {"남포필름거리", "BIFF광장 인근 관광객 환영.", "부산 중구 광복로 62", "35.0988", "129.0275"},
            {"기장필름스테이션", "기장 바닷가 풍경 맛집.", "부산 기장군 기장읍 차성로 221", "35.2445", "129.2190"},
            // 인천 (5개)
            {"인천필름아트", "인천공항 이용객 급한 현상 가능.", "인천 중구 영종대로 76", "37.4602", "126.4407"},
            {"송도필름클래식", "송도 국제도시 현대적 시설.", "인천 연수구 송도과학로 32", "37.3816", "126.6569"},
            {"부평아날로그", "부평역 지하상가 인근.", "인천 부평구 부평대로 38", "37.4899", "126.7234"},
            {"인천필름허브", "인천 대표 대형 현상소.", "인천 남동구 구월로 177", "37.4509", "126.7052"},
            {"차이나타운포토", "중국 여행 전후 현상 인기.", "인천 중구 차이나타운로 42", "37.4735", "126.6178"},
            // 대구 (5개)
            {"동성로필름", "대구 번화가 중심부.", "대구 중구 동성로 34", "35.8684", "128.5957"},
            {"수성못포토랩", "수성못 야경과 함께.", "대구 수성구 두산동로 55", "35.8290", "128.6185"},
            {"대구역필름스테이션", "대구역 앞 편리한 위치.", "대구 북구 태평로 161", "35.8796", "128.5954"},
            {"칠곡아날로그", "칠곡 주민 단골 현상소.", "대구 북구 칠곡중앙대로 442", "35.9426", "128.5665"},
            {"범어필름클럽", "범어네거리 직장인 인기.", "대구 수성구 범어로 155", "35.8595", "128.6282"}
    };

    // ===== Constants: Post Data =====
    private static final String[][] POST_DATA = {
            {"첫 필름 현상 후기", "처음으로 필름 카메라를 사용해봤는데 정말 감동적이었어요! 현상소에서 친절하게 설명해주셨습니다."},
            {"성수동 필름 현상소 추천", "성수동에 있는 빛과필름 현상소 다녀왔어요. 스캔 퀄리티가 정말 좋네요!"},
            {"흑백 필름의 매력", "컬러도 좋지만 흑백 필름만의 감성이 있는 것 같아요. 여러분은 어떻게 생각하세요?"},
            {"필름 보관 팁 공유", "필름 보관할 때 냉장고에 넣어두면 좋다고 하더라고요. 실제로 해보니 효과가 있는 것 같습니다."},
            {"주말 출사 사진 공유", "주말에 한강에서 찍은 사진들입니다. Kodak Portra 400으로 촬영했어요."},
            {"Kodak Gold 200 리뷰", "요즘 가장 핫한 필름 중 하나죠. 따뜻한 색감이 정말 예뻐요. 가성비도 최고!"},
            {"도쿄 여행 필름 사진", "지난 달 도쿄 여행 다녀왔어요. Fuji C200으로 찍었는데 일본 감성 물씬 나네요."},
            {"야경 필름 촬영 팁", "필름으로 야경 찍기 어렵다고 생각하셨죠? 몇 가지 팁 공유해드릴게요!"},
            {"중형 필름 입문기", "35mm만 쓰다가 처음으로 중형 카메라 질렀어요. 화질 차이가 어마어마하네요."},
            {"필름 스캔 직접 하기", "집에서 DSLR로 필름 스캔하는 방법 공유합니다. 장단점 있어요."},
            {"감성 카페 출사", "망원동 감성 카페에서 찍은 사진들이에요. Portra 800 짱!"},
            {"필름 카메라 수리 후기", "고장났던 올림푸스 수리 완료! 새 것처럼 됐어요."},
            {"빈티지 렌즈 추천", "필름 카메라에 잘 어울리는 빈티지 렌즈 추천해요."},
            {"제주도 필름 여행", "제주도 3박4일 필름 여행기입니다. 롤 10개 다 썼어요!"},
            {"필름 현상 비용 비교", "서울 주요 현상소 가격 비교해봤습니다. 참고하세요!"},
            {"Cinestill 800T 야경", "시네스틸로 찍은 네온사인 야경 공유합니다. 이 감성 인정?"},
            {"필름 초보 질문이요", "이제 막 필름 시작했는데 ISO랑 조리개 어떻게 맞추나요?"},
            {"홍대 스트릿 포토", "홍대에서 스트릿 포토 찍어봤어요. HP5+ 흑백의 매력!"},
            {"필름 만료 실험", "10년 지난 만료 필름 써봤습니다. 결과물이 독특해요."},
            {"부산 바다 필름", "부산 해운대에서 Ektar 100으로 찍은 바다 사진이에요."},
            {"필름 카메라 첫 구매", "필름 입문자인데 어떤 카메라 살까요? 추천 부탁드려요!"},
            {"라이카 M6 입양기", "드디어 평생 카메라 라이카 M6 질렀습니다!"},
            {"일회용 카메라의 재발견", "코닥 일회용 카메라 의외로 괜찮더라고요. 가볍게 들고 다니기 좋아요."},
            {"필름 동호회 가입했어요", "같이 출사 다닐 분들 있으신가요?"},
            {"비 오는 날 필름", "비 오는 날 필름 감성 최고예요. 물방울 표현이 예쁘더라고요."},
            {"필름 vs 디지털", "필름과 디지털 각각의 매력이 있는 것 같아요. 여러분 생각은?"},
            {"현상소 사장님 인터뷰", "단골 현상소 사장님 인터뷰했어요. 필름 시장 이야기 재밌네요."},
            {"자가 현상 도전기", "집에서 흑백 현상 처음 해봤어요. 생각보다 어렵지 않아요!"},
            {"벚꽃 시즌 필름", "벚꽃 시즌 Portra 160으로 담아봤습니다. 봄 느낌 물씬!"},
            {"필름 선물 추천", "필름 좋아하는 친구 생일 선물로 뭐가 좋을까요?"}
    };

    // ===== Constants: Comment Data =====
    private static final String[] COMMENT_CONTENTS = {
            "좋은 사진이네요!",
            "저도 가보고 싶어요~",
            "필름 감성 최고!",
            "어떤 카메라로 찍으셨나요?",
            "정보 감사합니다!",
            "색감이 너무 예뻐요!",
            "다음에 같이 출사 가요~",
            "이 현상소 저도 단골이에요!",
            "필름 종류가 뭔가요?",
            "스캔 퀄리티 대박이네요",
            "저도 이 필름 써봐야겠어요",
            "구도가 정말 좋네요",
            "분위기 있는 사진이에요",
            "저도 필름 시작하고 싶어요",
            "좋은 정보 공유 감사해요!"
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
    private final TermsRepository termsRepository;
    private final TermsSocialMappingRepository termsSocialMappingRepository;
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

            // TermsSocialMapping 생성
            createTermsSocialMappings();
            log.info("{} Created social mappings for Kakao tags", LOG_PREFIX);

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
            users = createUsers(USER_NAMES.length);
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
                .version("1.0")
                .title("서비스 이용약관")
                .content("""
                        ### 제 1 조 (목적)

                        본 약관은 파인더스(Finders)(이하 "회사")가 제공하는 필름 현상소 정보 및 중개 서비스(이하 "서비스")를 회원이 이용함에 있어 회사와 회원 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

                        ### 제 2 조 (용어의 정의)

                        1. "회사"란 파인더스 서비스를 운영하는 주체를 말합니다.
                        2. "회원"이란 본 약관에 동의하고 카카오 로그인을 통해 서비스에 가입한 자를 말합니다.
                        3. "현상소"란 회사와 제휴하여 필름 현상, 스캔 등 용역을 제공하는 업체를 말합니다.
                        4. "콘텐츠"란 회원이 서비스 내에 게시한 사진, 글, 댓글 등을 말합니다.

                        ### 제 3 조 (약관의 효력 및 변경)

                        본 약관은 회원이 회원가입 시 동의함으로써 효력이 발생하며, 회사는 관련 법령을 위반하지 않는 범위에서 약관을 변경할 수 있습니다.

                        ### 제 4 조 (제공 서비스)

                        회사는 다음의 서비스를 제공합니다.

                        1. 위치 기반 필름 현상소 정보 및 추천 서비스
                        2. 현상소 예약 및 진행 상태 안내 서비스
                        3. 필름 사진 커뮤니티 및 회원 간 상호작용 기능
                        4. 카카오 알림톡을 통한 진행 상태 및 안내 알림

                        ### 제 5 조 (중개 서비스의 책임 한계)

                        1. 회사는 회원과 현상소 간의 거래를 중개하는 플랫폼이며, 현상 용역의 직접 당사자가 아닙니다.
                        2. 필름의 분실, 손상, 결과물 품질에 대한 책임은 현상소에 있습니다.

                        ### 제 6 조 (콘텐츠의 권리)

                        1. 회원이 게시한 콘텐츠의 저작권은 회원에게 귀속됩니다.
                        2. 회사는 서비스 운영, 홍보 목적 범위 내에서 콘텐츠를 활용할 수 있습니다.""")
                .isRequired(true)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        Terms privacy = Terms.builder()
                .type(TermsType.PRIVACY)
                .version("1.0")
                .title("개인정보 수집·이용 동의")
                .content("""
                        ### 수집 항목

                        - 카카오 계정 정보: 이름, 카카오 닉네임, 프로필 이미지, 카카오계정(이메일)
                        - 연락처
                        - 서비스 이용 기록
                        - 위치정보 (현상소 추천 목적)

                        ### 이용 목적

                        - 회원 식별 및 서비스 제공
                        - 예약 및 진행 상태 안내
                        - 고객 문의 응대

                        ### 보유 및 이용 기간

                        - 회원 탈퇴 시까지
                        - 관련 법령에 따라 보존이 필요한 경우 해당 기간까지

                        ※ 회원은 동의를 거부할 권리가 있으나, 필수 항목 미동의 시 서비스 이용이 제한됩니다.""")
                .isRequired(true)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        Terms serviceInfo = Terms.builder()
                .type(TermsType.SERVICE_INFO)
                .version("1.0")
                .title("알림 수신 동의")
                .content("""
                        ### 필수 알림 (동의 필수)

                        - 현상 서비스 예약 확정, 취소, 진행 상태 안내
                        - 서비스 운영 관련 공지""")
                .isRequired(true)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        Terms marketing = Terms.builder()
                .type(TermsType.MARKETING)
                .version("1.0")
                .title("알림 수신 동의")
                .content("""
                        ### 선택 알림 (선택)

                        - 이벤트, 프로모션, 신규 기능 안내""")
                .isRequired(false)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        Terms location = Terms.builder()
                .type(TermsType.LOCATION)
                .version("1.0")
                .title("위치정보 이용 동의")
                .content("""
                        ### 수집 목적

                        - 회원 위치 기반 현상소 추천
                        - 서비스 품질 개선

                        ### 보유 기간

                        - 목적 달성 후 즉시 파기 또는 회원 탈퇴 시까지

                        ※ 위치정보는 서비스 제공 목적 외에는 이용되지 않습니다.""")
                .isRequired(false)
                .isActive(true)
                .effectiveDate(TERMS_EFFECTIVE_DATE)
                .build();

        termsToSave.add(service);
        termsToSave.add(privacy);
        termsToSave.add(location);
        termsToSave.add(serviceInfo);
        termsToSave.add(marketing);

        // Batch persist
        for (Terms terms : termsToSave) {
            entityManager.persist(terms);
        }
        entityManager.flush();

        termsList.addAll(termsToSave);
    }

    // ===== TermsSocialMapping =====
    private void createTermsSocialMappings() {
        List<TermsSocialMapping> mappings = new ArrayList<>();

        // 각 약관 타입별로 매핑 생성 (카카오 예시)
        termsList.forEach(terms -> {
            String tag = switch (terms.getType()) {
                case SERVICE -> "service_policy";
                case PRIVACY -> "privacy_policy";
                case SERVICE_INFO -> "required_notice";
                case MARKETING -> "marketing_info";
                case LOCATION -> "location_policy";
                default -> null;
            };

            if (tag != null) {
                mappings.add(TermsSocialMapping.builder()
                        .terms(terms)
                        .provider(SocialProvider.KAKAO)
                        .socialTag(tag)
                        .build());
            }
        });

        termsSocialMappingRepository.saveAll(mappings);
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
        Terms serviceInfoTerms = termsList.stream().filter(t -> t.getType() == TermsType.SERVICE_INFO).findFirst().orElse(null);

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

            if (serviceInfoTerms != null) {
                MemberAgreement agreement = MemberAgreement.builder()
                        .member(member)
                        .terms(serviceInfoTerms)
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
                    .phone(LAB_PHONES[i % LAB_PHONES.length])
                    .zipcode(LAB_ZIPCODES[i % LAB_ZIPCODES.length])
                    .address(LAB_DATA[i][2])
                    .addressDetail(LAB_ADDRESS_DETAILS[i % LAB_ADDRESS_DETAILS.length])
                    .latitude(new BigDecimal(LAB_DATA[i][3]))
                    .longitude(new BigDecimal(LAB_DATA[i][4]))
                    .status(PhotoLabStatus.ACTIVE)
                    .isDeliveryAvailable(i % 2 == 0)
                    .maxReservationsPerHour(3)
                    .avgWorkTime(LAB_AVG_WORK_TIMES[i % LAB_AVG_WORK_TIMES.length])
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
        if (photoLabs.isEmpty()) return;

        int[] favCountByUser = {3, 5, 2, 4, 1, 6, 3, 4, 2, 5, 3, 4, 2, 5, 1, 6, 3, 4, 2, 5};

        for (int i = 0; i < users.size(); i++) {
            MemberUser user = users.get(i);
            int favCount = favCountByUser[i % favCountByUser.length];

            for (int j = 0; j < favCount && j < photoLabs.size(); j++) {
                int labIndex = (i * 7 + j * 3) % photoLabs.size();
                PhotoLab favLab = photoLabs.get(labIndex);
                FavoritePhotoLab favorite = new FavoritePhotoLab(user, favLab);
                entityManager.persist(favorite);
            }
        }
        entityManager.flush();
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
                        .totalPrice(isDeliveryOrder ? PRINT_ORDER_DELIVERY_PRICE : PRINT_ORDER_PICKUP_PRICE)
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
                            .deliveryFee(DELIVERY_FEE)
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
        int[] likeCounts = {8, 5, 12, 6, 9, 4, 10, 7, 3, 11, 6, 8, 5, 9, 4, 7, 10, 6, 8, 5, 9, 4, 7, 10, 6, 8, 5, 9, 4, 7};
        int[] commentCounts = {4, 2, 5, 3, 2, 4, 3, 2, 5, 3, 4, 2, 5, 3, 2, 4, 3, 2, 5, 3, 4, 2, 5, 3, 2, 4, 3, 2, 5, 3};

        for (int postIdx = 0; postIdx < posts.size(); postIdx++) {
            Post post = posts.get(postIdx);
            int likeCount = likeCounts[postIdx % likeCounts.length];
            int startUserIdx = (postIdx * 3) % users.size();

            for (int i = 0; i < likeCount && i < users.size(); i++) {
                int userIdx = (startUserIdx + i) % users.size();
                MemberUser user = users.get(userIdx);
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
            int startCommenterIdx = (postIdx * 2 + 1) % users.size();
            for (int i = 0; i < commentCount; i++) {
                int commenterIdx = (startCommenterIdx + i) % users.size();
                MemberUser commenter = users.get(commenterIdx);
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
                    .user(user)
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
                        .user(user)
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

        for (int i = 0; i < keywords.length; i++) {
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
