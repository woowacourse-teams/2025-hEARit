# AI Agent Guidelines for hEARit Project

## Project Overview

**hEARit** (IT를 듣다, Hear It) is a podcast platform designed for developers and IT professionals to easily listen to IT trends and information rather than reading them. The name "hEARit" represents a single audio content piece that delivers information in a new, voice-first way.

- **Package**: `com.onair.hearit`
- **Version**: 1.0.1+9
- **Organization**: woowacourse-teams

## Technology Stack

### Primary Application (Flutter)
- **Location**: `ios/hearit/`
- **Framework**: Flutter with Dart SDK ^3.9.2
- **State Management**: Provider pattern (ChangeNotifierProvider)
- **Architecture**: Feature-based MVVM (ViewModel + Repository)
- **Key Libraries**:
  - Audio: `just_audio`, `audio_service`, `audio_session`
  - Networking: `dio` with custom interceptors
  - Storage: `flutter_secure_storage`
  - Analytics: Firebase Analytics
  - UI: Material Design with custom Pretendard & Prompt fonts

### Android Native
- **Location**: `android/`
- **Language**: Kotlin 2.0.21
- **Build Tool**: Gradle 8.9.1 with Kotlin DSL
- **Min SDK**: 29 (Android 10), Target SDK: 35
- **Java Version**: 21

### iOS Native
- **Location**: `ios/hearit/ios/`
- **Language**: Swift
- **Deployment Target**: iOS 13.0
- **Dependency Manager**: CocoaPods

### Backend
- **Location**: `backend/`
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Build Tool**: Gradle with Spring Boot plugin
- **Database**: H2 (dev), MySQL (production)
- **Key Technologies**: Spring Data JPA, Lombok

## Codebase Structure

```
2025-hEARit/
├── ios/hearit/              # Main Flutter application
│   ├── lib/
│   │   ├── core/            # Core infrastructure layer
│   │   │   ├── analytics/   # Firebase Analytics integration
│   │   │   ├── audio/       # Audio playback handlers & controllers
│   │   │   ├── device/      # Device UUID service
│   │   │   ├── network/     # API client, interceptors, exceptions
│   │   │   ├── presentation/# Shared UI widgets & navigation
│   │   │   └── theme/       # App colors & theming
│   │   ├── features/        # Feature modules (MVVM pattern)
│   │   │   ├── detail/      # Podcast detail & playback UI
│   │   │   ├── explore/     # Content discovery feed
│   │   │   ├── home/        # Home screen with recommendations
│   │   │   ├── search/      # Search & category browsing
│   │   │   └── setting/     # Settings & OSS licenses
│   │   └── main.dart        # App entry point
│   ├── assets/              # Images, fonts, icons
│   ├── test/                # Widget & unit tests
│   └── pubspec.yaml         # Flutter dependencies
├── android/                 # Native Android application
│   └── app/src/             # Kotlin source code
├── backend/                 # Spring Boot REST API
│   └── src/
│       ├── main/java/       # Application code
│       └── test/java/       # JUnit tests
└── .github/                 # Issue & PR templates
```

## Development Guidelines

### Issue & PR Conventions

**Issue Types** (use appropriate template):
- `[Feat]` - New feature requests
- `[Bug]` - Bug reports and fixes
- `[Refactor]` - Code refactoring without behavior changes
- `[Test]` - Test code additions or improvements

**PR Title Format**:
- `[BE]` - Backend changes
- `[AN]` - Android changes
- `[iOS]` - iOS/Flutter changes

**PR Template Sections**:
1. 📌 변경 내용 & 이유 (Changes & Reasons)
2. 📸 스크린샷 (Screenshots - optional for UI changes)
3. 🧪 테스트 방법 (Testing methods)
4. 📢 논의하고 싶은 내용 (Discussion points)
5. 🧩 관련 이슈 (Related issues - use `close #123`)

### Code Organization Patterns

**Flutter Features** (MVVM Pattern):
Each feature module should contain:
- `*_screen.dart` - UI layer (StatefulWidget/StatelessWidget)
- `*_viewmodel.dart` - Business logic layer (ChangeNotifier)
- `*_repository.dart` - Data access layer
- `*_models.dart` - Data models
- `widgets/` - Feature-specific reusable widgets

**Example**: `features/home/`
```dart
home_screen.dart          // UI + State
home_viewmodel.dart       // Business logic (extends ChangeNotifier)
home_repository.dart      // API calls & data fetching
home_models.dart          // Data classes
widgets/                  // Reusable components
```

### Architecture Patterns

**State Management**:
- Use `Provider` with `ChangeNotifierProvider` for state management
- ViewModels extend `ChangeNotifier` and call `notifyListeners()` on state changes
- Access via `Provider.of<T>(context)` or `context.watch<T>()`

**Network Layer**:
- All API calls go through `core/network/api_client.dart`
- Use Dio interceptors for cross-cutting concerns (logging, auth)
- Handle errors via `ApiException` classes

**Audio Playback**:
- Background audio uses `audio_service` package
- Audio state managed via `HearitPlayerController` (ChangeNotifier)
- Audio handler implementation: `core/audio/audio_handler.dart`

**Navigation**:
- Main navigation managed in `core/presentation/main_navigation.dart`
- Use Material navigation (`Navigator.push/pop`)

## Testing Strategy

