package com.onair.hearit.application;

import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.BookmarkHearitResponse;
import com.onair.hearit.dto.response.BookmarkInfoResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    public PagedResponse<BookmarkHearitResponse> getBookmarkHearits(CurrentMember currentMember, PagingRequest pagingRequest) {
        Member member = getMemberByCurrentMember(currentMember);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByMemberOrderByCreatedAtDesc(member, pageable);
        Page<BookmarkHearitResponse> bookmarkHearits = bookmarks.map(
                bookmark -> BookmarkHearitResponse.of(bookmark, bookmark.getHearit()));
        return PagedResponse.from(bookmarkHearits);
    }

    @Transactional
    public BookmarkInfoResponse addBookmark(CurrentMember currentMember, Long hearitId) {
        Member member = getMemberByCurrentMember(currentMember);
        Hearit hearit = getHearitById(hearitId);
        if (bookmarkRepository.existsByHearitIdAndMemberId(hearit.getId(), member.getId())) {
            throw new AlreadyExistException("이미 북마크된 히어릿입니다.");
        }
        Bookmark bookmark = new Bookmark(member, hearit);
        Bookmark saved = bookmarkRepository.save(bookmark);
        return BookmarkInfoResponse.from(saved);
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId, CurrentMember currentMember) {
        Bookmark bookmark = getBookmarkById(bookmarkId);
        Member member = getMemberByCurrentMember(currentMember);
        if (!bookmark.isCreatedBy(member)) {
            throw new UnauthorizedException("북마크를 삭제할 권한이 없습니다.");
        }
        bookmarkRepository.delete(bookmark);
    }

    private Member getMemberByCurrentMember(CurrentMember currentMember) {
        if(currentMember == null) {
            throw new UnauthorizedException("로그인한 회원이 아닙니다.");
        }
        return getMemberById(currentMember.memberId());
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
