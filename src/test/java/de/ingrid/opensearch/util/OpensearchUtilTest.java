package de.ingrid.opensearch.util;

import junit.framework.TestCase;

public class OpensearchUtilTest extends TestCase {

	public void testXmlEscape() {
		String specialChars = "";
		for (int i=0; i<9; i++)
			specialChars += (char)i;
		specialChars += (char)0x0B;
		specialChars += (char)0x0C;
		for (int i=0x0E; i<0x1F; i++)
			specialChars += (char)i;
		specialChars += (char)0x7F;
		for (int i=0x80; i<0x9F; i++)
			specialChars += (char)i;
		
		String testStr = "hallo ungewollte zeichen: " + specialChars + " Raus hier!";
		String result = OpensearchUtil.xmlEscape(testStr);
		assertEquals("hallo ungewollte zeichen:  Raus hier!", result);
	}

}
