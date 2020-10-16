package main;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinitySupport;
import util.TimeUtils;

public class TestLoop
{

    public static void main(final String[] args)
    {
        final Thread thread = Thread.currentThread();
        final int bindingCpu = Integer.getInteger("testLoopBindingCpu", 1);
        final int delay = Integer.getInteger("delay", 120);
        System.out.println(
                "Attempt to bind thread {} ({}) to cpu {}" +
                        thread.getId() +
                        AffinitySupport.getThreadId() +
                        bindingCpu);
        final AffinityLock affinityLock = AffinityLock.acquireLock(bindingCpu);
        System.out.println(
                "Successfully bound thread {} ({}) to cpu {}" +
                        thread.getId() +
                        AffinitySupport.getThreadId() +
                        affinityLock.cpuId());
        final long start = TimeUtils.now();
        System.out.println("Started at " + TimeUtils.toInstant(start));
        long now = start;
        while (true)
        {
            if (now < start + TimeUtils.NANOS_IN_SECOND * delay)
            {
                now = TimeUtils.now();
            }
            else
            {
                // Will be printed after 30 sec
                if (TimeUtils.now() > start + TimeUtils.NANOS_IN_SECOND * (delay + 30))
                {
                    final long finalNow = now;
                    System.out.println("Time is over at " +
                            TimeUtils.toInstant(finalNow) + " now: " +
                            TimeUtils.toInstant(TimeUtils.now()));
                    System.exit(0);
                }
            }
        }
    }
}
