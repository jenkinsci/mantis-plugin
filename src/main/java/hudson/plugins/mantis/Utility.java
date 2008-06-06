package hudson.plugins.mantis;

import hudson.Util;

/**
 * Utility class.
 * 
 * @author Seiji Sogabe
 */
public final class Utility {

    private Utility() {
        //
    }

    public static String escape(final String str) {
        if (str == null) {
            return null;
        }

        final int len = str.length();
        final StringBuffer buf = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            final char c = str.charAt(i);

            switch (c) {
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '&':
                    if ((i < len - 1) && (str.charAt(i + 1) == '#')) {
                        buf.append(c);
                    } else {
                        buf.append("&amp;");
                    }
                    break;
                case '"':
                    buf.append("&quot;");
                    break;
                case '\'':
                    buf.append("&#039;");
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }

        return buf.toString();
    }

    public static String join(final Long[] longs, final String separator) {
        boolean first = true;
        final StringBuffer sb = new StringBuffer();
        for (final Long l : longs) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(String.valueOf(l));
        }
        return sb.toString();
    }

    public static Long[] tokenize(final String str, final String delimiter) {
        if (str == null || delimiter == null) {
            return new Long[0];
        }
        final String[] s = Util.tokenize(str, delimiter);
        final Long[] l = new Long[s.length];
        for (int i = 0; i < s.length; i++) {
            l[i] = Long.valueOf(s[i]);
        }
        return l;
    }

}
