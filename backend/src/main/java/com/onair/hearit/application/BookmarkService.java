package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void addBookmark(Long hearitId, Long memberId) {
        if (bookmarkRepository.existsByHearitIdAndMemberId(hearitId, memberId)) {
            throw new AlreadyExistException("이미 북마크가 존재합니다.");
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
        if (bookmark.isCreatedBy(member)) {
            bookmarkRepository.delete(bookmark);
        }
        throw new UnauthorizedException("북마크를 삭제할 권한이 없습니다.");
    }

    private Bookmark getBookmarkById(Long bookmarkId) {
        return bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new NotFoundException("bookmarkId", bookmarkId.toString()));
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }
}
