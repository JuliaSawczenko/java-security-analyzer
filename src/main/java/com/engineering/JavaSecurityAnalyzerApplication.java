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
public class JavaSecurityAnalyzerApplication implements ApplicationRunner {

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

    public static void main(String[] args) {
        new SpringApplicationBuilder(JavaSecurityAnalyzerApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String src   = firstOption(args, "source",  args.getNonOptionArgs(), "musisz podać --source");
        String report= firstOption(args, "report", args.getNonOptionArgs(), null, "security-report.html");

        AnalyzerConfig cfg = args.containsOption("config")
                ? yaml.readValue(new File(args.getOptionValues("config").get(0)), AnalyzerConfig.class)
                : new AnalyzerConfig();

        List<SecurityRule> active = rules.stream()
                .filter(r -> cfg.isRuleEnabled(r.getId()))
                .collect(Collectors.toList());

        scanner.scan(Paths.get(src), active, collector);
        reportGen.generate(collector.getFindings(), Paths.get(report));

        System.out.printf("Gotowe: %d reguł, raport → %s%n", active.size(), report);
    }

    private static String firstOption(
            ApplicationArguments args,
            String name,
            List<String> positionals,
            String errorMsgIfMissing,
            String defaultValue
    ) {
        if (args.containsOption(name)) {
            return args.getOptionValues(name).get(0);
        }
        int idx = "source".equals(name) ? 0 : 1;
        if (positionals.size() > idx) {
            return positionals.get(idx);
        }
        if (errorMsgIfMissing != null) {
            System.err.println(errorMsgIfMissing);
            System.exit(1);
        }
        return defaultValue;
    }

    private static String firstOption(
            ApplicationArguments args,
            String name,
            List<String> positionals,
            String errorMsgIfMissing
    ) {
        return firstOption(args, name, positionals, errorMsgIfMissing, null);
    }
}