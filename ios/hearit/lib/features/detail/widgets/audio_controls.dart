import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../core/audio/hearit_player_controller.dart';

class AudioControls extends StatefulWidget {
  const AudioControls({
    super.key,
    required this.controller,
    required this.isBookmarked,
    required this.onBookmarkToggle,
    required this.onSeekRelative,
    required this.onTogglePlayback,
    required this.onSpeedTap,
    required this.speedLabel,
    required this.formatDuration,
  });

  final HearitPlayerController controller;
  final bool isBookmarked;
  final VoidCallback onBookmarkToggle;
  final void Function(Duration offset) onSeekRelative;
  final VoidCallback onTogglePlayback;
  final VoidCallback onSpeedTap;
  final String speedLabel;
  final String Function(Duration) formatDuration;

  @override
  State<AudioControls> createState() => _AudioControlsState();
}

class _AudioControlsState extends State<AudioControls> {
  double? _dragPositionMillis;

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
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final duration = widget.controller.duration;
        final currentPosition =
            _effectivePosition(widget.controller.position, duration);
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
                activeTrackColor: const Color(0xFF9533F5),
                inactiveTrackColor: Colors.white12,
                thumbColor: const Color(0xFF9533F5),
                overlayColor: const Color(0x33A86BFF),
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
                        color: Colors.white70,
                        fontWeight: FontWeight.w600,
                      ),
                ),
                Text(
                  widget.formatDuration(duration),
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Colors.white70,
                        fontWeight: FontWeight.w600,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  iconSize: 40,
                  onPressed: widget.onBookmarkToggle,
                  icon: Icon(
                    widget.isBookmarked ? Icons.bookmark : Icons.bookmark_border,
                    color: widget.isBookmarked
                        ? const Color(0xFF9533F5)
                        : Colors.white,
                  ),
                ),
                IconButton(
                  iconSize: 40,
                  onPressed: () => widget.onSeekRelative(
                    const Duration(seconds: -10),
                  ),
                  icon: const Icon(Icons.replay_10, color: Colors.white),
                ),
                Container(
                  width: 58,
                  height: 58,
                  decoration: const BoxDecoration(
                    color: Color(0xFF9533F5),
                    shape: BoxShape.circle,
                  ),
                  child: isBuffering
                      ? const Padding(
                          padding: EdgeInsets.all(12),
                          child: CircularProgressIndicator(
                            strokeWidth: 3,
                            valueColor: AlwaysStoppedAnimation<Color>(
                              Colors.white,
                            ),
                          ),
                        )
                      : IconButton(
                          onPressed: widget.onTogglePlayback,
                          icon: Icon(
                            playing ? Icons.pause : Icons.play_arrow,
                            color: Colors.white,
                            size: 30,
                          ),
                        ),
                ),
                IconButton(
                  iconSize: 40,
                  onPressed: () => widget.onSeekRelative(
                    const Duration(seconds: 10),
                  ),
                  icon: const Icon(Icons.forward_10, color: Colors.white),
                ),
                GestureDetector(
                  onTap: widget.onSpeedTap,
                  child: Text(
                    widget.speedLabel,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          color: Colors.white,
                          fontWeight: FontWeight.w700,
                          fontSize: 24,
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
