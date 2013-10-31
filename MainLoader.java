import java.io.File;
public class MainLoader {
	
	public static void main(String[] args) {
		File swtJar = new File(HearthHelper.getArchFilename("lib/swt"));
		HearthHelper.addJarToClasspath(swtJar);
		HearthUI.main(args);
	}

}
