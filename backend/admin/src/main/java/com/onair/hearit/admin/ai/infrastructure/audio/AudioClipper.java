package com.onair.hearit.admin.ai.infrastructure.audio;


public interface AudioClipper {

    byte[] clip(byte[] audioData, String extension, int durationSeconds);

    double getDuration(byte[] audioData, String extension);

    int getBitrate(byte[] audioData, String extension);
}
