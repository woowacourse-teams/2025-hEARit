package com.onair.hearit.presentation;

import com.onair.hearit.application.BookmarkService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.BookmarkHearitResponse;
import com.onair.hearit.dto.response.BookmarkInfoResponse;
import com.onair.hearit.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
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
@RequestMapping("/api/v1/bookmarks")
@Tag(name = "Bookmark", description = "북마크 및 북마크 재생목록")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 재생목록 조회", description = "로그인한 회원이 page, size로 재생목록을 조회합니다.")
    @GetMapping("/hearits")
    public ResponseEntity<PagedResponse<BookmarkHearitResponse>> readBookmarkHearits(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal CurrentMember member) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<BookmarkHearitResponse> responses = bookmarkService.getBookmarkHearits(member, pagingRequest);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "북마크 생성", description = "로그인한 회원이 히어릿 ID로 북마크를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "북마크 성공"),
                    @ApiResponse(responseCode = "400", description = "이미 북마크된 히어릿",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PostMapping("/hearits/{hearitId}")
    public ResponseEntity<BookmarkInfoResponse> createBookmark(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal CurrentMember member) {
        BookmarkInfoResponse response = bookmarkService.addBookmark(member, hearitId);
        return ResponseEntity.created(URI.create("/api/v1/hearits/" + hearitId)).body(response);
    }

    @Operation(summary = "북마크 삭제", description = "로그인한 히어릿 ID와 북마크 ID로 북마크를 삭제합니다.")
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable Long bookmarkId,
            @AuthenticationPrincipal CurrentMember member) {
        bookmarkService.deleteBookmark(bookmarkId, member);
        return ResponseEntity.noContent().build();
    }
}
