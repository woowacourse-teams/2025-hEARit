# Token Refresh 구현 LLD

## 1. 배경 및 문제 정의

### 문제
앱이 accessToken만으로 인증을 처리하고 있었으며, 토큰 만료 시 refreshToken을 사용하지 않고 즉시 로그아웃 처리.
결과적으로 로그인 상태가 유지되지 않아 사용자가 자주 재로그인해야 하는 UX 문제 발생.

### 근본 원인
1. **`auth_viewmodel.dart`** — 앱 시작 시 `checkAuthStatus()`에서 accessToken 유효성 실패 시 refresh 시도 없이 바로 `clearTokens()` 호출
2. **`api_client.dart`** — 앱 사용 중 401 응답 처리 인터셉터 없음
3. **`auth_repository.dart`** — refreshToken을 사용하는 API 호출 메서드 없음

---

## 2. Refresh API 스펙

```
POST /api/v1/auth/token/refresh
Content-Type: application/json

Request Body:
{
  "refreshToken": "string"
}

Response 200:
{
  "accessToken": "string"
}
```

> **주의:** 응답은 새 accessToken만 포함. refreshToken은 갱신되지 않으며 기존 값을 계속 사용.

---

## 3. 아키텍처 설계

### 순환 참조 문제
`AuthInterceptor`가 토큰 갱신 실패 시 `AuthViewModel`의 `clearAuthStatus()`를 직접 호출하면 순환 import 발생.

```
AuthViewModel → AuthRepository → ApiClient → AuthInterceptor → AuthViewModel (순환!)
```

### 해결: Stream 이벤트 방식

`AuthViewModel`이 `StreamController<AuthEvent>`를 소유하고 `ApiClient` → `AuthInterceptor`에 주입.
`AuthInterceptor`는 `AuthViewModel`을 전혀 모르고, Stream에 이벤트만 push.

```
AuthViewModel (StreamController 소유 + stream 구독)
     ↓ eventController 주입
ApiClient
     ↓ eventController 주입
AuthInterceptor → StreamController.add(tokenRefreshFailed)
                                ↑
              AuthViewModel이 listen하여 clearAuthStatus() 호출
```

### 의존성 방향 (순환 없음)

```
auth_event.dart
      ↑
auth_interceptor.dart      (auth_viewmodel 미참조)
      ↑
api_client.dart
      ↑
auth_repository.dart
      ↑
auth_viewmodel.dart        (Stream 구독으로 interceptor 이벤트 수신)
```

---

## 4. 토큰 갱신 시나리오

### 시나리오 A: 앱 시작 시 (Splash Screen)

```
앱 시작
  └─ checkAuthStatus()
       ├─ 저장된 토큰 없음 → unauthenticated
       └─ 저장된 토큰 있음
            ├─ GET /api/v1/auth/check (accessToken)
            │    ├─ 200 OK → authenticated ✓
            │    └─ 실패 (만료)
            │         ├─ POST /api/v1/auth/token/refresh (refreshToken)
            │         │    ├─ 200 OK → 새 accessToken 저장 → authenticated ✓
            │         │    └─ 실패 → clearTokens() → unauthenticated
            │         └─ 네트워크 오류 → unauthenticated
            └─ 예외 → unauthenticated
```

### 시나리오 B: 앱 사용 중 API 호출 시 (AuthInterceptor)

```
API 요청
  └─ 401 응답 수신
       └─ AuthInterceptor.onError()
            ├─ _isRefreshing == true → 갱신 중복 방지, 에러 그대로 전달
            └─ _isRefreshing == false
                 ├─ 저장된 토큰 없음 → Stream.add(tokenRefreshFailed)
                 └─ refreshToken 존재
                      ├─ POST /api/v1/auth/token/refresh
                      │    ├─ 200 OK → 새 accessToken 저장
                      │    │         → 원래 요청 재시도 (새 accessToken 헤더 교체)
                      │    │         → handler.resolve(retryResponse) ✓
                      │    └─ 실패 → Stream.add(tokenRefreshFailed)
                      └─ Stream 이벤트 수신
                           └─ AuthViewModel.clearAuthStatus()
                                └─ clearTokens() → unauthenticated → 로그인 화면
```

