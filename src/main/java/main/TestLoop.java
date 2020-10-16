package main;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinitySupport;
import util.RealtimeNanoClock;
import util.TimeUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestLoop
{

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
        final long startNanoTime = TimeUtils.now();
        System.out.printf("\nStarted at %s", start);
        // Switch time is a time when we switch from one
        Instant switchTime = start.plus(delay, ChronoUnit.SECONDS);
        long lastRecordedNanoTime = startNanoTime;
        while (true)
        {
            if (lastRecordedNanoTime < startNanoTime + RealtimeNanoClock.NANOS_IN_SECOND * delay)
            {
                lastRecordedNanoTime = TimeUtils.now();
            }
            else
            {
                if (TimeUtils.now() > startNanoTime + RealtimeNanoClock.NANOS_IN_SECOND * (delay + 30))
                {
                    final Instant lastRecordedInstant =
                            start.plus(lastRecordedNanoTime - startNanoTime, ChronoUnit.NANOS);
                    System.out.printf("\nSwitch time: %s", switchTime);
                    System.out.printf("\nLast recorded time: %s", lastRecordedInstant);
                    System.out.printf("\nPrint time: %s", TimeUtils.now());
                    System.exit(0);
                }
            }
        }
    }
}
