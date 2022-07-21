package com.xenon.parsing;

import java.util.Arrays;
import java.util.Map;

/**
 * Util class for Strings regarding parsing
 * @author Zenon
 */
public class StringMisc {

    /**
     * Home-made version of Apache commons <code>StringEscapeUtils.unescapeJava()</code>.
     * @param in the string to be formatted
     * @return the correctly formatted version of <code>in</code>
     * @throws ParsingException if <code>in</code> is malformed
     */
    public static String unescapeJava(String in) throws ParsingException {
        char[] cs = in.toCharArray();
        int readIndex = 0, setIndex = 0;    // we work on the same array, 2 cursors are needed
        for (; setIndex < cs.length && readIndex < cs.length; setIndex++){
            char c = cs[readIndex];
            if (c == '\\'){
                try{
                    if (cs[readIndex + 1] == 'u') {
                        try {
                            int code = 0;
                            for (int j = 0; j < 4; j++) {
                                char c1 = cs[readIndex + j + 2];
                                int hex;
                                if ('0' <= c1 && c1 <= '9')
                                    hex = c1 - '0';
                                else if ('a' <= c1 && c1 <= 'f')
                                    hex = c1 - 'a' + 10;
                                else if ('A' <= c1 && c1 <= 'F')
                                    hex = c1 - 'A' + 10;
                                else
                                    throw ParsingException.of(cs, readIndex + j + 2, "Invalid unicode char: "
                                            + new String(cs, readIndex + 2,
                                            Math.min(cs.length - readIndex - 2, 4)));

                                for (int y = 0; y < 3 - j; y++) // Math.pow(16, 3 - j) but I don't trust doubles
                                    hex *= 16;
                                code += hex;
                            }
                            cs[setIndex] = (char) code;
                            readIndex += 5;
                        } catch (IndexOutOfBoundsException exception) {
                            throw ParsingException.from(exception, "Invalid unicode char: "
                                    + new String(cs, readIndex + 2,
                                    Math.min(cs.length - readIndex - 2, 4)));
                        }
                    } else {
                        if (isAffectedByBackslash(cs[readIndex + 1])) {
                            cs[setIndex] = controlCodes.get(cs[readIndex + 1]);
                            readIndex++;
                        } else
                            throw ParsingException.of(cs, readIndex + 1,
                                    "Unknown control code: " + cs[readIndex + 1]);
                    }
                }catch(IndexOutOfBoundsException exception){
                    throw ParsingException.from(exception, "Invalid unicode char");
                }
            }else
                cs[setIndex] = c;

            readIndex++;
        }

        return new String(cs, 0, setIndex);
    }





    /*__________________________ ESCAPED STRING METHODS _______________________________*/

    private static final char[] wrongPattern = {'\\'};

    /**
     * Finds the nearest pattern in an "escaped" java string. We manually work with backslashes.
     * Weakly checks if encountered control codes are correct.
     * @param lookup the char array we search in
     * @param startIndex the index to start searching from in <code>lookup</code>
     * @param pattern the escaped char array pattern to look for. anything but <code>{'\\'}</code> as it doesn't make sense
     * @return the index where the pattern was first found, -1 if nothing was found
     * @see #isAffectedByBackslash(char)
     * @see #findNearestMatchEscapedUnchecked(char[], int, char...)
     */
    public static int findNearestMatchEscaped(char[] lookup, int startIndex,
                                              @StringValue("(.{2,})|([^\\])") char... pattern)
            throws ParsingException {
        int p_len = pattern.length;
        int len = lookup.length;
        if (p_len == 0 || startIndex >= len)
            return -1;

        assert !Arrays.equals(wrongPattern, pattern) : "Wrong pattern used. '\\' alone doesn't make sense";

        boolean oneChar = p_len == 1;
        for (int i=startIndex; i < len; i++){
            char c = lookup[i];
            label_equal:
            if (c == pattern[0]){
                if (oneChar)   // wrong if and only if pattern == {'\\'}, but that case just doesn't make sense.
                    // either you search for a "\\" pattern in the lookup, thus you supply {'\\', '\\'} as pattern,
                    // or you're looking for a control code, such as "\t", thus you supply {'\\', 't'} as pattern.
                    // '\\' alone in an escaped string is a weird idea. (except in TOML with multiline strings)
                    return i;

                else if (len - i > p_len - 1){
                    for (int j=1; j < p_len; j++)
                        if (lookup[i + j] != pattern[j])
                            break label_equal;
                    return i;
                }
            }
            if (c == '\\'){ // 'if', not 'else if', very important!
                if (len - i > 1 && isAffectedByBackslash(lookup[i + 1]))
                    i++;
                else
                    throw ParsingException.of(lookup, i,
                            "Invalid control code: "+lookup[Math.min(len - 1, i + 1)]);
            }
        }

        return -1;
    }

    /**
     * Finds the nearest pattern in an "escaped" java string.
     * Same as {@link #findNearestMatchEscaped(char[], int, char...)} except that it checks if the result is -1,
     * and if it is, throws an ParsingException.
     * @param lookup the char array we search in
     * @param startIndex the index to start searching from in <code>lookup</code>
     * @param pattern the escaped char array pattern to look for. anything but <code>{'\\'}</code> as it doesn't make sense
     * @return the index where the pattern was first found
     * @throws ParsingException if nothing is found
     * @see #findNearestMatchEscaped(char[], int, char...)
     */
    public static int findNearestMatchEscapedStrong(char[] lookup, int startIndex, char... pattern)
            throws ParsingException {
        int i = findNearestMatchEscaped(lookup, startIndex, pattern);
        if (i == -1)
            throw ParsingException.of(lookup, startIndex,
                    "Couldn't find pattern: "+Arrays.toString(pattern)+"in: "+Arrays.toString(lookup)
                            +"starting from "+startIndex);
        return i;
    }

