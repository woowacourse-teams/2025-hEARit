import 'package:audio_service/audio_service.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:kakao_flutter_sdk_common/kakao_flutter_sdk_common.dart';

import 'core/analytics/analytics_provider.dart';
import 'core/audio/audio_handler.dart';
import 'core/audio/hearit_player_controller.dart';
import 'core/device/device_uuid_service.dart';
import 'firebase_options.dart';
import 'core/theme/app_colors.dart';
import 'features/auth/auth_viewmodel.dart';
import 'features/auth/splash_screen.dart';

const SystemUiOverlayStyle _lightStatusBar = SystemUiOverlayStyle(
  statusBarColor: AppColors.hearitBlack,
  statusBarIconBrightness: Brightness.light,
  statusBarBrightness: Brightness.dark,
);

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  KakaoSdk.init(nativeAppKey: '613a999d3a3db5d91f9a1c3565bdb4e1');
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
          create: (_) => HearitPlayerController(audioHandler: audioHandler),
        ),
        ChangeNotifierProvider(create: (_) => AuthViewModel()),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

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
