package com.xenon.parsing;

/**
 * Basic representation of TOML objects.
 * Can be primitives, arrays or tables as well.
 * @author Zenon
 */
public class TOMLObject {

    public TOMLPrimitive getAsPrimitive(){
        return (TOMLPrimitive) this;
    }

    public boolean isPrimitive(){
        return this instanceof TOMLPrimitive;
    }

    public TOMLTable getAsTable() {
        return (TOMLTable) this;
    }

    public boolean isTable() {
        return this instanceof TOMLTable;
    }

}
