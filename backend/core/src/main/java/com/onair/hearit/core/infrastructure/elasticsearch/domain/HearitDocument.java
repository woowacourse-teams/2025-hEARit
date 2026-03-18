package com.onair.hearit.core.infrastructure.elasticsearch.domain;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Keyword;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "hearits", createIndex = true)
@Setting(settingPath = "elasticsearch/hearits-setting.json")
@Mapping(mappingPath = "elasticsearch/hearits-mapping.json")
public class HearitDocument {

    @Id
    @Field(name = "id", type = FieldType.Long)
    private Long id;

    @Field(name = "title", type = FieldType.Text)
    private String title;

    @Field(name = "summary", type = FieldType.Text)
    private String summary;

    @Field(name = "keywords", type = FieldType.Keyword)
    private List<String> keywords;

    @Field(name = "category", type = FieldType.Keyword)
    private String category;

    @Field(name = "created_at", type = FieldType.Date)
    private LocalDate createdAt;

    public static HearitDocument of(Hearit hearit, Category category, List<Keyword> keywords) {
        return new HearitDocument(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                keywords.stream().map(Keyword::getName).toList(),
                category.getName(),
                hearit.getCreatedAt().toLocalDate());
    }
}
