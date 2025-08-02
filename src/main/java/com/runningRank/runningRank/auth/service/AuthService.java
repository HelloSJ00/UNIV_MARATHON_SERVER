package com.runningRank.runningRank.auth.service;

import com.runningRank.runningRank.auth.dto.*;
import com.runningRank.runningRank.auth.jwt.JwtProvider;
import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.major.repository.MajorRepository;
import com.runningRank.runningRank.mileage.domain.Mileage;
import com.runningRank.runningRank.mileage.repository.MileageRepository;
import com.runningRank.runningRank.university.domain.University;
import com.runningRank.runningRank.university.repository.UniversityRepository;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 로거 추가
import com.runningRank.runningRank.user.domain.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j // Slf4j 로거 어노테이션 추가
public class AuthService {

    private final UserRepository userRepository;
    private final MileageRepository mileageRepository;
    private final MajorRepository majorRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniversityRepository universityRepository;
    private final JwtProvider jwtProvider;

    // 이메일 중복확인
    public boolean checkEmailDuplicate(String email) {
        log.info("이메일 중복 확인 요청: {}", email);
        boolean isDuplicate = userRepository.existsByEmail(email);
        if (isDuplicate) {
            log.warn("이메일 중복 감지: {}", email);
        } else {
            log.info("이메일 사용 가능: {}", email);
        }
        return isDuplicate;
    }

    @Transactional // 트랜잭션 관리
    public UserResponse signup(SignUpRequest request) {
        log.info("회원가입 요청 수신: 이메일={}, 이름={}", request.getEmail(), request.getName());

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("회원가입 실패: 이미 존재하는 이메일입니다. 이메일={}", request.getEmail());
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        log.debug("이메일 중복 없음: {}", request.getEmail());

        // 2. 관련 엔티티 조회 (조회 로직 집중)
        // 학교명 조회
        log.debug("학교 정보 조회 시도: 학교명={}", request.getUniversity());
        University university = universityRepository.findByUniversityName(request.getUniversity())
                .orElseThrow(() -> {
                    log.error("회원가입 실패: 존재하지 않는 학교입니다. 학교명={}", request.getUniversity());
                    return new IllegalArgumentException("해당 학교가 존재하지 않습니다.");
                });
        log.info("학교 정보 조회 성공: 학교명={}", university.getUniversityName());

        // 전공명과 학교명으로 Major 엔티티 조회
        log.debug("전공 정보 조회 시도: 전공명={}, 학교명={}", request.getMajor(), request.getUniversity());
        Major major = majorRepository.findByNameAndUniversityName(request.getMajor(), request.getUniversity())
                .orElseThrow(() -> {
                    log.error("회원가입 실패: 해당 학교에 전공이 존재하지 않습니다. 전공명={}, 학교명={}", request.getMajor(), request.getUniversity());
                    return new IllegalArgumentException("해당 전공이 존재하지 않습니다.");
                });
        log.info("전공 정보 조회 성공: 전공명={}, 학교명={}", major.getName(), major.getUniversityName());


        // 3. 비밀번호 암호화 (패스워드 인코더의 책임)
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("비밀번호 암호화 완료");

        // 4. User 객체 생성 (User 엔티티의 정적 팩토리 메서드 활용)
        User user = User.create(request, encodedPassword, university, major);
        log.debug("User 엔티티 생성 완료");

        // 5. User 저장
        User savedUser = userRepository.save(user);
        log.info("새로운 사용자 저장 완료: 사용자ID={}, 이메일={}", savedUser.getId(), savedUser.getEmail());

        // 6. UserResponse DTO 변환 (UserResponse DTO의 책임)
        UserResponse response = UserResponse.from(savedUser);
        log.debug("UserResponse DTO 변환 완료");

        log.info("회원가입 성공: 사용자ID={}", savedUser.getId());
        return response;
    }

