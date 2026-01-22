package com.onair.hearit.admin.ai.infrastructure.stt;

public interface SttProvider {

    SttResponse transcribe(SttRequest request);

    String getProviderName();

    int getMaxFileSizeBytes();

    boolean isAvailable();
}
