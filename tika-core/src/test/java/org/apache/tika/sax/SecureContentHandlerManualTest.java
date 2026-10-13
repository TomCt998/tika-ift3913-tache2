/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.sax;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;

class SecureContentHandlerManualTest {

    @Test
    void testOutputThreshold() throws Exception {
        try (TikaInputStream stream = TikaInputStream.get(new byte[10])) {
            SecureContentHandler handler =
                    new SecureContentHandler(new DefaultHandler(), stream);

            handler.setOutputThreshold(10);
            handler.setMaximumCompressionRatio(0);

            assertDoesNotThrow(() -> handler.characters(new char[10], 0, 10));
        }
    }

    @Test
    void testCompressionRatio() throws Exception {
        try (TikaInputStream stream = TikaInputStream.get(new byte[10])) {
            SecureContentHandler handler =
                    new SecureContentHandler(new DefaultHandler(), stream);

            handler.setOutputThreshold(0);
            handler.setMaximumCompressionRatio(2);

            assertDoesNotThrow(() -> handler.characters(new char[20], 0, 20));
        }
    }

    @Test
    void testStartElement() throws Exception {
        class SimpleHandler extends DefaultHandler {
            int count = 0;

            @Override
            public void startElement(String uri, String localName,
                    String name, org.xml.sax.Attributes attributes) {
                count++;
            }
        }

        SimpleHandler receiver = new SimpleHandler();

        try (TikaInputStream stream = TikaInputStream.get(new byte[10])) {
            SecureContentHandler handler = new SecureContentHandler(receiver, stream);

            handler.startElement("", "test", "test", new AttributesImpl());

            assertEquals(1, receiver.count);
        }
    }

    @Test
    void testDepth() throws Exception {
        try (TikaInputStream stream = TikaInputStream.get(new byte[10])) {
            SecureContentHandler handler =
                    new SecureContentHandler(new DefaultHandler(), stream);

            handler.setMaximumDepth(3);

            handler.startElement("", "a", "a", new AttributesImpl());
            handler.endElement("", "a", "a");

            handler.startElement("", "b", "b", new AttributesImpl());
            assertDoesNotThrow(() ->
                    handler.startElement("", "c", "c", new AttributesImpl()));
        }
    }

    @Test
    void testCharacters() throws Exception {
        class SimpleHandler extends DefaultHandler {
            String text = "";

            @Override
            public void characters(char[] ch, int start, int length) {
                text += new String(ch, start, length);
            }
        }

        SimpleHandler receiver = new SimpleHandler();

        try (TikaInputStream stream = TikaInputStream.get(new byte[10])) {
            SecureContentHandler handler = new SecureContentHandler(receiver, stream);

            handler.characters("hello".toCharArray(), 0, 5);

            assertEquals("hello", receiver.text);
        }
    }

    @Test
    void testOtherHandlerException() throws Exception {
        try (TikaInputStream stream = TikaInputStream.get(new byte[10])) {
            SecureContentHandler first = new SecureContentHandler(new DefaultHandler(), stream);
            SecureContentHandler second = new SecureContentHandler(new DefaultHandler(), stream);

            first.setMaximumDepth(1);
            SAXException exception = assertThrows(SAXException.class,() -> first.startElement("", "a", "a", new AttributesImpl()));
            assertDoesNotThrow(() -> second.throwIfCauseOf(exception));
            assertThrows(TikaException.class, () -> first.throwIfCauseOf(exception));
        }
    }
}
