package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.BookmarkHearitResponse;
import com.onair.hearit.app.dto.response.BookmarkInfoResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.UserInfo;
import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.ForbiddenException;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthenticatedException;
import com.onair.hearit.common.infrastructure.dto.BookmarkWithPlaytimeProjection;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
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

    public PagedResponse<BookmarkHearitResponse> getBookmarkHearits(
            UserInfo userInfo,
            PagingRequest pagingRequest) {
        Member member = getMemberByUserInfo(userInfo);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<BookmarkWithPlaytimeProjection> projections = bookmarkRepository.findAllByMemberOrderByRecent(
                member.getId(),
                pageable);
        Page<BookmarkHearitResponse> response = toBookmarkHearitResponse(projections);
        return PagedResponse.from(response);
    }

    private Page<BookmarkHearitResponse> toBookmarkHearitResponse(Page<BookmarkWithPlaytimeProjection> projections) {
        return projections.map(p -> BookmarkHearitResponse.of(
                p.getBookmark(),
                p.getBookmark().getHearit(),
                p.getLastPlayTime()
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
