/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.forge.addon.utils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class JaxbNoNamespaceWriter implements XMLStreamWriter {

    private final XMLStreamWriter delegate;

    public JaxbNoNamespaceWriter(XMLStreamWriter delegate) {
        this.delegate = delegate;
    }

    private int elements;
    private String rootElementName;
    private String skipAttributes;

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        delegate.writeStartElement(localName);
        elements++;
        if (rootElementName == null) {
            rootElementName = localName;
        }
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        // we do not want to write namespaces
        delegate.writeStartElement("", localName);
        elements++;
        if (rootElementName == null) {
            rootElementName = localName;
        }
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        // we do not want to write namespaces
        delegate.writeStartElement("", localName, "");
        elements++;
        if (rootElementName == null) {
            rootElementName = localName;
        }
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        // we do not want to write namespaces
        delegate.writeEmptyElement("", localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        // we do not want to write namespaces
        delegate.writeEmptyElement("", localName, "");
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        delegate.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        delegate.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        delegate.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        delegate.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        delegate.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        if (skipAttributes != null && isSkipAttribute(localName)) {
            return;
        }

        delegate.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        if ("xsi".equals(prefix)) {
            // skip xsi namespace
        } else {
            if (skipAttributes != null && isSkipAttribute(localName)) {
                return;
            }

            // we do not want to write namespaces
            delegate.writeAttribute(prefix, "", localName, value);
        }
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        if (skipAttributes != null && isSkipAttribute(localName)) {
            return;
        }
        // we do not want to write namespaces
        delegate.writeAttribute("", localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        // we do not want to write namespaces
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        // we do not want to write namespaces
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        delegate.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        delegate.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        delegate.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        delegate.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        delegate.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        delegate.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        delegate.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        delegate.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        delegate.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        delegate.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        delegate.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return delegate.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        delegate.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        delegate.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        delegate.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return delegate.getProperty(name);
    }

    public String getSkipAttributes() {
        return skipAttributes;
    }

    /**
     * Sets a comma separated list of attribute names to skip writing.
     */
    public void setSkipAttributes(String skipAttributes) {
        this.skipAttributes = skipAttributes;
    }

    /**
     * Number of elements in the XML
     */
    public int getElements() {
        return elements;
    }

    /**
     * The root element name
     */
    public String getRootElementName() {
        return rootElementName;
    }

    private boolean isSkipAttribute(String localName) {
        for (String att : skipAttributes.split(",")) {
            if (localName.equals(att)) {
                return true;
            }
        }
        return false;
    }

}
