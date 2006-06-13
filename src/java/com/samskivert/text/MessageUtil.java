//
// $Id: MessageUtil.java,v 1.3 2004/03/03 11:44:09 mdb Exp $

package com.samskivert.text;

import java.text.MessageFormat;

import com.samskivert.util.StringUtil;

/**
 * Utility functions for translation string handling.
 */
public class MessageUtil
{
    /** Text prefixed by this character will be considered tainted when
     * doing recursive translations and won't be translated. */
    public static final String TAINT_CHAR = "~";

    /** Used to mark fully qualified message keys. */
    public static final String QUAL_PREFIX = "%";

    /** Used to separate the bundle qualifier from the message key in a
     * fully qualified message key. */
    public static final String QUAL_SEP = ":";

    /**
     * Call this to "taint" any string that has been entered by an entity
     * outside the application so that the translation code knows not to
     * attempt to translate this string when doing recursive translations.
     */
    public static String taint (Object text)
    {
        return TAINT_CHAR + text;
    }

    /**
     * Composes a message key with an array of arguments. The message can
     * subsequently be decomposed and translated without prior knowledge
     * of how many arguments were provided.
     */
    public static String compose (String key, Object[] args)
    {
        StringBuilder buf = new StringBuilder();
        buf.append(key);
        buf.append('|');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                buf.append('|');
            }
            // escape the string while adding to the buffer
            String arg = (args[i] == null) ? "" : String.valueOf(args[i]);
            int alength = arg.length();
            for (int p = 0; p < alength; p++) {
                char ch = arg.charAt(p);
                if (ch == '|') {
                    buf.append("\\!");
                } else if (ch == '\\') {
                    buf.append("\\\\");
                } else {
                    buf.append(ch);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Used to escape single quotes so that they are not interpreted by
     * {@link MessageFormat}. As we assume all single quotes are to be
     * escaped, we cannot use the characters <code>{</code> and
     * <code>}</code> in our translation strings, but this is a small
     * price to pay to have to differentiate between messages that will
     * and won't eventually be parsed by a {@link MessageFormat} instance.
     */
    public static String escape (String message)
    {
        return StringUtil.replace(message, "'", "''");
    }

    /**
     * Unescapes characters that are escaped in a call to compose.
     */
    public static String unescape (String value)
    {
        int bsidx = value.indexOf('\\');
        if (bsidx == -1) {
            return value;
        }

        StringBuilder buf = new StringBuilder();
        int vlength = value.length();
        for (int i = 0; i < vlength; i++) {
            char ch = value.charAt(i);
            if (ch != '\\') {
                buf.append(ch);
            } else if (i < vlength-1) {
                // look at the next character
                ch = value.charAt(++i);
                buf.append((ch == '!') ? '|' : ch);
            } else {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with a single argument.
     */
    public static String compose (String key, Object arg)
    {
        return compose(key, new Object[] { arg });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with two arguments.
     */
    public static String compose (String key, Object arg1, Object arg2)
    {
        return compose(key, new Object[] { arg1, arg2 });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with three arguments.
     */
    public static String compose (
        String key, Object arg1, Object arg2, Object arg3)
    {
        return compose(key, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with a single argument that will be automatically tainted (see
     * {@link #taint}).
     */
    public static String tcompose (String key, Object arg)
    {
        return compose(key, new Object[] { taint(arg) });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with two arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (String key, Object arg1, Object arg2)
    {
        return compose(key, new Object[] { taint(arg1), taint(arg2) });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with three arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (
        String key, Object arg1, Object arg2, Object arg3)
    {
        return compose(key, new Object[] {
            taint(arg1), taint(arg2), taint(arg3) });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with an array of arguments that will be automatically tainted (see
     * {@link #taint}).
     */
    public static String tcompose (String key, Object[] args)
    {
        int acount = args.length;
        String[] targs = new String[acount];
        for (int ii = 0; ii < acount; ii++) {
            targs[ii] = taint(args[ii]);
        }
        return compose(key, targs);
    }

    /**
     * Decomposes a compound key into its constituent parts. Arguments
     * that were tainted during composition will remain tainted.
     */
    public static String[] decompose (String compoundKey)
    {
        String[] args = StringUtil.split(compoundKey, "|");
        for (int ii = 0; ii < args.length; ii++) {
            args[ii] = unescape(args[ii]);
        }
        return args;
    }

    /**
     * Returns a fully qualified message key which, when translated by
     * some other bundle, will know to resolve and utilize the supplied
     * bundle to translate this particular key.
     */
    public static String qualify (String bundle, String key)
    {
        // sanity check
        if (bundle.indexOf(QUAL_PREFIX) != -1 ||
            bundle.indexOf(QUAL_SEP) != -1) {
            String errmsg = "Message bundle may not contain '" + QUAL_PREFIX +
                "' or '" + QUAL_SEP + "' [bundle=" + bundle +
                ", key=" + key + "]";
            throw new IllegalArgumentException(errmsg);
        }
        return QUAL_PREFIX + bundle + QUAL_SEP + key;
    }

    /**
     * Returns the bundle name from a fully qualified message key.
     *
     * @see #qualify
     */
    public static String getBundle (String qualifiedKey)
    {
        if (!qualifiedKey.startsWith(QUAL_PREFIX)) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a fully qualified message key.");
        }

        int qsidx = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a valid fully qualified key.");
        }

        return qualifiedKey.substring(QUAL_PREFIX.length(), qsidx);
    }

    /**
     * Returns the unqualified portion of the key from a fully qualified
     * message key.
     *
     * @see #qualify
     */
    public static String getUnqualifiedKey (String qualifiedKey)
    {
        if (!qualifiedKey.startsWith(QUAL_PREFIX)) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a fully qualified message key.");
        }

        int qsidx = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a valid fully qualified key.");
        }

        return qualifiedKey.substring(qsidx+1);
    }
}
