import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 현재 재생 중인 팟캐스트 상태 모델
///
/// 앱 종료 후 재시작 시 재생바(미니플레이어)를 복원하기 위한 정보
class PlayerStateModel {
  /// 히어릿 ID
  final int hearitId;

  /// 팟캐스트 제목
  final String title;

  /// 앨범명 (소스명)
  final String album;

  /// 아티스트명 (소스명)
  final String artist;

  /// 전체 재생 시간 (초)
  final int durationSeconds;

  /// 마지막 재생 위치 (밀리초)
  final int lastPositionMs;

  /// 플레이리스트 인덱스 (null = 단일 재생, 0+ = 플레이리스트 재생)
  final int? playlistIndex;

  /// 상태 저장 시간 (만료 판단용)
  final DateTime savedAt;

  PlayerStateModel({
    required this.hearitId,
    required this.title,
    required this.album,
    required this.artist,
    required this.durationSeconds,
    required this.lastPositionMs,
    this.playlistIndex,
    required this.savedAt,
  });

  /// JSON으로 직렬화
  Map<String, dynamic> toJson() {
    return {
      'hearitId': hearitId,
      'title': title,
      'album': album,
      'artist': artist,
      'durationSeconds': durationSeconds,
      'lastPositionMs': lastPositionMs,
      'playlistIndex': playlistIndex,
      'savedAt': savedAt.millisecondsSinceEpoch,
    };
  }

  /// JSON에서 역직렬화
  factory PlayerStateModel.fromJson(Map<String, dynamic> json) {
    return PlayerStateModel(
      hearitId: json['hearitId'] as int,
      title: json['title'] as String,
      album: json['album'] as String,
      artist: json['artist'] as String,
      durationSeconds: json['durationSeconds'] as int,
      lastPositionMs: json['lastPositionMs'] as int,
      playlistIndex: json['playlistIndex'] as int?,
      savedAt: DateTime.fromMillisecondsSinceEpoch(json['savedAt'] as int),
    );
  }

  /// 7일 이상 오래된 상태인지 확인
  bool get isExpired {
    final now = DateTime.now();
    final difference = now.difference(savedAt);
    return difference.inDays >= 7;
  }

  @override
  String toString() {
    return 'PlayerStateModel(hearitId: $hearitId, title: $title, '
        'position: ${lastPositionMs}ms, playlistIndex: $playlistIndex, '
        'savedAt: $savedAt)';
  }
}

/// 현재 재생 상태 로컬 저장소 관리 (SharedPreferences)
///
/// 앱 종료 후 재시작 시 재생바를 복원하기 위해 사용
class PlayerStateStorage {
  PlayerStateStorage({SharedPreferences? preferences})
    : _preferences = preferences;

  SharedPreferences? _preferences;
  static const String _key = 'player_state';

  /// SharedPreferences 초기화
  Future<void> _ensureInitialized() async {
    _preferences ??= await SharedPreferences.getInstance();
  }

  /// 현재 재생 상태 저장
  Future<void> savePlayerState(PlayerStateModel state) async {
    await _ensureInitialized();

    try {
      final json = jsonEncode(state.toJson());
      await _preferences!.setString(_key, json);

      debugPrint(
        '💾 재생바 상태 저장 완료: ${state.title} (위치: ${state.lastPositionMs}ms)',
      );
    } catch (e) {
      debugPrint('❌ 재생바 상태 저장 실패: $e');
    }
  }

  /// 저장된 재생 상태 로드
  Future<PlayerStateModel?> loadPlayerState() async {
    await _ensureInitialized();

    try {
      final jsonString = _preferences!.getString(_key);
      if (jsonString == null || jsonString.isEmpty) {
        debugPrint('📭 저장된 재생바 상태 없음');
        return null;
      }

      // JSON 역직렬화
      final json = jsonDecode(jsonString) as Map<String, dynamic>;
      final state = PlayerStateModel.fromJson(json);

      debugPrint('📂 재생바 상태 로드 완료: ${state.title}');
      return state;
    } catch (e) {
      debugPrint('❌ 재생바 상태 로드 실패: $e');
      // 파싱 실패 시 데이터 삭제 (손상된 데이터 방지)
      await clearPlayerState();
      return null;
    }
  }

  /// 저장된 재생 상태 삭제
  Future<void> clearPlayerState() async {
    await _ensureInitialized();

    try {
      await _preferences!.remove(_key);
      debugPrint('🗑️ 재생바 상태 삭제 완료');
    } catch (e) {
      debugPrint('❌ 재생바 상태 삭제 실패: $e');
    }
  }

  /// 저장된 상태가 있는지 확인 (개발/디버깅용)
  Future<bool> hasPlayerState() async {
    await _ensureInitialized();
    return _preferences!.containsKey(_key);
  }
}
