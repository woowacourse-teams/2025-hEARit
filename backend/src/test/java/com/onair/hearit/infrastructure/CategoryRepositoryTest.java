package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.fixture.DbHelper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("가장 오래된 순으로 지정된 개수만큼 카테고리를 조회한다.")
    void findOldest() {
        // given
        Category c1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category c2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category c3 = dbHelper.insertCategory(new Category("React", "#0000FF"));

        // when
        List<Category> result = categoryRepository.findOldest(2);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).getId()).isEqualTo(c1.getId()),
                () -> assertThat(result.get(1).getId()).isEqualTo(c2.getId())
        );
    }

}
