package cz.cuni.mff.xrg.odcs.backend.execution;

/**
 * Contains definitions for execution states.
 * 
 * @author Petyr
 */
public class ExecutionResult {

    private boolean stop = false;

    private boolean failed = false;

    private boolean finished = false;

    /**
     * Stop the execution, does not give the reason.
     */
    public void stop() {
        stop = true;
    }

    /**
     * Set state to failure.
     */
    public void failure() {
        stop();
        failed = true;
    }

    /**
     * Announce that the execution finished standardly. Not by exception.
     */
    public void finished() {
        finished = true;
    }

    /**
     * If return true the post executors should be executed and then the execution
     * should end asp.
     * 
     * @return true if the execution should continue
     */
    public boolean continueExecution() {
        return !stop;
    }

    /**
     * @return true if the execution failed or has been terminated by exception.
     */
    public boolean executionFailed() {
        return failed;
    }

    /**
     * @return true if the execution calls any of {@link #stop()}, {@link #failure()} or {@link #finished()} methods.
     */
    public boolean executionEndsProperly() {
        return stop || failed || finished;
    }

    /**
     * @return true if the execution ends without errors
     */
    public boolean executionEndsSuccessfully() {
        return !failed && finished;
    }

    /**
     * @return true if the execution has been killed by exception.
     */
    public boolean nonStandardEnding() {
        return !finished;
    }

    /**
     * Add information from another execution result.
     * 
     * @param result
     */
    public void add(ExecutionResult result) {
        // if just one of the values is true
        // then the result is true
        this.stop |= result.stop;
        this.failed |= result.failed;
        this.finished |= result.finished;
    }

}
