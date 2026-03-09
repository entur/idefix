package org.entur.ror.idefix.xml;

import org.entur.ror.idefix.replacement.QuayRefReplacementResult;
import org.entur.ror.idefix.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.entur.ror.idefix.xml.XmlStreamUtils.copyStartElement;
import static org.entur.ror.idefix.xml.XmlStreamUtils.writeAttribute;
import static org.entur.ror.idefix.xml.XmlStreamUtils.writeNamespaces;
import static org.entur.ror.idefix.xml.XmlStreamUtils.writeStartElement;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class QuayRefReplacer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuayRefReplacer.class);
    private static final int PROGRESS_INTERVAL = 100_000;

    public QuayRefReplacementResult replaceQuayRefs(Path input, Path output, Map<String, String> lookupMap) {
        int matches = 0;
        int misses = 0;
        int elementsProcessed = 0;

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        try (InputStream is = new BufferedInputStream(Files.newInputStream(input));
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(output))) {

            XMLStreamReader reader = inputFactory.createXMLStreamReader(is, "UTF-8");
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(os, "UTF-8");

            while (reader.hasNext()) {
                int eventType = reader.next();

                switch (eventType) {
                    case XMLStreamConstants.START_DOCUMENT:
                        writer.writeStartDocument(reader.getEncoding(), reader.getVersion());
                        break;

                    case XMLStreamConstants.END_DOCUMENT:
                        writer.writeEndDocument();
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        elementsProcessed++;
                        if (elementsProcessed % PROGRESS_INTERVAL == 0) {
                            LOGGER.info("Replacement progress: {} elements processed, {} matches, {} misses so far",
                                    elementsProcessed, matches, misses);
                        }

                        String localName = reader.getLocalName();
                        String namespaceURI = reader.getNamespaceURI();
                        String prefix = reader.getPrefix();

                        if ("QuayRef".equals(localName)) {
                            String refValue = reader.getAttributeValue(null, "ref");
                            String replacement = null;
                            if (refValue != null) {
                                String lookupKey = StringUtils.extractLookupKey(refValue);
                                replacement = lookupMap.get(lookupKey);
                            }

                            if (replacement != null) {
                                writeStartElement(writer, prefix, localName, namespaceURI);
                                writeNamespaces(reader, writer);
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String attrLocal = reader.getAttributeLocalName(i);
                                    String attrNs = reader.getAttributeNamespace(i);
                                    String attrPrefix = reader.getAttributePrefix(i);
                                    String attrValue = "ref".equals(attrLocal) ? replacement : reader.getAttributeValue(i);
                                    writeAttribute(writer, attrPrefix, attrNs, attrLocal, attrValue);
                                }
                                matches++;
                            } else {
                                if (refValue != null) {
                                    LOGGER.debug("No mapping found for QuayRef: {}", refValue);
                                    misses++;
                                }
                                copyStartElement(reader, writer);
                            }
                        } else {
                            copyStartElement(reader, writer);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        writer.writeEndElement();
                        break;

                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.SPACE:
                        writer.writeCharacters(reader.getText());
                        break;

                    case XMLStreamConstants.CDATA:
                        writer.writeCData(reader.getText());
                        break;

                    case XMLStreamConstants.COMMENT:
                        writer.writeComment(reader.getText());
                        break;

                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
                        break;

                    case XMLStreamConstants.DTD:
                        writer.writeDTD(reader.getText());
                        break;

                    case XMLStreamConstants.ENTITY_REFERENCE:
                        writer.writeEntityRef(reader.getLocalName());
                        break;

                    default:
                        break;
                }
            }

            writer.flush();
            writer.close();
            reader.close();

        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to replace QuayRefs in XML", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read/write XML files", e);
        }

        LOGGER.info("Replacement complete: {} matches, {} misses ({} elements processed)", matches, misses, elementsProcessed);
        return new QuayRefReplacementResult(matches, misses);
    }

}
