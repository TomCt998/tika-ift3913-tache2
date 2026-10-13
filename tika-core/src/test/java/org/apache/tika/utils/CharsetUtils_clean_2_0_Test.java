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
package org.apache.tika.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CharsetUtils_clean_2_0_Test {

    @Test
    public void testCleanValidCharset() throws Exception {
        String charsetName = "UTF-8";
        String expected = "UTF-8";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanInvalidCharset() throws Exception {
        String charsetName = "INVALID";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithWhitespace() throws Exception {
        String charsetName = "  UTF-8  ";
        String expected = "UTF-8";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithTrailingComma() throws Exception {
        String charsetName = "UTF-8,";
        String expected = "UTF-8";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithLeadingComma() throws Exception {
        String charsetName = ",UTF-8";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithQuotes() throws Exception {
        String charsetName = "\"UTF-8\"";
        String expected = "UTF-8";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithISOName() throws Exception {
        String charsetName = "ISO 8859-1";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithCPName() throws Exception {
        String charsetName = "CP1252";
        String expected = "windows-1252";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithWinName() throws Exception {
        String charsetName = "WIN1252";
        String expected = "windows-1252";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithNone() throws Exception {
        String charsetName = "none";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithNo() throws Exception {
        String charsetName = "no";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithNull() throws Exception {
        String charsetName = null;
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithEmptyString() throws Exception {
        String charsetName = "";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithWhitespaceAndTrailingComma() throws Exception {
        String charsetName = "  UTF-8,";
        String expected = "UTF-8";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithWhitespaceAndLeadingComma() throws Exception {
        String charsetName = ",  UTF-8";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithWhitespaceAndQuotes() throws Exception {
        String charsetName = "  \"UTF-8\"  ";
        String expected = "UTF-8";
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }

    @Test
    public void testCleanWithWhitespaceAndISOName() throws Exception {
        String charsetName = "  ISO 8859-1  ";
        String expected = null;
        String result = CharsetUtils.clean(charsetName);
        assertEquals(expected, result);
    }
}
