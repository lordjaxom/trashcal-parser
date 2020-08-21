package net.magoa.trashcal;

import java.time.LocalDate;

public class TrashEntry {

    private final LocalDate date;
    private final TrashType type;

    public TrashEntry(LocalDate date, TrashType type) {
        this.date = date;
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public TrashType getType() {
        return type;
    }
}
