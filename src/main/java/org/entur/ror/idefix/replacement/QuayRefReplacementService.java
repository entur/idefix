package org.entur.ror.idefix.replacement;

import org.entur.ror.idefix.file.FileService;
import org.entur.ror.idefix.utils.FileUtils;
import org.entur.ror.idefix.xml.QuayRefReplacer;
import org.entur.ror.idefix.xml.RegistryParser;
import org.entur.ror.idefix.zip.ZipHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuayRefReplacementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuayRefReplacementService.class);

    public Map<String, QuayRefReplacementResult> run(FileService fileService) {
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

            Map<String, QuayRefReplacementResult> results = new LinkedHashMap<>();

            for (String provider : providers) {
                LOGGER.info("--- Processing provider: {} ---", provider);
                try {
                    QuayRefReplacementResult result = replaceQuayRefsForProvider(
                            fileService, zipHandler, lookupMap, tempDir, provider);
                    results.put(provider, result);
                    LOGGER.info("Provider {}: {} matches, {} misses",
                            provider, result.matches(), result.misses());
                } catch (Exception e) {
                    LOGGER.error("Provider {} failed", provider, e);
                    throw new RuntimeException("Replacement failed for provider: " + provider, e);
                }
            }

            int totalMatches = results.values().stream().mapToInt(QuayRefReplacementResult::matches).sum();
            int totalMisses = results.values().stream().mapToInt(QuayRefReplacementResult::misses).sum();
            LOGGER.info("Idefix completed. {} provider(s) processed. Total: {} matches, {} misses.",
                    results.size(), totalMatches, totalMisses);

            return results;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Replacement failed", e);
        } finally {
            if (tempDir != null) {
                FileUtils.cleanUp(tempDir);
            }
        }
    }

    private QuayRefReplacementResult replaceQuayRefsForProvider(
            FileService fileService,
            ZipHandler zipHandler,
            Map<String, String> lookupMap,
            Path tempDir,
            String provider) throws Exception {

        QuayRefReplacer replacer = new QuayRefReplacer();

        Path providerDir = tempDir.resolve("provider-" + provider);
        Files.createDirectories(providerDir);

        Path timetableZip = fileService.getTimetableZip(providerDir, provider);

        Path timetableDir = providerDir.resolve("timetable");
        zipHandler.extractZip(timetableZip, timetableDir);
        Path sharedDataXml = FileUtils.findSharedDataXml(timetableDir);
        LOGGER.info("Found shared data file: {}", sharedDataXml);

        Path replacedFile = providerDir.resolve("replaced_shared_data.xml");
        QuayRefReplacementResult result = replacer.replaceQuayRefs(sharedDataXml, replacedFile, lookupMap);

        Path outputZip = providerDir.resolve("output.zip");
        zipHandler.repackageZip(timetableZip, outputZip, Map.of("_shared_data.xml", replacedFile));

        fileService.publishOutput(outputZip, provider);

        return result;
    }
}
