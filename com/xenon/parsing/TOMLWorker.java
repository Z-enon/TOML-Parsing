package com.xenon.parsing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.xenon.parsing.StringMisc.*;

/**
 * A few static methods to parse TOML text.
 * {@link Lexer} was made public to allow custom behaviour and instantiation to use with TOMLWorker's static methods.
 * Exhaustive list of all TOML features that are not and won't be implemented,
 * that I consider more a hindrance than anything:
 * <ul>
 *     <li>Unquoted keys with space inside: imo, this is really a hindrance for anybody who writes manually TOML files
 *     as leaving a blank space at the beginning can happen anytime and can be really tricky to catch
 *     if your lines are widely spread out.</li>
 *     <li>Similar thing with dotted keys: "blabla . hello" should be considered the same as "blabla.hello"
 *     according to TOML specifications. If only for unquoted keys, I'd have perhaps understood a tiny bit why
 *     (though, it'd have made the parser way more hellish to make), but to do the same to quoted keys,
 *     this is too much.</li>
 *     <li>Keys that are composed of both quoted and unquoted part: huh, so having 'site."google.com"' in your file
 *     should be fine? A bit too brainfuck-ish imo.</li>
 *     <li>Array of tables: the syntax sugar for arrays of tables is not supported
 *     as it'll raise the complexity of the parser by at least 200% for what little gain.</li>
 *     <li>Inline table const properties: According to the specs, when an inline table is associated to a key,
 *     it shouldn't be edited more somewhere else.</li>
 * </ul>
 *
 *
 * @author Zenon
 * @see <a href="https://toml.io/en/">TOML specs</a>
 */
public class TOMLWorker {

    /*------------------ LEARNING METHODS -------------------*/

    /*
    @Deprecated
    public static TOMLTable start(Path p) throws ParsingException {
        try {
            var map = new TOMLTable();
            parseMap(
                    lexer(Files.readAllLines(p).toArray(new String[0])).toArray(new Token[0]),
                    0,
                    Token.markOf('\n'),
                    true,
                    map
            );
            return map;
        } catch (IOException e) {
            throw ParsingException.from(e);
        }
    }

    /**
     *
     * @param lexemes the nodes array
     * @param start the index we start adding things in our array
     * @return the index at which the array ended as well as the resulting array
     * @throws ParsingException if malformations are encountered
     *//*
    @Deprecated
    private static AbstractMap.SimpleEntry<Integer, TOMLArray> handleArray(Token[] lexemes, int start)
            throws ParsingException {
        var result = new TOMLArray();
        boolean expectMark = false, success = false;
        int i = start;
        label:
        for (; i < lexemes.length; i++) {
            Token t = lexemes[i];
            if (t.isMark()) {
                switch(t.mark) {
                    case '[' -> {
                        if (expectMark)
                            throw ParsingException.because("Encountered 2 TOMLObjects without ',' between them in: "+
                                    concat(lexemes, start, i + 1));
                        expectMark = true;
                        AbstractMap.SimpleEntry<Integer, TOMLArray> pair = handleArray(lexemes, i + 1);
                        result.add(pair.getValue());
                        i = pair.getKey();
                    }
                    case '{' -> {
                        if (expectMark)
                            throw ParsingException.because("Encountered 2 TOMLObjects without ',' between them in: "+
                                    concat(lexemes, start, i + 1));
                        expectMark = true;
                        var map = new TOMLTable();
                        result.add(map);
                        i = parseMap(lexemes, i + 1, Token.markOf(','), false, map);
                    }
                    case ',' -> {
                        if (!expectMark)
                            throw ParsingException.because("Encountered ',' right next to '[' or another ',' in: "+
                                    concat(lexemes, start, i + 1));
                        expectMark = false;
                    }
                    case ']' -> {
                        if (!expectMark)
                            throw ParsingException.because("Array ended directly after a comma at: "+
                                    concat(lexemes, start, i + 1));
                        success = true;
                        break label;
                    }
                    case '\n' -> {}
                    default -> throw ParsingException.because("Encountered a non-valid mark: '"+t+'\'');
                }
            } else {
                if (expectMark)
                    throw ParsingException.because("Encountered 2 TOMLObjects without ',' between them in: "+
                            concat(lexemes, start, i + 1));
                result.add(primitiveFromLexeme(t));
                expectMark = true;
            }
        }

        if (!success)
            throw ParsingException.because("Array left unclosed at: "+concat(lexemes, start, lexemes.length));

        return new AbstractMap.SimpleEntry<>(i, result);
    }*/

