package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    public void addBookmark(Long hearitId, Long memberId) {
        if(bookmarkRepository.existsByHearitIdAndMemberId(hearitId, memberId)) {
            throw new AlreadyExistException("이미 북마크가 존재합니다.");
        }
        Hearit hearit = findHearitById(hearitId);
        Member member = findMemberById(memberId);
        Bookmark bookmark = new Bookmark(member, hearit);
        bookmarkRepository.save(bookmark);
    }

    private Hearit findHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }
}
