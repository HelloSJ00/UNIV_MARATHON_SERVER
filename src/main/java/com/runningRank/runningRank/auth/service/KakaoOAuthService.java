//package com.runningRank.runningRank.auth.service;
//
//import com.runningRank.runningRank.auth.dto.*;
//import com.runningRank.runningRank.auth.jwt.JwtProvider;
//import com.runningRank.runningRank.major.domain.Major;
//import com.runningRank.runningRank.major.repository.MajorRepository;
//import com.runningRank.runningRank.university.domain.University;
//import com.runningRank.runningRank.university.repository.UniversityRepository;
//import com.runningRank.runningRank.user.domain.Gender;
//import com.runningRank.runningRank.user.domain.GraduationStatus;
//import com.runningRank.runningRank.user.domain.Role;
//import com.runningRank.runningRank.user.domain.User;
//import com.runningRank.runningRank.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class KakaoOAuthService {
//
//    private final UserRepository userRepository;
//    private final MajorRepository majorRepository;
//    private final UniversityRepository universityRepository;
//    private final JwtProvider jwtProvider;
//    private final PasswordEncoder passwordEncoder;
//
//    @Value("${kakao.client-id}")
//    private String clientId;
//
//    @Value("${kakao.redirect-uri}")
//    private String redirectUri;
//
//    public LoginResponse kakaoSignUp(KakaoSignupRequest request){
//        // 1. 이메일 중복 체크
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
//        }
//
//        // 2. 전공, 학교 조회
//        String majorName = request.getMajor();
//        Major major = majorRepository.findByName(majorName)
//                .orElseThrow(() -> new IllegalArgumentException("해당 전공이 존재하지 않습니다."));
//
//        University university = universityRepository.findByUniversityName(request.getUniversity())
//                .orElseThrow(() -> new IllegalArgumentException("해당 학교가 존재하지 않습니다."));
//
//        // 3. 비밀번호 암호화 (패스워드 인코더의 책임)
//        String encodedPassword = passwordEncoder.encode(request.getPassword());
//
//        // 4. User 객체 생성 (User 엔티티의 정적 팩토리 메서드 활용)
//        User user = User.kakaoCreate(request, encodedPassword, university, major);
//
//        // 5. User 저장
//        User savedUser = userRepository.save(user);
//
//        // 4. JWT 발급
//        String jwt = jwtProvider.createAccessToken(user.getEmail(), user.getRole());
//
//        // 5. 유저 정보 DTO 생성
//        UserInfo userInfo = UserInfo.from(user);
//
//        // 6. 통합 응답 반환
//        return LoginResponse.builder()
//                .accessToken(jwt)
//                .tokenType("Bearer")
//                .user(userInfo)
//                .build();
//    }
//
//    public KakaoLoginResponse kakaoLogin(String code) {
//        // 1. 카카오에 토큰 요청 (인가 코드 -> 액세스 토큰)
//        String kakaoAccessToken = getAccessToken(code);
//        // 2. 토큰으로 사용자 정보 요청
//        KakaoUserInfo userInfo = getUserInfo(kakaoAccessToken);
//
//        // 3. 이미 회원이면 return, 아니면 회원가입 로직으로
//        Optional<User> optionalUser = userRepository.findByOauthIdAndOauthProvider(userInfo.getOauthId(),"kakao");
//
//        if(optionalUser.isPresent()){
//            User user = optionalUser.get(); // Optional에서 실제 User 객체 꺼내기
//            String jwt = jwtProvider.createAccessToken(user.getEmail(),user.getRole());
//            return new KakaoLoginResponse(false,jwt,"Bearer",null);
//        } else {
//            return new KakaoLoginResponse(true,null,null,userInfo);
//        }
//    }
//
//    /**
//     * 1. 카카오에 토큰 요청 (인가 코드 -> 액세스 토큰)
//     * @param code
//     * @return
//     */
//    private String getAccessToken(String code) {
//        RestTemplate rest = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "authorization_code");
//        body.add("client_id", clientId);
//        body.add("redirect_uri", redirectUri);
//        body.add("code", code);
//
//        HttpEntity<?> request = new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> response = rest.postForEntity(
//                "https://kauth.kakao.com/oauth/token", request, Map.class);
//
//        return (String) response.getBody().get("access_token");
//    }
//
//    /**
//     * 2. 토큰으로 사용자 정보 요청
//     * @param accessToken
//     * @return
//     */
//    private KakaoUserInfo getUserInfo(String accessToken) {
//        RestTemplate rest = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        HttpEntity<Void> request = new HttpEntity<>(headers);
//
//        ResponseEntity<Map> response = rest.exchange(
//                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, Map.class);
//
//        Map<String, Object> responseBody = response.getBody();
//
//        // ✅ 1. 카카오 고유 사용자 ID
//        String oauthId = String.valueOf(responseBody.get("id"));
//
//        // ✅ 2. 닉네임 & 프로필 이미지
//        Map<String, Object> kakaoAccount = (Map<String, Object>) responseBody.get("kakao_account");
//        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
//        String nickname = (String) profile.get("nickname");
//        String profileImageUrl = (String) profile.get("profile_image_url");
//
//        return new KakaoUserInfo(oauthId, nickname, profileImageUrl);
//    }
//}