### Flutter Tests
- **Framework**: `flutter_test` (built-in)
- **Location**: `ios/hearit/test/`
- **Command**: `flutter test`
- **Linting**: `flutter analyze` (uses `flutter_lints` package)
- Write widget tests for UI components
- Write unit tests for ViewModels and Repositories

### Android Tests
- **Unit Tests**: JUnit 4.13.2
- **Instrumented Tests**: AndroidX Test with Espresso
- **Location**: `android/app/src/test/` and `android/app/src/androidTest/`
- **Command**: `./gradlew test` or `./gradlew connectedAndroidTest`

### iOS Tests
- **Framework**: XCTest
- **Location**: `ios/hearit/ios/RunnerTests/`
- **Command**: Run tests via Xcode or `flutter test`

### Backend Tests
- **Framework**: JUnit 5 with Spring Boot Test
- **Location**: `backend/src/test/java/`
- **Command**: `./gradlew test`
- Use `@SpringBootTest` for integration tests

## Important Considerations

### Firebase Integration
- Project ID: `hearit-f5ad5`
- Analytics configured for iOS
- Always use `AnalyticsProvider` for event logging
- Event names defined in `core/analytics/analytics_event_names.dart`
- Param keys defined in `core/analytics/analytics_param_keys.dart`

### Audio Service
- Background audio requires proper Android/iOS permissions
- Audio notifications handled by `LocalAudioHandler`
- Player state synchronized across app via `HearitPlayerController`
- Always dispose audio resources properly

### Device Identification
- Device UUID managed via `DeviceUUIDService`
- UUID automatically added to API requests via `DeviceUUIDInterceptor`
- Stored securely using `flutter_secure_storage`

### Assets & Fonts
- Images: `assets/images/` (LP backgrounds, icons)
- Fonts: Pretendard (various weights), Prompt ExtraBold
- App icons: `assets/icon/app_icon.png` (with Christmas variant)
- Configure in `pubspec.yaml` under `flutter.assets` and `flutter.fonts`

### API Configuration
- Base URL and endpoints in `core/network/api_config.dart`
- Use debug logging interceptor only in development
- Handle network errors gracefully via `ApiException`

## Code Style & Best Practices

### Flutter/Dart
- Follow `flutter_lints` rules (enabled via `analysis_options.yaml`)
- Use `const` constructors when possible for performance
- Prefer composition over inheritance
- Keep widgets small and focused (single responsibility)
- Use meaningful variable names (Korean comments are acceptable for team communication)

### Kotlin (Android)
- Follow Kotlin coding conventions
- Use data classes for models
- Leverage Kotlin extensions and lambdas
- Null safety with `?` and `!!` operators

### Java (Backend)
- Use Lombok to reduce boilerplate (`@Data`, `@Builder`, etc.)
- Follow Spring Boot best practices
- Use JPA repositories for data access
- Keep controllers thin, move business logic to services

### Swift (iOS)
- Follow Swift API Design Guidelines
- Use Swift's type safety features
- Minimal Flutter bridge code in AppDelegate

## Common Tasks

### Adding a New Feature
1. Create feature directory under `lib/features/[feature_name]/`
2. Implement MVVM pattern: Screen → ViewModel → Repository
3. Add models in `*_models.dart`
4. Create reusable widgets in `widgets/` subdirectory
5. Register ViewModel with Provider if needed
6. Add navigation entry point
7. Write tests for business logic

### Adding API Endpoints
1. Define endpoint in `core/network/api_config.dart`
2. Add method to appropriate Repository
3. Handle response/error cases
4. Update ViewModel to call repository
5. Update UI to reflect state changes

### Adding Analytics Events
1. Define event name in `core/analytics/analytics_event_names.dart`
2. Define param keys in `core/analytics/analytics_param_keys.dart`
3. Log event via `AnalyticsProvider.logEvent()`

### Debugging
- Use `DebugLoggingInterceptor` to inspect API calls
- Flutter DevTools for widget inspection and performance
- Android Studio/Xcode debuggers for native code
- Check Firebase Analytics DebugView for event tracking

## Build Commands

### Flutter
```bash
# Get dependencies
flutter pub get

# Run app (debug)
flutter run

# Run tests
flutter test

# Analyze code
flutter analyze

# Build for release
flutter build ios
flutter build android
```

### Android
```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Run tests
./gradlew test
```

### Backend
```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Run tests
./gradlew test
```

## Team Context

### Backend Team
- 사나(조은산), 멍구(이유영), 벡터(백승주), 가콩(최가빈)

### Android Team
- 조이(김가현), 미플(함범준), 비비(장민정)

### Communication
- Korean language is used for team communication in comments and documentation
- Issue/PR descriptions in Korean
- Code should be self-documenting with clear naming

## Resources

- [Project Wiki](https://github.com/woowacourse-teams/2025-hEARit/wiki) - 기술 스택, 그라운드 룰, 코드 컨벤션, 서비스 소개
- Flutter Documentation: https://flutter.dev/docs
- Spring Boot Documentation: https://spring.io/projects/spring-boot

## Notes for AI Agents

- This is an active development project by Woowa Course teams
- Respect existing architectural patterns (MVVM, Provider)
- Follow the issue/PR template conventions
- Test all changes appropriately
- Consider Korean team members when suggesting documentation
- Be mindful of Firebase quota and API rate limits
- Audio features are core to the app - handle with care
- Keep performance in mind (audio streaming, list rendering)
