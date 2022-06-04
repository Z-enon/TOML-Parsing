package com.xenon.parsing;

import java.util.*;

public class TOMLTable extends TOMLObject {


    private final Map<String, TOMLObject> children = new HashMap<>();


    /**
     * Puts the key and value into {@link #children}, with a little work with dotted keys.
     * If <code>value</code> is a table and another table is already associated with <code>key</code>, it'll attempt
     * merging the two in <code>value</code>. So the parameter <code>value</code> will be the real instance of what's
     * associated with <code>key</code> after this call, and not the old one.
     * @param key the key to append to
     * @param value the value to put with the key.
     * @throws ParsingException if conflicts between tables and primitives occur
     */
    void handle(String key, TOMLObject value) throws ParsingException {
        String[] path = key.split("\\.");
        int end = path.length - 1;
        TOMLTable context = this;
        for (int i=0; i < end; i++) {
            String s = path[i];
            TOMLObject o = context.children.get(s);
            if (o == null) {
                var t = new TOMLTable();
                context.children.put(s, t);
                context = t;
            } else if (o instanceof TOMLTable)
                context = (TOMLTable) o;
            else throw ParsingException.because(context+" does not contain "+s+" as a table.");
        }
        TOMLObject old = context.children.get(path[end]);
        if (old == null)
            context.children.put(path[end], value);
        else if (value.isTable() && old.isTable()) {
            context.children.put(path[end], value);
            merge(old.getAsTable(), value.getAsTable());
        }
        else throw ParsingException.because(context+" already has a value for "+path[end]);

    }

    /**
     * Fills the new Table with the old, going around recursively if it encounters a Table value.
     * @param old the old table
     * @param new_ the new table to be filled
     * @throws ParsingException if conflicts occur
     */
    private static void merge(TOMLTable old, TOMLTable new_) throws ParsingException {
        Map<String, TOMLObject> n_map = new_.children;
        for (Map.Entry<String, TOMLObject> entry : old.children.entrySet()) {
            String old_key = entry.getKey();
            TOMLObject old_value = entry.getValue();
            TOMLObject conflict = n_map.get(old_key);
            if (conflict == null)
                n_map.put(old_key, old_value);
            else if (old_value.isTable() && conflict.isTable()) {
                n_map.put(old_key, conflict);
                merge(old_value.getAsTable(), conflict.getAsTable());
            } else throw ParsingException.because(n_map+" already has a value for "+old_key);
        }
    }

    @Override
    public String toString() {
        return children.toString();
    }
}
