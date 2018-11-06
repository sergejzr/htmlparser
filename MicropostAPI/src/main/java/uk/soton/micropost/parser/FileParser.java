package uk.soton.micropost.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FileParser {
	public static void main(String[] args) {
		FileParser fp = new FileParser();
	
		String foldername="tweetsdl";
			fp.parseZipExtern(new File("/home/zerr/" + foldername + "zip/"),
					new File("/media/ssddrive/brexittweets/" + foldername + ".csv"),
							new File("cleartmp.sh"), new File("/media/ssdrive/tmp/"));

		int j = 0;
		j++;
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

	public void parseZipExtern(File indirectory, File csvout, File clearscript,File tmpdirectory) {
		idx.clear();
		try {
			FileWriter fw = new FileWriter(csvout);

			String dir = tmpdirectory.getAbsoluteFile().getAbsolutePath();

			boolean first = true;

			Stack<File> s = new Stack<>();
			s.add(indirectory);
			int cnt = 0;
			while (!s.empty()) {
				File f = s.pop();

				if (f.isDirectory()) {
					s.addAll(Arrays.asList(f.listFiles()));
					continue;
				}
				if (!f.getName().endsWith(".tar.gz")) {
					continue;
				}

				String line;

				String[] args;
				Process p = Runtime.getRuntime().exec(args = new String[] { "tar", "zxvf", f.toString(), "-C" + dir }
				// line="tar zxvf \""+fname+"\" -C "+dir+""

				);
				System.out.println("parse " + f.toString() + " still " + s.size() + " todo, done: " + cnt++);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
				parseAggregateDir(new File(dir), fw);

				p = Runtime.getRuntime()
						.exec(new String[] { "sh", clearscript.getCanonicalFile().getAbsolutePath()});
				br = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}

			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.gc();
		System.runFinalization();

	}

	public void parseAggregateDir(File directory, FileWriter fw) {
		boolean first = true;
		try {
			Stack<File> s = new Stack<>();
			s.add(directory);
			while (!s.empty()) {
				File f = s.pop();

				if (f.isDirectory()) {
					s.addAll(Arrays.asList(f.listFiles()));
					continue;
				}
				if (!f.getName().endsWith(".html")) {
					continue;
				}
				FileInputStream fis;
				java.util.List<Hashtable<String, String>> res = run(fis = new FileInputStream(f));

				fis.close();
				addRecordAsynchron(res, fw);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	private java.util.List<Hashtable<String, String>> run(InputStream input) {
		// File input = new File("/home/zerr/brexittweets/0_Thu Jan 01 1970
		// 00-00-00 GMT+0000 (BST)_brexit.html");
		java.util.List<Hashtable<String, String>> ret = new ArrayList<>();
		try {
			Document doc = Jsoup.parse(input, "UTF-8", "https://twitter.com");
			Elements tweetelements = doc.getElementsByAttribute("data-item-id");

			for (Element tweetelement : tweetelements) {
				Hashtable<String, String> parsed = new Hashtable<>();

				Elements header = tweetelement.getElementsByAttribute("data-tweet-id");
				String tweetid = tweetelement.attr("data-item-id").trim();
				parsed.put("tweetid", tweetid);
				String retweetid = header.attr("data-retweet-id").trim();
				parsed.put("retweetid", retweetid);
				String retweeter = header.attr("data-retweeter").trim();
				parsed.put("retweeter", retweeter);
				String userid = header.attr("data-user-id").trim();
				parsed.put("userid", userid);
				String screenname = header.attr("data-screen-name").trim();
				parsed.put("screenname", screenname);
				String name = header.attr("data-name").trim();
				parsed.put("name", name);

				Elements timelements = tweetelement.getElementsByAttribute("data-time-ms");
				String longtimestr = "";
				if (timelements.size() > 0) {
					Element timelement = timelements.get(0);
					longtimestr = timelement.attr("data-time-ms");
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
				parsed.put("tweetext", tweetext.replaceAll("\n", " ").replaceAll("\r", "").replaceAll("\t", " "));
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
					geostring = geocollection.get(0).attr("title").replaceAll("\t", " ");
				}
				parsed.put("geostring", geostring);

				ret.add(parsed);
			}
			return ret;
		} catch (IOException e) {
			// TODO Auto-generated forcatch block
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
}
