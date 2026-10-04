package com.ji.afkcinematic.music;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class BalancedShuffleBag<T> {
    private final Random random;
    private final List<T> entries = new ArrayList<T>();
    private int index;
    private T lastReturned;

    public BalancedShuffleBag() {
        this(new Random());
    }

    BalancedShuffleBag(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    public void replace(Collection<? extends T> values) {
        this.entries.clear();
        for (T value : values) {
            if (value == null || this.entries.contains(value)) continue;
            this.entries.add(value);
        }
        this.reshuffle();
    }

    public T next() {
        if (this.entries.isEmpty()) {
            return null;
        }
        if (this.index >= this.entries.size()) {
            this.reshuffle();
        }
        T value = this.entries.get(this.index++);
        this.lastReturned = value;
        return value;
    }

    public boolean isCycleComplete() {
        return this.entries.isEmpty() || this.index >= this.entries.size();
    }

    private void reshuffle() {
        Collections.shuffle(this.entries, this.random);
        this.index = 0;
        if (this.entries.size() > 1 && Objects.equals(this.entries.get(0), this.lastReturned)) {
            Collections.swap(this.entries, 0, 1);
        }
    }
}
