package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.BookmarkHearitResponse;
import com.onair.hearit.app.dto.response.BookmarkInfoResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final PlayingHistoryRepository playingHistoryRepository;

    public PagedResponse<BookmarkHearitResponse> getBookmarkHearits(
            UserContext userContext,
            PagingRequest pagingRequest) {
        Member member = getMemberByUserContext(userContext);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByMemberOrderByRecent(member, pageable);
        Map<Long, Long> playTimeMap = getPlayTimeMap(member.getId(), bookmarks.getContent());
        Page<BookmarkHearitResponse> response = mapToResponsePage(bookmarks, playTimeMap);

        return PagedResponse.from(response);
    }

    private Map<Long, Long> getPlayTimeMap(Long memberId, List<Bookmark> bookmarks) {
        List<Long> hearitIds = bookmarks.stream()
                .map(bookmark -> bookmark.getHearit().getId())
                .toList();
        return playingHistoryRepository.findByMemberIdAndHearitIdIn(memberId, hearitIds)
                .stream()
                .collect(Collectors.toMap(
                        PlayingHistory::getHearitId,
                        PlayingHistory::getLastPlayTime
                ));
    }

    private Page<BookmarkHearitResponse> mapToResponsePage(Page<Bookmark> bookmarks, Map<Long, Long> playTimeMap) {
        return bookmarks.map(bookmark -> {
            Hearit hearit = bookmark.getHearit();
            Long lastPlayTime = playTimeMap.getOrDefault(hearit.getId(), null);
            return BookmarkHearitResponse.of(bookmark, hearit, lastPlayTime);
        });
    }

    @Transactional
    public BookmarkInfoResponse addBookmark(UserContext userContext, Long hearitId) {
        Member member = getMemberByUserContext(userContext);
        Hearit hearit = getHearitById(hearitId);
        if (bookmarkRepository.existsByHearitAndMember(hearit, member)) {
            throw new AlreadyExistException("이미 북마크된 히어릿입니다.");
        }
        Bookmark bookmark = new Bookmark(member, hearit);
        Bookmark saved = bookmarkRepository.save(bookmark);
        return BookmarkInfoResponse.from(saved);
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId, UserContext userContext) {
        Bookmark bookmark = getBookmarkById(bookmarkId);
        Member member = getMemberByUserContext(userContext);
        if (!bookmark.isCreatedBy(member)) {
            throw new UnauthorizedException("북마크를 삭제할 권한이 없습니다.");
        }
        bookmarkRepository.delete(bookmark);
    }

    private Member getMemberByUserContext(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            throw new UnauthorizedException("로그인한 회원이 아닙니다.");
        }
        return getMemberById(userContext.memberId());
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
