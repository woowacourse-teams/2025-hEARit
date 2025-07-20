package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.BookmarkHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    public List<BookmarkHearitResponse> getBookmarkHearits(Long memberId, int page, int size) {
        Member member = getMemberById(memberId);
        Pageable pageable = PageRequest.of(page, size);
        List<Bookmark> bookmarks = bookmarkRepository.findAllByMemberOrderByCreatedAtDesc(member, pageable);
        return bookmarks.stream()
                .map(BookmarkHearitResponse::from)
                .toList();
    }

    @Transactional
    public void addBookmark(Long hearitId, Long memberId) {
        if (bookmarkRepository.existsByHearitIdAndMemberId(hearitId, memberId)) {
            throw new AlreadyExistException("이미 북마크된 히어릿입니다.");
        }
        Hearit hearit = getHearitById(hearitId);
        Member member = getMemberById(memberId);
        Bookmark bookmark = new Bookmark(member, hearit);
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId, Long memberId) {
        Bookmark bookmark = getBookmarkById(bookmarkId);
        Member member = getMemberById(memberId);
        if (!bookmark.isCreatedBy(member)) {
            throw new UnauthorizedException("북마크를 삭제할 권한이 없습니다.");
        }
        bookmarkRepository.delete(bookmark);
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
