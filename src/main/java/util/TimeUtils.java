package util;

import org.agrona.concurrent.CachedNanoClock;
import org.agrona.concurrent.NanoClock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;

/**
 * timestamp = nanos since epoch
 */
public class TimeUtils
{
    public static final ZoneId MOSCOW_ZONE_ID = ZoneId.of("Europe/Moscow");
    public static final long NANOS_IN_SECOND = 1_000_000_000L;
    public static final LocalDate SHINY_DAY = LocalDate.of(2018, 9, 10);
    private static final Logger LOGGER = LogManager.getLogger(TimeUtils.class);

    private static final CachedNanoClock UPDATEABLE_CLOCK = new CachedNanoClock();
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

    public static long toNanos(final Instant instant)
    {
        return instant.getEpochSecond() * NANOS_IN_SECOND + instant.getNano();
    }

    public static void setTimestamp(final long timestamp)
    {
        UPDATEABLE_CLOCK.update(timestamp);
        clock = UPDATEABLE_CLOCK;
    }

    public static void setMoscowTime(final LocalDate mskDate, final LocalTime mskTime)
    {
        final LocalDateTime mskDateTime = LocalDateTime.of(mskDate, mskTime);
        TimeUtils.setTimestamp(TimeUtils.toNanos(mskDateTime.atZone(MOSCOW_ZONE_ID).toInstant()));
    }

    public static void setDefaultClock()
    {
        clock = new RealtimeNanoClock();
    }

    /**
     * Sets day without holidays on major currencies.
     * Don't forget to revert using setDefaultClock() !!
     */
    public static void setShinyDay()
    {
        setMoscowMidDayAndRunClock(SHINY_DAY);
    }

    public static void setMoscowMidDayAndRunClock(final LocalDate date)
    {
        setHistoricalTickingClockFromProperty(date.atTime(12, 0).atZone(MOSCOW_ZONE_ID).toInstant());
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
