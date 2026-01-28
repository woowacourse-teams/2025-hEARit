package com.onair.hearit.admin.ai.infrastructure.audio;

import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;

@Component
@Slf4j
public class FfmpegAudioClipper implements AudioClipper {

    @Override
    public byte[] clip(byte[] audioData, String extension, int durationSeconds) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("audio-clip-");
            String uuid = UUID.randomUUID().toString();
            File inputFile = tempDir.resolve("input-" + uuid + "." + extension).toFile();
            Files.write(inputFile.toPath(), audioData);
            File outputFile = tempDir.resolve("output-" + uuid + "." + extension).toFile();

            MultimediaObject multimediaObject = new MultimediaObject(inputFile);
            MultimediaInfo info = multimediaObject.getInfo();
            long originalDurationMs = info.getDuration();
            double originalDurationSec = originalDurationMs / 1000.0;
            log.info("원본 오디오 길이: {}초, 목표 길이: {}초", String.format("%.1f", originalDurationSec), durationSeconds);

            if (originalDurationSec <= durationSeconds) {
                log.info("원본이 목표보다 짧아 전체 반환");
                return audioData;
            }

            AudioAttributes audioAttributes = new AudioAttributes();
            audioAttributes.setCodec("copy");
            EncodingAttributes encodingAttributes = new EncodingAttributes();
            encodingAttributes.setOutputFormat(getFormatForExtension(extension));
            encodingAttributes.setAudioAttributes(audioAttributes);
            encodingAttributes.setOffset(0f);
            encodingAttributes.setDuration((float) durationSeconds);

            Encoder encoder = new Encoder();
            encoder.encode(multimediaObject, outputFile, encodingAttributes);

            MultimediaObject outputMultimedia = new MultimediaObject(outputFile);
            MultimediaInfo outputInfo = outputMultimedia.getInfo();
            double outputDurationSec = outputInfo.getDuration() / 1000.0;
            byte[] result = Files.readAllBytes(outputFile.toPath());
            log.info("쇼츠 생성 완료: 원본={}KB({}초), 결과={}KB({}초)",
                    audioData.length / 1024, String.format("%.1f", originalDurationSec),
                    result.length / 1024, String.format("%.1f", outputDurationSec));

            return result;

        } catch (EncoderException e) {
            log.error("FFmpeg 인코딩 실패", e);
            throw new AudioProcessingException("오디오 자르기 실패: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("파일 처리 실패", e);
            throw new AudioProcessingException("오디오 자르기 중 파일 처리 실패", e);
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    @Override
    public double getDuration(byte[] audioData, String extension) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("audio-duration-");
            File tempFile = tempDir.resolve("temp." + extension).toFile();
            Files.write(tempFile.toPath(), audioData);

            MultimediaObject multimediaObject = new MultimediaObject(tempFile);
            MultimediaInfo info = multimediaObject.getInfo();

            return info.getDuration() / 1000.0;

        } catch (Exception e) {
            log.warn("FFmpeg로 재생시간 추출 실패, 기본값 반환", e);
            return -1;
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    @Override
    public int getBitrate(byte[] audioData, String extension) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("audio-bitrate-");
            File tempFile = tempDir.resolve("temp." + extension).toFile();
            Files.write(tempFile.toPath(), audioData);

            MultimediaObject multimediaObject = new MultimediaObject(tempFile);
            MultimediaInfo info = multimediaObject.getInfo();

            if (info.getAudio() != null) {
                return info.getAudio().getBitRate() / 1000; // bps -> kbps
            }
            return -1;

        } catch (Exception e) {
            log.warn("FFmpeg로 비트레이트 추출 실패", e);
            return -1;
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    private String getFormatForExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "mp3" -> "mp3";
            case "m4a" -> "mp4";
            case "aac" -> "adts";
            default -> extension;
        };
    }

    private void cleanupTempDir(Path tempDir) {
        if (tempDir == null) {
            return;
        }
        try {
            File[] files = tempDir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        log.warn("임시 파일 삭제 실패: {}", file.getAbsolutePath());
                    }
                }
            }
            if (!tempDir.toFile().delete()) {
                log.warn("임시 디렉토리 삭제 실패: {}", tempDir);
            }
        } catch (Exception e) {
            log.warn("임시 디렉토리 정리 실패", e);
        }
    }
}
