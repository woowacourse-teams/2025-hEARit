// ============================================================================
// 유틸 함수
// ============================================================================
// 출처 파싱
function extractSources(formData) {
    const sources = {};

    for (const [key, value] of formData.entries()) {
        const match = key.match(/^sources\[(\d+)\]\.(sourceName|sourceUrl)$/);
        if (!match) continue;

        const index = Number.parseInt(match[1], 10);
        const field = match[2];

        if (!sources[index]) {
            sources[index] = {};
        }

        sources[index][field] = value;
    }

    return Object.values(sources);
}

// 프로그래스 바 업데이트
function updateProgress(msg, percent) {
    const container = document.getElementById('progress-container');
    const bar = document.getElementById('progress-bar');
    const text = document.getElementById('progress-text');

    container.classList.remove('d-none');
    bar.style.width = percent + '%';
    text.textContent = msg;

    // 100% 완료시 애니메이션 제거
    if (percent >= 100) {
        bar.classList.remove('progress-bar-animated', 'progress-bar-striped');
        bar.classList.add('bg-success');
    }
}

function showCompleteButton() {
    const buttonContainer = document.getElementById('button-container');
    buttonContainer.innerHTML = `
        <button type="button" class="btn btn-primary" onclick="location.reload()">
            <i class="bi bi-plus-circle me-1"></i>더 등록하기
        </button>
    `;
}

function showButtons() {
    const buttonContainer = document.getElementById('button-container');
    buttonContainer.classList.remove('d-none');
    buttonContainer.innerHTML = `
        <button type="submit" class="btn btn-success" id="submit-btn">등록</button>
        <button type="button" class="btn btn-secondary" id="cancel-upload">취소</button>
    `;
    // 취소 버튼 이벤트 다시 연결
    document.getElementById('cancel-upload').addEventListener('click', () => window.app.reset());
}

function updateProgressError(msg) {
    const container = document.getElementById('progress-container');
    const bar = document.getElementById('progress-bar');
    const text = document.getElementById('progress-text');

    container.classList.remove('d-none');
    bar.style.width = '100%';

    bar.classList.remove('progress-bar-animated', 'progress-bar-striped', 'bg-success', 'bg-primary');
    bar.classList.add('bg-danger');

    text.textContent = msg;
}

function hideProgress() {
    const container = document.getElementById('progress-container');
    container.classList.add('d-none');
}

function hideButtons() {
    document.getElementById('button-container').classList.add('d-none');
}

// ============================================================================
// SourceManager - 출처 관리
// ============================================================================
class SourceManager {
    constructor(containerId, addButtonId) {
        this.container = document.getElementById(containerId);
        this.addButton = document.getElementById(addButtonId);
        this.sourceIndex = 0;
        this.init();
    }

    init() {
        this.addButton.addEventListener('click', () => this.addRow());
        this.addRow(); // 초기 한 줄 추가
    }

    createRow() {
        const row = document.createElement('div');
        row.className = 'input-group mb-2';
        row.innerHTML = `
                <input type="text" name="sources[${this.sourceIndex}].sourceName" class="form-control" placeholder="출처 이름 (예: 테코톡)" required>
                <input type="text" name="sources[${this.sourceIndex}].sourceUrl" class="form-control" placeholder="URL (예: https://...)" >
                <button type="button" class="btn btn-outline-danger remove-source-btn">삭제</button>
            `;
        row.querySelector('.remove-source-btn').addEventListener('click', function () {
            this.parentElement.remove();
        });
        return row;
    }

    addRow() {
        this.container.appendChild(this.createRow());
        this.sourceIndex++;
    }

    reset() {
        this.container.innerHTML = '';
        this.sourceIndex = 0;
        this.addRow();
    }
}

// ============================================================================
// CategoryManager - 카테고리 관리
// ============================================================================
class CategoryManager {
    constructor(selectId) {
        this.select = document.getElementById(selectId);
    }

    async fetch() {
        try {
            const response = await fetch('/api/v1/admin/categories/all');
            if (response.ok) {
                const categories = await response.json();
                this.render(categories);
            } else {
                console.error('카테고리를 불러오는 데 실패했습니다.');
            }
        } catch (error) {
            console.error('Error fetching categories:', error);
        }
    }

