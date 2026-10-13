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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CharsetUtilsManualTest {

    private Method originalIsSupportedICU;

    // Méthodes simulant les réponses d'ICU4J.
    public static boolean icuSupports(String charsetName) {
        return true;
    }

    public static boolean icuRejects(String charsetName) {
        return false;
    }

    public static boolean icuThrows(String charsetName) {
        throw new IllegalStateException("Erreur simulée d'ICU4J");
    }

    @BeforeEach
    public void saveOriginalMethod() throws Exception {
        Field field = CharsetUtils.class.getDeclaredField("isSupportedICU");
        field.setAccessible(true);
        originalIsSupportedICU = (Method) field.get(null);
    }

    @AfterEach
    public void restoreOriginalMethod() throws Exception {
        Field field = CharsetUtils.class.getDeclaredField("isSupportedICU");
        field.setAccessible(true);
        field.set(null, originalIsSupportedICU);
    }

    private void setICUMethod(String methodName) throws Exception {
        Field field = CharsetUtils.class.getDeclaredField("isSupportedICU");
        field.setAccessible(true);
        field.set(null,
                CharsetUtilsManualTest.class.getMethod(methodName, String.class));
    }

    @Test
    public void testICUSupportedCharset() throws Exception {
        setICUMethod("icuSupports");

        // Le JDK ne reconnaît pas cet encodage, mais ICU4J simulé l'accepte.
        assertTrue(CharsetUtils.isSupported("unknown-icu-charset"));
    }

    @Test
    public void testICUFallbackToJDK() throws Exception {
        setICUMethod("icuRejects");

        // ICU4J simulé refuse, mais le JDK reconnaît UTF-8.
        assertTrue(CharsetUtils.isSupported("UTF-8"));

        // Aucun des deux ne reconnaît cet encodage.
        assertFalse(CharsetUtils.isSupported("unknown-icu-charset"));
    }

    @Test
    public void testICUUnexpectedException() throws Exception {
        setICUMethod("icuThrows");

        // Une exception inattendue doit produire false.
        assertFalse(CharsetUtils.isSupported("UTF-8"));
    }
}
