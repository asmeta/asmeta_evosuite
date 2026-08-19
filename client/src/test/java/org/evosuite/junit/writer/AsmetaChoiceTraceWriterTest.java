package org.evosuite.junit.writer;

import org.evosuite.Properties;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.AsmetaChoiceTraceEntry;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTraceImpl;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsmetaChoiceTraceWriterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void resetProperties() {
        Properties.getInstance().resetToDefaults();
    }

    @Test
    public void writesFinalNamesAndNeverReexecutesCanonicalTests() throws Exception {
        File output = temporaryFolder.newFolder("tests");
        File traceFile = new File(output, "Sample_ATG_ESTest.choices.properties");

        Properties.CLASS_PREFIX = "";
        Properties.TARGET_CLASS = "Sample_ATG";
        Properties.TEST_SCAFFOLDING = false;
        Properties.TEST_NAMING_STRATEGY = Properties.TestNamingStrategy.NUMBERED;
        Properties.OUTPUT_GRANULARITY = Properties.OutputGranularity.MERGED;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.COMPILE_ONLY;
        Properties.ASMETA_CHOICE_TRACE_FILE = traceFile.getAbsolutePath();

        TestCase first = new DefaultTestCase();
        TestCase second = new DefaultTestCase();
        first.addStatement(new IntPrimitiveStatement(first, 1));
        second.addStatement(new IntPrimitiveStatement(second, 2));
        ExecutionResult firstResult = resultFor(first, Collections.singletonList(
                new AsmetaChoiceTraceEntry(0, "r_Main", 0, "$x", "String", 2, "a:=b\n c")));
        ExecutionResult secondResult = resultFor(second, Collections.<AsmetaChoiceTraceEntry>emptyList());

        TestSuiteWriter writer = new TestSuiteWriter() {
            @Override
            protected ExecutionResult runTest(TestCase test) {
                throw new AssertionError("The writer must not re-execute canonical tests");
            }
        };
        writer.insertAllTests(Arrays.asList(first, second));
        writer.writeTestSuite("Sample_ATG_ESTest", output.getAbsolutePath(),
                Arrays.asList(firstResult, secondResult));

        assertTrue(traceFile.isFile());
        java.util.Properties trace = new java.util.Properties();
        try (FileInputStream input = new FileInputStream(traceFile)) {
            trace.load(input);
        }

        assertEquals("1", trace.getProperty("format.version"));
        assertEquals("2", trace.getProperty("test.count"));
        assertEquals("test0", trace.getProperty("test.0.name"));
        assertEquals("1", trace.getProperty("test.0.choice.count"));
        assertEquals("a:=b\n c", trace.getProperty("test.0.choice.0.value"));
        assertEquals("test1", trace.getProperty("test.1.name"));
        assertEquals("0", trace.getProperty("test.1.choice.count"));
    }

    private static ExecutionResult resultFor(TestCase test, List<AsmetaChoiceTraceEntry> choices) {
        ExecutionResult result = new ExecutionResult(test);
        result.setTrace(new ExecutionTraceImpl());
        result.setAsmetaChoiceTrace(choices);
        return result;
    }
}
