import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../hearit_detail.dart';

class SourceCard extends StatelessWidget {
  const SourceCard({super.key, required this.sources});

  final List<HearitSource> sources;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFF2C2C2C),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '출처',
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 8),
          if (sources.isEmpty)
            Text(
              '출처 정보가 없어요.',
              style: Theme.of(
                context,
              ).textTheme.bodyMedium?.copyWith(color: Colors.white70),
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
                                    color: Colors.white70,
                                    decoration: TextDecoration.none,
                                  ),
                            )
                          : InkWell(
                              onTap: () => _launch(source.sourceUrl),
                              child: Text(
                                source.sourceName,
                                style: Theme.of(context).textTheme.bodyMedium
                                    ?.copyWith(
                                      color: const Color(0xFFB5C6FF),
                                      decoration: TextDecoration.underline,
                                      decorationColor: const Color(0xFFB5C6FF),
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
