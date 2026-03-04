package org.entur.ror.idefix.transform;

import org.entur.ror.idefix.file.FileService;
import org.entur.ror.idefix.xml.QuayRefTransformer;
import org.entur.ror.idefix.xml.RegistryParser;
import org.entur.ror.idefix.zip.ZipHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;

public class TransformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformService.class);

    public QuayRefTransformer.TransformResult run(FileService fileService) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("idefix-");
            LOGGER.info("Working directory: {}", tempDir);

            Path timetableZip = fileService.getTimetableZip(tempDir);
            Path registryZip = fileService.getRegistryZip(tempDir);

            ZipHandler zipHandler = new ZipHandler();
            RegistryParser registryParser = new RegistryParser();
            QuayRefTransformer transformer = new QuayRefTransformer();

            Path registryDir = tempDir.resolve("registry");
            zipHandler.extractZip(registryZip, registryDir);
            Map<String, String> lookupMap = registryParser.parse(registryDir);
            LOGGER.info("Loaded {} imported-id mappings from registry", lookupMap.size());

            Path timetableDir = tempDir.resolve("timetable");
            zipHandler.extractZip(timetableZip, timetableDir);
            Path sharedDataXml = findSharedDataXml(timetableDir);
            LOGGER.info("Found shared data file: {}", sharedDataXml);

            Path transformedFile = tempDir.resolve("transformed_shared_data.xml");
            QuayRefTransformer.TransformResult result = transformer.transform(sharedDataXml, transformedFile, lookupMap);
            LOGGER.info("Transform result: {} matches, {} misses", result.matches(), result.misses());

            Path outputZip = tempDir.resolve("output.zip");
            zipHandler.repackageZip(timetableZip, outputZip, Map.of("_shared_data.xml", transformedFile));

            fileService.publishOutput(outputZip);

            LOGGER.info("Idefix completed successfully. {} QuayRefs replaced, {} unresolved.",
                    result.matches(), result.misses());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Transform failed", e);
        } finally {
            if (tempDir != null) {
                cleanUp(tempDir);
            }
        }
    }

    private static Path findSharedDataXml(Path dir) throws IOException {
        return Files.walk(dir)
                .filter(p -> p.getFileName().toString().endsWith("_shared_data.xml"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No _shared_data.xml found in timetable"));
    }

    private static void cleanUp(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete temp file: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to clean up temp directory: {}", dir, e);
        }
    }
}
