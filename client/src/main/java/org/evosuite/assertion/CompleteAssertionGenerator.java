/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package org.evosuite.assertion;

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.AsmetaChoiceTraceObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * <p>
 * CompleteAssertionGenerator class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class CompleteAssertionGenerator extends AssertionGenerator {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.assertion.AssertionGenerator#addAssertions(org.evosuite.
     * testcase.TestCase)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAssertions(TestCase test) {
        addAssertionsAndReturnResult(test);
    }

    private ExecutionResult addAssertionsAndReturnResult(TestCase test) {
        ExecutionResult result = runTest(test);
        for (OutputTrace<?> trace : result.getTraces()) {
            trace.getAllAssertions(test);
            trace.clear();
        }
        logger.debug("Test after adding assertions: " + test.toCode());
        return result;
    }

    @Override
    public void addAssertions(TestSuiteChromosome suite) {
        if (Properties.ASMETA_CHOICE_TRACE_FILE == null
                || Properties.ASMETA_CHOICE_TRACE_FILE.trim().isEmpty()) {
            super.addAssertions(suite);
            return;
        }

        setupClassLoader(suite);
        for (TestChromosome test : suite.getTestChromosomes()) {
            if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
                throw new IllegalStateException("Not enough time to capture the canonical ASMETA choose rules trace");
            }

            AsmetaChoiceTraceObserver observer = new AsmetaChoiceTraceObserver();
            observer.prepare();
            TestCaseExecutor.getInstance().addObserver(observer);
            try {
                ExecutionResult result = addAssertionsAndReturnResult(test.getTestCase());
                observer.assertSuccessful();
                test.setLastExecutionResult(result);
                test.setChanged(false);
            } finally {
                TestCaseExecutor.getInstance().removeObserver(observer);
            }
        }
    }
}
