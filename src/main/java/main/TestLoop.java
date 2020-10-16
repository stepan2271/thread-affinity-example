package main;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinitySupport;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestLoop
{
    private static final long NANOS_IN_SECOND = 1_000_000_000;

    public static void main(final String[] args)
    {
        final Thread thread = Thread.currentThread();
        final int bindingCpu = Integer.getInteger("testLoopBindingCpu", 1);
        final int delay = Integer.getInteger("delaySec", 120);
        final long threadId = thread.getId();
        final int osThreadId = AffinitySupport.getThreadId();
        System.out.printf("\nAttempt to bind thread %s (%s) to cpu %s",
            threadId,
            osThreadId,
            bindingCpu);
        final AffinityLock affinityLock = AffinityLock.acquireLock(bindingCpu);
        System.out.printf("\nSuccessfully bound thread %s (%s) to cpu %s",
            threadId,
            osThreadId,
            affinityLock.cpuId());
        final Instant start = Instant.now();
        final long startNanoTime = System.nanoTime();
        System.out.printf("\nStarted at %s", start);
        // Switch time is a time when we switch from one
        Instant switchTime = start.plus(delay, ChronoUnit.SECONDS);
        final long switchDelayNs = delay * NANOS_IN_SECOND;
        // Print time is the time when we print results. It differs from switch time to avoid
        // print-related effects.
        Instant printTime = switchTime.plus(30, ChronoUnit.SECONDS);
        final long printDelayNanos = switchDelayNs + 30 * NANOS_IN_SECOND;
        long lastRecordedNanoTime = startNanoTime;
        while (true)
        {
            final long nanoTime = System.nanoTime();
            if (nanoTime - startNanoTime < switchDelayNs)
            {
                lastRecordedNanoTime = nanoTime;
            }
            else
            {
                if (nanoTime - startNanoTime > printDelayNanos)
                {
                    final Instant lastRecordedInstant =
                            start.plus(lastRecordedNanoTime - startNanoTime, ChronoUnit.NANOS);
                    System.out.printf("\nSwitch time: %s", switchTime);
                    System.out.printf("\nLast recorded time: %s", lastRecordedInstant);
                    System.out.printf("\nPrint time: %s", printTime);
                    System.exit(0);
                }
            }
        }
    }
}
