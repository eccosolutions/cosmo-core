/*
 * Copyright 2008 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import junit.framework.TestCase;

/**
 * Test StringPropertyUtils
 *
 */
public class StringPropertyUtilsTest {

    @Test
    public void testGetChildKeys() {
        String[] testKeys = {"a.b.c", "a.b.d", "a.b.d.foo", "a.e.f.g.h.i"};

        String[] childKeys = StringPropertyUtils.getChildKeys("a", testKeys);
        Assertions.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "b");
        verifyContains(childKeys, "e");

        childKeys = StringPropertyUtils.getChildKeys("a.", testKeys);
        Assertions.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "b");
        verifyContains(childKeys, "e");

        childKeys = StringPropertyUtils.getChildKeys("a.b", testKeys);
        Assertions.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "c");
        verifyContains(childKeys, "d");

        childKeys = StringPropertyUtils.getChildKeys("a.b.d", testKeys);
        Assertions.assertEquals(1, childKeys.length);
        verifyContains(childKeys, "foo");

        childKeys = StringPropertyUtils.getChildKeys("a.b.d.foo", testKeys);
        Assertions.assertEquals(0, childKeys.length);

        childKeys = StringPropertyUtils.getChildKeys("ldksf", testKeys);
        Assertions.assertEquals(0, childKeys.length);

    }

    @Test
    public void testGetChildProperties() {
        HashMap<String, String> testProps = new HashMap<>();
        testProps.put("a.b.c", "foo1");
        testProps.put("a.b.d", "foo2");
        testProps.put("a.b.e.f", "foo3");

        Map<String, String> childProps = StringPropertyUtils.getChildProperties("a.b", testProps);
        Assertions.assertEquals(2, childProps.size());
        Assertions.assertEquals("foo1", childProps.get("c"));
        Assertions.assertEquals("foo2", childProps.get("d"));

        childProps = StringPropertyUtils.getChildProperties("a.b.c", testProps);
        Assertions.assertEquals(0, childProps.size());

        childProps = StringPropertyUtils.getChildProperties("a", testProps);
        Assertions.assertEquals(0, childProps.size());

        childProps = StringPropertyUtils.getChildProperties("afsdfasd", testProps);
        Assertions.assertEquals(0, childProps.size());

    }

    private void verifyContains(String[] strs, String str) {
        for(String s: strs)
            if(s.equals(str))
                return;

        Assertions.fail("String " + str + " not found");
    }

}
