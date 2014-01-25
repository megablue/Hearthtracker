package my.hearthtracking.app;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;

import org.eclipse.swt.internal.win32.BITMAPINFOHEADER;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HRESULT;

public class HearthRobot {
	private static Robot robot = null;	
    private static final User32 USER = User32.INSTANCE;
    private static final GDI32 GDI = GDI32.INSTANCE;
    private static NativeLibrary DWM = null;

	public static BufferedImage capture(HWND hwnd, Rectangle bounds) {
		if(HearthHelper.getOSName().equals("win")){
			return _capture(hwnd, bounds);
		}
		
		if(robot == null){
			try {
				robot = new Robot();
			} catch (AWTException e) { }
		}
		
		if(robot != null){
			return robot.createScreenCapture(bounds);
		}
		
		return null;
	}
	
	public static boolean isAeroEnabled(){
	    if( HearthHelper.getOSName().equals("win") && DWM == null){
	    	try{
	    		DWM = NativeLibrary.getInstance("dwmapi");
	    	} catch(Throwable e) {}
	    }
	    
	    boolean dwmEnabled = false;
	    
        if(DWM != null){
        	boolean[] bool = { false };
        	Object[] args = { bool };
        	Function DwmIsCompositionEnabled = DWM.getFunction("DwmIsCompositionEnabled");
        	HRESULT result = (HRESULT) DwmIsCompositionEnabled.invoke(HRESULT.class, args);
        	boolean success = result.intValue()==0;
        	
        	if(success && bool[0]){
        		dwmEnabled = true;
        	}
        }  

	    return dwmEnabled;
	}
	
	public BufferedImage getScreenshot(HWND hwnd)
	  {
	    RECT winRect = new RECT();
	    USER.GetWindowRect(hwnd, winRect);
	    HDC windowDC = GDI.GetDC(hwnd);
	    Rectangle bounds = winRect.toRectangle();
	    HBITMAP outputBitmap = GDI.CreateCompatibleBitmap(windowDC, bounds.width, bounds.height);
	    try
	    {
	      HDC blitDC = GDI.CreateCompatibleDC(windowDC);
	      try
	      {
	        HANDLE oldBitmap = GDI.SelectObject(blitDC, outputBitmap);
	        try
	        {
	          USER.RedrawWindow(hwnd, null, null, 0x0400 | 0x0001 | 0x0004 | 0x0100 | 0x0080);
	          boolean success = USER.PrintWindow(hwnd, blitDC, 1);
	          if (!success)
	          {
	           	System.out.println("Screen capture Failed: " + Kernel32.INSTANCE.GetLastError());
	          }
	        }
	        finally
	        {
	        	GDI.SelectObject(blitDC, oldBitmap);
	        }
	        BITMAPINFO bi = new BITMAPINFO(40);
	        bi.bmiHeader.biSize = 40;
	        boolean ok = GDI.GetDIBits(blitDC, outputBitmap, 0, bounds.height, (byte[]) null, bi,
	        		WinGDI.DIB_RGB_COLORS);
	        if (ok)
	        {
	        	   WinGDI.BITMAPINFOHEADER bih = bi.bmiHeader;
                   bih.biHeight = -Math.abs(bih.biHeight);
                   bi.bmiHeader.biCompression = 0;
                   return bufferedImageFromBitmap(blitDC, outputBitmap, bi);
	        }
	        else
	        {
	          return null;
	        }
	      }
	      finally
	      {
	        GDI.DeleteObject(blitDC);
	      }
	    }
	    finally
	    {
            GDI.DeleteObject(outputBitmap);
            GDI.DeleteObject(windowDC);
	    }
	  }
	
	public static HWND FindWindow(String title, String classname){
		return USER.FindWindow(title, classname);
	}
	
	public static BufferedImage _capture2(Rectangle bounds) {
		//RDW_FRAME | RDW_INVALIDATE | RDW_ERASE | RDW_UPDATENOW | RDW_ALLCHILDREN 
		int flags = 0x0400 | 0x0001 | 0x0004 | 0x0100 | 0x0080;
		
		HWND tagetHwnd = User32.INSTANCE.FindWindow("Hearthstone", "UnityWndClass");
		
		HDC windowDC = GDI.GetDC(tagetHwnd);
        HBITMAP outputBitmap = GDI.CreateCompatibleBitmap(windowDC, bounds.width, bounds.height);
        
        try{
        	HDC blitDC = GDI.CreateCompatibleDC(windowDC);
        	try{
                HANDLE oldBitmap =
                        GDI.SelectObject(blitDC, outputBitmap);
                
                //boolean success1 = USER.RedrawWindow(tagetHwnd, null, null, flags);
                //System.out.println("RedrawWindow: " + success1);
                
                boolean success2 = USER.PrintWindow(tagetHwnd, blitDC, 0);
                
                System.out.println("PrintWindow: " + success2);
                
                if(success2){
                	GDI.SelectObject(blitDC, oldBitmap);
                	
                	WinGDI.BITMAPINFO bi = new WinGDI.BITMAPINFO(40);
                	bi.bmiHeader.biSize = 40;
                	boolean ok =
                	         GDI.GetDIBits(blitDC, outputBitmap, 0, bounds.height,
                	         (byte[]) null, bi, WinGDI.DIB_RGB_COLORS);
                	if (ok) {
                		WinGDI.BITMAPINFOHEADER bih = bi.bmiHeader;
                		bih.biHeight = -Math.abs(bih.biHeight);
                		bi.bmiHeader.biCompression = 0;
                		return bufferedImageFromBitmap(blitDC, outputBitmap, bi);
                	} else {
                		return null;
                	}
                }
        	} finally{
        		GDI.DeleteObject(blitDC);
        	}
        } finally {
            GDI.DeleteObject(outputBitmap);
            GDI.DeleteObject(windowDC);
        }
		return null;
	}

