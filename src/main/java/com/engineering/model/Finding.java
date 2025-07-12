package com.engineering.model;

import lombok.Getter;

/**
 * Reprezentuje pojedyncze zgłoszenie  wykryte przez regułę.
 */
@Getter
public class Finding {

    private final String ruleId;
    private final String filePath;
    private final int line;
    private final String message;

    /**
     * @param ruleId   unikalny identyfikator reguły, która zgłosiła problem
     * @param filePath ścieżka do pliku źródłowego, w którym wykryto problem
     * @param line     numer linii w pliku (lub -1, jeśli nieznany)
     * @param message  opis problemu i/lub rekomendacja naprawy
     */
    public Finding(String ruleId, String filePath, int line, String message) {
        this.ruleId = ruleId;
        this.filePath = filePath;
        this.line = line;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s:%d - %s", ruleId, filePath, line, message);
    }
}