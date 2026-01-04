import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../features/library/playlist_models.dart';

/// 플레이리스트 로컬 스토리지 관리 (SharedPreferences)
///
/// - 플레이리스트는 새로운 전체재생 시작 시까지 유지됨
/// - 최대 50개 항목 저장
/// - audioUrl은 저장하지 않음 (캐싱 데이터, 재생 시 다시 fetch)
class PlaylistStorage {
  PlaylistStorage({SharedPreferences? preferences})
    : _preferences = preferences;

  SharedPreferences? _preferences;
  static const String _key = 'playlist_items';
  static const int _maxPlaylistSize = 50; // 최대 50개 제한

  /// SharedPreferences 초기화
  Future<void> _ensureInitialized() async {
    _preferences ??= await SharedPreferences.getInstance();
  }

  /// 플레이리스트 저장 (최대 50개 제한)
  Future<void> savePlaylist(List<PlaylistItem> playlist) async {
    await _ensureInitialized();

    try {
      // 50개 초과 시 앞에서 50개만 저장
      final itemsToSave = playlist.length > _maxPlaylistSize
          ? playlist.take(_maxPlaylistSize).toList()
          : playlist;

      // JSON 직렬화
      final jsonList = itemsToSave.map((item) => item.toJson()).toList();
      final jsonString = jsonEncode(jsonList);

      // 저장
      await _preferences!.setString(_key, jsonString);

      debugPrint(
        '💾 플레이리스트 저장 완료: ${itemsToSave.length}개 항목'
        '${playlist.length > _maxPlaylistSize ? ' (${playlist.length}개 중 50개)' : ''}',
      );
    } catch (e) {
      debugPrint('❌ 플레이리스트 저장 실패: $e');
    }
  }

  /// 플레이리스트 로드
  Future<List<PlaylistItem>?> loadPlaylist() async {
    await _ensureInitialized();

    try {
      final jsonString = _preferences!.getString(_key);
      if (jsonString == null || jsonString.isEmpty) {
        debugPrint('📭 저장된 플레이리스트 없음');
        return null;
      }

      // JSON 역직렬화
      final jsonList = jsonDecode(jsonString) as List<dynamic>;
      final playlist = jsonList
          .map((json) => PlaylistItem.fromJson(json as Map<String, dynamic>))
          .toList();

      debugPrint('📂 플레이리스트 로드 완료: ${playlist.length}개 항목');
      return playlist;
    } catch (e) {
      debugPrint('❌ 플레이리스트 로드 실패: $e');
      // 파싱 실패 시 데이터 삭제 (손상된 데이터 방지)
      await clearPlaylist();
      return null;
    }
  }

  /// 플레이리스트 삭제
  Future<void> clearPlaylist() async {
    await _ensureInitialized();

    try {
      await _preferences!.remove(_key);
      debugPrint('🗑️ 플레이리스트 삭제 완료');
    } catch (e) {
      debugPrint('❌ 플레이리스트 삭제 실패: $e');
    }
  }

  /// 저장된 플레이리스트 개수 확인 (개발/디버깅용)
  Future<int> getPlaylistSize() async {
    final playlist = await loadPlaylist();
    return playlist?.length ?? 0;
  }
}
