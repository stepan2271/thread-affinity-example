package util;

import org.agrona.concurrent.NanoClock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * timestamp = nanos since epoch
 */
public class TimeUtils
{
    public static final long NANOS_IN_SECOND = 1_000_000_000L;
    private static final Logger LOGGER = LogManager.getLogger(TimeUtils.class);
    private static NanoClock clock = setHistoricalTickingClockFromProperty();

    /**
     * @return Nanoseconds using internal clock
     */
    public static long now()
    {
        return clock.nanoTime();
    }

    public static Instant toInstant(final long timestamp)
    {
        return Instant.ofEpochSecond(timestamp / NANOS_IN_SECOND, timestamp % NANOS_IN_SECOND);
    }

    public static void setDefaultClock()
    {
        clock = new RealtimeNanoClock();
    }

    private static NanoClock setHistoricalTickingClockFromProperty(final Instant instant)
    {
        clock = new HistoricalTickingClock(instant.getEpochSecond() * NANOS_IN_SECOND);
        return clock;
    }

    public static void setVariableRateTickingClock(final Instant instant, final double rate)
    {
        clock = new VariableRateTickingClock(instant.getEpochSecond() * NANOS_IN_SECOND, rate);
    }

    private static NanoClock setHistoricalTickingClockFromProperty()
    {
        final String clockStartTime = System.getProperty("clockStartTime");
        try
        {
            final Instant start = ZonedDateTime.parse(clockStartTime).toInstant();
            setHistoricalTickingClockFromProperty(start);
            LOGGER.info("Successfully set clock start time to {}", clockStartTime);
        }
        catch (final Exception ignored)
        {
            setDefaultClock();
            LOGGER.info("Default realtime clock is used");
        }
        return clock;
    }
}
