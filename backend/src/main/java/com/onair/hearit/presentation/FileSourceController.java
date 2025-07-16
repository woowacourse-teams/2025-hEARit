package com.onair.hearit.presentation;

import com.onair.hearit.application.FileSourceService;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits")
public class FileSourceController {

    private final FileSourceService fileSourceService;

    @GetMapping("/{id}/original-audio-url")
    public ResponseEntity<OriginalAudioResponse> readOriginalAudioUrl(@PathVariable Long id) {
        return ResponseEntity.ok(fileSourceService.getOriginalAudio(id));
    }

    @GetMapping("/{id}/short-audio-url")
    public ResponseEntity<ShortAudioResponse> readShortAudioUrl(@PathVariable Long id) {
        return ResponseEntity.ok(fileSourceService.getShortAudio(id));
    }

    @GetMapping("/{id}/script-url")
    public ResponseEntity<ScriptResponse> readScriptUrl(@PathVariable Long id) {
        return ResponseEntity.ok(fileSourceService.getScript(id));
    }
}
