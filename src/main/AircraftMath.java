public class AircraftMath {

	private AircraftMath() { }


	public static String generateFlightNumber(int length) {
		String str = "";
		for (int i = 0; i < length; i++)
			str += (int) (Math.random() * 10);
		return str;
	}


	public static String generateTailNumber(int length) {
		String[] symbols = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
							"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1",
							"2", "3", "4", "5", "6", "7", "8", "9"};
		String str = "";
		for (int i = 0; i < length; i++)
			str += symbols[(int) (Math.random() * symbols.length)];
		return str;
	}


	public static int runwayHdg(String identifier) {
		// Remove non-numeric characters
		String cleaned = identifier.replaceAll("[^0-9]", "");

		// Get integer heading
		int hdg;
		try {
			hdg = Integer.parseInt(cleaned);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("invalid runway identifier: " + identifier);
		}
		hdg *= 10;

		// Get heading in range 0 < hdg <= 360 to the nearest 10 degrees
		int adjusted = (int) AircraftMath.adjustHdg(hdg);
		if (adjusted == 0)
			adjusted = 360;
		adjusted = AircraftMath.round(adjusted, 10);
		return adjusted;
	}


	public static int round(double current, double interval) {
		int floor = (int) (((int) current / (int) interval) * (int) interval);
		int ceil = (int) (floor + interval);
		return (current - floor > ceil - current) ? ceil : floor;
	}


	public static double adjustHdg(double hdg) {
		int offset = (hdg < 0) ? 360 : -360;
		while (hdg < 0 || hdg >= 360) {
			hdg += offset;
			 // Recompute offset to prevent runaways from precision error in doubles
			offset = (hdg < 0) ? 360 : -360;
		}
		return hdg;
	}


	public static double hdgToRad(double hdg) {
		double deg = (450 - hdg) % 360;
		return deg * (Math.PI / 180);
	}


	public static double radToHdg(double rad) {
		double hdg = 90 - (rad * (180 / Math.PI));
		return AircraftMath.adjustHdg(hdg);
	}


	public static double hdgDifference(double a, double b) {
	    if (b >= a)
			return b - a;
		return (360 - a) + b;
	}


	public static double hdgToTarget(double currentX, double currentY,
									 double targetX, double targetY)
	{
	    double directXDist = targetX - currentX;
		double directYDist = currentY - targetY;
		double directDist = Math.sqrt(Math.pow(directXDist, 2) + Math.pow(directYDist, 2));
		if (directDist == 0)
			return 0.0;
			
		double directRad = Math.asin(directYDist / directDist);
		if (directXDist < 0) // Account for domain restriction of arcsin function
			directRad = Math.PI - directRad;
		
		return AircraftMath.radToHdg(directRad);
	}


	public static double approachHdg(double current, double target, double interval) {
		double ceil = current + interval;
		double floor = current - interval;
		if (ceil > target && floor < target)
			return target;

		if (AircraftMath.hdgDifference(current, target) < 180)
			return AircraftMath.adjustHdg(current + interval);
		return AircraftMath.adjustHdg(current - interval);
	}


	public static double approachValue(double current, double target, double interval) {
		double ceil = current + interval;
		double floor = current - interval;

		if (ceil > target && floor < target)
			return target;
		if (current > target)
			return floor;
		return ceil;
	}
	
}
