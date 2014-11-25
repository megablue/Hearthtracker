package my.hearthtracking.app;

import java.awt.image.BufferedImage;

public class HearthScanResult {
	public String scene;
	public String result;
	public float score;
	public BufferedImage match = null;
	public long time = 0;
	public long expire = 0;
	public long frameCount = 0;
	
	public HearthScanResult(String s, String r, float scr, long frameN){
		scene = s;
		result = r;
		score = scr;
		time = System.currentTimeMillis();
		frameCount = frameN;
	}

	public void setTime(){
		time = System.currentTimeMillis();
	}
	
	public void setTime(long t){
		this.time = t;
	}
	
	public long getTime(){
		return this.time;
	}

	public void setExpiry(long t){
		this.expire = System.currentTimeMillis() + t;
	}
	
	public boolean isExpired(){
		 return (System.currentTimeMillis() > expire);
	}
	
	public boolean isExpired(long time){
		 return (time > expire);
	}
}
