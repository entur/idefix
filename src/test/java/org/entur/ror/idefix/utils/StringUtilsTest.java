package org.entur.ror.idefix.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void shouldExtractLookupKey() {
        assertThat(StringUtils.extractLookupKey("SE:014:Quay:9022014072021001")).isEqualTo("014:9022014072021001");
        assertThat(StringUtils.extractLookupKey("SE:050:Quay:9025050030510001")).isEqualTo("050:9025050030510001");
        assertThat(StringUtils.extractLookupKey("noColons")).isEqualTo("noColons");
        assertThat(StringUtils.extractLookupKey("one:colon")).isEqualTo("one:colon");
        assertThat(StringUtils.extractLookupKey("two:colons:only")).isEqualTo("two:colons:only");
    }
}