import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../core/audio/hearit_player_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../detail_font.dart';
import 'bookmark_button.dart';

class AudioControls extends StatefulWidget {
  const AudioControls({
    super.key,
    required this.controller,
    required this.onSeekRelative,
    required this.onTogglePlayback,
    required this.onSpeedSelected,
    required this.speedOptions,
    required this.currentSpeed,
    required this.speedLabel,
    required this.formatDuration,
    required this.isBookmarked,
    required this.onBookmarkToggle,
    this.isTablet = false,
  });

  final HearitPlayerController controller;
  final void Function(Duration offset) onSeekRelative;
  final VoidCallback onTogglePlayback;
  final ValueChanged<double> onSpeedSelected;
  final List<double> speedOptions;
  final double currentSpeed;
  final String speedLabel;
  final String Function(Duration) formatDuration;
  final bool isBookmarked;
  final VoidCallback onBookmarkToggle;
  final bool isTablet;

  @override
  State<AudioControls> createState() => _AudioControlsState();
}

class _AudioControlsState extends State<AudioControls> {
  double? _dragPositionMillis;
  String _formatSpeed(double speed) {
    final int hundred = (speed * 100).round();
    final int remainder = hundred % 100;
    if (remainder == 0 || remainder == 50) {
      return speed.toStringAsFixed(1);
    }
    return speed.toStringAsFixed(2);
  }

  Duration _effectivePosition(Duration position, Duration duration) {
    if (_dragPositionMillis != null) {
      final ms = _dragPositionMillis!.clamp(
        0.0,
        duration.inMilliseconds.toDouble(),
      );
      return Duration(milliseconds: ms.round());
    }
    return position > duration ? duration : position;
  }

  @override
  Widget build(BuildContext context) {
    final double fontDelta = widget.isTablet ? 5 : 0;
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final double popupMaxHeight = 210;
        final duration = widget.controller.duration;
        final currentPosition = _effectivePosition(
          widget.controller.position,
          duration,
        );
        final totalMillis = math.max(duration.inMilliseconds, 1);
        final isBuffering = widget.controller.isBuffering;
        final playing = widget.controller.isPlaying;

        return Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SliderTheme(
              data: SliderTheme.of(context).copyWith(
                trackHeight: 3.5,
                trackShape: const RectangularSliderTrackShape(),
                thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 7),
                overlayShape: SliderComponentShape.noOverlay,
                activeTrackColor: AppColors.hearitPurple3,
                inactiveTrackColor: AppColors.gray4,
                thumbColor: AppColors.hearitPurple3,
                overlayColor: AppColors.hearitPurple3.withOpacity(0.2),
              ),
              child: Slider(
                value: currentPosition.inMilliseconds.toDouble(),
                max: totalMillis.toDouble(),
                onChangeStart: duration == Duration.zero
                    ? null
                    : (value) => setState(() {
                        _dragPositionMillis = value;
                      }),
                onChanged: duration == Duration.zero
                    ? null
                    : (value) => setState(() {
                        _dragPositionMillis = value;
                      }),
                onChangeEnd: duration == Duration.zero
                    ? null
                    : (value) async {
                        final target = Duration(milliseconds: value.round());
                        await widget.controller.seek(target);
                        if (mounted) {
                          setState(() {
                            _dragPositionMillis = null;
                          });
                        } else {
                          _dragPositionMillis = null;
                        }
                      },
              ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  widget.formatDuration(currentPosition),
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    fontFamily: detailFontFamily,
                    color: AppColors.gray4,
                    fontWeight: FontWeight.w500,
                    fontSize:
                        (Theme.of(context).textTheme.bodySmall?.fontSize ??
                            12) +
                        fontDelta,
                  ),
                ),
                Text(
                  widget.formatDuration(duration),
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    fontFamily: detailFontFamily,
                    color: AppColors.gray4,
                    fontWeight: FontWeight.w500,
                    fontSize:
                        (Theme.of(context).textTheme.bodySmall?.fontSize ??
                            12) +
                        fontDelta,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                BookmarkButton(
                  isBookmarked: widget.isBookmarked,
                  onPressed: widget.onBookmarkToggle,
                ),
                IconButton(
                  iconSize: 40,
                  onPressed: () =>
                      widget.onSeekRelative(const Duration(seconds: -10)),
                  icon: const Icon(Icons.replay_10, color: AppColors.gray4),
                ),
                Container(
                  width: 58,
                  height: 58,
                  decoration: const BoxDecoration(
                    color: AppColors.hearitPurple3,
                    shape: BoxShape.circle,
                  ),
                  child: isBuffering
                      ? const Padding(
                          padding: EdgeInsets.all(12),
                          child: CircularProgressIndicator(
                            strokeWidth: 3,
                            valueColor: AlwaysStoppedAnimation<Color>(
                              AppColors.gray4,
                            ),
                          ),
                        )
                      : IconButton(
                          onPressed: widget.onTogglePlayback,
                          icon: Icon(
                            playing ? Icons.pause : Icons.play_arrow,
                            color: AppColors.gray4,
                            size: 30,
                          ),
                        ),
                ),
                IconButton(
                  iconSize: 40,
                  onPressed: () =>
                      widget.onSeekRelative(const Duration(seconds: 10)),
                  icon: const Icon(Icons.forward_10, color: AppColors.gray4),
                ),
                PopupMenuButton<double>(
                  onSelected: widget.onSpeedSelected,
                  color: const Color(0xFF3A3A3A),
                  elevation: 6,
                  constraints: BoxConstraints(
                    maxHeight: popupMaxHeight,
                    minWidth: 140,
                  ),
                  itemBuilder: (context) {
                    return widget.speedOptions
                        .map(
                          (speed) => PopupMenuItem<double>(
                            value: speed,
                            child: Row(
                              children: [
                                if (speed == widget.currentSpeed)
                                  const Padding(
                                    padding: EdgeInsets.only(right: 8),
                                    child: Icon(
                                      Icons.check,
                                      size: 18,
                                      color: AppColors.gray4,
                                    ),
                                  )
                                else
                                  const SizedBox(width: 26),
                                Text(
                                  '${_formatSpeed(speed)}x',
                                  style: Theme.of(context).textTheme.bodyMedium
                                      ?.copyWith(
                                        fontFamily: detailFontFamily,
                                        color: AppColors.gray4,
                                        fontWeight: FontWeight.w500,
                                        fontSize: 16 + fontDelta,
                                      ),
                                ),
                              ],
                            ),
                          ),
                        )
                        .toList();
                  },
                  child: Text(
                    widget.speedLabel,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontFamily: detailFontFamily,
                      color: AppColors.gray4,
                      fontWeight: FontWeight.w700,
                      fontSize: 24 + fontDelta,
                    ),
                  ),
                ),
              ],
            ),
          ],
        );
      },
    );
  }
}
