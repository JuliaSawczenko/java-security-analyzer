package com.engineering.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class AnalyzerConfig {

    /**
     * YAML structure:
     *
     * rules:
     *   SEC_SQL_INJECTION:
     *     enabled: true
     *   SEC_HARDCODED_SECRET:
     *     enabled: false
     */
    private Map<String, RuleConfig> rules = new HashMap<>();

    public Map<String, RuleConfig> getRules() {
        return rules;
    }

    public void setRules(Map<String, RuleConfig> rules) {
        this.rules = rules;
    }

    public boolean isRuleEnabled(String ruleId) {
        RuleConfig rc = rules.get(ruleId);
        return rc == null || rc.isEnabled();
    }

    @Getter
    public static class RuleConfig {
        private boolean enabled = true;

        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}