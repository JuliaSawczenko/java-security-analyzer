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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;


@SpringBootApplication
public class JavaSecurityAnalyzerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(JavaSecurityAnalyzerApplication.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }

    @Component
    static class Runner implements CommandLineRunner {
        private final SourceScanner sourceScanner;
        private final List<SecurityRule> securityRules;
        private final FindingCollector collector;
        private final ReportGenerator reportGenerator;

        public Runner(SourceScanner sourceScanner,
            List<SecurityRule> securityRules,
            FindingCollector collector,
            ReportGenerator reportGenerator) {
            this.sourceScanner = sourceScanner;
            this.securityRules = securityRules;
            this.collector = collector;
            this.reportGenerator = reportGenerator;
        }

        @Override
        public void run(String... args) throws Exception {
            // 1) Parse --source, --report, and --config flags:
            Map<String, String> opts = new HashMap<>();
            List<String> pos = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--source" -> opts.put("source", args[++i]);
                    case "--report" -> opts.put("report", args[++i]);
                    case "--config" -> opts.put("config", args[++i]);
                    default -> {
                        if (args[i].startsWith("--")) {
                            System.err.println("Unknown flag: " + args[i]);
                            return;
                        }
                        pos.add(args[i]);
                    }
                }
            }
            String sourceDir = opts.getOrDefault("source", pos.isEmpty()? null: pos.get(0));
            if (sourceDir == null) {
                System.err.println("Error: no source directory specified. Use --source <dir>");
                return;
            }
            String reportFile = opts.getOrDefault("report", pos.size()>1? pos.get(1): "security-report.html");

            // 2) Load config (if any):
            AnalyzerConfig config = new AnalyzerConfig();
            if (opts.containsKey("config")) {
                ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
                config = yaml.readValue(new File(opts.get("config")), AnalyzerConfig.class);
            }

            // 3) Filter rules by config:
            AnalyzerConfig finalConfig = config;
            List<SecurityRule> toRun = securityRules.stream()
                .filter(r -> finalConfig.isRuleEnabled(r.getId()))
                .collect(Collectors.toList());

            // 4) Perform scan + report:
            sourceScanner.scan(Paths.get(sourceDir), toRun, collector);
            reportGenerator.generate(collector.getFindings(), Paths.get(reportFile));

            System.out.printf("Done: scanned %d rules, report at %s%n",
                toRun.size(), reportFile);
        }
    }
}