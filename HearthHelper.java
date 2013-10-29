
public class HearthHelper {

	public static boolean isDevelopmentEnvironment() {
	    boolean isEclipse = true;
	    if (System.getenv("eclipseKelper") == null) {
	        isEclipse = false;
	    }
	    return isEclipse;
	}

}
