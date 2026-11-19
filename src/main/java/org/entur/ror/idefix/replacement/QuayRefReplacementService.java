package org.entur.ror.idefix.replacement;

import org.entur.ror.idefix.utils.FileUtils;
import org.entur.ror.idefix.xml.QuayRefReplacer;
import org.entur.ror.idefix.xml.RegistryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuayRefReplacementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuayRefReplacementService.class);

    public Map<String, QuayRefReplacementResult> run(Path timetablesDir, Path registryXml, Path outputDir) {
        try {
            Files.createDirectories(outputDir);

            Map<String, String> lookupMap = new RegistryParser().parse(registryXml);
            LOGGER.info("Loaded {} imported-id mappings from registry", lookupMap.size());

            List<Path> providerDirs = FileUtils.listSubdirectories(timetablesDir).stream()
                    .filter(this::containsXml)
                    .toList();
            LOGGER.info("Processing {} provider(s): {}", providerDirs.size(),
                    providerDirs.stream().map(QuayRefReplacementService::providerName).toList());

            Map<String, QuayRefReplacementResult> results = new LinkedHashMap<>();
            for (Path providerDir : providerDirs) {
                String provider = providerName(providerDir);
                LOGGER.info("--- Processing provider: {} ---", provider);
                try {
                    QuayRefReplacementResult result = processProvider(providerDir, provider, lookupMap, outputDir);
                    results.put(provider, result);
                    LOGGER.info("Provider {}: {} matches, {} misses", provider, result.matches(), result.misses());
                } catch (Exception e) {
                    LOGGER.error("Provider {} failed", provider, e);
                }
            }

            int totalMatches = results.values().stream().mapToInt(QuayRefReplacementResult::matches).sum();
            int totalMisses = results.values().stream().mapToInt(QuayRefReplacementResult::misses).sum();
            LOGGER.info("Idefix completed. {} of {} provider(s) processed. Total: {} matches, {} misses.",
                    results.size(), providerDirs.size(), totalMatches, totalMisses);

            Files.copy(registryXml, outputDir.resolve("_stops.xml"), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Output written to {}", outputDir);

            return results;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Replacement failed", e);
        }
    }

    private QuayRefReplacementResult processProvider(
            Path providerDir, String provider, Map<String, String> lookupMap, Path outputDir) throws IOException {
        List<Path> xmlFiles = FileUtils.listXmlFiles(providerDir);
        Path sharedDataXml = xmlFiles.stream()
                .filter(p -> p.getFileName().toString().endsWith("_shared_data.xml"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No _shared_data.xml found in timetable"));
        LOGGER.info("Found shared data file: {}", sharedDataXml);

        // follow the pattern of the Norwegian timetable files
        QuayRefReplacementResult result = new QuayRefReplacer().replaceQuayRefs(
                sharedDataXml, outputDir.resolve("_" + provider + "_shared_data.xml"), lookupMap);

        for (Path xmlFile : xmlFiles) {
            String fileName = xmlFile.getFileName().toString();
            // skip the provider's stop place file, the common stops file from the registry is used instead
            if (xmlFile.equals(sharedDataXml) || fileName.endsWith("_stops.xml")) {
                continue;
            }
            Files.copy(xmlFile, outputDir.resolve(provider + "_" + fileName), StandardCopyOption.REPLACE_EXISTING);
        }
        return result;
    }

    private boolean containsXml(Path providerDir) {
        try {
            if (FileUtils.listXmlFiles(providerDir).isEmpty()) {
                LOGGER.warn("Skipping provider directory without XML files: {}", providerDir);
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list XML files in " + providerDir, e);
        }
    }

    private static String providerName(Path providerDir) {
        return providerDir.getFileName().toString();
    }
}
