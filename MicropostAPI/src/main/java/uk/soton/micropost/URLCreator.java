package uk.soton.micropost;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
/**
 * Based on a folder with html files constructs the search URLs to continue with.
 * @author zerr
 *
 */
public class URLCreator {

	public static void main(String[] args) {
		
	
		File indir = new File("/media/zerr/BA0E0E3E0E0DF3E3/brexittweets/");
		if(args.length>0)
		{
			indir = new File(args[0]);
		}
		URLCreator uc=new URLCreator();
		
		uc.listQueries(indir);
	
	}

	public void listQueries(File indir) {
		
		Hashtable<String, Long> minimummodified = new Hashtable<>();
		Hashtable<String, File> minfile = new Hashtable<>();

		for (File f : indir.listFiles()) {
			if (!f.getName().endsWith("html"))
				continue;
			String gmt = "_";
			if (!f.getName().contains(gmt))
				continue;
			int startidx = f.getName().indexOf(gmt) + gmt.length();
			String tag = f.getName().substring(startidx, f.getName().indexOf("_", startidx + 1));

			Long cl = minimummodified.get(tag);
			if (cl == null) {
				cl = f.lastModified();
				minfile.put(tag, f);
			}

			if (f.lastModified() > cl) {
				cl = f.lastModified();
				minfile.put(tag, f);
			}
			minimummodified.put(tag,cl);

		}
		StringBuilder QS=new StringBuilder();
		StringBuilder fnames=new StringBuilder();
		StringBuilder query=new StringBuilder();
		for(String tag:minfile.keySet())
		{
			Document doc;
			try {
				doc = Jsoup.parse(minfile.get(tag), "UTF-8");
				Elements tweetelements = doc.getElementsByTag("a");
				String href=tweetelements.get(0).attr("href");
				String tok="max_position=TWEET-";
				int idx1=href.indexOf(tok)+tok.length();
				tok=href.substring(idx1, href.indexOf("-",idx1+1));
				if(query.length()>0)query.append("&");
				query.append(tag+"="+tok);
				String[] parts = href.split("max_position=TWEET-");
QS.append("Q.unshift(\""+parts[0]+"max_position=\"+\n\t\"TWEET-"+parts[1]+"\");\n");
fnames.append(minfile.get(tag).getName()+"\n");			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		//https://ritetag.com/hashtag-stats/lightpollution
		System.out.println(query);
		System.out.println(fnames);
		System.out.println(QS);

	}
}
