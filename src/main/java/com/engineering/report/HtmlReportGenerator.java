package com.engineering.report;

import com.engineering.model.Finding;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import freemarker.template.Template;
import freemarker.template.Configuration;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HtmlReportGenerator implements ReportGenerator {

    private final Configuration freemarker;

    public HtmlReportGenerator(Configuration freemarker) {
        this.freemarker = freemarker;
    }

    @Override
    public void generate(List<Finding> findings, Path output) throws Exception {
        Template tpl = freemarker.getTemplate("report.ftl");
        Map<String,Object> model = new HashMap<>();
        model.put("findings", findings);

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            tpl.process(model, writer);
        }
    }
}