package uk.soton.micropost;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ExtractUsersUSA {
	// q=%23illary%20OR%20%23alwaystrump%20OR%20%23election%20OR%20%23neverhillary%20OR%20%23hillaryclinton%20OR%20%23hillaryforprison2016%20OR%20%23crookedhillary%20OR%20%23conservative%20OR%20%23lockherup%20OR%20%23patriot%20OR%20%23libtards%20OR%20%23trump%20
	//
	// pos -> fortrump
	public static final HashSet<String> postags = new HashSet<>(Arrays
			.asList("killary,crookedhilary,corrupthillary,womenfortrump,trumpforpresident,trumppresident,otetrump,presidenttrump,makeamericagreatagain,hillyes,illary,imwithhim,alwaystrump,neverhillary,hillaryforprison,hillaryforprison2016,crookedhillary,lockherup"
					.split(",")));
	// &q=%23nevertrump%20OR%20%23hillaryforprison%20OR%20%23clinton%20OR%20%23hillyes%20OR%20%23hillaryclinton2016%20OR%20%23notmypresident%20OR%20%23imwithhim%20OR%20%23stillwithher%20OR%20%23dumptrump%20OR%20%23vote%20
	public static final HashSet<String> negtags = new HashSet<>(Arrays
			.asList("imwither,notmypresidentelect,gohillary,iamwithher,imwithher2016,hillaryclintonforpresident,fucktrump,hesnotmypresident,hilarious,nevertrump,notmypresident,imwithher,stillwithher,dumptrump"
					.split(",")));
	public static final List<String> stoppnames = Arrays
			.asList("infowars,republican,hillary,romney,democrat,conservati,usa_2016,electionday,election,trump,hillaryclinton2016,clinton,vote,elections2016"
					.split(","));
	public static void main(String[] args) {
		ExtractUsersUSA eu = new ExtractUsersUSA();
		try {
			eu.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void test() throws IOException {

		Hashtable<String, Integer> unknowntags = new Hashtable<>();
		HashSet<String> toprintid = new HashSet<>(Arrays.asList("Juliet777777".split(",")));
	

		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter('\t').withQuote(null);
		File csvdir = new File("/media/zerr/BA0E0E3E0E0DF3E3/brexittweets");
		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(csvdir, "elections.csv")),
				StandardCharsets.UTF_8);
		CSVParser parser = new CSVParser(ir, format);
		List<CSVRecord> towork = new ArrayList<>();
		Hashtable<String, PolarScore> polar = new Hashtable<>();

		for (CSVRecord record : parser) {
			String screenname = (record.get("screenname"));
			PolarScore score = polar.get(screenname);
			if (score == null) {
				polar.put(screenname, score = new PolarScore());
			}

			String text = record.get("tweetext");

			if (toprintid.contains(screenname)) {
				// System.out.println(text);
			}
			Pattern MY_PATTERN = Pattern.compile("#([\\w\\d]+)");
			Matcher mat = MY_PATTERN.matcher(text.toLowerCase());
			score.addtweet();
			while (mat.find()) {
				String hashtag = mat.group(1);
				if (postags.contains(hashtag)) {
					score.addPosTag(hashtag);

				} else if (negtags.contains(hashtag)) {
					score.addNegTag(hashtag);

				} else {
					Integer cnt = unknowntags.get(hashtag);
					if (cnt == null)
						cnt = 0;
					unknowntags.put(hashtag, cnt + 1);
				}
				// System.out.println(mat.group(1));
				// strs.add(mat.group(1));
			}

			;

		}

		for (PolarScore score : polar.values()) {
			int p = score.postags.size();
			int n = score.negtags.size();
			double div = Math.abs(p - n) * 1. / (p + n);

			if (p > n)
				score.setScore(div * p * score.cnttweets);
			else if (p < n)
				score.setScore(-div * n * score.cnttweets);
			else
				score.setScore(0);
		}

		List<String> posusers = new ArrayList<>();
		posusers.addAll(polar.keySet());

		Collections.sort(posusers, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {

				return Double.compare(polar.get(o1).getScore(), polar.get(o2).getScore());
			}

		});
		System.out.println("neg:");
		StringBuilder posquery = new StringBuilder();
		StringBuilder negquery = new StringBuilder();
		int top = 200;
		int cntq = 0;
		for (int i = 50; i < top; i++) {
			String userid = posusers.get(i);
			if (!isvalidname(userid, stoppnames))
				continue;

			System.out.println(userid + " " + polar.get(userid).getScore());
			if (cntq++ > 50)
				continue;
			if (negquery.length() > 0)
				negquery.append(" ");
			negquery.append(userid);
		}
		cntq = 0;
		System.out.println("pos:");
		for (int i = 50; i < top; i++) {
			String userid = posusers.get(posusers.size() - i - 1);
			if (!isvalidname(userid, stoppnames))
				continue;
			if (cntq++ > 50)
				continue;
			System.out.println(userid + " " + polar.get(userid).getScore());
			if (posquery.length() > 0)
				posquery.append(" ");
			posquery.append(userid);
		}

		List<String> othertags = new ArrayList<>();
		othertags.addAll(unknowntags.keySet());

		Collections.sort(othertags, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {

				return unknowntags.get(o1).compareTo(unknowntags.get(o2));
			}
		});
		// Collections.reverse(othertags);
		System.out.println("unknowntags");
		for (int i = 0; i < top + 500; i++) {
			String tag = othertags.get(othertags.size() - i - 1);
			System.out.println(tag + " " + unknowntags.get(tag));
		}
		System.out.println(wrap("posquery", posquery));
		System.out.println(wrap("negquery", negquery));
		// takecontrol votein votedremain remainin leavevote ivotedremain
		// bregret nobrexit voteleavetakecontrol remainineu strongertogether
		// bremain ivotedleave takebackcontrol intogether bettertogether voteout
	}

	private String wrap(String string, StringBuilder posquery) {

		List<StringBuilder> queries = new ArrayList<>();

		StringBuilder ret = new StringBuilder();
		ret.append(string + ": \n");
		StringBuilder curquery=new StringBuilder();
		queries.add(curquery);
		int cnt = 0;
		for (String s : posquery.toString().split("\\s+")) {
			if (cnt++ > 12) {
				cnt = 0;
				curquery=new StringBuilder();
				queries.add(curquery);
			}
			if(curquery.length()>0) curquery.append(" ");
			curquery.append(s);
		}
		cnt=1;
for(StringBuilder sb:queries)
{
	ret.append(string+cnt+++" "+sb.toString()+"\n");
}
		return ret.toString();
	}

	private boolean isvalidname(String userid, List<String> stoppnames) {
		for (String uname : stoppnames) {
			if (userid.toLowerCase().contains(uname))
				return false;
		}
		return true;
	}
}
