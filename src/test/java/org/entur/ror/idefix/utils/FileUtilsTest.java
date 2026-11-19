package org.entur.ror.idefix.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldListXmlFilesRecursively() throws Exception {
        Path nested = Files.createDirectories(tempDir.resolve("sub/dir"));
        Path xml = Files.createFile(nested.resolve("a.xml"));
        Files.createFile(nested.resolve("b.txt"));

        assertThat(FileUtils.listXmlFiles(tempDir)).containsExactly(xml);
    }

    @Test
    void shouldListSingleFile() throws Exception {
        Path xml = Files.createFile(tempDir.resolve("a.xml"));

        assertThat(FileUtils.listXmlFiles(xml)).containsExactly(xml);
    }

    @Test
    void shouldListSubdirectories() throws Exception {
        Path b = Files.createDirectories(tempDir.resolve("b"));
        Path a = Files.createDirectories(tempDir.resolve("a"));
        Files.createFile(tempDir.resolve("c.xml"));

        assertThat(FileUtils.listSubdirectories(tempDir)).containsExactly(a, b);
    }
}