    /**
     * Finds the nearest pattern in an "escaped" java string. We manually work with backslashes.
     * Does not check encountered control codes' correctness,
     * whereas {@link #findNearestMatchEscaped(char[], int, char...)} does.
     * @param lookup the char array we search in
     * @param startIndex the index to start searching from in <code>lookup</code>
     * @param pattern the escaped char array pattern to look for. anything but <code>{'\\'}</code> as it doesn't make sense
     * @return the index where the pattern was first found, -1 if nothing was found
     * @see #findNearestMatchEscaped(char[], int, char...)
     */
    public static int findNearestMatchEscapedUnchecked(char[] lookup, int startIndex,
                                                       @StringValue("(.{2,})|([^\\])") char... pattern){
        int p_len = pattern.length;
        int len = lookup.length;
        if (p_len == 0 || startIndex >= len)
            return -1;

        assert !Arrays.equals(wrongPattern, pattern) : "Wrong pattern used. '\\' alone doesn't make sense";

        boolean oneChar = p_len == 1;
        for (int i=startIndex; i < len; i++){
            char c = lookup[i];
            label_equal:
            if (c == pattern[0]){
                if (oneChar)   // wrong if and only if pattern == {'\\'}, but that case just doesn't make sense.
                    // either you search for a "\\" pattern in the lookup, thus you supply {'\\', '\\'} as pattern,
                    // or you're looking for a control code, such as "\t", thus you supply {'\\', 't'} as pattern.
                    // '\\' alone in an escaped string is a weird idea. (except in TOML with multiline strings)
                    return i;

                else if (len - i > p_len - 1){
                    for (int j=1; j < p_len; j++)
                        if (lookup[i + j] != pattern[j])
                            break label_equal;
                    return i;
                }
            }
            if (c == '\\'){ // 'if', not 'else if', very important!
                if (len - i > 1 && isAffectedByBackslash(lookup[i + 1]))
                    i++;
            }
        }

        return -1;
    }





    /*__________________________ UNESCAPED STRING METHODS _______________________________*/

    /**
     * Finds the nearest pattern in a normal (unescaped) java string.
     * @param lookup the char array we search in
     * @param startIndex the index to start searching from in <code>lookup</code>
     * @param pattern the char array pattern to look for.
     * @return the index where the pattern was first found, -1 if nothing was found
     * @see #findNearestMatchStrong(char[], int, char...)
     */
    public static int findNearestMatch(char[] lookup, int startIndex, char... pattern){
        int p_len = pattern.length;
        int len = lookup.length;
        if (p_len == 0 || startIndex >= len)
            return -1;

        boolean oneChar = p_len == 1;
        for (int i=startIndex; i < len; i++){
            char c = lookup[i];
            label_equal:
            if (c == pattern[0]){
                if (oneChar)
                    return i;

                else if (len - i > p_len - 1){
                    for (int j=1; j < p_len; j++)
                        if (lookup[i + j] != pattern[j])
                            break label_equal;
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Finds the nearest pattern in a normal (unescaped) java string.
     * Same as {@link #findNearestMatch(char[], int, char...)} except that it checks if the result is -1,
     * and if it is, throws an ParsingException.
     * @param lookup the char array we search in
     * @param startIndex the index to start searching from in <code>lookup</code>
     * @param pattern the escaped char array pattern to look for
     * @return the index where the pattern was first found
     * @throws ParsingException if nothing is found
     * @see #findNearestMatch(char[], int, char...)
     */
    public static int findNearestMatchStrong(char[] lookup, int startIndex, char... pattern)
            throws ParsingException {
        int i = findNearestMatch(lookup, startIndex, pattern);
        if (i == -1)
            throw ParsingException.of(lookup, startIndex,
                    "Couldn't find pattern: "+Arrays.toString(pattern)+"in: "+Arrays.toString(lookup)
                            +"starting from "+startIndex);
        return i;
    }



    private static final Map<Character, Character> controlCodes = Map.of(
            '"', '"',
            '\'', '\'',
            '\\', '\\',
            't', '\t',
            'b', '\b',
            'n', '\n',
            'r', '\r',
            'f', '\f',
            'u', '\u0000'   // value should never be accessed
    );

    /**
     * Checks whether a character can be turned into a control code with a backslash before.
     * Checking is weak for unicode as it does not check if 'u' is correctly followed by 4 hex numbers.
     * @param c the character
     * @return whether backslash can impact it
     */
    public static boolean isAffectedByBackslash(char c){
        return controlCodes.containsKey(c);
    }


    /**
     * Equivalent to:
     * <pre><code>
     *     new String(c, start, end + 1 - start)
     * </code></pre>
     * @param c the chars
     * @param start the index at which the string will start
     * @param afterEnd the index right after the string will finish (<code>end + 1</code>)
     * @return the new String
     */
    public static String of(char[] c, int start, int afterEnd){
        return new String(c, start, afterEnd - start);
    }

    /**
     *
     * @param objects the array
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return the string from the objects concatenated
     */
    public static String concat(Object[] objects, int start, int end) {
        var b = new StringBuilder();
        for (int i = start; i < end; i++)
            b.append(objects[i]);
        return b.toString();
    }

}
