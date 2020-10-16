package util;

import org.agrona.concurrent.NanoClock;

/**
 * Ticking clock with nano precision.
 * Threadsafe.
 */
public class RealtimeNanoClock
        implements NanoClock
{
    private static final long NANOS_IN_MILLI = 1_000_000L;
    private static final long START_TIME_NS = NANOS_IN_MILLI * System.currentTimeMillis() - System.nanoTime();

    @Override
    public long nanoTime()
    {
        return START_TIME_NS + System.nanoTime();
    }
}
