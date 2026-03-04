package org.entur.ror.idefix.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public final class XmlStreamUtils {

    private XmlStreamUtils() {
    }

    public static void copyStartElement(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        writeStartElement(writer, reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
        writeNamespaces(reader, writer);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            writeAttribute(writer, reader.getAttributePrefix(i), reader.getAttributeNamespace(i),
                    reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
    }

    public static void writeStartElement(XMLStreamWriter writer, String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            writer.writeStartElement(prefix != null ? prefix : "", localName, namespaceURI);
        } else {
            writer.writeStartElement(localName);
        }
    }

    public static void writeNamespaces(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            String nsPrefix = reader.getNamespacePrefix(i);
            String nsURI = reader.getNamespaceURI(i);
            if (nsPrefix == null || nsPrefix.isEmpty()) {
                writer.writeDefaultNamespace(nsURI);
            } else {
                writer.writeNamespace(nsPrefix, nsURI);
            }
        }
    }

    public static void writeAttribute(XMLStreamWriter writer, String prefix, String namespace, String localName, String value)
            throws XMLStreamException {
        if (namespace != null && !namespace.isEmpty()) {
            writer.writeAttribute(prefix != null ? prefix : "", namespace, localName, value);
        } else {
            writer.writeAttribute(localName, value);
        }
    }
}
