package com.onair.hearit.presentation;

import com.onair.hearit.application.FileSourceService;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits")
@Tag(name = "FileSource", description = "오디오 및 스크립트 파일")
public class FileSourceController {

    private final FileSourceService fileSourceService;

    @Operation(summary = "원본 오디오 파일 조회", description = "히어릿ID룰 통해 원본 오디오 파일을 조회합니다.")
    @GetMapping("/{hearitId}/original-audio-url")
    public ResponseEntity<OriginalAudioResponse> readOriginalAudioUrl(@PathVariable Long hearitId) {
        OriginalAudioResponse response = fileSourceService.getOriginalAudio(hearitId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "1분 미리보기 오디오 파일 조회", description = "히어릿ID룰 통해 1분 미리보기 오디오 파일을 조회합니다.")
    @GetMapping("/{hearitId}/short-audio-url")
    public ResponseEntity<ShortAudioResponse> readShortAudioUrl(@PathVariable Long hearitId) {
        ShortAudioResponse response = fileSourceService.getShortAudio(hearitId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스크립트 파일 조회", description = "히어릿ID룰 통해 스크립트 파일을 조회합니다.")
    @GetMapping("/{hearitId}/script-url")
    public ResponseEntity<ScriptResponse> readScriptUrl(@PathVariable Long hearitId) {
        ScriptResponse response = fileSourceService.getScript(hearitId);
        return ResponseEntity.ok(response);
    }
}
