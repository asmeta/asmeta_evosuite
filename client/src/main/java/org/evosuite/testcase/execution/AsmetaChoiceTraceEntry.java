package org.evosuite.testcase.execution;

/**
 * One value selected by an ASMeta choose rule during a test execution.
 */
public final class AsmetaChoiceTraceEntry {

    private final int step;
    private final String rule;
    private final int occurrence;
    private final String variable;
    private final String domain;
    private final int randomIndex;
    private final String value;

    public AsmetaChoiceTraceEntry(int step, String rule, int occurrence, String variable,
                                  String domain, int randomIndex, String value) {
        this.step = step;
        this.rule = rule;
        this.occurrence = occurrence;
        this.variable = variable;
        this.domain = domain;
        this.randomIndex = randomIndex;
        this.value = value;
    }

    public int getStep() {
        return step;
    }

    public String getRule() {
        return rule;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public String getVariable() {
        return variable;
    }

    public String getDomain() {
        return domain;
    }

    public int getRandomIndex() {
        return randomIndex;
    }

    public String getValue() {
        return value;
    }
}