    public LoginResponse login(LoginRequest request){
        log.info("로그인 요청 수신: 이메일={}", request.getEmail());

        // 1. 유저 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("로그인 실패: 존재하지 않는 이메일입니다. 이메일={}", request.getEmail());
                    return new RuntimeException("존재하지 않는 이메일입니다.");
                });
        log.debug("사용자 조회 성공: 이메일={}", user.getEmail());

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("로그인 실패: 비밀번호가 일치하지 않습니다. 이메일={}", request.getEmail());
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        log.info("비밀번호 확인 성공: 이메일={}", user.getEmail());

        // 3. 토큰 생성
        String token = jwtProvider.createAccessToken(user.getEmail(), user.getRole());
        log.debug("JWT 토큰 생성 완료");

        Optional<Mileage> optionalMileage = mileageRepository.findByUserAndYearAndMonth(user, LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        Mileage mileage = optionalMileage.orElse(null);        // 4. 유저 정보 DTO 생성 (러닝기록 포함해서 정리)
        UserInfo userInfo = UserInfo.from(user,mileage);
        log.debug("UserInfo DTO 변환 완료");

        // 5. 통합 응답
        LoginResponse response = LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userInfo)
                .build();
        log.info("로그인 성공: 이메일={}", user.getEmail());
        return response;
    }

    /**
     * 회원가입시 등록된 모든 학교 조회
     * @return 모든 학교 이름 리스트
     */
    @Cacheable("allUniversitiesCache")
    public List<String> getAllUniversityNames() {
        log.info("모든 학교 이름 조회 요청");
        List<String> universityNames = universityRepository.findAll().stream()
                .map(University::getUniversityName)
                .toList();
        log.info("조회된 학교 개수: {}", universityNames.size());
        log.debug("조회된 학교 목록: {}", universityNames);
        return universityNames;
    }

    /**
     * 학교 선택시 해당 학교 전공 조회
     * @param universityName 조회할 대학교 이름
     * @return 해당 대학교의 전공 이름 리스트
     */
    @Cacheable(value = "majorsByUniversityCache", key = "#universityName") // 캐시 이름과 키 지정 (key는 universityName 매개변수 사용)
    public List<String> getMajorsByUniversityName(String universityName) {
        log.info("특정 학교의 전공 조회 요청: 학교명={}", universityName);
        List<Major> majors = majorRepository.findByUniversityName(universityName);
        List<String> majorNames = majors.stream()
                .map(Major::getName)
                .toList();
        log.info("학교 {}에 대한 조회된 전공 개수: {}", universityName, majorNames.size());
        log.debug("조회된 전공 목록 for {}: {}", universityName, majorNames);
        return majorNames;
    }

    /**
     * 내 정보 수정
     * @param request 사용자 정보 수정 요청 DTO
     * @param userId 수정할 사용자의 ID
     * @return 수정 성공 여부
     */
    @Transactional
    public boolean updateUserInfo(UserUpdateRequest request, Long userId) {
        log.info("사용자 정보 수정 요청 수신: 사용자ID={}", userId);
        log.debug("수정 요청 내용: {}", request);

        // 사용자를 찾거나 프록시 로드
        User user = userRepository.getReferenceById(userId);
        log.debug("사용자 엔티티 참조 로드: 사용자ID={}", userId);

        University newUniversity = null;
        Major newMajor = null;

        // 1. 대학교 이름으로 University 엔티티 조회 (요청에 universityName이 있다면)
        if (request.getUniversityName() != null && !request.getUniversityName().isEmpty()) {
            log.debug("새로운 대학교 정보 조회 시도: 학교명={}", request.getUniversityName());
            newUniversity = universityRepository.findByUniversityName(request.getUniversityName())
                    .orElseThrow(() -> {
                        log.error("사용자 정보 수정 실패: 대학교를 찾을 수 없습니다. 학교명={}", request.getUniversityName());
                        return new EntityNotFoundException("대학교를 찾을 수 없습니다: " + request.getUniversityName());
                    });
            log.info("새로운 대학교 정보 조회 성공: 학교명={}", newUniversity.getUniversityName());
        } else {
            log.debug("대학교 정보 변경 요청 없음.");
        }

        // 2. 전공 이름과 (선택적으로) 대학교로 Major 엔티티 조회 (요청에 major가 있다면)
        if (request.getMajorName() != null && !request.getMajorName().isEmpty()) {
            // newUniversity가 null이면 (즉, 대학교 변경 요청이 없으면) 기존 사용자 대학을 사용하거나 적절히 처리해야 함
            // 여기서는 newUniversity가 반드시 있어야 Major 조회가 가능하도록 로직이 설계되어 있습니다.
            // 만약 학교는 변경하지 않고 전공만 변경하는 시나리오라면 이 로직을 수정해야 합니다.
            // 예: if (newUniversity == null) newUniversity = user.getUniversity();
            if (newUniversity == null) {
                log.warn("전공만 변경 요청 시 대학교 정보가 명시되지 않아 기존 대학교 정보 사용 시도: 사용자ID={}", userId);
                newUniversity = user.getUniversity(); // 기존 대학교 정보 사용 시나리오
                if (newUniversity == null) { // 기존 대학교 정보도 없는 경우
                    log.error("사용자 정보 수정 실패: 전공 변경 요청에 대학교 정보가 없어 전공 조회가 불가합니다.");
                    throw new IllegalArgumentException("전공 변경 시에는 유효한 대학교 정보가 필요합니다.");
                }
            }
            log.debug("새로운 전공 정보 조회 시도: 전공명={}, 학교명={}", request.getMajorName(), newUniversity.getUniversityName());
            University finalNewUniversity = newUniversity;
            newMajor = majorRepository.findByNameAndUniversityName(request.getMajorName(), newUniversity.getUniversityName())
                    .orElseThrow(() -> {
                        log.error("사용자 정보 수정 실패: 해당 대학교에서 전공을 찾을 수 없습니다. 전공명={}, 학교명={}", request.getMajorName(), finalNewUniversity.getUniversityName());
                        return new EntityNotFoundException("해당 대학교에서 전공을 찾을 수 없습니다: " + request.getMajorName());
                    });
            log.info("새로운 전공 정보 조회 성공: 전공명={}, 학교명={}", newMajor.getName(), newMajor.getUniversityName());
        } else {
            log.debug("전공 정보 변경 요청 없음.");
        }

        // 3. User 엔티티의 업데이트 메서드 호출
        user.updateInfo(request, newUniversity, newMajor);
        log.info("사용자 엔티티 정보 업데이트 완료: 사용자ID={}", userId);

        // @Transactional 덕분에 변경사항이 자동으로 영속화됩니다.
        // userRepository.save(user); // 명시적 save는 필수는 아니지만, 명확성을 위해 사용 가능
        // log.debug("사용자 엔티티 저장 (자동 커밋될 예정)");

        log.info("사용자 정보 수정 성공: 사용자ID={}", userId);
        return true;
    }
}