    /**
     * Creates a new {@link TOMLPrimitive} from the parsed <code>value</code>.
     * @param token the node to be turned into a TOMLPrimitive
     * @return a new {@link TOMLPrimitive} instance wrapping <code>value</code>
     * @throws ParsingException in the occurrence of a {@link NumberFormatException}
     */
    private static TOMLPrimitive primitiveFromLexeme(Token token) throws ParsingException {
        assert !token.isMark() : "Cannot convert a mark into a TOMLPrimitive";
        if (token.type == Type.QUOTED)
            return new TOMLPrimitive.TOMLString(token.value);

        String v = token.value;

        v = v.replaceAll("_", "");
        try {
            boolean tru_e;
            if ((tru_e = v.equalsIgnoreCase("true")) || v.equalsIgnoreCase("false"))
                return new TOMLPrimitive.TOMLBoolean(tru_e);

            else if (v.contains(".")) {
                if (v.length() > 10)    // float digit threshold (actually more like 7)
                    return new TOMLPrimitive.TOMLDouble(Double.parseDouble(v));
                else
                    return new TOMLPrimitive.TOMLFloat(Float.parseFloat(v));
            } else {
                if (v.length() > 10)    // int digit threshold
                    return new TOMLPrimitive.TOMLLong(Long.parseLong(v));
                else
                    return new TOMLPrimitive.TOMLInt(Integer.parseInt(v));
            }
        } catch(NumberFormatException exception) {
            throw ParsingException.from(exception, "Malformed line for value: "+v);
        }
    }


