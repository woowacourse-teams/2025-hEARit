package com.onair.hearit.app.bookmark.application;

import com.onair.hearit.app.bookmark.dto.BookmarkHearitResponseV2;
import com.onair.hearit.app.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.app.bookmark.dto.param.BookmarkFilter;
import com.onair.hearit.app.bookmark.dto.param.BookmarkSort;
import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.AlreadyExistException;
import com.onair.hearit.app.exception.custom.ForbiddenException;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.exception.custom.UnauthenticatedException;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import com.onair.hearit.core.infrastructure.projection.BookmarkWithPlayingHistoryProjection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional(readOnly = true)
    public PagedResponse<BookmarkHearitResponseV2> getBookmarkHearits(UserInfo userInfo,
                                                                      PagingRequest pagingRequest,
                                                                      BookmarkFilter filter,
                                                                      BookmarkSort sort) {
        if (userInfo == null || userInfo.isGuest()) {
            return PagedResponse.empty();
        }
        UUID memberUuid = getMemberUuidFromUserInfo(userInfo);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(), sort.toSort());
        Page<BookmarkWithPlayingHistoryProjection> projections = bookmarkRepository.findFilteredByMember(
                memberUuid,
                filter.isFinished(), pageable);
        return PagedResponse.from(toBookmarkHearitResponse(projections));
    }

    /*will be deprecated after the client update*/
    @Transactional(readOnly = true)
    public PagedResponse<BookmarkHearitResponseV2> getBookmarkHearitsV2(UserInfo userInfo,
                                                                        PagingRequest pagingRequest,
                                                                        BookmarkFilter filter) {
        UUID memberUuid = getMemberUuidFromUserInfo(userInfo);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(),
                Sort.by(Direction.DESC, "createdAt"));
        Page<BookmarkWithPlayingHistoryProjection> projections = bookmarkRepository.findFilteredByMember(
                memberUuid,
                filter.isFinished(), pageable);
        return PagedResponse.from(toBookmarkHearitResponse(projections));
    }

    private Page<BookmarkHearitResponseV2> toBookmarkHearitResponse(
            Page<BookmarkWithPlayingHistoryProjection> projections) {
        return projections.map(p -> BookmarkHearitResponseV2.of(
                p.getBookmark(),
                p.getBookmark().getHearit(),
                p.getPlayingHistory()
        ));
    }

    @Transactional
    public BookmarkInfoResponse addBookmark(UserInfo userInfo, Long hearitId) {
        UUID memberUuid = getMemberUuidFromUserInfo(userInfo);
        validateMemberExists(memberUuid);
        Hearit hearit = getHearitById(hearitId);
        if (bookmarkRepository.existsByHearitAndMemberUuid(hearit, memberUuid)) {
            throw new AlreadyExistException("이미 북마크된 히어릿입니다.");
        }
        Bookmark bookmark = new Bookmark(memberUuid, hearit);
        Bookmark saved = bookmarkRepository.save(bookmark);
        return BookmarkInfoResponse.from(saved);
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId, UserInfo userInfo) {
        Bookmark bookmark = getBookmarkById(bookmarkId);
        UUID memberUuid = getMemberUuidFromUserInfo(userInfo);
        if (!bookmark.isCreatedBy(memberUuid)) {
            throw new ForbiddenException("북마크를 삭제할 권한이 없습니다.");
        }
        bookmarkRepository.delete(bookmark);
    }

    private UUID getMemberUuidFromUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new UnauthenticatedException();
        }
        return userInfo.getUuid();
    }

    private Bookmark getBookmarkById(Long bookmarkId) {
        return bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new NotFoundException("bookmarkId", bookmarkId.toString()));
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    private void validateMemberExists(UUID memberUuid) {
        if (!memberRepository.findByUuid(memberUuid).isPresent()) {
            throw new NotFoundException("memberUuid", memberUuid.toString());
        }
    }
}
