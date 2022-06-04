package com.xenon.parsing;

/**
 * Generic exception for parsing errors.
 * @author Zenon
 */
@SuppressWarnings("unused")
public class ParsingException extends Exception{

    /**
     * Blank ParsingException
     */
    public ParsingException(){
        super();
    }

    /**
     * ParsingException with a message associated.
     * @param msg the message
     * @see #because(String)
     */
    private ParsingException(String msg){
        super(msg);
    }

    /**
     * ParsingException associated to a Throwable parent and a message.
     * @param parent the Throwable parent
     * @param msg the message
     * @see #from(Throwable, String)
     */
    private ParsingException(Throwable parent, String msg){
        super(msg, parent);
    }

    /**
     *
     * @param parent the Throwable parent
     * @return a new ParsingException instance associated to a Throwable parent
     */
    public static ParsingException from(Throwable parent){
        return from(parent, "");
    }

    /**
     *
     * @param parent the Throwable parent
     * @param msg the message
     * @return a new ParsingException instance associated to a Throwable parent and a message
     */
    public static ParsingException from(Throwable parent, String msg){
        return new ParsingException(parent, msg);
    }

    /**
     *
     * @param parent the Throwable parent
     * @param chars the chars that were being parsed
     * @param index the index a problem occurred
     * @param msg the message
     * @return a new ParsingException instance associated to a Throwable parent with a formatted message
     * @see #ParsingException(String)
     */
    public static ParsingException from(Throwable parent, char[] chars, int index, String msg){
        int startIndex = Math.max(0, index - 8);
        int endIndex = Math.min(chars.length, index + 8);
        return new ParsingException(
                parent,
                "Syntax error for character '" + (index < chars.length ? chars[index] : "out of bounds") + "' in: " +
                        new String(chars, startIndex, endIndex - startIndex)
                        + "; " + msg
        );
    }

    /**
     *
     * @param chars the chars that were being parsed
     * @param index the index a problem occurred
     * @param msg the message
     * @return a new ParsingException instance with a formatted message
     * @see #ParsingException(String)
     */
    public static ParsingException of(char[] chars, int index, String msg){
        int startIndex = Math.max(0, index - 8);
        int endIndex = Math.min(chars.length, index + 8);
        return new ParsingException(
                "Syntax error for character '" + (index < chars.length ? chars[index] : "out of bounds") + "' in: " +
                new String(chars, startIndex, endIndex - startIndex)
                + "; " + msg
        );
    }

    /**
     *
     * @param chars the chars that were being parsed
     * @param index the index a problem occurred
     * @return a new ParsingException instance with a formatted message
     * @see #of(char[], int, String)
     */
    public static ParsingException of(char[] chars, int index){
        return of(chars, index, "");
    }

    /**
     *
     * @param args the objects that were being parsed
     * @param index the index a problem occurred
     * @param msg the message
     * @return a new ParsingException instance with a formatted message
     * @see #ParsingException(String)
     */
    public static ParsingException of(Object[] args, int index, String msg){
        int startIndex = Math.max(0, index - 8);
        int endIndex = Math.min(args.length, index + 8);
        return new ParsingException(
                "Parsing error for object '" + (index < args.length ? args[index] : "out of bounds") + "' in: " +
                        StringMisc.concat(args, startIndex, endIndex)
                        + "; " + msg
        );
    }

    /**
     *
     * @param args the objects that were being parsed
     * @param index the index a problem occurred
     * @return a new ParsingException instance with a formatted message
     * @see #of(Object[], int, String)
     */
    public static ParsingException of(Object[] args, int index){
        return of(args, index, "");
    }


    public static ParsingException because(String msg){
        return new ParsingException(msg);
    }
}
