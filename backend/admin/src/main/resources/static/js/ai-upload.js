/**
 * AI Upload Page JavaScript
 * 파일 업로드 및 AI 처리 상태 폴링
 */
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('audio-file');
    const selectFileBtn = document.getElementById('select-file-btn');
    const filePreview = document.getElementById('file-preview');
    const fileName = document.getElementById('file-name');
    const fileSize = document.getElementById('file-size');
    const removeFileBtn = document.getElementById('remove-file-btn');
    const startProcessBtn = document.getElementById('start-process');

    const uploadSection = document.getElementById('upload-section');
    const progressSection = document.getElementById('progress-section');
    const progressBar = document.getElementById('progress-bar');
    const progressText = document.getElementById('progress-text');
    const processSteps = document.getElementById('process-steps');
    const errorMessage = document.getElementById('error-message');
    const errorText = document.getElementById('error-text');

    // CSRF Token
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // Constants
    const MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB
    const ALLOWED_TYPES = ['audio/mpeg', 'audio/mp3', 'audio/mp4', 'audio/aac', 'audio/x-m4a'];
    const ALLOWED_EXTENSIONS = ['.mp3', '.m4a', '.aac'];
    const POLLING_INTERVAL = 2000; // 2초

    let selectedFile = null;
    let pollingTimer = null;

    // ========== File Selection ==========

    // 파일 선택 버튼 클릭
    selectFileBtn.addEventListener('click', function() {
        fileInput.click();
    });

    // 드롭존 클릭
    dropZone.addEventListener('click', function(e) {
        if (e.target === dropZone || e.target.closest('.upload-area')) {
            if (e.target !== selectFileBtn && !selectFileBtn.contains(e.target)) {
                fileInput.click();
            }
        }
    });

    // 파일 선택 변경
    fileInput.addEventListener('change', function() {
        if (this.files.length > 0) {
            handleFileSelect(this.files[0]);
        }
    });

    // 드래그 앤 드롭
    dropZone.addEventListener('dragover', function(e) {
        e.preventDefault();
        dropZone.classList.add('dragover');
    });

    dropZone.addEventListener('dragleave', function(e) {
        e.preventDefault();
        dropZone.classList.remove('dragover');
    });

    dropZone.addEventListener('drop', function(e) {
        e.preventDefault();
        dropZone.classList.remove('dragover');

        if (e.dataTransfer.files.length > 0) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    });

    // 파일 선택 처리
    function handleFileSelect(file) {
        // 파일 타입 검증
        const fileName = file.name.toLowerCase();
        const hasValidExtension = ALLOWED_EXTENSIONS.some(ext => fileName.endsWith(ext));
        if (!ALLOWED_TYPES.includes(file.type) && !hasValidExtension) {
            alert('MP3, M4A, AAC 파일만 업로드 가능합니다.');
            return;
        }

        // 파일 크기 검증
        if (file.size > MAX_FILE_SIZE) {
            alert('파일 크기는 25MB를 초과할 수 없습니다.');
            return;
        }

        selectedFile = file;
        showFilePreview(file);
    }

    // 파일 미리보기 표시
    function showFilePreview(file) {
        fileName.textContent = file.name;
        fileSize.textContent = formatFileSize(file.size);
        filePreview.classList.remove('d-none');
        startProcessBtn.disabled = false;
    }

    // 파일 제거
    removeFileBtn.addEventListener('click', function() {
        selectedFile = null;
        fileInput.value = '';
        filePreview.classList.add('d-none');
        startProcessBtn.disabled = true;
    });

    // ========== AI Processing ==========

    // AI 처리 시작
    startProcessBtn.addEventListener('click', async function() {
        if (!selectedFile) {
            alert('파일을 선택해주세요.');
            return;
        }

        // UI 전환
        uploadSection.classList.add('d-none');
        progressSection.classList.remove('d-none');
        resetProgress();

        try {
            // 파일 업로드 및 처리 시작
            const processId = await uploadAndStartProcess();

            // 상태 폴링 시작
            startPolling(processId);

        } catch (error) {
            showError(error.message || 'AI 처리 시작에 실패했습니다.');
        }
    });

    // 파일 업로드 및 처리 시작
    async function uploadAndStartProcess() {
        updateStep('UPLOADING');

        const formData = new FormData();
        formData.append('audioFile', selectedFile);

        const response = await fetch('/admin/api/ai/process', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            },
            body: formData
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || '업로드 실패');
        }

        const result = await response.json();
        return result.processId;
    }

    // 상태 폴링
    function startPolling(processId) {
        pollingTimer = setInterval(async () => {
            try {
                const status = await fetchStatus(processId);
                updateProgress(status);

                if (status.status === 'COMPLETED') {
                    stopPolling();
                    // 결과 페이지로 이동
                    window.location.href = `/admin/ai-result/${processId}`;
                } else if (status.status === 'FAILED') {
                    stopPolling();
                    showError(status.errorMessage || 'AI 처리에 실패했습니다.');
                }
            } catch (error) {
                stopPolling();
                showError('상태 조회에 실패했습니다.');
            }
        }, POLLING_INTERVAL);
    }

    function stopPolling() {
        if (pollingTimer) {
            clearInterval(pollingTimer);
            pollingTimer = null;
        }
    }

    // 상태 조회
    async function fetchStatus(processId) {
        const response = await fetch(`/admin/api/ai/process/${processId}/status`, {
            headers: {
                [csrfHeader]: csrfToken
            }
        });

        if (!response.ok) {
            throw new Error('상태 조회 실패');
        }

        return await response.json();
    }

    // 진행 상황 업데이트
    function updateProgress(status) {
        // 프로그레스 바
        progressBar.style.width = `${status.progress}%`;
        progressText.textContent = `${status.progress}%`;

        // 단계 업데이트
        updateStep(status.status);
    }

    // 단계 상태 업데이트
    function updateStep(currentStatus) {
        const steps = processSteps.querySelectorAll('li');
        const statusOrder = ['UPLOADING', 'CONVERTING', 'TRANSCRIBING', 'CORRECTING', 'GENERATING_META', 'COMPLETED'];
        const currentIndex = statusOrder.indexOf(currentStatus);

        steps.forEach((step, index) => {
            const stepStatus = step.dataset.step;
            const stepIndex = statusOrder.indexOf(stepStatus);
            const icon = step.querySelector('.icon i');

            step.classList.remove('active', 'completed');

            if (stepIndex < currentIndex || currentStatus === 'COMPLETED') {
                // 완료된 단계
                step.classList.add('completed');
                icon.className = 'bi bi-check-circle-fill';
            } else if (stepIndex === currentIndex) {
                // 현재 진행 중인 단계
                step.classList.add('active');
                icon.className = 'bi bi-arrow-right-circle';
            } else {
                // 대기 중인 단계
                icon.className = 'bi bi-hourglass';
            }
        });
    }

    // 진행 상황 초기화
    function resetProgress() {
        progressBar.style.width = '0%';
        progressText.textContent = '0%';
        errorMessage.classList.add('d-none');

        const steps = processSteps.querySelectorAll('li');
        steps.forEach(step => {
            step.classList.remove('active', 'completed');
            const icon = step.querySelector('.icon i');
            icon.className = 'bi bi-hourglass';
        });
    }

    // 에러 표시
    function showError(message) {
        errorMessage.classList.remove('d-none');
        errorText.textContent = message;
        progressBar.classList.remove('progress-bar-animated', 'progress-bar-striped');
        progressBar.classList.add('bg-danger');
    }

    // ========== Utility ==========

    function formatFileSize(bytes) {
        if (bytes < 1024) {
            return bytes + ' B';
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(1) + ' KB';
        } else {
            return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
        }
    }
});
