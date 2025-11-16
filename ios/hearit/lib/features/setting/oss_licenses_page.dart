import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher_string.dart';

import '../../oss_licenses.dart' as ossdata;

class OssLicensesPage extends StatelessWidget {
  const OssLicensesPage({super.key});

  static Future<List<ossdata.Package>> _loadLicenses() async {
    final merged = <String, List<String>>{};
    await for (final entry in LicenseRegistry.licenses) {
      for (final package in entry.packages) {
        final paragraphs = merged.putIfAbsent(package, () => []);
        paragraphs.addAll(entry.paragraphs.map((p) => p.text));
      }
    }
    final licenses = ossdata.allDependencies.toList();
    for (final packageName in merged.keys) {
      licenses.add(
        ossdata.Package(
          name: packageName,
          description: '',
          authors: const [],
          version: '',
          license: merged[packageName]!.join('\n\n'),
          isMarkdown: false,
          isSdk: false,
          dependencies: const [],
          devDependencies: const [],
        ),
      );
    }
    licenses.sort((a, b) => a.name.compareTo(b.name));
    return licenses;
  }

  static final Future<List<ossdata.Package>> _licenses = _loadLicenses();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1F1F1F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF1F1F1F),
        elevation: 0,
        title: const Text(
          '오픈 라이선스',
          style: TextStyle(
            color: Colors.white,
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: FutureBuilder<List<ossdata.Package>>(
        future: _licenses,
        builder: (context, snapshot) {
          final data = snapshot.data ?? const [];
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (data.isEmpty) {
            return const Center(
              child: Text(
                '라이선스 정보를 불러올 수 없습니다.',
                style: TextStyle(color: Colors.white70),
              ),
            );
          }
          return ListView.separated(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: data.length,
            separatorBuilder: (_, __) => const Divider(
              height: 1,
              color: Color(0xFF3A3A3A),
            ),
            itemBuilder: (context, index) {
              final package = data[index];
              return ListTile(
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => _LicenseDetailPage(package: package),
                  ),
                ),
                title: Text(
                  '${package.name} ${package.version}',
                  style: const TextStyle(color: Colors.white),
                ),
                subtitle: package.description.isNotEmpty
                    ? Text(
                        package.description,
                        style: const TextStyle(color: Colors.white70),
                      )
                    : null,
                trailing: const Icon(Icons.chevron_right, color: Colors.white70),
              );
            },
          );
        },
      ),
    );
  }
}

class _LicenseDetailPage extends StatelessWidget {
  const _LicenseDetailPage({required this.package});

  final ossdata.Package package;

  String _bodyText() {
    return (package.license ?? '')
        .split('\n')
        .map((line) {
          var text = line;
          if (text.startsWith('//')) text = text.substring(2);
          return text.trimRight();
        })
        .join('\n');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1F1F1F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF1F1F1F),
        elevation: 0,
        title: Text(
          package.name,
          style: const TextStyle(color: Colors.white),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.copy, color: Colors.white),
            onPressed: () {
              Clipboard.setData(ClipboardData(text: _bodyText()));
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('라이선스가 복사되었습니다.')),
              );
            },
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        children: [
          if (package.description.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Text(
                package.description,
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          if (package.homepage != null && package.homepage!.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: InkWell(
                onTap: () => launchUrlString(package.homepage!),
                child: Text(
                  package.homepage!,
                  style: const TextStyle(
                    color: Colors.lightBlueAccent,
                    decoration: TextDecoration.underline,
                  ),
                ),
              ),
            ),
          Text(
            _bodyText(),
            style: const TextStyle(color: Colors.white70, height: 1.4),
          ),
        ],
      ),
    );
  }
}