    render(categories) {
        this.select.innerHTML = '<option value="" disabled selected>카테고리를 선택하세요</option>';
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            this.select.appendChild(option);
        });
    }
}

// ============================================================================
// KeywordManager - 키워드 관리
// ============================================================================
class KeywordManager {
    constructor(containerId, searchInputId, paginationId, countId) {
        this.container = document.getElementById(containerId);
        this.searchInput = document.getElementById(searchInputId);
        this.paginationContainer = document.getElementById(paginationId);
        this.countElement = document.getElementById(countId);
        this.KEYWORDS_PER_PAGE = 50;
        this.allKeywords = [];
        this.filteredKeywords = [];
        this.currentPage = 1;
        this.selectedIds = new Set();
    }

    async fetch() {
        try {
            const response = await fetch('/api/v1/admin/keywords/all');
            if (response.ok) {
                const keywords = await response.json();
                this.allKeywords = this.sortKorean(keywords);
                this.filteredKeywords = [...this.allKeywords];
                this.currentPage = 1;
                this.render();
            } else {
                console.error('키워드를 불러오는 데 실패했습니다.');
            }
        } catch (error) {
            console.error('Error fetching keywords:', error);
        }
    }

    sortKorean(keywords) {
        return keywords.sort((a, b) => a.name.localeCompare(b.name, 'ko-KR'));
    }

    createCheckbox(keyword) {
        const isChecked = this.selectedIds.has(keyword.id);

        // 레이블 자체를 컨테이너로 사용하여 클릭 범위를 넓힘
        const label = document.createElement('label');
        label.className = `keyword-item ${isChecked ? 'selected' : ''}`;
        label.setAttribute('for', `keyword-${keyword.id}`);

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.className = 'form-check-input';
        checkbox.value = keyword.id;
        checkbox.id = `keyword-${keyword.id}`;
        checkbox.checked = isChecked;

        checkbox.addEventListener('change', (e) => {
            if (e.target.checked) {
                this.selectedIds.add(keyword.id);
                label.classList.add('selected');
            } else {
                this.selectedIds.delete(keyword.id);
                label.classList.remove('selected');
            }
            this.updateCount();
        });

        const text = document.createTextNode(keyword.name);

        label.appendChild(checkbox);
        label.appendChild(text);
        return label;
    }

    getPagedKeywords() {
        const start = (this.currentPage - 1) * this.KEYWORDS_PER_PAGE;
        const end = start + this.KEYWORDS_PER_PAGE;
        return this.filteredKeywords.slice(start, end);
    }

    getTotalPages() {
        return Math.ceil(this.filteredKeywords.length / this.KEYWORDS_PER_PAGE);
    }

    render() {
        this.container.innerHTML = '';
        const pagedKeywords = this.getPagedKeywords();

        if (pagedKeywords.length === 0) {
            this.container.innerHTML = '<p class="text-muted text-center py-3">검색 결과가 없습니다.</p>';
        } else {
            pagedKeywords.forEach(keyword => {
                this.container.appendChild(this.createCheckbox(keyword));
            });
        }

        this.renderPagination();
        this.updateCount();
    }

    renderPagination() {
        this.paginationContainer.innerHTML = '';
        const totalPages = this.getTotalPages();

        if (totalPages <= 1) return;

        const prevBtn = document.createElement('button');
        prevBtn.type = 'button';
        prevBtn.className = 'btn btn-outline-secondary';
        prevBtn.textContent = '‹';
        prevBtn.disabled = this.currentPage === 1;
        prevBtn.addEventListener('click', () => {
            if (this.currentPage > 1) {
                this.currentPage--;
                this.render();
            }
        });
        this.paginationContainer.appendChild(prevBtn);

        const pageInfo = document.createElement('button');
        pageInfo.type = 'button';
        pageInfo.className = 'btn btn-outline-secondary';
        pageInfo.textContent = `${this.currentPage} / ${totalPages}`;
        pageInfo.disabled = true;
        this.paginationContainer.appendChild(pageInfo);

        const nextBtn = document.createElement('button');
        nextBtn.type = 'button';
        nextBtn.className = 'btn btn-outline-secondary';
        nextBtn.textContent = '›';
        nextBtn.disabled = this.currentPage === totalPages;
        nextBtn.addEventListener('click', () => {
            if (this.currentPage < totalPages) {
                this.currentPage++;
                this.render();
            }
        });
        this.paginationContainer.appendChild(nextBtn);
    }

