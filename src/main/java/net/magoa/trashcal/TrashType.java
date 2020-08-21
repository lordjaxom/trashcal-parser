package net.magoa.trashcal;

import java.util.Arrays;

public enum TrashType {

    GELBER_SACK("DSD", "Gelber Sack"),
    RESTMUELL("Restmüll"),
    PAPIER("Papiertonne");

    private final String value;
    private final String description;

    TrashType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    TrashType(String value) {
        this(value, value);
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TrashType findByValue(String value) {
        return Arrays.stream(values())
                .filter(it -> it.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown TrashType '" + value + "'"));
    }
}
