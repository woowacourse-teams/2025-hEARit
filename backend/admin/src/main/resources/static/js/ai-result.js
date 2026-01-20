/**
 * AI Result Page JavaScript
 * 결과 검토, 편집 및 Hearit 등록
 */
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const processId = document.getElementById('process-id').value;

    // Script data from inline Thymeleaf script (window.scriptData)
    const scriptData = window.scriptData || {};

    const rawScriptPanel = document.getElementById('raw-script-panel');
    const correctedScriptPanel = document.getElementById('corrected-script-panel');
    const editScriptPanel = document.getElementById('edit-script-panel');

    const titleInput = document.getElementById('title-input');
    const summaryInput = document.getElementById('summary-input');
    const categorySelect = document.getElementById('category-select');
    const keywordContainer = document.getElementById('keyword-container');
    const selectedKeywordsInput = document.getElementById('selected-keywords');
    const sourcesContainer = document.getElementById('sources-container');
    const addSourceBtn = document.getElementById('add-source-btn');

    const saveScriptBtn = document.getElementById('save-script-btn');
    const resetScriptBtn = document.getElementById('reset-script-btn');
    const saveMetadataBtn = document.getElementById('save-metadata-btn');
    const confirmBtn = document.getElementById('confirm-btn');
    const cancelBtn = document.getElementById('cancel-btn');
    const modalConfirmBtn = document.getElementById('modal-confirm-btn');
    const modalDeleteBtn = document.getElementById('modal-delete-btn');

    // CSRF Token
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // State
    let rawTranscript = [];
    let correctedScript = [];
    let editableScript = [];
    let selectedKeywords = [];
    let sourceIndex = 0;
    const MAX_SOURCES = 5;

    // Modals
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmModal'));
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));

    // ========== Initialization ==========

    // 이벤트 리스너는 먼저 등록 (init 오류가 발생해도 동작하도록)
    bindEventListeners();

    // 초기화는 try-catch로 감싸서 오류가 발생해도 이벤트 리스너가 동작하도록
    try {
        init();
    } catch (error) {
        console.error('초기화 중 오류 발생:', error);
    }

    function init() {
        // Load script data from window.scriptData (set by Thymeleaf inline script)
        rawTranscript = scriptData.rawTranscript || [];
        correctedScript = scriptData.correctedScript || [];
        editableScript = scriptData.finalScript || [];

        console.log('Script data loaded:', {
            rawTranscript: rawTranscript.length,
            correctedScript: correctedScript.length,
            editableScript: editableScript.length
        });

        // Clone corrected script for editing if editable is empty
        if (editableScript.length === 0 && correctedScript.length > 0) {
            editableScript = JSON.parse(JSON.stringify(correctedScript));
        }

        // Render script panels
        renderScriptPanel(rawScriptPanel, rawTranscript, false);
        renderScriptPanel(correctedScriptPanel, correctedScript, false);
        renderEditableScriptPanel();

        // Initialize keyword selection
        initKeywordSelection();

        // Initialize source inputs
        initSourceInputs();

        // Initialize shorts audio duration display
        initShortsDuration();
    }

    function initShortsDuration() {
        const shortsAudio = document.getElementById('shorts-audio');
        const shortsDurationSpan = document.getElementById('shorts-duration');

        if (shortsAudio && shortsDurationSpan) {
            shortsAudio.addEventListener('loadedmetadata', function() {
                const duration = shortsAudio.duration;
                if (duration && isFinite(duration)) {
                    // 실제 오디오 파일의 duration 표시 (최대 60초)
                    const displayDuration = Math.min(duration, 60);
                    const minutes = Math.floor(displayDuration / 60);
                    const seconds = Math.floor(displayDuration % 60);
                    shortsDurationSpan.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;

                    // 로그: 메타데이터와 실제 duration 비교
                    if (duration > 61) {
                        console.warn('쇼츠 오디오 duration이 60초를 초과합니다:', duration);
                    }
                }
            });
        }
    }

    function bindEventListeners() {
        // 출처 추가 버튼
        if (addSourceBtn) {
            addSourceBtn.addEventListener('click', function() {
                const currentCount = sourcesContainer.querySelectorAll('.source-input-group').length;
                if (currentCount >= MAX_SOURCES) {
                    alert('출처는 최대 5개까지 추가할 수 있습니다.');
                    return;
                }
                addSourceRow();
            });
        }
    }

    // ========== Script Rendering ==========

    function renderScriptPanel(container, segments, editable) {
        container.innerHTML = '';

        if (!segments || segments.length === 0) {
            container.innerHTML = '<p class="text-muted">대본이 없습니다.</p>';
            return;
        }

        segments.forEach((segment, index) => {
            const div = document.createElement('div');
            div.className = 'script-segment';
            div.innerHTML = `
                <div class="timestamp">${formatTime(segment.start)} - ${formatTime(segment.end)}</div>
                <div class="text">${escapeHtml(segment.text)}</div>
            `;
            container.appendChild(div);
        });
    }

    function renderEditableScriptPanel() {
        editScriptPanel.innerHTML = '';

        if (!editableScript || editableScript.length === 0) {
            editScriptPanel.innerHTML = '<p class="text-muted">편집할 대본이 없습니다.</p>';
            return;
        }

        editableScript.forEach((segment, index) => {
            const div = document.createElement('div');
            div.className = 'script-segment editable-segment';
            div.innerHTML = `
                <div class="timestamp">${formatTime(segment.start)} - ${formatTime(segment.end)}</div>
                <textarea class="text" data-index="${index}" rows="2">${escapeHtml(segment.text)}</textarea>
            `;
            const textarea = div.querySelector('textarea');
            textarea.addEventListener('input', function() {
                editableScript[index].text = this.value;
                autoResize(this);
            });
            textarea.addEventListener('focus', function() {
                autoResize(this);
            });
            editScriptPanel.appendChild(div);
        });

        // Initial resize
        editScriptPanel.querySelectorAll('textarea').forEach(autoResize);
    }

    function autoResize(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = textarea.scrollHeight + 'px';
    }

    // ========== Script Actions ==========

    // 대본 저장
    saveScriptBtn.addEventListener('click', async function() {
        try {
            saveScriptBtn.disabled = true;
            saveScriptBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 저장 중...';

            const response = await fetch(`/admin/api/ai/results/${processId}/script`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({ segments: editableScript })
            });

            if (!response.ok) {
                throw new Error('저장 실패');
            }

            alert('대본이 저장되었습니다.');

        } catch (error) {
            alert('대본 저장에 실패했습니다: ' + error.message);
        } finally {
            saveScriptBtn.disabled = false;
            saveScriptBtn.innerHTML = '<i class="bi bi-save"></i> 대본 저장';
        }
    });

    // 대본 초기화 (AI 교정 결과로)
    resetScriptBtn.addEventListener('click', function() {
        if (confirm('편집한 내용을 모두 삭제하고 AI 교정 결과로 초기화하시겠습니까?')) {
            editableScript = JSON.parse(JSON.stringify(correctedScript));
            renderEditableScriptPanel();
        }
    });

    // ========== Metadata Actions ==========

    // 메타데이터 저장
    saveMetadataBtn.addEventListener('click', async function() {
        try {
            saveMetadataBtn.disabled = true;
            saveMetadataBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 저장 중...';

            const response = await fetch(`/admin/api/ai/results/${processId}/metadata`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    title: titleInput.value,
                    summary: summaryInput.value
                })
            });

            if (!response.ok) {
                throw new Error('저장 실패');
            }

            alert('메타데이터가 저장되었습니다.');

        } catch (error) {
            alert('메타데이터 저장에 실패했습니다: ' + error.message);
        } finally {
            saveMetadataBtn.disabled = false;
            saveMetadataBtn.innerHTML = '<i class="bi bi-save"></i> 메타데이터 저장';
        }
    });

    // ========== Keyword Selection ==========

    function initKeywordSelection() {
        const badges = keywordContainer.querySelectorAll('.keyword-badge');
        badges.forEach(badge => {
            badge.addEventListener('click', function() {
                const keywordId = parseInt(this.dataset.id);

                if (this.classList.contains('selected')) {
                    this.classList.remove('selected');
                    selectedKeywords = selectedKeywords.filter(id => id !== keywordId);
                } else {
                    this.classList.add('selected');
                    selectedKeywords.push(keywordId);
                }

                selectedKeywordsInput.value = selectedKeywords.join(',');
            });
        });
    }

    // ========== Source Management ==========

    function initSourceInputs() {
        // Add initial source input
        addSourceRow();
    }

    function addSourceRow() {
        const div = document.createElement('div');
        div.className = 'source-input-group mb-2';
        div.innerHTML = `
            <div class="input-group">
                <input type="text" class="form-control source-name" data-index="${sourceIndex}"
                       placeholder="출처 이름 (예: 테코톡)" required/>
                <input type="text" class="form-control source-url" data-index="${sourceIndex}"
                       placeholder="URL (예: https://...)"/>
                <button type="button" class="btn btn-outline-danger remove-source-btn">
                    <i class="bi bi-x"></i>
                </button>
            </div>
        `;

        const removeBtn = div.querySelector('.remove-source-btn');
        removeBtn.addEventListener('click', function() {
            div.remove();
            updateRemoveButtons();
        });

        sourcesContainer.appendChild(div);
        sourceIndex++;
        updateRemoveButtons();
    }

    function updateRemoveButtons() {
        const groups = sourcesContainer.querySelectorAll('.source-input-group');
        groups.forEach((group, index) => {
            const removeBtn = group.querySelector('.remove-source-btn');
            if (groups.length === 1) {
                removeBtn.classList.add('d-none');
            } else {
                removeBtn.classList.remove('d-none');
            }
        });
    }

    function collectSources() {
        const sources = [];
        sourcesContainer.querySelectorAll('.source-input-group').forEach(group => {
            const sourceName = group.querySelector('.source-name').value.trim();
            const sourceUrl = group.querySelector('.source-url').value.trim();
            if (sourceName) {
                sources.push({
                    sourceName: sourceName,
                    sourceUrl: sourceUrl || null
                });
            }
        });
        return sources;
    }

    // ========== Confirmation ==========

    confirmBtn.addEventListener('click', function() {
        // Validation
        if (!categorySelect.value) {
            alert('카테고리를 선택해주세요.');
            categorySelect.focus();
            return;
        }

        if (!titleInput.value.trim()) {
            alert('제목을 입력해주세요.');
            titleInput.focus();
            return;
        }

        // 모달에 정보 표시
        document.getElementById('confirm-title').textContent = titleInput.value;
        document.getElementById('confirm-category').textContent =
            categorySelect.options[categorySelect.selectedIndex].text;
        document.getElementById('confirm-keywords').textContent =
            selectedKeywords.length > 0
                ? getSelectedKeywordNames().join(', ')
                : '(없음)';

        confirmModal.show();
    });

    modalConfirmBtn.addEventListener('click', async function() {
        try {
            modalConfirmBtn.disabled = true;
            modalConfirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 등록 중...';

            // 출처 수집 (sourceName + sourceUrl 형식)
            const sources = collectSources();

            // 출처 검증 (최소 1개 필요)
            if (sources.length === 0) {
                alert('최소 1개의 출처를 입력해주세요.');
                modalConfirmBtn.disabled = false;
                modalConfirmBtn.innerHTML = '등록';
                return;
            }

            const response = await fetch(`/admin/api/ai/results/${processId}/confirm`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    categoryId: parseInt(categorySelect.value),
                    keywordIds: selectedKeywords,
                    sources: sources,
                    finalTitle: titleInput.value,
                    finalSummary: summaryInput.value,
                    finalScript: editableScript
                })
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || '등록 실패');
            }

            confirmModal.hide();
            alert('Hearit이 성공적으로 등록되었습니다!');

            // 목록 페이지로 이동
            window.location.href = '/admin';

        } catch (error) {
            alert('등록에 실패했습니다: ' + error.message);
        } finally {
            modalConfirmBtn.disabled = false;
            modalConfirmBtn.innerHTML = '등록';
        }
    });

    // ========== Deletion ==========

    cancelBtn.addEventListener('click', function() {
        deleteModal.show();
    });

    modalDeleteBtn.addEventListener('click', async function() {
        try {
            modalDeleteBtn.disabled = true;
            modalDeleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 삭제 중...';

            const response = await fetch(`/admin/api/ai/results/${processId}`, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });

            if (!response.ok) {
                throw new Error('삭제 실패');
            }

            deleteModal.hide();
            alert('삭제되었습니다.');

            // 업로드 페이지로 이동
            window.location.href = '/admin/ai-upload';

        } catch (error) {
            alert('삭제에 실패했습니다: ' + error.message);
        } finally {
            modalDeleteBtn.disabled = false;
            modalDeleteBtn.innerHTML = '삭제';
        }
    });

    // ========== Utility ==========

    function formatTime(ms) {
        if (ms === undefined || ms === null) return '0:00';
        const totalSeconds = Math.floor(ms / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function getSelectedKeywordNames() {
        const names = [];
        keywordContainer.querySelectorAll('.keyword-badge.selected').forEach(badge => {
            names.push(badge.textContent.trim());
        });
        return names;
    }
});
