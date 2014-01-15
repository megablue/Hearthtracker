package my.hearthtracking.app;

public class HearthUpdaterLog {
	int[] version = {MainLoader.version[0], MainLoader.version[1], MainLoader.version[2]};
	long lastCheck = 0;
	long updated = 0;
	String message = "";
}
