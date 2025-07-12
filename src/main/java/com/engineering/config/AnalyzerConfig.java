package com.engineering.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Konfiguracja analizatora wczytywana z pliku YAML.
 *
 * Przykładowa struktura pliku YAML:
 * rules:
 *   SEC_SQL_INJECTION:
 *     enabled: true
 *   SEC_HARDCODED_SECRET:
 *     enabled: false
 */
public class AnalyzerConfig {

    /** Mapa identyfikatorów reguł na ich konfiguracje. */
    private Map<String, RuleConfig> rules = new HashMap<>();

    /** Zwraca konfigurację wszystkich reguł. */
    public Map<String, RuleConfig> getRules() {
        return rules;
    }

    /** Ustawia konfigurację reguł. */
    public void setRules(Map<String, RuleConfig> rules) {
        this.rules = rules;
    }

    /**
     * Sprawdza, czy dana reguła jest włączona.
     * Jeśli nie ma wpisu dla tej reguły, zwraca true (domyślnie włączona).
     *
     * @param ruleId unikalny identyfikator reguły
     * @return true, jeśli reguła jest włączona lub brak jej w konfiguracji
     */
    public boolean isRuleEnabled(String ruleId) {
        RuleConfig rc = rules.get(ruleId);
        return rc == null || rc.isEnabled();
    }

    /**
     * Wewnętrzna klasa reprezentująca konfigurację pojedynczej reguły.
     */
    @Getter
    public static class RuleConfig {
        /** Flaga mówiąca, czy reguła jest włączona (domyślnie true). */
        private boolean enabled = true;

        /** Ustawia, czy reguła ma być włączona. */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}