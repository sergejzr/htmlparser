package de.l3s.sz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BatchProcessor {
	File dir;
	private FileWriter fw;

	public BatchProcessor(File dir, File csvout) throws IOException {
		this.dir = dir;
		fw = new FileWriter(csvout);
	}

	public static void main(String[] args) {
		BatchProcessor bp;
		try {
			bp = new BatchProcessor(new File("/home/zerr/tweetsdl/tweettest/"),new File("/home/zerr/tweetsdl/tweettest/profiles.csv"));
			ArrayList<Hashtable<String, String>> items = bp.extractURLS();
			bp.addRecordAsynchron(items,bp.fw);
			// System.out.println(items);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 

	}

	private ArrayList<Hashtable<String, String>> extractURLS() {
		ArrayList<File> jsons = new ArrayList<>();
		ArrayList<Hashtable<String, String>> ret=new ArrayList<>();
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".html")) {
				jsons.add(f);
			}
		}
		HashSet<String> idx=new HashSet<>();
		for (File f : jsons) {
			
			try {
				Document doc = Jsoup.parse(f, "UTF-8", "https://twitter.com");
				Elements tweetelements = doc.getElementsByAttribute("data-item-id");

				
				
				
				for (Element tweetelement : tweetelements) {
					Hashtable<String, String> parsed = new Hashtable<>();

					Elements header = tweetelement.getElementsByAttribute("data-tweet-id");
					String tweetid = tweetelement.attr("data-item-id").trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
					if(idx.contains(tweetid))
					{
						continue;
					}
					
					idx.add(tweetid);
					parsed.put("tweetid", tweetid);
					String retweetid = header.attr("data-retweet-id").trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
					parsed.put("retweetid", retweetid);
					String retweeter = header.attr("data-retweeter").trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
					parsed.put("retweeter", retweeter);
					String userid = header.attr("data-user-id").trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
					parsed.put("userid", userid);
					if(userid.toLowerCase().startsWith("eva"))
					{
						int t=0;
						t++;
					}
					String screenname = header.attr("data-screen-name").trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
					parsed.put("screenname", screenname);
					String name = header.attr("data-name").trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
					parsed.put("name", name);

					Elements timelements = tweetelement.getElementsByAttribute("data-time-ms");
					String longtimestr = "";
					if (timelements.size() > 0) {
						Element timelement = timelements.get(0);
						longtimestr = timelement.attr("data-time-ms").replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
						parsed.put("tweettime", longtimestr);
						Date d = new Date(Long.parseLong(longtimestr));

						String pattern = "dd.MM.yy HH:mm:s";
						SimpleDateFormat format = new SimpleDateFormat(pattern);
						String ds = format.format(d);
						parsed.put("tweetdatetime", ds);
					}
					if (tweetid.equals("756592226768155000")) {
						int y = 0;
						y++;
					}
					if (tweetelement.getElementsByClass("js-tweet-text-container").size() == 0) {
						continue;
					}
					String tweetext = tweetelement.getElementsByClass("js-tweet-text-container").get(0).text();
					if(tweetext.startsWith("Mami"))
					{
						int z=0;
						z++;
					}
					parsed.put("tweetext", tweetext.replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ").replaceAll("\"", "&#8243;"));
					Element replyelement = tweetelement.getElementsByClass("ProfileTweet-action--reply").get(0);
					replyelement = replyelement.getElementsByAttribute("data-tweet-stat-count").get(0);
					String replystr = replyelement.attr("data-tweet-stat-count");
					parsed.put("replystr", replystr);
					Element retweetelement = tweetelement.getElementsByClass("ProfileTweet-action--retweet").get(0);
					retweetelement = replyelement.getElementsByAttribute("data-tweet-stat-count").get(0);
					String retweetstr = retweetelement.attr("data-tweet-stat-count");
					parsed.put("reply", retweetstr);
					Element likeelement = tweetelement.getElementsByClass("ProfileTweet-action--favorite").get(0);
					likeelement = replyelement.getElementsByAttribute("data-tweet-stat-count").get(0);
					String likestr = likeelement.attr("data-tweet-stat-count");
					parsed.put("likestr", likestr);

					Elements geocollection = tweetelement.getElementsByClass("Tweet-geo");
					String geostring = "";
					if (geocollection.size() > 0) {
						geostring = geocollection.get(0).attr("title").replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " ");
					}
					parsed.put("geostring", geostring);

					ret.add(parsed);
				}
			
			} catch (IOException e) {
				// TODO Auto-generated forcatch block
				e.printStackTrace();
			}
		
		}
		

		return ret;
	}
	HashSet<String> idx = new HashSet<>();
	private void addRecordAsynchron(List<Hashtable<String, String>> run, FileWriter fw) throws IOException {

		if (idx.isEmpty()) {
			for (Hashtable<String, String> entry : run) {
				for (String key : entry.keySet()) {
					fw.write(key);
					fw.write("\t");
				}
				fw.write("\n");
				break;
			}
		}
		for (Hashtable<String, String> entry : run) {
			if (idx.contains(entry.get("tweetid"))) {
				continue;
			}
			idx.add(entry.get("tweetid"));
			for (String key : entry.keySet()) {
				fw.write(entry.get(key));
				fw.write("\t");

			}
			fw.write("\n");
		}
		fw.flush();

	}
}
