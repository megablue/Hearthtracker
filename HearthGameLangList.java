
public class HearthGameLangList {
	
	public class Language{
		public String code;
		public String label;
		
		public Language (String c, String l){
			code = c;
			label = l;
		}
	}
	
	public Language[] langs = {
		new Language("enUS", "English"),
		new Language("zhTW", "繁體中文"),
	};
}
