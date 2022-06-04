package com.xenon.parsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TOMLArray extends TOMLObject implements Iterable<TOMLObject>{

    private final List<TOMLObject> elements;

    TOMLArray(){
        elements = new ArrayList<>();
    }

    void add(TOMLObject o){
        elements.add(o);
    }

    public TOMLObject get(int index){
        return elements.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<TOMLObject> iterator() {
        return (Iterator<TOMLObject>) elements;
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
