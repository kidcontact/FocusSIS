package com.slensky.focussis.data.domains;

public interface Domain<K, M> {

    void addMember(K key, M member);

}
