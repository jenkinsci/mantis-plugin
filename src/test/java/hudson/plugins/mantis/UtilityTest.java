package hudson.plugins.mantis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author sogabe
 *
 */
public class UtilityTest {

    /**
     * Test method for
     * {@link hudson.plugins.mantis.Utility#escapeRegExp(java.lang.String)}.
     */
    @Test
    public void testEscapeRegExp() {
        assertNull(Utility.escapeRegExp(null));
        assertEquals("\\\\", Utility.escapeRegExp("\\"));
        assertEquals("\\[\\]", Utility.escapeRegExp("[]"));
        assertEquals("\\{\\}", Utility.escapeRegExp("{}"));
        assertEquals("\\(\\)", Utility.escapeRegExp("()"));
        assertEquals("\\^\\,\\|\\&\\$", Utility.escapeRegExp("^,|&$"));
        assertEquals("\\+\\*\\,\\.", Utility.escapeRegExp("+*,."));
        assertEquals("ABCD", Utility.escapeRegExp("ABCD"));
        assertEquals("%ID%", Utility.escapeRegExp("%ID%"));
    }

}