    /*
    /**
     *
     * @param ts the tokens array
     * @param start the start index
     * @param delimiter the token that delimits each assignation
     * @param top_lvl whether '\n' should not be ignored
     * @param context the TOMLTable to put elements in
     * @return the index at which the map ended
     * @throws ParsingException in the occurrence of malformations
     *//*
    @Deprecated
    public static int parseMap(Token[] ts, int start, final Token delimiter, boolean top_lvl, TOMLTable context)
            throws ParsingException {

        int i = start, len = ts.length;
        String key = null;
        State state = State.KEY;
        label:
        for (; i < len; i++) {
            Token t = ts[i];
            switch(t.type) {
                case QUOTED, UNQUOTED -> {
                    switch(state) {
                        case KEY -> key = t.value;
                        case VALUE -> context.handle(key, primitiveFromLexeme(t));
                        default -> throw ParsingException.of(ts, i);
                    }
                }
                case MARK -> {
                    if (t.is('\n') && (!top_lvl || state == State.KEY))    // either not top_lvl or empty line case
                        continue label; // don't cycle state
                    switch(state) {
                        case EQUAL -> {
                            if (!t.is('='))
                                throw ParsingException.of(ts, i, "Expected '=', got '"+t+'\'');
                        }
                        case DELIMITER -> {
                            if (!top_lvl && t.is('}'))
                                return i;
                            else if (!t.is(delimiter))
                                throw ParsingException.of(ts, i, "Expected '"+delimiter+'\''+", got '"+t+'\'');
                        }
                        case KEY -> {
                            if (!top_lvl && t.is('}') && i == start)
                                return i;
                            throw ParsingException.of(ts, i, "Expected a key, got '"+t+'\'');
                        }
                        case VALUE -> {
                            TOMLObject value;
                            switch(t.mark) {
                                case '[' -> {
                                    AbstractMap.SimpleEntry<Integer, TOMLArray> result = handleArray(ts, i+1);
                                    i = result.getKey() + 1;
                                    value = result.getValue();
                                }
                                case '{' -> {
                                    var map = new TOMLTable();
                                    i = 1 + parseMap(ts, i + 1, Token.markOf(','), false, map);
                                    value = map;
                                }
                                default -> throw ParsingException.of(ts, i, "Expected a value for "+key+", got "+t);
                            }
                            context.handle(key, value);
                        }
                        default -> throw ParsingException.of(ts, i);
                    }
                }
            }
            state = state.cycle();
        }
        if (!top_lvl)
            throw ParsingException.of(ts, i, "Nested map must be closed");
        if (!(state == State.DELIMITER || state == State.KEY))
            throw ParsingException.of(ts, i, "Malformed end of file");
        return i;
    }



    /**
     * Tokenizer method to help with parsing.
     * @param lines the lines to be tokenized
     * @return a list of tokens
     * @throws ParsingException in case of string malformations
     *//*
    @Deprecated
    @SuppressWarnings("StatementWithEmptyBody")
    public static List<Token> lexer(String[] lines) throws ParsingException {
        List<Token> result = new ArrayList<>();
        boolean multiline = false;
        char[] multiline_pattern = null;
        var multilineContent = new StringBuilder();
        label_all_lines:
        for (var line : lines) {
            char[] c = line.toCharArray();
            int i = 0, len = c.length;
            if (multiline) {
                i = handleMultiLine(c, multiline_pattern, multilineContent, result, 0, true);
                multiline = i == -1;
                if (!multiline)
                    i = Math.min(len, i + 3);
                else continue;
            }
            int unquoteStart = 0;
            boolean unquoted = false;
            for (; i < len && Character.isWhitespace(c[i]); i++);
            label_line:
            for (; i < len; i++) {
                char ch = c[i];
                switch(ch) {
                    case '"', '\'' -> {
                        if (unquoted)
                            result.add(Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i))));
                        unquoted = false;
                        if (len - i > 2 && c[i + 1] == ch && c[i + 2] == ch) {  // three single/double quotes
                            multiline_pattern = new char[]{ch, ch, ch};
                            i += 3;
                            i = handleMultiLine(c, multiline_pattern, multilineContent, result, i, false);
                            multiline = i == -1;
                            if (!multiline)
                                i = Math.min(len, i + 2);
                            else continue label_all_lines;
                        } else {    // simple single/double quotes
                            int st = ++i;
                            String s;
                            if (ch == '"') {
                                i = findNearestMatchEscapedStrong(c, st, ch);
                                s = of(c, st, i);
                                s = unescapeJava(s);
                            } else {  // should not escape
                                i = findNearestMatchStrong(c, st, ch);
                                s = of(c, st, i);
                            }
                            result.add(Token.of(Type.QUOTED, s));
                        }
                    }
                    case '[', ']', '{', '}', ',', '=' -> {
                        if (unquoted)
                            result.add(Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i))));
                        unquoted = false;
                        result.add(Token.markOf(ch));
                    }
                    case '#' -> {
                        if (unquoted)
                            result.add(Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i))));
                        unquoted = false;
                        break label_line;
                    }
                    default -> {
                        if (Character.isWhitespace(ch)) {
                            if (unquoted)
                                result.add(Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i))));
                            unquoted = false;
                        } else {
                            if (!unquoted)
                                unquoteStart = i;
                            unquoted = true;
                        }

                        if (ch == '\\') {
                            if (!(len - i > 1 && isAffectedByBackslash(c[i + 1])))
                                throw ParsingException.of(c, i, "Invalid control code at line: "+line);
                            i++;
                        }
                    }
                }
            }
            if (unquoted)
                result.add(Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, len))));

            result.add(Token.markOf('\n'));
        }
        if (multiline)
            throw ParsingException.because("Multiline string left unclosed at the end of the file: "+lines[lines.length - 1]);
        return result;
    }

    /**
     * Parse a line that's part of a multiline string, and put the result in the container.
     * @param line the line
     * @param pattern the pattern that indicates the end of the multiline section
     * @param container the string builder to append in
     * @param lexemes the list we will append
     * @return where the multiline mode ended, -1 if it didn't, so we need to search at the next line
     * @see #lexer(String[])
     *//*
    @Deprecated
    @SuppressWarnings("StatementWithEmptyBody")
    private static int handleMultiLine(char[] line, char[] pattern, StringBuilder container, List<Token> lexemes,
                                       int startIndex, boolean shouldSkipFirstBlank)
            throws ParsingException {
        int i = startIndex, len = line.length;
        if (shouldSkipFirstBlank)
            for (; i < len && Character.isWhitespace(line[i]); i++);
        int tmp = i;
        i = findNearestMatchEscapedUnchecked(line, tmp, pattern);
        if (i == -1) {
            for (i = len - 1; i > tmp && Character.isWhitespace(line[i]); i--);
            int ed = i; // last non-space character
            if (line[i] == '\\') {
                int tmp2 = ed;
                for (i--; i > tmp && line[i] == '\\'; i--);
                tmp2 -= i;
                if ((tmp2 & 1) != 0)   // tmp2 % 2 != 0
                    container.append(of(line, tmp, ed));

            } else container.append(of(line, tmp, ed + 1)).append('\n');
        } else {
            container.append(of(line, tmp, i));
            int builder_end = container.length() - 1;
            if (container.charAt(builder_end) == '\n')
                container.deleteCharAt(builder_end);
            if (container.charAt(0) == '\n')
                container.deleteCharAt(0);
            lexemes.add(Token.of(Type.QUOTED, unescapeJava(container.toString())));
            container.setLength(0);
            return i;
        }
        return -1;
    }
    */

