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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.tika.io.TikaInputStream;

class SecureContentHandlerAITest {

    @Test
    void testEndElement() throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        DefaultHandler contentHandler = new DefaultHandler() {
            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                count.incrementAndGet();
            }
        };

        try (TikaInputStream stream =
                     TikaInputStream.get(new byte[100])) {
            SecureContentHandler handler =
                    new SecureContentHandler(contentHandler, stream);
            handler.startElement("", "test", "test",
                    new AttributesImpl());
            handler.endElement("", "test", "test");
            assertEquals(1, count.get());
        }
    }

    
    @Test
    void testIgnorableWhitespace() throws Exception {
        class SimpleHandler extends DefaultHandler {
            String text = "";

            @Override
            public void ignorableWhitespace(char[] ch, int start, int length) {
                text += new String(ch, start, length);
            }
        }

        SimpleHandler receiver = new SimpleHandler();

        try (TikaInputStream stream = TikaInputStream.get(new byte[10])) {
            SecureContentHandler handler =
                    new SecureContentHandler(receiver, stream);

            char[] text = {'a', 'b', 'c'};
            handler.ignorableWhitespace(text, 0, 3);

            assertEquals("abc", receiver.text);
        }
    }

}
