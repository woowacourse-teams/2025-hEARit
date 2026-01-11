import 'package:audio_service/audio_service.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:provider/provider.dart';
import 'package:kakao_flutter_sdk_common/kakao_flutter_sdk_common.dart';

import 'core/analytics/analytics_provider.dart';
import 'core/audio/audio_handler.dart';
import 'core/audio/hearit_player_controller.dart';
import 'core/device/device_uuid_service.dart';
import 'features/library/library_repository.dart';
import 'firebase_options.dart';
import 'core/theme/app_colors.dart';
import 'features/auth/auth_viewmodel.dart';
import 'features/auth/splash_screen.dart';
import 'features/setting/setting_viewmodel.dart';

const SystemUiOverlayStyle _lightStatusBar = SystemUiOverlayStyle(
  statusBarColor: AppColors.hearitBlack,
  statusBarIconBrightness: Brightness.light,
  statusBarBrightness: Brightness.dark,
);

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Load environment variables
  await dotenv.load(fileName: ".env");

  // Initialize Kakao SDK with environment variable
  final kakaoKey = dotenv.env['KAKAO_NATIVE_APP_KEY'];
  if (kakaoKey == null || kakaoKey.isEmpty) {
    throw Exception('KAKAO_NATIVE_APP_KEY not found in .env file');
  }
  KakaoSdk.init(nativeAppKey: kakaoKey);

  SystemChrome.setSystemUIOverlayStyle(_lightStatusBar);
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  // Warm up device UUID so network calls don't block on first launch.
  await DeviceUUIDService.getUUID();
  AnalyticsProvider.configure(
    FirebaseAnalyticsLogger(FirebaseAnalytics.instance),
  );
  final audioHandler = await AudioService.init(
    builder: () => LocalAudioHandler(),
    config: const AudioServiceConfig(
      androidNotificationChannelId: 'hearit.playback',
      androidNotificationChannelName: 'Hearit Playback',
      androidNotificationOngoing: true,
    ),
  );
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => HearitPlayerController(
            audioHandler: audioHandler,
            libraryRepository: LibraryRepository(),
          ),
        ),
        ChangeNotifierProvider(create: (_) => AuthViewModel()),
        ChangeNotifierProxyProvider<AuthViewModel, SettingViewModel>(
          create: (context) =>
              SettingViewModel(authViewModel: context.read<AuthViewModel>()),
          update: (context, authViewModel, previous) =>
              previous ?? SettingViewModel(authViewModel: authViewModel),
        ),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with WidgetsBindingObserver {
  bool _wasPlayingExplorePreviewBeforeBackground = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);

    // Get player controller from context
    final playerController = context.read<HearitPlayerController>();

    switch (state) {
      case AppLifecycleState.paused:
      case AppLifecycleState.inactive:
        // App goes to background or receives interruption
        playerController.saveOnAppPaused();

        // 탐색 화면 미리듣기 중이면 일시정지
        if (playerController.isPlayingExplorePreview) {
          // 이미 true로 저장된 경우 덮어쓰지 않음 (여러 생명주기 이벤트 연속 발생 대응)
          if (!_wasPlayingExplorePreviewBeforeBackground) {
            _wasPlayingExplorePreviewBeforeBackground =
                playerController.isPlaying;
          }

          // 실제로 재생 중일 때만 일시정지 실행
          if (_wasPlayingExplorePreviewBeforeBackground &&
              playerController.isPlaying) {
            // 비동기 작업을 별도 태스크로 실행
            Future.microtask(() => playerController.pause());
          }
        }
        break;
      case AppLifecycleState.resumed:
        // App returns to foreground
        // 탐색 화면 미리듣기였고 재생 중이었다면 다시 재생
        if (playerController.isPlayingExplorePreview &&
            _wasPlayingExplorePreviewBeforeBackground) {
          // 비동기 작업을 별도 태스크로 실행
          Future.microtask(() => playerController.play());
          _wasPlayingExplorePreviewBeforeBackground = false;
        }
        break;
      case AppLifecycleState.detached:
        // App is about to terminate
        playerController.saveOnAppDetached();
        break;
      case AppLifecycleState.hidden:
        // No action needed
        break;
    }
  }

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'hEARit',
      debugShowCheckedModeBanner: false,
      builder: (context, child) => AnnotatedRegion<SystemUiOverlayStyle>(
        value: _lightStatusBar,
        child: child ?? const SizedBox.shrink(),
      ),
      theme: ThemeData(
        fontFamily: 'Pretendard',
        appBarTheme: const AppBarTheme(systemOverlayStyle: _lightStatusBar),
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const SplashScreen(),
    );
  }
}