    handleSearch(searchTerm) {
        const term = searchTerm.toLowerCase().trim();
        this.filteredKeywords = term === ''
            ? [...this.allKeywords]
            : this.allKeywords.filter(k => k.name.toLowerCase().includes(term));
        this.currentPage = 1;
        this.render();
    }

    updateCount() {
        this.countElement.textContent = this.selectedIds.size;
    }

    reset() {
        this.selectedIds.clear();
        this.updateCount();
    }

    initSearch() {
        this.searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
    }

    getSelectedIds() {
        return Array.from(this.selectedIds);
    }
}

// ============================================================================
// FileUploadManager - 파일 업로드 관리
// ============================================================================
class FileUploadManager {
    constructor(inputId, previewId) {
        this.input = document.getElementById(inputId);
        this.preview = document.getElementById(previewId);
    }

    async extractDuration(file) {
        return new Promise((resolve, reject) => {
            const audio = new Audio();
            const objectUrl = URL.createObjectURL(file);

            audio.addEventListener('loadedmetadata', () => {
                const duration = Math.round(audio.duration);
                URL.revokeObjectURL(objectUrl);
                resolve(duration);
            });

            audio.addEventListener('error', () => {
                URL.revokeObjectURL(objectUrl);
                reject(new Error('오디오 파일을 읽을 수 없습니다.'));
            });

            audio.src = objectUrl;
        });
    }

    formatDuration(seconds) {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    }

    async handleChange(e) {
        const files = Array.from(e.target.files);
        this.preview.innerHTML = '';

        if (files.length === 0) {
            this.clearHiddenInputs();
            return;
        }

        const mp3Files = files.filter(f => f.type === 'audio/mpeg' || f.name.endsWith('.mp3'));
        const jsonFiles = files.filter(f => f.type === 'application/json' || f.name.endsWith('.json'));

        if (mp3Files.length !== 2 || jsonFiles.length !== 1) {
            alert('MP3 파일 2개와 JSON 파일 1개를 선택해주세요.');
            e.target.value = '';
            return;
        }

        try {
            const durations = await Promise.all(
                mp3Files.map(async (file) => ({
                    file,
                    duration: await this.extractDuration(file)
                }))
            );

            durations.sort((a, b) => b.duration - a.duration);
            const originalFile = durations[0];
            const shortFile = durations[1];
            const scriptFile = jsonFiles[0];

            this.setHiddenInputs(originalFile.file, shortFile.file, scriptFile, originalFile.duration);
            this.renderPreview(originalFile, shortFile, scriptFile);
        } catch (error) {
            console.error('파일 처리 오류:', error);
            alert('파일을 처리하는 중 오류가 발생했습니다.');
            e.target.value = '';
        }
    }

    setHiddenInputs(originalFile, shortFile, scriptFile, duration) {
        const dt1 = new DataTransfer();
        dt1.items.add(originalFile);
        document.getElementById('originalAudio').files = dt1.files;

        const dt2 = new DataTransfer();
        dt2.items.add(shortFile);
        document.getElementById('shortAudio').files = dt2.files;

        const dt3 = new DataTransfer();
        dt3.items.add(scriptFile);
        document.getElementById('scriptFile').files = dt3.files;

        document.getElementById('playTime').value = duration;
    }

    clearHiddenInputs() {
        document.getElementById('originalAudio').value = '';
        document.getElementById('shortAudio').value = '';
        document.getElementById('scriptFile').value = '';
        document.getElementById('playTime').value = '';
    }

