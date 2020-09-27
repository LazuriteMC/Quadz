package bluevista.fpvracing.client.math;

public class BetaflightHelper {
	public static double calculateRates(double rcCommand, double rcRate, double expo, double superRate, double delta) {
	    double absRcCommand = Math.abs(rcCommand);
		
	    if (rcRate > 2.0)
	        rcRate = rcRate + (14.54 * (rcRate - 2.0));

	    if (expo != 0)
	        rcCommand = rcCommand * Math.pow(Math.abs(rcCommand), 3) * expo + rcCommand * (1.0 - expo);

	    double angleRate = 200.0 * rcRate * rcCommand;
	    if (superRate != 0){
	        double rcSuperFactor = 1.0 / (clamp(1.0 - absRcCommand * (superRate), 0.01, 1.00));
	        angleRate *= rcSuperFactor;
	    }

	    return angleRate * delta;
	}
	
    private static double clamp(double n, double minn, double maxn) {
    	return Math.max(Math.min(maxn, n), minn);
    }
}