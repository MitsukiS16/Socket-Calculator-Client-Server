

import java.util.Random;
import java.util.logging.Logger;

/**
 * Poisson process random number generation.
 * Simulates events occurring at a constant average rate.
 */
public final class PoissonProcess {
  
    private final double rateParameter;
    private final Random randomGenerator;
    private static final Logger logger = Logger.getLogger(PoissonProcess.class.getName());

    /**
     * Constructor.
     *
     * @param rateParameter     The average number of events per unit time (must be positive).
     * @param randomGenerator   Random number generator (must not be null).
     * @throws IllegalArgumentException If rateParameter is not positive or randomGenerator is null.
     */
    public PoissonProcess(double rateParameter, Random randomGenerator) {
        if (rateParameter <= 0) {
            throw new IllegalArgumentException("Rate parameter must be positive: " + rateParameter);
        }
        if (randomGenerator == null) {
            throw new IllegalArgumentException("Random number generator cannot be null.");
        }
        this.rateParameter = rateParameter;
        this.randomGenerator = randomGenerator;

        logger.info("PoissonProcess initialized with rateParameter=" + rateParameter);
    }

    /**
     * Calculate the time until the next event occurs or the number of events in a specified interval.
     *
     * @param intervalLength The length of the time interval (if 0, calculates next event time).
     * @return If intervalLength > 0, the number of events in the interval; otherwise, the time to the next event.
     * @throws IllegalArgumentException If the interval length is negative.
     */
    public double calculate(double intervalLength) {
        if (intervalLength < 0) {
            throw new IllegalArgumentException("Interval length must not be negative: " + intervalLength);
        }

        if (intervalLength == 0) {
            // Calculate time to next event (exponentially distributed)
            return generateInterArrivalTime();
        } else {
            // Calculate number of events in the interval (Poisson distributed)
            return generateEventCount(intervalLength);
        }
    }

    /**
     * Generates the inter-arrival time for the next event.
     * The inter-arrival time is exponentially distributed with rate parameter lambda.
     *
     * @return The inter-arrival time until the next event.
     */
    private double generateInterArrivalTime() {
        // Exponentially distributed inter-arrival time
        double randomValue = randomGenerator.nextDouble();
        double interArrivalTime = -Math.log(1.0 - randomValue) / rateParameter;
        logger.info(String.format("Generated inter-arrival time: %.4f", interArrivalTime));
        return interArrivalTime;
    }

    /**
     * Generates the number of events that occur in a given time interval.
     * The number of events in an interval follows a Poisson distribution with rate parameter lambda.
     *
     * @param intervalLength The length of the time interval.
     * @return The number of events that occur in the interval.
     */
    private int generateEventCount(double intervalLength) {
        // Poisson distributed number of events
        double lambda = rateParameter * intervalLength;
        int eventCount = 0;
        double cumulativeProbability = Math.exp(-lambda);
        double randomValue = randomGenerator.nextDouble();

        // Count the number of events that occur in the interval
        while (randomValue > cumulativeProbability) {
            eventCount++;
            cumulativeProbability += (Math.pow(lambda, eventCount) / factorial(eventCount)) * Math.exp(-lambda);
        }

        logger.info(String.format("Generated %d events in interval of length %.2f", eventCount, intervalLength));
        return eventCount;
    }

    /**
     * Helper function to compute the factorial of a number.
     *
     * @param n The number to compute the factorial of.
     * @return The factorial of the number.
     */
    private static long factorial(int n) {
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            PoissonProcess poissonProcess = new PoissonProcess(3.0, new Random());

            System.out.printf("Rate Parameter: %.2f%n", poissonProcess.rateParameter);
            System.out.printf("Time to Next Event: %.4f%n", poissonProcess.calculate(0));
            System.out.printf("Events in 1 Time Unit: %.0f%n", poissonProcess.calculate(1.0));
            System.out.printf("Events in 2 Time Units: %.0f%n", poissonProcess.calculate(2.0));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