    renderPreview(originalFile, shortFile, scriptFile) {
        this.preview.innerHTML = `
                <div class="alert alert-success py-2">
                    <div class="row g-2 align-items-center">
                        <div class="col-md-4">
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="text-truncate">
                                    <strong class="small">✓ 원본</strong>
                                    <div class="small text-muted text-truncate">${originalFile.file.name}</div>
                                </div>
                                <span class="badge bg-secondary ms-2">${this.formatDuration(originalFile.duration)}</span>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="text-truncate">
                                <strong class="small">✓ 요약 (1분)</strong>
                                <div class="small text-muted text-truncate">${shortFile.file.name}</div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="text-truncate">
                                <strong class="small">✓ 대본</strong>
                                <div class="small text-muted text-truncate">${scriptFile.name}</div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
    }

    init() {
        this.input.addEventListener('change', (e) => this.handleChange(e));
    }
}

// ============================================================================
// FormValidator - 폼 유효성 검사
// ============================================================================
class FormValidator {
    constructor(titleInputId, summaryInputId, titleCountId, summaryCountId, submitBtnId) {
        this.titleInput = document.getElementById(titleInputId);
        this.summaryInput = document.getElementById(summaryInputId);
        this.titleCount = document.getElementById(titleCountId);
        this.summaryCount = document.getElementById(summaryCountId);
        this.submitBtn = document.getElementById(submitBtnId);
        this.TITLE_MAX = 35;
        this.SUMMARY_MAX = 250;
    }

    updateCount(input, countElement, maxLength) {
        const length = input.value.length;
        countElement.textContent = `${length} / ${maxLength}`;

        if (length > maxLength) {
            countElement.classList.remove('text-muted');
            countElement.classList.add('text-danger', 'fw-bold');
            input.classList.add('is-invalid');
            return false;
        } else {
            countElement.classList.remove('text-danger', 'fw-bold');
            countElement.classList.add('text-muted');
            input.classList.remove('is-invalid');
            return true;
        }
    }

    validate() {
        const titleValid = this.updateCount(this.titleInput, this.titleCount, this.TITLE_MAX);
        const summaryValid = this.updateCount(this.summaryInput, this.summaryCount, this.SUMMARY_MAX);
        this.submitBtn.disabled = !(titleValid && summaryValid);
    }

    init() {
        this.titleInput.addEventListener('input', () => this.validate());
        this.summaryInput.addEventListener('input', () => this.validate());
    }
}

// ============================================================================
// FormSubmitHandler - 폼 제출 처리
// ============================================================================
class FormSubmitHandler {
    constructor(formId) {
        this.form = document.getElementById(formId);
    }

    prepareFormData() {
        const formData = new FormData(this.form);
        this.removeEmptySourceUrls(formData);

        const files = document.getElementById("audioFiles").files;

        let originalAudio = null;
        let shortAudio = null;
        let script = null;

        for (const file of files) {
            if (file.name.startsWith("ORG")) {
                originalAudio = file;
            } else if (file.name.startsWith("SHR")) {
                shortAudio = file;
            } else if (file.name.startsWith("SCR")) {
                script = file;
            }
        }

        if (!originalAudio || !shortAudio || !script) {
            console.error("ORG / SHR / SCR prefix 기준으로 필요한 파일을 찾지 못했습니다.");
            return null;
        }

        formData.set("originalAudio", originalAudio);
        formData.set("shortAudio", shortAudio);
        formData.set("script", script);

        return formData;
    }

    removeEmptySourceUrls(formData) {
        const keysToDelete = [];
        for (const [key, value] of formData.entries()) {
            if (key.endsWith('].sourceUrl') && typeof value === 'string' && value.trim() === '') {
                keysToDelete.push(key);
            }
        }
        keysToDelete.forEach(key => formData.delete(key));
    }

    getCsrfHeaders() {
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
        return {[csrfHeader]: csrfToken};
    }

    async submit(e) {
        e.preventDefault();

        hideButtons();
        updateProgress("Presigned URL 발급 중...", 10);

        const formData = this.prepareFormData();
        const headers = this.getCsrfHeaders();

        // 1. presigned URL 요청
        const originalAudio = formData.get("originalAudio");
        const shortAudio = formData.get("shortAudio");
        const script = formData.get("script");

        const presignedRequestBody = JSON.stringify({
            originalAudioFileName: originalAudio?.name,
            shortAudioFileName: shortAudio?.name,
            scriptFileName: script?.name
        });

        const presignedUrlResponse = await fetch('/api/v1/admin/storage/upload-urls', {
            method: 'POST',
            headers: {...headers, 'Content-Type': 'application/json'},
            body: presignedRequestBody
        });

        if (!presignedUrlResponse.ok) {
            updateProgressError("Presigned URL 발급 실패");
            await new Promise(r => setTimeout(r, 1200));
            alert('파일 업로드 URL 발급에 실패했습니다.');
            hideProgress();
            showButtons();
            return;
        }

        updateProgress("파일 업로드 중...", 40);
        const presigned = await presignedUrlResponse.json();

        // 2. 파일 업로드
        const uploadToS3 = (url, file) => fetch(url, {
            method: 'PUT',
            headers: {'Content-Type': file.type || 'application/octet-stream'},
            body: file
        });

        const [origRes, shortRes, scriptRes] = await Promise.all([
            uploadToS3(presigned.originalAudio.url, originalAudio),
            uploadToS3(presigned.shortAudio.url, shortAudio),
            uploadToS3(presigned.script.url, script)
        ]);

        if (!origRes.ok || !shortRes.ok || !scriptRes.ok) {
            updateProgressError("파일 업로드 실패");
            await new Promise(r => setTimeout(r, 1200));
            alert('파일 업로드 중 오류가 발생했습니다.');
            hideProgress();
            showButtons();
            return;
        }

        updateProgress("메타데이터 저장 중...", 70);

        // 메타데이터 JSON 생성
        formData.delete('originalAudio');
        formData.delete('shortAudio');
        formData.delete('script');

        const selectedKeywordIds = window.app.keywordManager.getSelectedIds();

        const json = {
            title: formData.get("title"),
            summary: formData.get("summary"),
            playTime: formData.get("playTime"),
            categoryId: formData.get("categoryId"),
            keywordIds: selectedKeywordIds,
            sources: extractSources(formData),
            originalAudioKey: presigned.originalAudio.key,
            shortAudioKey: presigned.shortAudio.key,
            scriptFileKey: presigned.script.key
        };

        try {
            const createResponse = await fetch('/api/v1/admin/hearits', {
                method: 'POST',
                headers: {...headers, 'Content-Type': 'application/json'},
                body: JSON.stringify(json)
            });

            if (!createResponse.ok) {
                updateProgressError("메타데이터 저장 실패");
                await new Promise(r => setTimeout(r, 1200));

                const responseText = await createResponse.text();
                let message = '서버 오류';


                const data = JSON.parse(responseText);
                const detail = data.detail || data.message;
                if (detail) {
                    message = detail
                        .split(';')
                        .map(m => m.trim())
                        .filter(m => m.length > 0)
                        .join('\n');
                }

                alert(message);
                hideProgress();
                showButtons();
                return;
            }

            updateProgress("등록 완료!", 100);

            setTimeout(() => {
                alert('히어릿이 성공적으로 추가되었습니다.');
                showCompleteButton();
                window.app.reset();
            }, 500);

        } catch (err) {
            updateProgressError("에러 발생");
            await new Promise(r => setTimeout(r, 1200));
            alert('추가 중 오류가 발생했습니다.' + err.message);
            hideProgress();
            showButtons();
        }
    }

    init() {
        this.form.addEventListener('submit', (e) => this.submit(e));
    }
}

// ============================================================================
// HearitApp - 메인 애플리케이션
// ============================================================================
class HearitApp {
    constructor() {
        this.sourceManager = new SourceManager('sources-container', 'add-source-btn');
        this.categoryManager = new CategoryManager('categoryId');
        this.keywordManager = new KeywordManager('keyword-checkboxes', 'keyword-search', 'keyword-pagination', 'selected-keyword-count');
        this.fileUploadManager = new FileUploadManager('audioFiles', 'file-preview');
        this.formValidator = new FormValidator('title', 'summary', 'title-count', 'summary-count', 'submit-btn');
        this.formSubmitHandler = new FormSubmitHandler('upload-form');
    }

    async init() {
        await this.categoryManager.fetch();
        await this.keywordManager.fetch();
        this.keywordManager.initSearch();
        this.fileUploadManager.init();
        this.formValidator.init();
        this.formSubmitHandler.init();
        this.initCancelButton();
    }

    initCancelButton() {
        document.getElementById('cancel-upload').addEventListener('click', () => this.reset());
    }

    reset() {
        document.getElementById('upload-form').reset();
        this.sourceManager.reset();
        this.keywordManager.reset();
    }
}

// ============================================================================
// 앱 초기화
// ============================================================================
window.app = new HearitApp();
document.addEventListener('DOMContentLoaded', () => window.app.init());