    /*----------------- REALISTIC METHODS ------------------*/

    public static TOMLTable parse(Path tomlFile) throws ParsingException {
        var map = new TOMLTable();
        Lexer lexer;
        try {
            lexer = Lexer.build(tomlFile);
        } catch (IOException e) {
            throw ParsingException.from(e);
        }
        parseMap(lexer, Token.markOf('\n'), true, map);
        return map;
    }


    private static void parseMap(Lexer supplier, Token delimiter, boolean top_lvl, TOMLTable context)
            throws ParsingException {
        State state = State.KEY;
        boolean declaring_table = false;    // special state for when the global context is switched, e.g. [a_table]
        TOMLTable top_table_back_up = context;
        String key = null, declared_table = null;
        Token t = supplier.next();
        Token backup_first_token = t;   // used to detect if there's a trailing comma
        label:
        for (; t != null; t = supplier.next()) {
            switch(t.type) {
                case QUOTED, UNQUOTED -> {
                    switch(state) {
                        case KEY -> key = t.value;
                        case VALUE -> {
                            if (declaring_table)
                                throw ParsingException.because("Expected ']' for table declaration, got "+t);
                            context.handle(key, primitiveFromLexeme(t));
                        }
                        case EQUAL -> {
                            if (!declaring_table)
                                throw ParsingException.because("Got text '"+t+"' when expecting a "+state);
                            declared_table = t.value;
                        }
                        default -> throw ParsingException.because("Got text '"+t+"' when expecting a "+state);
                    }
                }
                case MARK -> {
                    if (t.is('\n') && (!top_lvl || state == State.KEY))    // either not top_lvl or empty line case
                        continue label; // don't cycle state
                    switch(state) {
                        case EQUAL -> {
                            if (declaring_table)
                                throw ParsingException.because("Expected a table name, got '"+t+'\'');
                            if (!t.is('='))
                                throw ParsingException.because("Expected '=', got '"+t+'\'');
                        }
                        case DELIMITER -> {
                            if (!top_lvl && t.is('}'))
                                return;
                            if (!t.is(delimiter))
                                throw ParsingException.because("Expected '"+delimiter+'\''+", got '"+t+'\'');
                            if (declaring_table) {
                                var map = new TOMLTable();
                                top_table_back_up.handle(declared_table, map);
                                // context is forever changed until end of file or another table declaration
                                context = map;
                                declaring_table = false;
                            }
                        }
                        case KEY -> {
                            if (!top_lvl && t.is('}') && t != backup_first_token)
                                return;
                            if (top_lvl && t.is('['))
                                declaring_table = true;
                            else throw ParsingException.because("Expected a key, got '"+t+'\'');
                        }
                        case VALUE -> {
                            if (!declaring_table){
                                TOMLObject value;
                                switch(t.mark) {
                                    case '[' -> value = parseArray(supplier);
                                    case '{' -> {
                                        value = new TOMLTable();
                                        parseMap(supplier, Token.markOf(','), false, (TOMLTable) value);
                                    }
                                    default -> throw ParsingException.because("Expected a value for "+key+", got "+t);
                                }
                                context.handle(key, value);
                            } else if (!t.is(']'))
                                throw ParsingException.because("Expected a value for "+key+", got "+t);
                        }
                        default -> throw ParsingException.because("Got a mark when expecting a "+state);
                    }
                }
            }
            state = state.cycle();
        }
    }


