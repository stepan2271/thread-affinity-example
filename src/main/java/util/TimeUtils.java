package util;

import org.agrona.concurrent.CachedNanoClock;
import org.agrona.concurrent.NanoClock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;
import java.util.stream.Stream;

/**
 * timestamp = nanos since epoch
 */
public class TimeUtils
{
    public static final ZoneId MOSCOW_ZONE_ID = ZoneId.of("Europe/Moscow");
    public static final int MINUTES_IN_HOUR = 60;
    public static final int MINUTES_IN_DAY = MINUTES_IN_HOUR * 24;
    public static final long SECONDS_IN_DAY = 60 * MINUTES_IN_DAY;
    public static final long NANOS_IN_SECOND = 1_000_000_000L;
    public static final long NANOS_IN_MINUTE = NANOS_IN_SECOND * 60;
    public static final long NANOS_IN_DAY = NANOS_IN_MINUTE * MINUTES_IN_DAY;
    public static final long NANOS_IN_MILLISECOND = 1_000_000L;
    public static final long MILLIS_IN_WEEK = 1000 * SECONDS_IN_DAY * 7;
    public static final LocalDate SHINY_DAY = LocalDate.of(2018, 9, 10);
    private static final Logger LOGGER = LogManager.getLogger(TimeUtils.class);

    private static final CachedNanoClock UPDATEABLE_CLOCK = new CachedNanoClock();
    private static final int NANOS_IN_MILLIS = 1_000_000;
    private static final int NANOS_IN_MICROS = 1_000;
    private static NanoClock clock = setHistoricalTickingClockFromProperty();

    /**
     * @return Nanoseconds using internal clock
     */
    public static long now()
    {
        return clock.nanoTime();
    }

    public static long nowMillis()
    {
        return now() / NANOS_IN_MILLIS;
    }

    public static long nowMicros()
    {
        return now() / NANOS_IN_MICROS;
    }

    public static Instant toInstant(final long timestamp)
    {
        return Instant.ofEpochSecond(timestamp / NANOS_IN_SECOND, timestamp % NANOS_IN_SECOND);
    }

    public static ZonedDateTime toMoscowZonedTime(final long timestamp)
    {
        return toInstant(timestamp).atZone(TimeUtils.MOSCOW_ZONE_ID);
    }

    public static LocalDateTime toMoscowZonedTimeAsLocal(final long timestamp)
    {
        return toMoscowZonedTime(timestamp).toLocalDateTime();
    }

    public static Instant toMoscowInstant(final LocalDateTime localDateTime)
    {
        return localDateTime.atZone(TimeUtils.MOSCOW_ZONE_ID).toInstant();
    }

    public static Instant instantNow()
    {
        return toInstant(now());
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

    public static LocalDate today()
    {
        return toMoscowDate(toInstant(clock.nanoTime()));
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

    public static long getStartOfDayTimestampMSK(final LocalDate date)
    {
        return toNanos(getStartOfDayInstantMSK(date));
    }

    public static Instant getStartOfDayInstantMSK(final LocalDate date)
    {
        return date.atStartOfDay(MOSCOW_ZONE_ID).toInstant();
    }

    public static Instant getStartOfDayInstantMSK(final Instant instant)
    {
        return getStartOfDayInstantMSK(toMoscowDate(instant));
    }

    public static Instant getEndOfDayInstantMSK(final LocalDate date)
    {
        return getStartOfDayInstantMSK(date.plusDays(1)).minusNanos(1);
    }

    public static Instant getEndOfDayInstantMSK(final Instant instant)
    {
        return getEndOfDayInstantMSK(toMoscowDate(instant));
    }

    public static LocalTime toMoscowTime(final Instant instant)
    {
        return instant.atZone(MOSCOW_ZONE_ID).toLocalTime();
    }

    public static LocalDate toMoscowDate(final Instant instant)
    {
        return instant.atZone(MOSCOW_ZONE_ID).toLocalDate();
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

    public static long millis2Nanos(final long millis)
    {
        return millis * TimeUtils.NANOS_IN_MILLISECOND;
    }

    public static Instant min(final Instant x, final Instant y)
    {
        return x.isBefore(y) ? x : y;
    }

    public static Instant max(final Instant x, final Instant y)
    {
        return x.isAfter(y) ? x : y;
    }

    public static Stream<LocalDate> datesStream(final Instant startTime, final Instant endTime)
    {
        return datesStream(toMoscowDate(startTime), toMoscowDate(endTime));
    }

    public static Stream<LocalDate> datesStream(final LocalDate startDate, final LocalDate endDate)
    {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    public static LocalDate toMoscowDate(final long timestamp)
    {
        return toMoscowDate(toInstant(timestamp));
    }

    public static boolean isWeekend(final LocalDate date)
    {
        return date.getDayOfWeek().getValue() >= DayOfWeek.SATURDAY.getValue();
    }

    private static boolean isWeekday(final LocalDate date)
    {
        return !isWeekend(date);
    }

}
