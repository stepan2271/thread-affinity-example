package util;

import org.agrona.concurrent.NanoClock;

/**
 * Ticking clock with nano precision.
 * Threadsafe.
 */
class HistoricalTickingClock
    implements NanoClock
{
    final long startTimeNs;

    HistoricalTickingClock(final long startTimeNs)
    {
        this.startTimeNs = startTimeNs - System.nanoTime();
    }

    @Override
    public long nanoTime()
    {
        return startTimeNs + System.nanoTime();
    }
}
