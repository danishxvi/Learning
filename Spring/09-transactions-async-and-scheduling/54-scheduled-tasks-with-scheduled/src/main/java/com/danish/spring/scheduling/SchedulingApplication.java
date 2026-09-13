package com.danish.spring.scheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// ============================================================================
// 54 - SCHEDULED TASKS WITH @Scheduled
// ============================================================================
// Run: mvn -f Spring/09-transactions-async-and-scheduling/54-scheduled-tasks-with-scheduled spring-boot:run
// ============================================================================
@SpringBootApplication
public class SchedulingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulingApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final ExecutionRecorder recorder;
        private final ConfigurableApplicationContext context;

        Demo(ExecutionRecorder recorder, ConfigurableApplicationContext context) {
            this.recorder = recorder;
            this.context = context;
        }

        @Override
        public void run(String... args) throws Exception {
            System.out.println("=".repeat(74));
            System.out.println("Letting @Scheduled tasks run for 4 seconds...");
            System.out.println("=".repeat(74));
            Thread.sleep(4000);

            printTask("fastTask", "fixedRate=300, no artificial work");
            printTask("slowTask", "fixedRate=300, sleeps 800ms each run - shares the scheduler thread with fastTask");
            printTask("fixedDelayTask", "fixedDelay=300, sleeps 200ms each run");
            printTask("cronTask", "cron=*/1 * * * * * (every second on the second)");

            Set<String> distinctThreads = new TreeSet<>();
            for (String task : List.of("fastTask", "slowTask", "fixedDelayTask", "cronTask")) {
                distinctThreads.addAll(recorder.getThreadNames(task));
            }
            System.out.println();
            System.out.println("distinct threads used across ALL @Scheduled methods: " + distinctThreads);

            context.close();
            System.exit(0);
        }

        private void printTask(String taskName, String description) {
            List<Long> executions = recorder.getExecutions(taskName);
            System.out.println();
            System.out.println(taskName + " (" + description + ")");
            System.out.println("  execution times (ms since start): " + executions);
            StringBuilder deltas = new StringBuilder("  deltas between executions (ms):  [");
            for (int i = 1; i < executions.size(); i++) {
                if (i > 1) deltas.append(", ");
                deltas.append(executions.get(i) - executions.get(i - 1));
            }
            deltas.append("]");
            System.out.println(deltas);
        }
    }
}
