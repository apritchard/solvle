package com.appsoil.solvle.data;

import java.util.Map;
import java.util.Objects;

public record KnownPosition(Map<Integer, Character> pos) implements Comparable<KnownPosition>{

    @Override
    public String toString() {
        return String.format("Shares %d characters: %s", getShared(), pos.values());
    }

    public int getShared() {
        return pos.keySet().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnownPosition that = (KnownPosition) o;
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

    @Override
    public int compareTo(KnownPosition o) {
        if(getShared() == o.getShared()) {
            //largest word size supported = 9, just look for them all to avoid overhead
            for(int i = 0; i < 10; i++) {
                if(pos.containsKey(i) && !o.pos.containsKey(i)) {
                    return -1;
                } else if (!pos.containsKey(i) && o.pos.containsKey(i)) {
                    return 1;
                } else if(pos.containsKey(i) && o.pos.containsKey(i)){
                    int compPos = pos.get(i).compareTo(o.pos.get(i));
                    if(compPos != 0) {
                        return compPos;
                    }
                }
            }
            throw new IllegalStateException("comparing invalid KnownPositions");
        } else {
            return o.getShared() - getShared();
        }
    }
}