---

## 5. 변경 파일 상세

### 5-1. `lib/features/auth/models/auth_event.dart` (신규)

인터셉터와 ViewModel 간 통신에 사용되는 이벤트 타입.

```dart
enum AuthEvent {
  tokenRefreshFailed,
}
```

---

### 5-2. `lib/core/network/interceptors/auth_interceptor.dart` (신규)

401 에러 자동 처리 인터셉터. `AuthViewModel`에 대한 의존성 없음.

**주요 설계 결정:**
- refresh 호출에 **별도 `Dio` 인스턴스** 사용 → 동일 `ApiClient`의 인터셉터 루프 방지
- `_isRefreshing` 플래그로 중복 refresh 요청 방지
- refresh 성공 시 `err.requestOptions`의 Authorization 헤더를 교체 후 `Dio().fetch()`로 원래 요청 재시도

```dart
class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required AuthStorageService storageService,
    required StreamController<AuthEvent> eventController,
  })  : _storageService = storageService,
        _eventController = eventController;

  final AuthStorageService _storageService;
  final StreamController<AuthEvent> _eventController;
  bool _isRefreshing = false;

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode != 401 || _isRefreshing) {
      return handler.next(err);
    }

    _isRefreshing = true;
    try {
      final tokens = await _storageService.getTokens();
      if (tokens == null) {
        _eventController.add(AuthEvent.tokenRefreshFailed);
        return handler.next(err);
      }

      // 별도 Dio 인스턴스로 refresh (인터셉터 루프 방지)
      final refreshDio = Dio();
      final response = await refreshDio.post(
        '${ApiConfig.defaultBaseUrl}/api/v1/auth/token/refresh',
        data: {'refreshToken': tokens.refreshToken},
      );
      final newAccessToken = response.data['accessToken'] as String;

      await _storageService.saveTokens(
        AuthTokens(
          accessToken: newAccessToken,
          refreshToken: tokens.refreshToken, // refreshToken은 그대로 유지
        ),
      );

      // 원래 요청 재시도
      final retryOptions = err.requestOptions;
      retryOptions.headers['Authorization'] = 'Bearer $newAccessToken';
      final retryResponse = await Dio().fetch(retryOptions);
      return handler.resolve(retryResponse);
    } catch (e) {
      _eventController.add(AuthEvent.tokenRefreshFailed);
      return handler.next(err);
    } finally {
      _isRefreshing = false;
    }
  }
}
```

---

### 5-3. `lib/core/network/api_client.dart` (수정)

`AuthInterceptor` 등록을 위한 파라미터 추가. 두 파라미터 모두 제공될 때만 인터셉터 활성화.
인터셉터 등록 순서: `AuthInterceptor` → `DeviceUUIDInterceptor` → `DebugLoggingInterceptor`.

**변경 전:**
```dart
ApiClient({
  ApiConfig config = const ApiConfig(),
  Duration connectTimeout = ...,
  Duration sendTimeout = ...,
  Duration receiveTimeout = ...,
  Dio? dio,
})
```

**변경 후:**
```dart
ApiClient({
  ApiConfig config = const ApiConfig(),
  Duration connectTimeout = ...,
  Duration sendTimeout = ...,
  Duration receiveTimeout = ...,
  Dio? dio,
  AuthStorageService? storageService,                    // 추가
  StreamController<AuthEvent>? authEventController,      // 추가
}) {
  if (storageService != null && authEventController != null) {
    _dio.interceptors.add(
      AuthInterceptor(
        storageService: storageService,
        eventController: authEventController,
      ),
    );
  }
  _dio.interceptors.add(DeviceUUIDInterceptor());
  ...
}
```

---

### 5-4. `lib/features/auth/auth_repository.dart` (수정)

앱 시작 시 `checkAuthStatus()`에서 사용하는 refresh 메서드 추가.

