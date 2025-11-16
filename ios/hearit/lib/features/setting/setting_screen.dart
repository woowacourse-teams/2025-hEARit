import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import 'oss_licenses_page.dart';

class SettingScreen extends StatelessWidget {
  const SettingScreen({super.key, this.onBackToHome});

  final VoidCallback? onBackToHome;
  static const Color _backgroundColor = Color(0xFF1F1F1F);
  static const TextStyle _itemTextStyle = TextStyle(
    color: Colors.white70,
    fontSize: 16,
    fontWeight: FontWeight.w500,
  );

  static const String _privacyPolicyUrl =
      'https://glistening-eclipse-58b.notion.site/231d39b9c3c3809b9f92ec3e812ea24b';
  static const String _termsUrl =
      'https://glistening-eclipse-58b.notion.site/231d39b9c3c3800eb03cc7e1fc00f6f1';

  Future<void> _openExternalUrl(BuildContext context, String url) async {
    final uri = Uri.parse(url);
    final launched = await launchUrl(uri, mode: LaunchMode.externalApplication);
    if (!launched) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('링크를 열 수 없어요. 잠시 후 다시 시도해주세요.')),
      );
    }
  }

  void _handleBack(BuildContext context) {
    final canPop = Navigator.of(context).canPop();
    if (canPop) {
      Navigator.of(context).pop();
    } else {
      onBackToHome?.call();
    }
  }

  void _openLicenses(BuildContext context) {
    Navigator.of(
      context,
    ).push(MaterialPageRoute(builder: (_) => const OssLicensesPage()));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _backgroundColor,
      appBar: AppBar(
        backgroundColor: _backgroundColor,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white),
          onPressed: () => _handleBack(context),
        ),
        title: const Text(
          '설정',
          style: TextStyle(
            color: Colors.white,
            fontSize: 24,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 12),
              _SettingItem(
                label: '개인정보처리방침',
                onTap: () => _openExternalUrl(context, _privacyPolicyUrl),
              ),
              const SizedBox(height: 20),
              _SettingItem(
                label: '이용 약관',
                onTap: () => _openExternalUrl(context, _termsUrl),
              ),
              const SizedBox(height: 20),
              _SettingItem(
                label: '오픈 라이선스',
                onTap: () => _openLicenses(context),
              ),
              const Spacer(),
              Center(
                child: Padding(
                  padding: const EdgeInsets.only(bottom: 24),
                  child: _SettingItem(
                    label: '피드백 및 문의하기',
                    onTap: () => _openExternalUrl(
                      context,
                      'https://forms.gle/KGjHNi9ASdN3jR5v6',
                    ),
                    textStyle: _itemTextStyle.copyWith(
                      color: Colors.white,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SettingItem extends StatelessWidget {
  const _SettingItem({required this.label, this.onTap, this.textStyle});

  final String label;
  final VoidCallback? onTap;
  final TextStyle? textStyle;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      splashColor: Colors.white12,
      highlightColor: Colors.white10,
      borderRadius: BorderRadius.circular(4),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 2),
        child: Text(label, style: textStyle ?? SettingScreen._itemTextStyle),
      ),
    );
  }
}
