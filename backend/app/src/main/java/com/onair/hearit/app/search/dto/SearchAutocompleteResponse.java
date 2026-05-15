package com.onair.hearit.app.search.dto;

import java.util.List;

public record SearchAutocompleteResponse(
        List<String> autocompletes
) {
}
