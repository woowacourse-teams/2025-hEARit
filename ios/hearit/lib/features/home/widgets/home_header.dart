import 'package:flutter/material.dart';

class HomeHeader extends StatelessWidget {
  const HomeHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'hEARit',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.w900,
                    color: Colors.white,
                  ),
            ),
            const SizedBox(height: 4),
            Text(
              'IT를 쉽게 들을 수 있는 팟캐스트, 히어릿',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: const Color(0xFFBFBFBF),
                    fontSize: 15,
                  ),
            ),
          ],
        ),
        CircleAvatar(
          radius: 18,
          backgroundColor: const Color(0xFF6C5CE7),
          child: Text(
            'H',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
          ),
        ),
      ],
    );
  }
}
