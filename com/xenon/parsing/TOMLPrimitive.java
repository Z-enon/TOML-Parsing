package com.xenon.parsing;

/**
 * Representation of TOML primitives such as numbers, booleans and strings.
 * @author Zenon
 */
public abstract class TOMLPrimitive extends TOMLObject {

    public abstract String asString();
    public abstract int asInt();
    public abstract long asLong();
    public abstract double asDouble();
    public abstract float asFloat();
    public abstract boolean asBoolean();

    public final boolean isString(){
        return this instanceof TOMLString;
    }
    public final boolean isInt(){
        return this instanceof TOMLInt;
    }
    public final boolean isLong(){
        return this instanceof TOMLLong;
    }
    public final boolean isDouble(){
        return this instanceof TOMLDouble;
    }
    public final boolean isFloat(){
        return this instanceof TOMLFloat;
    }
    public final boolean isBoolean(){
        return this instanceof TOMLBoolean;
    }

    @Override
    public final String toString() {
        return asString();
    }

    static class TOMLString extends TOMLPrimitive{
        private final String value;

        TOMLString(String s){
            value = s;
        }


        @Override
        public String asString() {
            return value;
        }

        @Override
        public int asInt() {
            return (int) asFloat();
        }

        @Override
        public long asLong() {
            return asInt();
        }

        @Override
        public double asDouble() {
            return asFloat();
        }

        @Override
        public float asFloat() {
            return Float.parseFloat(value);
        }

        @Override
        public boolean asBoolean() {
            return value.length() > 0;
        }
    }

    static class TOMLInt extends TOMLPrimitive {

        private final int value;

        TOMLInt(int i){
            value = i;
        }


        @Override
        public String asString() {
            return Integer.toString(value);
        }

        @Override
        public int asInt() {
            return value;
        }

        @Override
        public long asLong() {
            return value;
        }

        @Override
        public double asDouble() {
            return value;
        }

        @Override
        public float asFloat() {
            return value;
        }

        @Override
        public boolean asBoolean() {
            return value != 0;
        }
    }

    static class TOMLLong extends TOMLPrimitive {

        private final long value;

        TOMLLong(long l){
            value = l;
        }

        @Override
        public String asString() {
            return Long.toString(value);
        }

        @Override
        public int asInt() {
            return (int) value;
        }

        @Override
        public long asLong() {
            return value;
        }

        @Override
        public double asDouble() {
            return value;
        }

        @Override
        public float asFloat() {
            return value;
        }

        @Override
        public boolean asBoolean() {
            return value != 0;
        }
    }

    static class TOMLDouble extends TOMLPrimitive{

        private final double value;

        TOMLDouble(double d){
            value = d;
        }

        @Override
        public String asString() {
            return Double.toString(value);
        }

        @Override
        public int asInt() {
            return (int) value;
        }

        @Override
        public long asLong() {
            return (long) value;
        }

        @Override
        public double asDouble() {
            return value;
        }

        @Override
        public float asFloat() {
            return (float) value;
        }

        @Override
        public boolean asBoolean() {
            return value != 0;
        }
    }

    static class TOMLFloat extends TOMLPrimitive{

        private final float value;

        TOMLFloat(float f){
            value = f;
        }

        @Override
        public String asString() {
            return Float.toString(value);
        }

        @Override
        public int asInt() {
            return (int) value;
        }

        @Override
        public long asLong() {
            return (long) value;
        }

        @Override
        public double asDouble() {
            return value;
        }

        @Override
        public float asFloat() {
            return value;
        }

        @Override
        public boolean asBoolean() {
            return value != 0;
        }
    }

    static class TOMLBoolean extends TOMLPrimitive{

        private final boolean value;

        TOMLBoolean(boolean b){
            value = b;
        }

        @Override
        public String asString() {
            return Boolean.toString(value);
        }

        @Override
        public int asInt() {
            return value ? 1 : 0;
        }

        @Override
        public long asLong() {
            return asInt();
        }

        @Override
        public double asDouble() {
            return asInt();
        }

        @Override
        public float asFloat() {
            return asInt();
        }

        @Override
        public boolean asBoolean() {
            return value;
        }
    }
}
