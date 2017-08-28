package pro.civitaspo.embulk.spi;

public class ElapsedTime
{
    private final static long POLLING_INTERVAL = 1_000; // ms

    private static long getNow()
    {
        return System.currentTimeMillis();
    }

    private static long getElapsed(long start)
    {
        return System.currentTimeMillis() - start;
    }

    private static void waitUntilNextPolling()
    {
        try {
            Thread.sleep(POLLING_INTERVAL);
        }
        catch (InterruptedException e) {
            // Do Nothing
        }
    }

    public interface Measurable<T>
    {
        T run();

        void onStart();

        void onFinished(long elapsedMillis);
    }

    public interface Pollable<T>
    {
        boolean poll();

        T getResult();

        void onStart();

        void onWaiting(long elapsedMillis);

        void onFinished(long elapsedMillis);
    }

    public static <T> T measure(Measurable<T> measurable)
    {
        long start = getNow();
        measurable.onStart();
        try {
            return measurable.run();
        }
        finally {
            measurable.onFinished(getElapsed(start));
        }
    }

    public static <T> T measureWithPolling(Pollable<T> pollable)
    {
        long start = getNow();
        pollable.onStart();
        while (true) {
            if (pollable.poll()) {
                try {
                    return pollable.getResult();
                }
                finally {
                    pollable.onFinished(getElapsed(start));
                }
            }
            pollable.onWaiting(getElapsed(start));
            waitUntilNextPolling();
        }
    }
}
