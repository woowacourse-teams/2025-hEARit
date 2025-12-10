import 'package:audio_service/audio_service.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import 'core/analytics/analytics_provider.dart';
import 'core/audio/audio_handler.dart';
import 'core/audio/hearit_player_controller.dart';
import 'core/device/device_uuid_service.dart';
import 'core/presentation/main_navigation.dart';
import 'firebase_options.dart';
import 'core/theme/app_colors.dart';

const SystemUiOverlayStyle _lightStatusBar = SystemUiOverlayStyle(
  statusBarColor: AppColors.hearitBlack,
  statusBarIconBrightness: Brightness.light,
  statusBarBrightness: Brightness.dark,
);

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
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
    ChangeNotifierProvider(
      create: (_) => HearitPlayerController(audioHandler: audioHandler),
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
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      builder: (context, child) => AnnotatedRegion<SystemUiOverlayStyle>(
        value: _lightStatusBar,
        child: child ?? const SizedBox.shrink(),
      ),
      theme: ThemeData(
        fontFamily: 'Pretendard',
        appBarTheme: const AppBarTheme(systemOverlayStyle: _lightStatusBar),
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a purple toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MainNavigation(),
    );
  }
}
