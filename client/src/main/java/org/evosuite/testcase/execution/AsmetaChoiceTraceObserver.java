package org.evosuite.testcase.execution;

import org.evosuite.Properties;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the lightweight choice recorder exposed by ASMETA generated ATG classes.
 */
public final class AsmetaChoiceTraceObserver extends ExecutionObserver {

    public static final String START_METHOD = "__asmetaStartChoiceRecording";
    public static final String STOP_METHOD = "__asmetaStopChoiceRecording";

    private Method startMethod;
    private Method stopMethod;
    private boolean started;
    private boolean finished;
    private RuntimeException failure;

    /** Resolve the hooks without executing target code. */
    public void prepare() {
        clear();
        Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
        if (targetClass == null) {
            throw new IllegalStateException("ASMETA choose rules tracing is enabled, but the target class could not be loaded");
        }
        try {
            startMethod = targetClass.getDeclaredMethod(START_METHOD);
            stopMethod = targetClass.getDeclaredMethod(STOP_METHOD);
            startMethod.setAccessible(true);
            stopMethod.setAccessible(true);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new IllegalStateException("ASMETA choose rules tracing is enabled, but target class "
                    + targetClass.getName() + " does not expose the required recorder hooks", e);
        }
    }

    @Override
    public void beforeStatement(Statement statement, Scope scope) {
        if (!started && failure == null) {
            invokeStart();
        }
    }

    @Override
    public void afterStatement(Statement statement, Scope scope, Throwable exception) {
        // Nothing to do.
    }

    @Override
    public void output(int position, String output) {
        // Nothing to do.
    }

    @Override
    public void testExecutionFinished(ExecutionResult result, Scope scope) {
        try {
            if (!started && failure == null) {
                invokeStart();
            }
            if (failure == null) {
                Object rawTrace = stopMethod.invoke(null);
                result.setAsmetaChoiceTrace(parseTrace(rawTrace));
            }
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            failure = recorderFailure("stop", e);
        } finally {
            finished = true;
        }
    }

    @Override
    public void clear() {
        started = false;
        finished = false;
        failure = null;
    }

    /** Fail outside the test runner if recorder extraction was incomplete. */
    public void assertSuccessful() {
        if (failure != null) {
            throw failure;
        }
        if (!finished) {
            throw new IllegalStateException("ASMETA choose rules trace was not completed before the target was reset");
        }
    }

    private void invokeStart() {
        try {
            startMethod.invoke(null);
            started = true;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            failure = recorderFailure("start", e);
        }
    }

    private List<AsmetaChoiceTraceEntry> parseTrace(Object rawTrace) {
        if (!(rawTrace instanceof String[][])) {
            throw new IllegalStateException("ASMETA choose rules recorder returned an invalid trace type");
        }

        List<AsmetaChoiceTraceEntry> trace = new ArrayList<>();
        for (String[] row : (String[][]) rawTrace) {
            if (row == null || row.length != 7) {
                throw new IllegalStateException("ASMETA choose rules recorder returned an invalid trace row");
            }
            try {
                trace.add(new AsmetaChoiceTraceEntry(Integer.parseInt(row[0]), row[1],
                        Integer.parseInt(row[2]), row[3], row[4], Integer.parseInt(row[5]), row[6]));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("ASMETA choose rules recorder returned an invalid numeric field", e);
            }
        }
        return trace;
    }

    private RuntimeException recorderFailure(String operation, Exception exception) {
        Throwable cause = exception instanceof InvocationTargetException
                ? ((InvocationTargetException) exception).getCause() : exception;
        return new IllegalStateException("Failed to " + operation + " ASMETA choose rules recording", cause);
    }
}
