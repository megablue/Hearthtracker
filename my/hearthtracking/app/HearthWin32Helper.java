package my.hearthtracking.app;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.WINDOWINFO;
import com.sun.jna.win32.*;

public class HearthWin32Helper {
	
	public interface User32 extends StdCallLibrary {
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class,
               W32APIOptions.DEFAULT_OPTIONS);
		
		int FindWindow(String lpClassName, String lpWindowName);
		int GetWindowRect(HWND handle, int[] rect);
		int GetClientRect(HWND handle, int[] rect);
		boolean ClientToScreen( HWND hWnd, int[] point);
		int GetSystemMetrics(int something);
		boolean GetWindowInfo( HWND hWnd, WINDOWINFO info);
	}
	
	public static int findWindow(String className, String windowName){
		int hwnd = User32.INSTANCE.FindWindow(className, windowName);		
		return hwnd;
	}
	
	public static boolean isWindowMinimized(int hwndpt){
		HWND hwnd = new HWND(new Pointer(hwndpt)); 
		WINDOWINFO info = new WINDOWINFO();
		User32.INSTANCE.GetWindowInfo(hwnd, info);

        // print out the title of minimized (WS_ICONIC) windows
        if ((info.dwStyle & com.sun.jna.platform.win32.WinUser.WS_MINIMIZE ) == com.sun.jna.platform.win32.WinUser.WS_MINIMIZE ) {
        	return true;
        }
        
        return false;
	}

	public static int[] getRect(String className, String windowName) throws WindowNotFoundException, GetWindowRectException {
		HWND hwnd = new HWND(new Pointer(User32.INSTANCE.FindWindow(className, windowName)));
		if (hwnd == null) {
			throw new WindowNotFoundException("", windowName);
		}

		int[] cRect = {0, 0, 0, 0};
		int[] point1 = {0, 0}; 
		int[] point2 = {0, 0};
      
		int result = User32.INSTANCE.GetClientRect(hwnd, cRect);
      
		if (result == 0) {
			throw new GetWindowRectException(windowName);
		}
      
		point1[0] = cRect[0];
		point1[1] = cRect[1];
		point2[0] = cRect[2];
		point2[1] = cRect[3];
      
		//calculate the absolute position of the client area (exclude the window borders and title bar)
		if(User32.INSTANCE.ClientToScreen(hwnd, point1)){
	  
		}
      
		if(User32.INSTANCE.ClientToScreen(hwnd, point2)){
			
		}

		cRect[0] = point1[0]; 
		cRect[1] = point1[1];
		cRect[2] = point2[0];
		cRect[3] = point2[1];
      
		return cRect;
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