package util;

import org.agrona.concurrent.NanoClock;

public class TimeUtils
{
    private static NanoClock clock = setClock();

    public static NanoClock setClock()
    {
        final boolean useSystemNanoTime = Boolean.getBoolean("useSystemNanoTime");
        NanoClock nanoClock = System::nanoTime;
        if (useSystemNanoTime)
        {
            clock = nanoClock;
        }
        else
        {
            clock = new RealtimeNanoClock();
        }
        return clock;
    }

    public static long now()
    {
        return clock.nanoTime();
    }
}
