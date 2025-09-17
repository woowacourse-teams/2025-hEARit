package com.onair.hearit.app.presentation;


import com.onair.hearit.app.application.BookmarkService;
import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.BookmarkHearitResponse;
import com.onair.hearit.app.dto.response.BookmarkHearitResponseV1;
import com.onair.hearit.app.dto.response.BookmarkInfoResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.auth.domain.RequestUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping("/v1/bookmarks/hearits")
    public ResponseEntity<PagedResponse<BookmarkHearitResponseV1>> readBookmarkHearitsV1(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<BookmarkHearitResponse> responses = bookmarkService.getBookmarkHearits(requestUser.getUserInfo(),
                pagingRequest);
        List<BookmarkHearitResponse> contents = responses.content();
        List<BookmarkHearitResponseV1> v1Contents = contents.stream()
                .map(BookmarkHearitResponseV1::from)
                .toList();
        PagedResponse<BookmarkHearitResponseV1> v1Responses =
                new PagedResponse<>(
                        v1Contents,
                        responses.page(),
                        responses.size(),
                        responses.totalPages(),
                        responses.totalElements(),
                        responses.isFirst(),
                        responses.isLast()
                );
        return ResponseEntity.ok(v1Responses);
    }

    @GetMapping("/v2/bookmarks/hearits")
    public ResponseEntity<PagedResponse<BookmarkHearitResponse>> readBookmarkHearitsV2(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<BookmarkHearitResponse> responses = bookmarkService.getBookmarkHearits(requestUser.getUserInfo(),
                pagingRequest);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/v1/bookmarks/hearits/{hearitId}")
    public ResponseEntity<BookmarkInfoResponse> createBookmark(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        BookmarkInfoResponse response = bookmarkService.addBookmark(requestUser.getUserInfo(), hearitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/v1/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable Long bookmarkId,
            @AuthenticationPrincipal RequestUser requestUser) {
        bookmarkService.deleteBookmark(bookmarkId, requestUser.getUserInfo());
        return ResponseEntity.noContent().build();
    }
}
