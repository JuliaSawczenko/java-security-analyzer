package com.engineering.model;

import lombok.Getter;

@Getter
public class Finding {
    private final String ruleId;
    private final String filePath;
    private final int line;
    private final String message;

    /**
     * @param ruleId   unique ID of the rule that triggered this finding
     * @param filePath path to the source file where the issue was found
     * @param line     line number in the file (or -1 if unknown)
     * @param message  description of the issue and/or recommendation
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
