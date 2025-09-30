package com.onair.hearit.app.bookmark.application;

import com.onair.hearit.app.bookmark.dto.BookmarkHearitResponseV2;
import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.app.exception.custom.AlreadyExistException;
import com.onair.hearit.app.exception.custom.ForbiddenException;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.exception.custom.UnauthenticatedException;
import com.onair.hearit.core.infrastructure.projection.BookmarkWithPlayingHistoryProjection;
import com.onair.hearit.core.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    public Page<BookmarkHearitResponseV2> getBookmarkHearits(
            UserInfo userInfo,
            PagingRequest pagingRequest) {
        Member member = getMemberByUserInfo(userInfo);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<BookmarkWithPlayingHistoryProjection> projections = bookmarkRepository.findAllByMemberOrderByRecent(
                member.getId(),
                pageable);
        return toBookmarkHearitResponse(projections);
    }

    private Page<BookmarkHearitResponseV2> toBookmarkHearitResponse(Page<BookmarkWithPlayingHistoryProjection> projections) {
        return projections.map(p -> BookmarkHearitResponseV2.of(
                p.getBookmark(),
                p.getBookmark().getHearit(),
                p.getPlayingHistory()
        ));
    }

    @Transactional
    public BookmarkInfoResponse addBookmark(UserInfo userInfo, Long hearitId) {
        Member member = getMemberByUserInfo(userInfo);
        Hearit hearit = getHearitById(hearitId);
        if (bookmarkRepository.existsByHearitAndMember(hearit, member)) {
            throw new AlreadyExistException("이미 북마크된 히어릿입니다.");
        }
        Bookmark bookmark = new Bookmark(member, hearit);
        Bookmark saved = bookmarkRepository.save(bookmark);
        return BookmarkInfoResponse.from(saved);
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId, UserInfo userInfo) {
        Bookmark bookmark = getBookmarkById(bookmarkId);
        Member member = getMemberByUserInfo(userInfo);
        if (!bookmark.isCreatedBy(member)) {
            throw new ForbiddenException("북마크를 삭제할 권한이 없습니다.");
        }
        bookmarkRepository.delete(bookmark);
    }

    private Member getMemberByUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new UnauthenticatedException();
        }
        return getMemberById(userInfo.getMemberId());
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }

    private Bookmark getBookmarkById(Long bookmarkId) {
        return bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new NotFoundException("bookmarkId", bookmarkId.toString()));
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
