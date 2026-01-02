class MemberProfile {
  final int id;
  final String nickname;
  final String? profileImage;

  MemberProfile({required this.id, required this.nickname, this.profileImage});

  factory MemberProfile.fromJson(Map<String, dynamic> json) {
    return MemberProfile(
      id: json['id'] as int,
      nickname: json['nickname'] as String,
      profileImage: json['profileImage'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'nickname': nickname, 'profileImage': profileImage};
  }

  /// 닉네임의 첫 글자를 대문자로 반환 (프로필 아바타에 표시용)
  String get initial {
    if (nickname.isEmpty) return '?';
    return nickname[0].toUpperCase();
  }

  /// 프로필 이미지가 있는지 확인
  bool get hasProfileImage {
    return profileImage != null && profileImage!.isNotEmpty;
  }
}
