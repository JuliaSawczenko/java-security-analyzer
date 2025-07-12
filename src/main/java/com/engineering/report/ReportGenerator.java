package com.engineering.report;

import com.engineering.model.Finding;
import java.nio.file.Path;
import java.util.List;

/**
 * Generuje raport na podstawie zebranych problemów i zapisuje go do wskazanej ścieżki.
 */
public interface ReportGenerator {

    /**
     * Tworzy raport dla przekazanej listy problemów i zapisuje go do pliku.
     *
     * @param findings lista problemów do uwzględnienia w raporcie
     * @param output   ścieżka, pod którą ma zostać zapisany raport (np. plik HTML)
     * @throws Exception w razie błędów podczas zapisu raportu
     */
    void generate(List<Finding> findings, Path output) throws Exception;
}
