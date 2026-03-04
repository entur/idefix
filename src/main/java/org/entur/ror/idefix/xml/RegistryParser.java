package org.entur.ror.idefix.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegistryParser extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryParser.class);

    private final Map<String, String> importedIdMap = new HashMap<>();
    private String currentQuayId;
    private boolean importedIdKeyFound;
    private StringBuilder charBuffer;

    public Map<String, String> parse(Path registryDir) {
        File[] xmlFiles = registryDir.toFile().listFiles((dir, name) -> name.endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            LOGGER.warn("No XML files found in registry directory: {}", registryDir);
            return Collections.emptyMap();
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();

            for (File xmlFile : xmlFiles) {
                LOGGER.info("Parsing registry file: {}", xmlFile.getName());
                saxParser.parse(xmlFile, this);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse registry XML", e);
        }

        LOGGER.info("Registry parsing complete. Found {} imported-id mappings", importedIdMap.size());
        return importedIdMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if ("Quay".equals(localName)) {
            currentQuayId = attributes.getValue("id");
            importedIdKeyFound = false;
        } else if (currentQuayId != null && ("Key".equals(localName) || "Value".equals(localName))) {
            charBuffer = new StringBuilder();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (charBuffer != null) {
            charBuffer.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (currentQuayId == null) return;

        if ("Key".equals(localName)) {
            importedIdKeyFound = charBuffer != null && "imported-id".equals(charBuffer.toString().trim());
            charBuffer = null;
        } else if ("Value".equals(localName)) {
            if (importedIdKeyFound && charBuffer != null) {
                String[] entries = charBuffer.toString().trim().split(",");
                for (String entry : entries) {
                    String trimmed = entry.trim();
                    if (!trimmed.isEmpty()) {
                        importedIdMap.put(trimmed, currentQuayId);
                    }
                }
                importedIdKeyFound = false;
            }
            charBuffer = null;
        } else if ("Quay".equals(localName)) {
            currentQuayId = null;
            importedIdKeyFound = false;
        }
    }
}