    private static TOMLArray parseArray(Lexer supplier) throws ParsingException {
        var array = new TOMLArray();
        boolean expectMark = false, success = false;
        Token t = supplier.next();
        Token backup_first_token = t;   // used to detect if there's a trailing comma
        label:
        for (; t != null; t = supplier.next()) {
            switch (t.type) {
                case QUOTED, UNQUOTED -> {
                    if (expectMark)
                        throw ParsingException.because("Encountered 2 TOMLObjects without ',' between them in: "+t);
                    array.add(primitiveFromLexeme(t));
                    expectMark = true;
                }
                case MARK -> {
                    switch(t.mark) {
                        case '[' -> {
                            if (expectMark)
                                throw ParsingException.because("Encountered 2 TOMLObjects without ',' between them in: "
                                        +t);
                            expectMark = true;
                            array.add(parseArray(supplier));
                        }
                        case '{' -> {
                            if (expectMark)
                                throw ParsingException.because("Encountered 2 TOMLObjects without ',' between them in: "
                                        +t);
                            expectMark = true;
                            var map = new TOMLTable();
                            array.add(map);
                            parseMap(supplier, Token.markOf(','), false, map);
                        }
                        case ',' -> {
                            if (!expectMark)
                                throw ParsingException.because("Encountered ',' right next to '[' or another ',' in: "
                                        +t);
                            expectMark = false;
                        }
                        case ']' -> {
                            if (!expectMark && t != backup_first_token)
                                throw ParsingException.because("Array ended directly after a comma at: "+t);
                            success = true;
                            break label;
                        }
                        case '\n' -> {}
                        default -> throw ParsingException.because("Encountered a non-valid mark: '"+t+'\'');
                    }
                }
            }
        }
        if (!success)
            throw ParsingException.because("Array left unclosed at the end of the file.");
        return array;
    }


    /**
     * Tokenizer class for TOML parsing.
     * Use static methods from {@link TOMLWorker} with an instance of it, or directly use {@link #parse(Path)}.
     * @author Zenon
     */
    private static class Lexer {

        private boolean end_of_file;
        private int i;
        private char[] tempLine;
        private final Iterator<String> lines;

        /**
         * Builds a new TOML Lexer for the supplied TOML file.
         * @param tomlFile the file to be parsed
         * @return a new TOML Lexer
         * @throws IOException if reading <code>tomlFile</code> fails
         */
        public static Lexer build(Path tomlFile) throws IOException {
            return new Lexer(Files.lines(tomlFile).iterator());
        }
        private Lexer(Iterator<String> lineSupplier) {
            lines = lineSupplier;
            updateLine();
        }

