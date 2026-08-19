package org.evosuite.testcase.execution;

import org.evosuite.Properties;
import org.evosuite.testcase.DefaultTestCase;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AsmetaChoiceTraceObserverTest {

    @After
    public void resetProperties() {
        Properties.getInstance().resetToDefaults();
        Properties.resetTargetClass();
    }

    @Test
    public void readsRecorderHooksAndKeepsTraceWhenResultIsCloned() {
        Properties.TARGET_CLASS = RecorderTarget.class.getName();
        Properties.resetTargetClass();

        AsmetaChoiceTraceObserver observer = new AsmetaChoiceTraceObserver();
        observer.prepare();
        observer.beforeStatement(null, null);

        ExecutionResult result = new ExecutionResult(new DefaultTestCase());
        result.setTrace(new ExecutionTraceImpl());
        observer.testExecutionFinished(result, null);
        observer.assertSuccessful();

        AsmetaChoiceTraceEntry choice = result.getAsmetaChoiceTrace().get(0);
        assertEquals(2, choice.getStep());
        assertEquals("r_Main(Integer)", choice.getRule());
        assertEquals(3, choice.getOccurrence());
        assertEquals("$x", choice.getVariable());
        assertEquals("Integer", choice.getDomain());
        assertEquals(1, choice.getRandomIndex());
        assertEquals("a:=b", choice.getValue());

        ExecutionResult clone = result.clone();
        assertEquals(1, clone.getAsmetaChoiceTrace().size());
        assertEquals("$x", clone.getAsmetaChoiceTrace().get(0).getVariable());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedTraceCannotBeModified() {
        ExecutionResult result = new ExecutionResult(new DefaultTestCase());
        result.setAsmetaChoiceTrace(Collections.<AsmetaChoiceTraceEntry>emptyList());
        result.getAsmetaChoiceTrace().add(null);
    }

    public static class RecorderTarget {
        private static boolean recording;

        public static void __asmetaStartChoiceRecording() {
            recording = true;
        }

        public static String[][] __asmetaStopChoiceRecording() {
            if (!recording) {
                throw new IllegalStateException("recording was not started");
            }
            recording = false;
            return new String[][]{{"2", "r_Main(Integer)", "3", "$x", "Integer", "1", "a:=b"}};
        }
    }
}
