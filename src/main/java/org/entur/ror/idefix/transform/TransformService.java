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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformService.class);

    public Map<String, QuayRefTransformer.TransformResult> run(FileService fileService) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("idefix-");
            LOGGER.info("Working directory: {}", tempDir);

            ZipHandler zipHandler = new ZipHandler();
            RegistryParser registryParser = new RegistryParser();

            Path registryZip = fileService.getRegistryZip(tempDir);
            Path registryDir = tempDir.resolve("registry");
            zipHandler.extractZip(registryZip, registryDir);
            Map<String, String> lookupMap = registryParser.parse(registryDir);
            LOGGER.info("Loaded {} imported-id mappings from registry", lookupMap.size());

            List<String> providers = fileService.getProviders();
            LOGGER.info("Processing {} provider(s): {}", providers.size(), providers);

            Map<String, QuayRefTransformer.TransformResult> results = new LinkedHashMap<>();

            for (String provider : providers) {
                LOGGER.info("--- Processing provider: {} ---", provider);
                try {
                    QuayRefTransformer.TransformResult result = transformProvider(
                            fileService, zipHandler, lookupMap, tempDir, provider);
                    results.put(provider, result);
                    LOGGER.info("Provider {}: {} matches, {} misses",
                            provider, result.matches(), result.misses());
                } catch (Exception e) {
                    LOGGER.error("Provider {} failed", provider, e);
                    throw new RuntimeException("Transform failed for provider: " + provider, e);
                }
            }

            int totalMatches = results.values().stream().mapToInt(QuayRefTransformer.TransformResult::matches).sum();
            int totalMisses = results.values().stream().mapToInt(QuayRefTransformer.TransformResult::misses).sum();
            LOGGER.info("Idefix completed. {} provider(s) processed. Total: {} matches, {} misses.",
                    results.size(), totalMatches, totalMisses);

            return results;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Transform failed", e);
        } finally {
            if (tempDir != null) {
                cleanUp(tempDir);
            }
        }
    }

    private QuayRefTransformer.TransformResult transformProvider(
            FileService fileService,
            ZipHandler zipHandler,
            Map<String, String> lookupMap,
            Path tempDir,
            String provider) throws Exception {

        QuayRefTransformer transformer = new QuayRefTransformer();

        Path providerDir = tempDir.resolve("provider-" + provider);
        Files.createDirectories(providerDir);

        Path timetableZip = fileService.getTimetableZip(providerDir, provider);

        Path timetableDir = providerDir.resolve("timetable");
        zipHandler.extractZip(timetableZip, timetableDir);
        Path sharedDataXml = findSharedDataXml(timetableDir);
        LOGGER.info("Found shared data file: {}", sharedDataXml);

        Path transformedFile = providerDir.resolve("transformed_shared_data.xml");
        QuayRefTransformer.TransformResult result = transformer.transform(sharedDataXml, transformedFile, lookupMap);

        Path outputZip = providerDir.resolve("output.zip");
        zipHandler.repackageZip(timetableZip, outputZip, Map.of("_shared_data.xml", transformedFile));

        fileService.publishOutput(outputZip, provider);

        return result;
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