    public static BufferedImage _capture(HWND hwnd, Rectangle bounds) {
        HDC windowDC = null;
        
        if(hwnd == null){
        	windowDC = GDI.GetDC(USER.GetDesktopWindow());
        } else {
        	windowDC = GDI.GetDC(hwnd);
        }
        
        HBITMAP outputBitmap =
                GDI.CreateCompatibleBitmap(windowDC,
                bounds.width, bounds.height);
       
        //boolean dwmEnabled = isAeroEnabled();

        try {
            WinDef.HDC blitDC = GDI.CreateCompatibleDC(windowDC);
            try {
                WinNT.HANDLE oldBitmap =
                        GDI.SelectObject(blitDC, outputBitmap);
                try {
                    GDI.BitBlt(blitDC,
                            0, 0, bounds.width, bounds.height,
                            windowDC,
                            bounds.x, bounds.y,
                            GDI32.SRCCOPY);
                } finally {
                    GDI.SelectObject(blitDC, oldBitmap);
                }
                WinGDI.BITMAPINFO bi = new WinGDI.BITMAPINFO(40);
                bi.bmiHeader.biSize = 40;
                boolean ok =
                        GDI.GetDIBits(blitDC, outputBitmap, 0, bounds.height,
                        (byte[]) null, bi, WinGDI.DIB_RGB_COLORS);
                if (ok) {
                    WinGDI.BITMAPINFOHEADER bih = bi.bmiHeader;
                    bih.biHeight = -Math.abs(bih.biHeight);
                    bi.bmiHeader.biCompression = 0;
                    return bufferedImageFromBitmap(blitDC, outputBitmap, bi);
                } else {
                    return null;
                }
            } finally {
                GDI.DeleteObject(blitDC);
            }
        } finally {
            GDI.DeleteObject(outputBitmap);
            GDI.DeleteObject(windowDC);
        }
    }

    private static BufferedImage bufferedImageFromBitmap(WinDef.HDC blitDC,
            WinDef.HBITMAP outputBitmap,
            WinGDI.BITMAPINFO bi) {
        WinGDI.BITMAPINFOHEADER bih = bi.bmiHeader;
        int height = Math.abs(bih.biHeight);
        final ColorModel cm;
        final DataBuffer buffer;
        final WritableRaster raster;
        int strideBits =
                (bih.biWidth * bih.biBitCount);
        int strideBytesAligned =
                (((strideBits - 1) | 0x1F) + 1) >> 3;
        final int strideElementsAligned;
        switch (bih.biBitCount) {
            case 16:
                strideElementsAligned = strideBytesAligned / 2;
                cm = new DirectColorModel(16, 0x7C00, 0x3E0, 0x1F);
                buffer =
                        new DataBufferUShort(strideElementsAligned * height);
                raster =
                        Raster.createPackedRaster(buffer,
                        bih.biWidth, height,
                        strideElementsAligned,
                        ((DirectColorModel) cm).getMasks(),
                        null);
                break;
            case 32:
                strideElementsAligned = strideBytesAligned / 4;
                cm = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF);
                buffer =
                        new DataBufferInt(strideElementsAligned * height);
                raster =
                        Raster.createPackedRaster(buffer,
                        bih.biWidth, height,
                        strideElementsAligned,
                        ((DirectColorModel) cm).getMasks(),
                        null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported bit count: " + bih.biBitCount);
        }
        final boolean ok;
        switch (buffer.getDataType()) {
            case DataBuffer.TYPE_INT: {
                int[] pixels = ((DataBufferInt) buffer).getData();
                ok = GDI.GetDIBits(blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0);
            }
            break;
            case DataBuffer.TYPE_USHORT: {
                short[] pixels = ((DataBufferUShort) buffer).getData();
                ok = GDI.GetDIBits(blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0);
            }
            break;
            default:
                throw new AssertionError("Unexpected buffer element type: " + buffer.getDataType());
        }
        if (ok) {
            return new BufferedImage(cm, raster, false, null);
        } else {
            return null;
        }
    }
}

interface GDI32 extends com.sun.jna.platform.win32.GDI32,
        com.sun.jna.platform.win32.WinGDI,
        com.sun.jna.platform.win32.WinDef {

    GDI32 INSTANCE =
            (GDI32) Native.loadLibrary(GDI32.class);

    boolean BitBlt(HDC hdcDest, int nXDest, int nYDest,
            int nWidth, int nHeight, HDC hdcSrc,
            int nXSrc, int nYSrc, int dwRop);

    HDC GetDC(HWND hWnd);

    boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines,
            byte[] pixels, BITMAPINFO bi, int usage);

    boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines,
            short[] pixels, BITMAPINFO bi, int usage);

    boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines,
            int[] pixels, BITMAPINFO bi, int usage);
    int SRCCOPY = 0xCC0020;
}

interface User32 extends com.sun.jna.platform.win32.User32 {

    User32 INSTANCE = (User32) Native.loadLibrary(User32.class, W32APIOptions.UNICODE_OPTIONS);

    HWND GetDesktopWindow();
    boolean PrintWindow(HWND hwnd, HDC hdcBlt, int nFlags);
    boolean RedrawWindow(HWND hwnd, RECT lprcUpdate, HRGN hrgnUpdate, int flags);
    HWND FindWindow(String lpClassName, String lpWindowName);
}

interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {

	Kernel32 INSTANCE = (Kernel32) Native.loadLibrary(Kernel32.class);

	int GetLastError();
}
