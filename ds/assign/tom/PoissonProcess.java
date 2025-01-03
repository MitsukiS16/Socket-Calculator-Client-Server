import java.util.Random;
import java.util.logging.Logger;

/**
 * PoissonProcess class simulates a Poisson process for generating random events.
 * 
 * Responsibilities:
 * 1. Calculate the time until the next event (inter-arrival time).
 * 2. Calculate the number of events in a specified time interval.
 * 3. Use exponential and Poisson distributions to model real-world scenarios.
 * 
 * This class is useful in applications such as network traffic, call arrivals, and anti-entropy mechanisms.
 */
public final class PoissonProcess {

    private final double rateParameter; // The average number of events per unit time (lambda)
    private final Random randomGenerator; // Random number generator for generating events
    private static final Logger logger = Logger.getLogger(PoissonProcess.class.getName()); // Logger for debugging

    /**
     * Constructor for initializing a PoissonProcess instance.
     *
     * @param rateParameter     The average number of events per unit time (lambda). Must be positive.
     * @param randomGenerator   A random number generator. Must not be null.
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
     * Calculates either the time to the next event or the number of events in a time interval.
     *
     * @param intervalLength The length of the time interval. If 0, calculates the inter-arrival time.
     * @return If intervalLength > 0, returns the number of events in the interval.
     *         If intervalLength == 0, returns the time to the next event.
     * @throws IllegalArgumentException If intervalLength is negative.
     */
    public double calculate(double intervalLength) {
        if (intervalLength < 0) {
            throw new IllegalArgumentException("Interval length must not be negative: " + intervalLength);
        }

        if (intervalLength == 0) {
            // Calculate time to the next event
            return generateInterArrivalTime();
        } else {
            // Calculate number of events in the interval
            return generateEventCount(intervalLength);
        }
    }

    /**
     * Generates the inter-arrival time to the next event.
     * 
     * Explanation:
     * The inter-arrival time follows an exponential distribution with parameter lambda (rateParameter).
     * 
     * Formula: interArrivalTime = -log(1 - U) / lambda
     * where U is a uniform random variable in [0, 1).
     *
     * @return The time to the next event.
     */
    private double generateInterArrivalTime() {
        double randomValue = randomGenerator.nextDouble(); // Generate a uniform random number
        double interArrivalTime = -Math.log(1.0 - randomValue) / rateParameter; // Apply exponential distribution formula
        logger.info(String.format("Generated inter-arrival time: %.4f", interArrivalTime));
        return interArrivalTime;
    }

    /**
     * Generates the number of events in a given time interval.
     * 
     * Explanation:
     * The number of events in an interval follows a Poisson distribution with mean lambda * intervalLength.
     * 
     * Method:
     * - Use inverse transform sampling to calculate the number of events.
     * - Incrementally calculate probabilities until the cumulative probability exceeds a random threshold.
     *
     * @param intervalLength The length of the time interval.
     * @return The number of events that occur in the interval.
     */
    private int generateEventCount(double intervalLength) {
        double lambda = rateParameter * intervalLength; // Calculate the expected number of events
        int eventCount = 0; // Initialize event count
        double randomValue = randomGenerator.nextDouble(); // Generate a random threshold

        // Calculate e^(-lambda) for the Poisson distribution
        double expLambda = Math.exp(-lambda); // P(0 events)
        double cumulativeProbability = expLambda;

        // Increment event count while the random threshold is not exceeded
        while (randomValue > cumulativeProbability) {
            eventCount++;
            cumulativeProbability += (Math.pow(lambda, eventCount) / factorial(eventCount)) * expLambda;
        }

        logger.info(String.format("Generated %d events in interval of length %.2f", eventCount, intervalLength));
        return eventCount;
    }

    /**
     * Helper function to compute the factorial of a number.
     * 
     * Explanation:
     * The factorial of a number n is defined as n! = n × (n - 1) × ... × 1.
     * Used in calculating probabilities for the Poisson distribution.
     *
     * @param n The number to compute the factorial of (n >= 0).
     * @return The factorial of n.
     */
    private static long factorial(int n) {
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Main method for testing the PoissonProcess class.
     *
     * Demonstrates:
     * 1. Calculating the time to the next event.
     * 2. Calculating the number of events in various intervals.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            // Initialize PoissonProcess with a rate of 3 events per unit time
            PoissonProcess poissonProcess = new PoissonProcess(3.0, new Random());

            // Display the rate parameter
            System.out.printf("Rate Parameter: %.2f%n", poissonProcess.rateParameter);

            // Demonstrate inter-arrival time calculation
            System.out.printf("Time to Next Event: %.4f%n", poissonProcess.calculate(0));

            // Demonstrate number of events in specific intervals
            System.out.printf("Events in 1 Time Unit: %.0f%n", poissonProcess.calculate(1.0));
            System.out.printf("Events in 2 Time Units: %.0f%n", poissonProcess.calculate(2.0));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
