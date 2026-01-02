import 'package:kakao_flutter_sdk_user/kakao_flutter_sdk_user.dart';

class KakaoUser {
  final int id;
  final String? nickname;
  final String? profileImageUrl;
  final String? email;

  KakaoUser({
    required this.id,
    this.nickname,
    this.profileImageUrl,
    this.email,
  });

  factory KakaoUser.fromKakaoAccount(User kakaoUser) {
    return KakaoUser(
      id: kakaoUser.id,
      nickname: kakaoUser.kakaoAccount?.profile?.nickname,
      profileImageUrl: kakaoUser.kakaoAccount?.profile?.profileImageUrl,
      email: kakaoUser.kakaoAccount?.email,
    );
  }
}
