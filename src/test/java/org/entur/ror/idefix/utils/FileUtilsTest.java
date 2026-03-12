package org.entur.ror.idefix.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFindSharedDataXml() throws Exception {
        Path nested = tempDir.resolve("sub/dir");
        Files.createDirectories(nested);
        Path expected = Files.createFile(nested.resolve("test_shared_data.xml"));

        Path result = FileUtils.findSharedDataXml(tempDir);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowWhenNoSharedDataXmlExists() {
        assertThatThrownBy(() -> FileUtils.findSharedDataXml(tempDir))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No _shared_data.xml found in timetable");
    }

    @Test
    void shouldCleanUpDirectoryAndContents() throws Exception {
        Path nested = tempDir.resolve("a/b");
        Files.createDirectories(nested);
        Files.createFile(nested.resolve("file.txt"));

        Path target = tempDir.resolve("a");
        FileUtils.cleanUp(target);

        assertThat(target).doesNotExist();
    }

    @Test
    void shouldNotThrowWhenCleaningUpNonExistentDirectory() {
        Path nonExistent = tempDir.resolve("does-not-exist");

        FileUtils.cleanUp(nonExistent);

        assertThat(nonExistent).doesNotExist();
    }
}
