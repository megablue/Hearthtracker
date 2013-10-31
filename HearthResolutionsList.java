public class HearthResolutionsList {

	public class Resolution{
		int width = 0;
		int height = 0;
		
		public Resolution(int w, int h){
			width=w;
			height=h;
		}
	}
	
	public Resolution[] resolutions = {
			new Resolution(1024,768),
			new Resolution(1152,864),
			new Resolution(1280,800),
			new Resolution(1280,960),
			new Resolution(1360,768),
			new Resolution(1360,1024),
			new Resolution(1366,768),
			new Resolution(1400,1050),
			new Resolution(1440,900),
			new Resolution(1600,900),
			new Resolution(1600,1200),
			new Resolution(1680,1050),
			new Resolution(1920,1080),
	};
}
