import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

/// 라이브러리 메인 화면
/// 사용자가 북마크한 히어릿 목록 표시
class LibraryScreen extends StatefulWidget {
  const LibraryScreen({super.key});

  @override
  State<LibraryScreen> createState() => _LibraryScreenState();
}

class _LibraryScreenState extends State<LibraryScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.hearitBlack,
      appBar: AppBar(
        backgroundColor: AppColors.hearitBlack,
        elevation: 0,
        centerTitle: true,
        title: const Text(
          '라이브러리',
          style: TextStyle(
            color: Colors.white,
            fontSize: 24,
            fontWeight: FontWeight.w600,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings, color: Colors.white),
            onPressed: () {
              // TODO: 설정 화면으로 이동
            },
          ),
        ],
      ),
      body: const SafeArea(
        child: Center(
          child: Text(
            '라이브러리 화면 (구현 예정)',
            style: TextStyle(color: Colors.white),
          ),
        ),
      ),
    );
  }
}
