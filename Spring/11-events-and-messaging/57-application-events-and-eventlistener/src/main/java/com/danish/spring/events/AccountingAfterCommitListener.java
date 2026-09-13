package com.danish.spring.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AccountingAfterCommitListener {

    private final EventRecorder recorder;

    public AccountingAfterCommitListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    // AFTER_COMMIT (the default phase, spelled out here for clarity) defers this
    // listener until the transaction that was active when the event was published
    // actually commits. If that transaction rolls back instead, this method never runs
    // at all - unlike the plain @EventListener methods above, which already ran,
    // unconditionally, before the transaction outcome was even known.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        recorder.record("accounting-after-commit");
    }
}
