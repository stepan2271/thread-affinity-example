package util;

/**
 * Ticking clock with nano precision and variable speed.
 * rate = 1 -- realtime, same as HistoricalTickingClock
 * rate = 2 -- clock runs 2 times faster
 * rate = 0.5 -- clock runs 2 times slower
 *
 * So it features ticking, speed setting and time setting altogether.
 *
 * Threadsafe.
 */
class VariableRateTickingClock
    extends HistoricalTickingClock
{
    private final long realStartTimeNs;
    private final double rate;

    VariableRateTickingClock(final long startTimeNs, final double rate)
    {
        super(startTimeNs);
        this.realStartTimeNs = startTimeNs;
        this.rate = rate;
    }

    @Override
    public long nanoTime()
    {
        final long realTime = super.nanoTime();
        return (long)((realTime - realStartTimeNs) * rate) + realStartTimeNs;
    }
}
