package main;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinitySupport;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestLoop
{
    public static void main(final String[] args)
    {
        final Thread thread = Thread.currentThread();
        final int bindingCpu = Integer.getInteger("testLoopBindingCpu", 1);
        final int delay = Integer.getInteger("delaySec", 180);
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
        System.out.printf("\nStarted at %s", start);
        Instant lastRecordedTimeBeforeSwitch = start;
        // Switch time is a time when we switch from one
        Instant switchTime = start.plus(delay, ChronoUnit.SECONDS);
        Instant printTime = switchTime.plus(30, ChronoUnit.SECONDS);
        while (true)
        {
            final Instant now = Instant.now();
            if (lastRecordedTimeBeforeSwitch.isBefore(switchTime))
            {
                lastRecordedTimeBeforeSwitch = now;
            }
            else
            {
                if (now.isAfter(printTime))
                {
                    final Instant lastRecordedFinal = lastRecordedTimeBeforeSwitch;
                    System.out.printf("\nSwitch time: %s", switchTime);
                    System.out.printf("\nLast recorded time: %s", lastRecordedFinal);
                    System.out.printf("\nPrint time: %s", printTime);
                    System.exit(0);
                }
            }
        }
    }
}
