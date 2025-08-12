package com.onair.hearit.common.log.message;

import com.onair.hearit.domain.FileType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlMasker {

    public static final int SUFFIX_LEN = 10;
    public static final int PREFIX_LEN = 10;

    @Value("${amazon.s3.bucket}")
    private String baseBucketUrl;

    public String maskUrl(String url) {
        if (url == null) {
            return null;
        }

        String baseUrl = baseBucketUrl + "/";
        if (!url.startsWith(baseUrl)) {
            return "*****";
        }

        String suffix = url.substring(baseUrl.length());
        for (FileType fileType : FileType.values()) {
            if (suffix.startsWith(fileType.getUploadPath())) {
                String fileName = suffix.substring(fileType.getUploadPath().length());
                return "https://.../" + maskPrefixAndSuffix(fileName, PREFIX_LEN, SUFFIX_LEN);
            }
        }
        return "*****";
    }

    private String maskPrefixAndSuffix(String str, int prefixLen, int suffixLen) {
        return str.substring(0, prefixLen) +
                "*".repeat(str.length() - prefixLen - suffixLen) +
                str.substring(str.length() - suffixLen);
    }
}
