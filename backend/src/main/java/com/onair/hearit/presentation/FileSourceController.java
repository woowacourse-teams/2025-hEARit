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
        OriginalAudioResponse response = fileSourceService.getOriginalAudio(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/short-audio-url")
    public ResponseEntity<ShortAudioResponse> readShortAudioUrl(@PathVariable Long id) {
        ShortAudioResponse response = fileSourceService.getShortAudio(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/script-url")
    public ResponseEntity<ScriptResponse> readScriptUrl(@PathVariable Long id) {
        ScriptResponse response = fileSourceService.getScript(id);
        return ResponseEntity.ok(response);
    }
}
