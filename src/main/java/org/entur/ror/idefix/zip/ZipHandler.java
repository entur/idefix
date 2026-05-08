package org.entur.ror.idefix.zip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipHandler.class);

    public void extractZip(Path zipFile, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath);
                }
                zis.closeEntry();
            }
        }
    }

    public void repackageZip(Path originalZip, Path outputZip, Map<String, Path> replacements) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(originalZip));
             ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputZip))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                Path replacement = findReplacement(entryName, replacements);

                ZipEntry newEntry = new ZipEntry(entryName);
                zos.putNextEntry(newEntry);

                if (replacement != null) {
                    LOGGER.info("Replacing ZIP entry: {}", entryName);
                    try (InputStream replacementIs = Files.newInputStream(replacement)) {
                        replacementIs.transferTo(zos);
                    }
                } else {
                    zis.transferTo(zos);
                }

                zos.closeEntry();
                zis.closeEntry();
            }
        }
    }

    public void assembleAggregatedZip(Path stopsXml, Map<String, Path> providerZips, Path outputZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputZip))) {
            zos.putNextEntry(new ZipEntry("_stops.xml"));
            Files.copy(stopsXml, zos);
            zos.closeEntry();

            for (Map.Entry<String, Path> entry : providerZips.entrySet()) {
                String provider = entry.getKey();
                Path providerZip = entry.getValue();
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(providerZip))) {
                    ZipEntry zipEntry;
                    while ((zipEntry = zis.getNextEntry()) != null) {
                        String entryName = zipEntry.getName();
                        // skip providers stop place file, will use the common stops file
                        if (!zipEntry.isDirectory() && !entryName.endsWith("_stops.xml")) {
                            String fileName = Path.of(entryName).getFileName().toString();
                            zos.putNextEntry(new ZipEntry(provider + "_" + fileName));
                            zis.transferTo(zos);
                            zos.closeEntry();
                        }
                        zis.closeEntry();
                    }
                }
            }
        }
    }

    private Path findReplacement(String entryName, Map<String, Path> replacements) {
        for (Map.Entry<String, Path> replacement : replacements.entrySet()) {
            if (entryName.endsWith(replacement.getKey())) {
                return replacement.getValue();
            }
        }
        return null;
    }
}
