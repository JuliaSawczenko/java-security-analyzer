package com.engineering;

import com.engineering.config.AnalyzerConfig;
import com.engineering.model.FindingCollector;
import com.engineering.report.ReportGenerator;
import com.engineering.rule.SecurityRule;
import com.engineering.scanner.SourceScanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class JavaSecurityAnalyzerApplication implements CommandLineRunner {

    private final SourceScanner scanner;
    private final List<SecurityRule> rules;
    private final FindingCollector collector;
    private final ReportGenerator reportGen;
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    public JavaSecurityAnalyzerApplication(
            SourceScanner scanner,
            List<SecurityRule> rules,
            FindingCollector collector,
            ReportGenerator reportGen
    ) {
        this.scanner   = scanner;
        this.rules     = rules;
        this.collector = collector;
        this.reportGen = reportGen;
    }

    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    public static void main(String[] args) {
        new SpringApplicationBuilder(JavaSecurityAnalyzerApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        // ----- 1) parsowanie argumentów -----
        String sourcePath = null;
        String reportPath = "security-report.html";
        String configPath = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--source" -> sourcePath = args[++i];
                case "--report" -> reportPath = args[++i];
                case "--config" -> configPath = args[++i];
            }
        }
        if (sourcePath == null) {
            System.err.println("Błąd: musisz podać --source <katalog>");
            System.exit(1);
        }

        // ----- 2) wczytanie konfiguracji (opcjonalnie) -----
        AnalyzerConfig cfg = (configPath != null)
                ? yaml.readValue(new File(configPath), AnalyzerConfig.class)
                : new AnalyzerConfig();

        // ----- 3) wybór reguł -----
        List<SecurityRule> activeRules = rules.stream()
                .filter(r -> cfg.isRuleEnabled(r.getId()))
                .collect(Collectors.toList());

        // ----- 4) skanowanie i generowanie raportu -----
        scanner.scan(Paths.get(sourcePath), activeRules, collector);
        reportGen.generate(collector.getFindings(), Paths.get(reportPath));

        System.out.printf("Gotowe: uruchomiono %d reguł, raport → %s%n",
                activeRules.size(), reportPath);
    }
}