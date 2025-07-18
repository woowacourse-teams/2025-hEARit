package com.onair.hearit.presentation;

import com.onair.hearit.application.BookmarkService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits/")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    //TODO 로그인한 사용자 아이디로 수정
    @PostMapping("/{hearitId}/bookmarks")
    public ResponseEntity<Void> createBookmark(@PathVariable Long hearitId, Long memberId) {
        bookmarkService.addBookmark(hearitId, memberId);
        //TODO 단건조회 API 넘기는데 이게 맞음?
        return ResponseEntity.created(URI.create("/api/v1/hearits/{id}")).build();
    }

    //TODO 로그인한 사용자 아이디로 수정
    @DeleteMapping("/{hearitId}/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long hearitId, @PathVariable Long bookmarkId, Long memberId) {
        bookmarkService.deleteBookmark(bookmarkId, memberId);
        return ResponseEntity.noContent().build();
    }
}
