package com.engineering.report;

import com.engineering.model.Finding;
import java.nio.file.Path;
import java.util.List;

public interface ReportGenerator {
    /**
     * Generates a report for the given findings and writes it to the specified output path.
     *
     * @param findings the list of findings to include in the report
     * @param output   the path to write the report file (e.g., HTML)
     * @throws Exception if an error occurs while writing the report
     */
    void generate(List<Finding> findings, Path output) throws Exception;
}