        public Token next() throws ParsingException {
            char[] c = tempLine;
            if (c == null)
                return null;
            if(!end_of_file) {
                int len = c.length;
                int unquoteStart = 0;
                boolean unquote = false;
                labelLine:
                for (; i < len; i++) {
                    char ch = c[i];
                    switch(ch) {
                        case '"', '\'' -> {
                            if (unquote)
                                return Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i)));
                            if (len - i > 2 && c[i + 1] == ch && c[i + 2] == ch) {
                                i += 3;
                                return Token.of(Type.QUOTED, handleMultiline(new char[]{ch, ch, ch}));
                            }
                            int st = ++i;
                            String s;
                            if (ch == '"') {
                                i = findNearestMatchEscapedStrong(c, st, ch);
                                s = of(c, st, i);
                                s = unescapeJava(s);
                            } else {  // should not escape
                                i = findNearestMatchStrong(c, st, ch);
                                s = of(c, st, i);
                            }
                            ++i;
                            return Token.of(Type.QUOTED, s);
                        }
                        case '[', ']', '{', '}', ',', '=' -> {
                            if (unquote)
                                return Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i)));
                            ++i;
                            return Token.markOf(ch);
                        }
                        case '#' -> {
                            break labelLine;
                        }
                        default -> {
                            if (Character.isWhitespace(ch)) {
                                if (unquote)
                                    return Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i++)));
                            } else {
                                if (!unquote)
                                    unquoteStart = i;
                                unquote = true;
                            }

                            if (ch == '\\') {
                                if (!(len - i > 1 && isAffectedByBackslash(c[i + 1])))
                                    throw ParsingException.of(c, i, "Invalid control code at line: "
                                            +Arrays.toString(c));
                                i++;
                            }
                        }
                    }
                }
                if (unquote)
                    return Token.of(Type.UNQUOTED, unescapeJava(of(c, unquoteStart, i)));

                end_of_file = !updateLine();
                return Token.markOf('\n');
            }

            return null;
        }

        @SuppressWarnings("StatementWithEmptyBody")
        private String handleMultiline(char[] stop_pattern) throws ParsingException {
            var builder = new StringBuilder();
            do {
                char[] c = tempLine;
                int len = c.length;
                if (i == 0)
                    for (; i < len && Character.isWhitespace(c[i]); i++) ;
                int tmp = i;
                i = findNearestMatchEscapedUnchecked(c, tmp, stop_pattern);
                if (i == -1) {  // if i == -1, line will get updated and i will be set back to 0
                    for (i = len - 1; i > tmp && Character.isWhitespace(c[i]); i--);
                    int ed = i; // last non-space character
                    if (c[i] == '\\') {
                        int tmp2 = ed;
                        for (i--; i > tmp && c[i] == '\\'; i--);
                        tmp2 -= i;
                        if ((tmp2 & 1) != 0)   // tmp2 % 2 != 0
                            builder.append(of(c, tmp, ed));

                    } else builder.append(of(c, tmp, ed + 1)).append('\n');
                } else {
                    builder.append(of(c, tmp, i));
                    i += 3;
                    if (builder.charAt(0) == '\n')
                        builder.deleteCharAt(0);
                    int builder_end = builder.length() - 1;
                    if (builder.charAt(builder_end) == '\n')
                        builder.deleteCharAt(builder_end);
                    return unescapeJava(builder.toString());
                }
            } while(updateLine());
            throw ParsingException.because("Multiline string left unclosed at the end of the file: "+
                    tempLine[tempLine.length - 1]);
        }

        private boolean updateLine() {
            if (lines.hasNext()) {
                i = 0;
                tempLine = lines.next().toCharArray();
                return true;
            }
            return false;
        }

    }


    /**
     * Represents the possible states of the parser.
     */
    private enum State {
        KEY, EQUAL, VALUE, DELIMITER;

        private static final State[] VALUES = values();

        State cycle() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }
    }

    /**
     * Node class for the lexer
     */
    private record Token(Type type, String value, char mark) {

        /*
        * Constructor shortcuts
        * */
        static Token of(Type t, String value){
            return new Token(t, value, '\u0000');
        }
        static Token markOf(char c){
            return new Token(Type.MARK, null, c);
        }

        boolean is(Token other){
            if (other.type != type)
                return false;
            if (type == Type.MARK)
                return other.mark == mark;
            return value.equals(other.value);
        }
        boolean is(char c){
            assert isMark() : "Node must be a mark to contain";
            return mark == c;
        }

        boolean isMark(){
            return type == Type.MARK;
        }

        @Override
        public String toString() {
            return type + (isMark() ? String.valueOf(mark) : value);
        }
    }

    /**
     * Type of node for the lexer
     */
    private enum Type {
        MARK, QUOTED, UNQUOTED
    }
}
