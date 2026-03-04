package org.entur.ror.idefix.xml;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class XmlStreamUtilsTest {

    private static final XMLInputFactory INPUT_FACTORY = XMLInputFactory.newInstance();
    private static final XMLOutputFactory OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    @Test
    void copyStartElementPreservesNameAndAttributes() throws Exception {
        String xml = "<root attr1=\"val1\" attr2=\"val2\"/>";
        XMLStreamReader reader = INPUT_FACTORY.createXMLStreamReader(new StringReader(xml));
        reader.next();

        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        XmlStreamUtils.copyStartElement(reader, writer);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        String result = sw.toString();
        assertThat(result)
                .contains("root")
                .contains("attr1=\"val1\"")
                .contains("attr2=\"val2\"");
    }

    @Test
    void copyStartElementPreservesNamespace() throws Exception {
        String xml = "<ns:root xmlns:ns=\"http://example.com\"/>";
        XMLStreamReader reader = INPUT_FACTORY.createXMLStreamReader(new StringReader(xml));
        reader.next();

        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        XmlStreamUtils.copyStartElement(reader, writer);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        String result = sw.toString();
        assertThat(result)
                .contains("ns:root")
                .contains("http://example.com");
    }

    @Test
    void writeStartElementWithoutNamespace() throws Exception {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        XmlStreamUtils.writeStartElement(writer, null, "simple", null);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        assertThat(sw.toString()).contains("simple");
    }

    @Test
    void writeStartElementWithNamespace() throws Exception {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        XmlStreamUtils.writeStartElement(writer, "px", "elem", "http://ns.example.com");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        String result = sw.toString();
        assertThat(result).contains("px:elem");
    }

    @Test
    void writeNamespacesHandlesDefaultAndPrefixed() throws Exception {
        String xml = "<root xmlns=\"http://default.ns\" xmlns:p=\"http://prefixed.ns\"/>";
        XMLStreamReader reader = INPUT_FACTORY.createXMLStreamReader(new StringReader(xml));
        reader.next();

        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        XmlStreamUtils.writeStartElement(writer, reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
        XmlStreamUtils.writeNamespaces(reader, writer);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        String result = sw.toString();
        assertThat(result)
                .contains("http://default.ns")
                .contains("http://prefixed.ns");
    }

    @Test
    void writeAttributePlain() throws Exception {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        writer.writeStartElement("el");
        XmlStreamUtils.writeAttribute(writer, null, null, "key", "value");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        assertThat(sw.toString()).contains("key=\"value\"");
    }

    @Test
    void writeAttributeWithNamespaceAndPrefix() throws Exception {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        writer.writeStartElement("el");
        XmlStreamUtils.writeAttribute(writer, "px", "http://attr.ns", "key", "value");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        String result = sw.toString();
        assertThat(result).contains("px:key=\"value\"");
    }

    @Test
    void writeAttributeWithEmptyNamespace() throws Exception {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = OUTPUT_FACTORY.createXMLStreamWriter(sw);
        writer.writeStartDocument();
        writer.writeStartElement("el");
        XmlStreamUtils.writeAttribute(writer, "", "", "key", "value");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        assertThat(sw.toString()).contains("key=\"value\"");
    }
}
