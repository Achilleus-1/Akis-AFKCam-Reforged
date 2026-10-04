package com.ji.afkcinematic.cinematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ShotSequencePlanner {
    public static final int VISIBLE_SHOT_COUNT = 15;

    private ShotSequencePlanner() {
    }

    public static int[] plan(int characterPercentage, int characterPoolSize, int environmentPoolSize, long seed) {
        if (characterPoolSize < 15 || environmentPoolSize < 15) {
            throw new IllegalArgumentException("Both shot pools must contain at least fifteen presets");
        }
        int normalized = ShotSequencePlanner.normalizePercentage(characterPercentage);
        Random random = new Random(seed);
        float exactCharacterCount = (float)(15 * normalized) / 100.0f;
        int characterCount = (int)Math.floor(exactCharacterCount);
        if (ShotSequencePlanner.unitInterval(seed) < (double)(exactCharacterCount - (float)characterCount)) {
            ++characterCount;
        }
        int environmentCount = 15 - characterCount;
        List<Integer> characters = ShotSequencePlanner.indexes(0, characterPoolSize);
        List<Integer> environments = ShotSequencePlanner.indexes(characterPoolSize, environmentPoolSize);
        Collections.shuffle(characters, random);
        Collections.shuffle(environments, random);
        boolean[] characterSlots = new boolean[15];
        for (int i = 0; i < 15; ++i) {
            characterSlots[i] = (i + 1) * characterCount / 15 > i * characterCount / 15;
        }
        int rotation = random.nextInt(15);
        int[] result = new int[15];
        int nextCharacter = 0;
        int nextEnvironment = 0;
        for (int i = 0; i < 15; ++i) {
            boolean character = characterSlots[(i + rotation) % 15];
            result[i] = character ? characters.get(nextCharacter++) : environments.get(nextEnvironment++);
        }
        if (nextCharacter != characterCount || nextEnvironment != environmentCount) {
            throw new IllegalStateException("Shot mix planner produced an invalid category count");
        }
        return result;
    }

    public static int normalizePercentage(int value) {
        int clamped = Math.max(0, Math.min(100, value));
        return Math.round((float)clamped / 10.0f) * 10;
    }

    private static List<Integer> indexes(int offset, int size) {
        ArrayList<Integer> values = new ArrayList<Integer>(size);
        for (int i = 0; i < size; ++i) {
            values.add(offset + i);
        }
        return values;
    }

    private static double unitInterval(long seed) {
        long mixed = seed + -7046029254386353131L;
        mixed = (mixed ^ mixed >>> 30) * -4658895280553007687L;
        mixed = (mixed ^ mixed >>> 27) * -7723592293110705685L;
        mixed ^= mixed >>> 31;
        return (double)(mixed >>> 11) * (double)1.110223E-16f;
    }
}
