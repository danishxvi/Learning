package com.danish.spring.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final ExecutionRecorder recorder;

    public ScheduledTasks(ExecutionRecorder recorder) {
        this.recorder = recorder;
    }

    // fixedRate: the next execution is scheduled 300ms after THIS ONE STARTED,
    // regardless of how long it takes - in theory, back-to-back executions can even
    // overlap. In practice, with the default single-threaded scheduler, an execution
    // that's still running when its next fixedRate trigger fires just delays that next
    // run instead of overlapping it.
    @Scheduled(fixedRate = 300)
    public void fastTask() {
        recorder.record("fastTask");
    }

    // A second fixedRate task, deliberately slow, sharing the same default scheduler
    // thread as fastTask - this is what creates the contention this lesson documents.
    @Scheduled(fixedRate = 300)
    public void slowTask() throws InterruptedException {
        recorder.record("slowTask");
        Thread.sleep(800);
    }

    // fixedDelay: the next execution is scheduled 300ms after THIS ONE FINISHES. With
    // ~200ms of simulated work per run, executions should land roughly 500ms apart
    // (200ms work + 300ms delay), not 300ms apart.
    @Scheduled(fixedDelay = 300)
    public void fixedDelayTask() throws InterruptedException {
        recorder.record("fixedDelayTask");
        Thread.sleep(200);
    }

    // cron: fires on a wall-clock schedule rather than relative to the previous run -
    // "*/1 * * * * *" means "every second, on the second" (second minute hour
    // day-of-month month day-of-week, Spring's 6-field format).
    @Scheduled(cron = "*/1 * * * * *")
    public void cronTask() {
        recorder.record("cronTask");
    }
}
