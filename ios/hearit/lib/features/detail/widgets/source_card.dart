import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../core/theme/app_colors.dart';
import '../detail_font.dart';
import '../hearit_detail.dart';

class SourceCard extends StatelessWidget {
  const SourceCard({
    super.key,
    required this.sources,
    this.onSourceTap,
  });

  final List<HearitSource> sources;
  final void Function(HearitSource source)? onSourceTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.gray1,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '출처',
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
              fontFamily: detailFontFamily,
              color: AppColors.gray3,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 8),
          if (sources.isEmpty)
            Text(
              '출처 정보가 없어요.',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                fontFamily: detailFontFamily,
                color: AppColors.gray2,
                fontWeight: FontWeight.w500,
              ),
            )
          else
            ...sources.map(
              (source) => Padding(
                padding: const EdgeInsets.only(bottom: 6),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(top: 3),
                      child: Icon(Icons.link, color: Colors.white70, size: 18),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: source.sourceUrl.isEmpty
                          ? Text(
                              source.sourceName,
                              style: Theme.of(context).textTheme.bodyMedium
                                  ?.copyWith(
                                    fontFamily: detailFontFamily,
                                    fontWeight: FontWeight.w500,
                                    color: AppColors.gray2,
                                    decoration: TextDecoration.none,
                                  ),
                            )
                          : InkWell(
                              onTap: () {
                                onSourceTap?.call(source);
                                _launch(source.sourceUrl);
                              },
                              child: Text(
                                source.sourceName,
                                style: Theme.of(context).textTheme.bodyMedium
                                    ?.copyWith(
                                      fontFamily: detailFontFamily,
                                      fontWeight: FontWeight.w500,
                                      color: AppColors.gray2,
                                      decoration: TextDecoration.underline,
                                      decorationColor: AppColors.gray2,
                                    ),
                              ),
                            ),
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }

  Future<void> _launch(String url) async {
    final Uri uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }
}
