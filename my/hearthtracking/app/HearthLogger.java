package my.hearthtracking.app;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class HearthLogger {
	private static HearthLogger instance = null;
	static Calendar cal = Calendar.getInstance();
	static String fileName = cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);
	static String logfile = String.format(HearthFilesNameManager.logFile, fileName);
	static Logger logger = null;

	public HearthLogger() {
		if(instance == null){
			instance = this;
		}
		
		initLogger();
	}
	
	public static HearthLogger getInstance(){
		if(instance == null){
			instance = new HearthLogger();
		}
		
		return instance;
	}
	
	public static void initLogger(){
		logger = Logger.getLogger("HearthTrackerLog");
        int limit	=	1024000; 
        int rotate 	=	10;
        FileHandler fh;
        
        try {
            // This block configure the logger with handler and formatter  
            fh = new FileHandler(logfile, limit, rotate, true);
            logger.addHandler(fh);  
            logger.setLevel(Level.INFO);  
            MyHtmlFormatter formatter = new MyHtmlFormatter();  
            fh.setFormatter(formatter);     
        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
	}	
	
	public void setLogLevel(Level loglevel){
		logger.setLevel(loglevel);
	}

	public void severe(String line){
		logger.severe(line);
	}
	
	public void warning(String line){
		logger.warning(line);
	}
	
	public void info(String line){
		logger.info(line);
	}
	
	public void fine(String line){
		logger.fine(line);
	}
	
	public void finer(String line){
		logger.finer(line);
	}
	
	public void finest(String line){
		logger.finest(line);
	}

	static class MyHtmlFormatter extends Formatter {
		// This method is called for every log records
		public String format(LogRecord rec) {
			StringBuffer buf = new StringBuffer(1000);
			// Bold any levels >= WARNING
			buf.append("<tr>");
			buf.append("<td>");

			if (rec.getLevel().intValue() >= Level.WARNING.intValue()) {
				buf.append("<b>");
				buf.append(rec.getLevel());
				buf.append("</b>");
			} else {
				buf.append(rec.getLevel());
			}

			buf.append("</td>");
			buf.append("<td>");
			buf.append(calcDate(rec.getMillis()));
			buf.append("</td><td>");
			buf.append(formatMessage(rec));
			buf.append("</td>");
			buf.append("</tr>\n");

			return buf.toString();
		}

		private String calcDate(long millisecs) {
			SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
			Date resultdate = new Date(millisecs);
			return date_format.format(resultdate);
		}

		// This method is called just after the handler using this
		// formatter is created
		public String getHead(Handler h) {
			return "<HTML>\n<HEAD>\n" + (new Date()) 
			+ "\n</HEAD>\n<BODY>\n<PRE>\n"
			+ "<table width=\"100%\" border>\n  "
			+ "<tr><th>Level</th>" +
			"<th>Time</th>" +
			"<th>Log Message</th>" +
			"</tr>\n";
		}

		// This method is called just after the handler using this
		// formatter is closed
		public String getTail(Handler h) {
			return "</table>\n  </PRE></BODY>\n</HTML>\n";
		}
	} 
}