```dart
/// refreshToken으로 새 accessToken 발급
Future<String> refreshAccessToken(String refreshToken) async {
  try {
    final response = await _apiClient.post<Map<String, dynamic>>(
      '/api/v1/auth/token/refresh',
      body: {'refreshToken': refreshToken},
      parser: (data) => data as Map<String, dynamic>,
    );
    return response['accessToken'] as String;
  } catch (error) {
    throw Exception('토큰 갱신 실패: $error');
  }
}
```

---

### 5-5. `lib/features/auth/auth_viewmodel.dart` (수정)

**생성자 변경:**
`StreamController` 소유 및 `AuthRepository`에 auth-aware `ApiClient` 주입.

```dart
AuthViewModel({
  AuthRepository? repository,
  AuthStorageService? storageService,
}) : _storageService = storageService ?? AuthStorageService() {
  _authEventController = StreamController<AuthEvent>.broadcast();
  _repository = repository ??
      AuthRepository(
        apiClient: ApiClient(
          storageService: _storageService,
          authEventController: _authEventController,
        ),
      );
  _authEventSubscription = _authEventController.stream.listen((event) {
    if (event == AuthEvent.tokenRefreshFailed) {
      clearAuthStatus();
    }
  });
}
```

**필드 추가:**
```dart
late final AuthRepository _repository;
late final StreamController<AuthEvent> _authEventController;
late final StreamSubscription<AuthEvent> _authEventSubscription;
```

**`checkAuthStatus()` 변경 — else 블록:**

```dart
// 변경 전
} else {
  await _storageService.clearTokens();
  _status = AuthStatus.unauthenticated;
}

// 변경 후
} else {
  try {
    final newAccessToken =
        await _repository.refreshAccessToken(tokens.refreshToken);
    await _storageService.saveTokens(
      AuthTokens(
        accessToken: newAccessToken,
        refreshToken: tokens.refreshToken,
      ),
    );
    _status = AuthStatus.authenticated;
    try {
      _user = await _repository.getKakaoUserInfo();
    } catch (e) {
      debugPrint('사용자 정보 조회 실패: $e');
    }
  } catch (e) {
    debugPrint('토큰 갱신 실패, 로그아웃 처리: $e');
    await _storageService.clearTokens();
    _status = AuthStatus.unauthenticated;
  }
}
```

**`dispose()` 추가:**
```dart
@override
void dispose() {
  _authEventSubscription.cancel();
  _authEventController.close();
  super.dispose();
}
```

---

## 6. 변경 파일 요약

| 파일 | 변경 유형 | 핵심 변경 내용 |
|------|-----------|--------------|
| `features/auth/models/auth_event.dart` | 신규 | `AuthEvent` enum 정의 |
| `core/network/interceptors/auth_interceptor.dart` | 신규 | 401 자동 갱신 인터셉터 |
| `core/network/api_client.dart` | 수정 | `storageService`, `authEventController` 파라미터 추가 및 `AuthInterceptor` 등록 |
| `features/auth/auth_repository.dart` | 수정 | `refreshAccessToken()` 메서드 추가 |
| `features/auth/auth_viewmodel.dart` | 수정 | `StreamController` 소유, Stream 구독, `checkAuthStatus()` refresh 로직, `dispose()` |

---

## 7. 제약 및 한계

| 항목 | 내용 |
|------|------|
| 동시 401 처리 | `_isRefreshing` 플래그로 중복 refresh 방지. 첫 번째 요청만 refresh 시도, 나머지는 401 에러 그대로 전달됨. 앱 규모상 허용 수준으로 판단. |
| refreshToken 만료 주기 | 서버 정책에 따름. 현재 클라이언트는 refresh 실패 시 재로그인 유도로 처리. |
| 토큰 저장소 | `FlutterSecureStorage` 사용. OS 키체인/키스토어 기반으로 암호화 저장됨. |
| refreshToken 갱신 없음 | 백엔드 API가 refresh 응답에 새 refreshToken을 포함하지 않으므로 기존 refreshToken을 계속 사용. 서버 정책 변경 시 `AuthTokens` 저장 로직 수정 필요. |
