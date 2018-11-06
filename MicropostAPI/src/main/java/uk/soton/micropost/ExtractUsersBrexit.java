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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ExtractUsersBrexit {
//	 votein votedremain remainin leavevote ivotedremain bregret nobrexit voteleavetakecontrol remainineu strongertogether bremain ivotedleave takebackcontrol intogether bettertogether voteout
	
	public static final HashSet<String>postags=new HashSet<>(Arrays.asList("bremain,nobrexit,remainineu,strongertogether,ivotedremain,votein,votedremain,intogether,voteremain,remain,votestay,strongerin".split(",")));
	
	public static final HashSet<String>negtags=new HashSet<>(Arrays.asList("takebackcontrol,ivotedleave,voteleavetakecontrol,voteleave,leave,leaveeu,voteleaveeu,britainout,voteout".split(",")));
	
	 public static void main(String[] args) {
		ExtractUsersBrexit eu = new ExtractUsersBrexit();
		try {
			eu.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void test() throws IOException {
		
		Hashtable<String, Integer> unknowntags=new Hashtable<>();
		HashSet<String>toprintid=new HashSet<>(Arrays.asList("Juliet777777".split(",")));
		List<String> stoppnames = Arrays.asList("4eu,media,conserva,social,deal,party,labour,separat,euro,britain,brexit,union,vote,cameron,remain,stop,democra,magazin,news,answer,analysi,strongerin,script".split(","));
		
		
		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter('\t').withQuote(null);
		File csvdir = new File("/media/zerr/BA0E0E3E0E0DF3E3/brexittweets");
		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(csvdir, "britainout.csv")),
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

			if(toprintid.contains(screenname))
			{
				//System.out.println(text);
			}
			Pattern MY_PATTERN = Pattern.compile("#([\\w\\d]+)");
			Matcher mat = MY_PATTERN.matcher(text.toLowerCase());
			score.addtweet();
			while (mat.find()) {
				String hashtag = mat.group(1);
				if(postags.contains(hashtag))
				{
					score.addPosTag(hashtag);
					
				} else 	if(negtags.contains(hashtag))
				{
					score.addNegTag(hashtag);
					
				} else
				{
					Integer cnt = unknowntags.get(hashtag);
					if(cnt==null) cnt=0;
					 unknowntags.put(hashtag, cnt+1);
				}
				// System.out.println(mat.group(1));
				//strs.add(mat.group(1));
			}

			;

		}
		
for (PolarScore score : polar.values()) {
	int p=score.postags.size();
	int n=score.negtags.size();
	double div = Math.abs(p - n)*1. / (p + n);
	
	if(p>n) score.setScore(div*p*score.cnttweets);
	else
	if(p<n) score.setScore(-div*n*score.cnttweets);
	else
		score.setScore(0);
}
		
		List<String> posusers=new ArrayList<>();
		posusers.addAll(polar.keySet());
		
		Collections.sort(posusers, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				
				return Double.compare(polar.get(o1).getScore(),polar.get(o2).getScore());
			}
			
		});
		System.out.println("neg:");
		StringBuilder posquery=new StringBuilder();
		StringBuilder negquery=new StringBuilder();
int top=200;
int cntq=0;
		for(int i=50;i<top;i++)
		{
			String userid = posusers.get(i);
			if(!isvalidname(userid,stoppnames)) continue;
			
			System.out.println(userid+" "+polar.get(userid).getScore());
		if(cntq++>50) continue;
			if(negquery.length()>0) negquery.append(" ");
			negquery.append(userid);
		}
		cntq=0;
		System.out.println("pos:");
		for(int i=50;i<top;i++)
		{
			String userid = posusers.get(posusers.size()-i-1);
			if(!isvalidname(userid,stoppnames)) continue;
			if(cntq++>50) continue;
			System.out.println(userid+" "+polar.get(userid).getScore());
			if(posquery.length()>0) posquery.append(" ");
			posquery.append(userid);
		}
		
		
		List<String> othertags=new ArrayList<>();
		othertags.addAll(unknowntags.keySet());
		
		Collections.sort(othertags,new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				
				return unknowntags.get(o1).compareTo(unknowntags.get(o2));
			}
		});
		//Collections.reverse(othertags);
		System.out.println("unknowntags");
		for(int i=0;i<top+500;i++)
		{
			String tag = othertags.get(othertags.size()-i-1);
			System.out.println(tag+" "+unknowntags.get(tag));
		}
		System.out.println("posquery: "+posquery);
		System.out.println("negquery: "+negquery);
		//takecontrol votein votedremain remainin leavevote ivotedremain bregret nobrexit voteleavetakecontrol remainineu strongertogether bremain ivotedleave takebackcontrol intogether bettertogether voteout
	}

	private boolean isvalidname(String userid, List<String> stoppnames) {
		for(String uname:stoppnames)
		{
			if(userid.toLowerCase().contains(uname)) return false;
		}
		return true;
	}
}
