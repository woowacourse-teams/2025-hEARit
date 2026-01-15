/**
 * AI Result Page JavaScript
 * 결과 검토, 편집 및 Hearit 등록
 */
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const processId = document.getElementById('process-id').value;
    const rawTranscriptData = document.getElementById('raw-transcript').value;
    const correctedScriptData = document.getElementById('corrected-script').value;
    const finalScriptData = document.getElementById('final-script').value;

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

    // Modals
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmModal'));
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));

    // ========== Initialization ==========

    init();

    function init() {
        // Parse script data
        try {
            rawTranscript = JSON.parse(rawTranscriptData || '[]');
            correctedScript = JSON.parse(correctedScriptData || '[]');
            editableScript = JSON.parse(finalScriptData || '[]');

            // Clone corrected script for editing if editable is empty
            if (editableScript.length === 0) {
                editableScript = JSON.parse(JSON.stringify(correctedScript));
            }
        } catch (e) {
            console.error('Failed to parse script data:', e);
        }

        // Render script panels
        renderScriptPanel(rawScriptPanel, rawTranscript, false);
        renderScriptPanel(correctedScriptPanel, correctedScript, false);
        renderEditableScriptPanel();

        // Initialize keyword selection
        initKeywordSelection();
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
            container = div.querySelector('textarea');
            container.addEventListener('input', function() {
                editableScript[index].text = this.value;
                autoResize(this);
            });
            container.addEventListener('focus', function() {
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

    let sourceCount = 1;
    const MAX_SOURCES = 5;

    addSourceBtn.addEventListener('click', function() {
        if (sourceCount >= MAX_SOURCES) {
            alert('출처는 최대 5개까지 추가할 수 있습니다.');
            return;
        }

        sourceCount++;
        addSourceInput();
        updateSourceNumbers();
        updateRemoveButtons();
    });

    function addSourceInput() {
        const div = document.createElement('div');
        div.className = 'source-input-group';
        div.innerHTML = `
            <div class="input-group">
                <span class="input-group-text source-number">${sourceCount}</span>
                <input type="text" class="form-control source-input" placeholder="출처 URL 또는 제목"/>
                <button type="button" class="btn btn-outline-danger remove-source-btn">
                    <i class="bi bi-x"></i>
                </button>
            </div>
        `;

        const removeBtn = div.querySelector('.remove-source-btn');
        removeBtn.addEventListener('click', function() {
            div.remove();
            sourceCount--;
            updateSourceNumbers();
            updateRemoveButtons();
        });

        sourcesContainer.appendChild(div);
    }

    function updateSourceNumbers() {
        const numbers = sourcesContainer.querySelectorAll('.source-number');
        numbers.forEach((span, index) => {
            span.textContent = index + 1;
        });
    }

    function updateRemoveButtons() {
        const removeButtons = sourcesContainer.querySelectorAll('.remove-source-btn');
        removeButtons.forEach((btn, index) => {
            if (sourceCount === 1) {
                btn.classList.add('d-none');
            } else {
                btn.classList.remove('d-none');
            }
        });
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

            // 출처 수집
            const sources = [];
            sourcesContainer.querySelectorAll('.source-input').forEach(input => {
                if (input.value.trim()) {
                    sources.push(input.value.trim());
                }
            });

            // 최종 스크립트 텍스트 생성
            const finalScriptText = editableScript.map(s => s.text).join('\n\n');

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
                    finalScript: finalScriptText
                })
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || '등록 실패');
            }

            confirmModal.hide();
            alert('Hearit이 성공적으로 등록되었습니다!');

            // 목록 페이지로 이동
            window.location.href = '/admin/hearit';

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
