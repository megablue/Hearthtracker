import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.*;

public class HearthWin32Helper {
	
private static final int SM_CYCAPTION = 4;
private static final int SM_CXBORDER = 5;

   public interface User32 extends StdCallLibrary {
      User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
               W32APIOptions.DEFAULT_OPTIONS);

      HWND FindWindow(String lpClassName, String lpWindowName);

      int GetWindowRect(HWND handle, int[] rect);
      int GetClientRect(HWND handle, int[] rect);
      int GetSystemMetrics(int something);
   }

   
   
   public static int[] getRect(String windowName, String className) throws WindowNotFoundException,
            GetWindowRectException {
      HWND hwnd = User32.INSTANCE.FindWindow(className, windowName);
      if (hwnd == null) {
         throw new WindowNotFoundException("", windowName);
      }

      int[] rect = {0, 0, 0, 0};  
      int result = User32.INSTANCE.GetWindowRect(hwnd, rect);
      int titlebarHeight = User32.INSTANCE.GetSystemMetrics(SM_CYCAPTION);
      int borderSize = User32.INSTANCE.GetSystemMetrics(SM_CXBORDER);

      if (result == 0) {
         throw new GetWindowRectException(windowName);
      }

      //calculate the absolute position of the client area (exclude the window border)
      rect[0] += borderSize; 
      rect[1] += borderSize + titlebarHeight;
      rect[2] -= borderSize;
      rect[3] -= borderSize;
      
      return rect;
   }

   @SuppressWarnings("serial")
   public static class WindowNotFoundException extends Exception {
      public WindowNotFoundException(String className, String windowName) {
         super(String.format("Window null for className: %s; windowName: %s", 
                  className, windowName));
      }
   }

   @SuppressWarnings("serial")
   public static class GetWindowRectException extends Exception {
      public GetWindowRectException(String windowName) {
         super("Window Rect not found for " + windowName);
      }
   }
}