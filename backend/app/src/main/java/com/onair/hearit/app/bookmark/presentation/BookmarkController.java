package com.onair.hearit.app.bookmark.presentation;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.bookmark.application.BookmarkService;
import com.onair.hearit.app.bookmark.dto.BookmarkHearitResponseV2;
import com.onair.hearit.app.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.app.bookmark.dto.param.BookmarkFilter;
import com.onair.hearit.app.bookmark.dto.param.BookmarkSort;
import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /*will be deprecated after the client update*/
    @GetMapping("/api/v2/bookmarks/hearits")
    public ResponseEntity<PagedResponse<BookmarkHearitResponseV2>> readBookmarkHearitsV2(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<BookmarkHearitResponseV2> response = bookmarkService.getBookmarkHearitsV2(
                requestUser.getUserInfo(),
                pagingRequest,
                BookmarkFilter.ALL);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/bookmarks")
    public ResponseEntity<PagedResponse<BookmarkHearitResponseV2>> readBookmarkHearits(
            @RequestParam(name = "filter", defaultValue = "all") BookmarkFilter filter,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") BookmarkSort sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<BookmarkHearitResponseV2> response = bookmarkService.getBookmarkHearits(
                requestUser.getUserInfo(),
                pagingRequest,
                filter,
                sort);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/bookmarks/hearits/{hearitId}")
    public ResponseEntity<BookmarkInfoResponse> createBookmark(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        BookmarkInfoResponse response = bookmarkService.addBookmark(requestUser.getUserInfo(), hearitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/v1/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable Long bookmarkId,
            @AuthenticationPrincipal RequestUser requestUser) {
        bookmarkService.deleteBookmark(bookmarkId, requestUser.getUserInfo());
        return ResponseEntity.noContent().build();
    }
}
