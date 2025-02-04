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

import java.util.Random;

import org.junit.Assert;
import junit.framework.TestCase;

public class UriTemplateTest {
	private final static int MIN_CHARCODE = 0x21; //ASCII range
	private final static int MAX_CHARCODE = 0x7e; //ASCII range
	private final Random rnd = new Random();

    @Test
    public void testUnescapeSpaces() {
        Assertions.assertEquals("test test", UriTemplate.unescapeSegment("test+test"));
    }

    private String getPlaceHolder(int length) {
    	return getPlaceHolder(length, "/{}");
    }

    private String getPlaceHolder(int length, String delims) {
    	if (length <= 0)
    		length = 1;
    	StringBuilder result = new StringBuilder();
    	while (result.length() < length) {
    		char ch = (char)(rnd.nextInt(MAX_CHARCODE - MIN_CHARCODE) + MIN_CHARCODE);
    		if (delims.indexOf(ch) < 0 && Character.isJavaIdentifierPart(ch))
    			result.append(ch);
    	}
    	return result.toString();
    }

    @Test
    public void testBindAbsolute() {
    	String username = getPlaceHolder(10);
        Assertions.assertEquals("Error binding template: ", "/" + username + "/Inbox", new UriTemplate("/{username}/Inbox").bindAbsolute(false, "", username));
    }

}
