package uk.soton.micropost.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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

import javax.naming.directory.SchemaViolationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import uk.soton.micropost.ExtractUsersBrexit;
import uk.soton.micropost.ExtractUsersUSA;

public class ExtractTextFromCSV {
	public static void main(String[] args) {

		

	//
		ExtractTextFromCSV eu = new ExtractTextFromCSV();
		//File in=null;
		//File out=null;
		//eu.extract(in, out);
	
		
		
		try {
			HashSet<String> stopptags=new HashSet<>();
			stopptags.addAll(ExtractUsersBrexit.negtags);
			stopptags.addAll(ExtractUsersBrexit.postags);
			stopptags.add("brexit");
			stopptags.addAll(ExtractUsersUSA.negtags);
			stopptags.addAll(ExtractUsersUSA.postags);
			stopptags.addAll(ExtractUsersUSA.stoppnames);
			stopptags.add("elections");
			stopptags.add("elections2016");
			HashSet<String>stoppword=new HashSet<>();
			
			stoppword.addAll(Arrays.asList("brexit,obama,hillary,clinton,brexit,trump,donald,election,democra,republic,politic,conservat".split(",")));
			
			File outdir=new File("/media/zerr/BA0E0E3E0E0DF3E3/brexittweets/textpolitics");
			outdir.mkdirs();
			
			eu.test("negbrexit.csv",outdir,"negbrexit",stopptags,stoppword);
			eu.test("posbrexit.csv",outdir,"posbrexit",stopptags,stoppword);
			
	
			
			eu.test("negusa.csv",outdir,"negusa",stopptags,stoppword);
			eu.test("posusa.csv",outdir,"posusa",stopptags,stoppword);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void test(String csvin, File outdir, String out, HashSet<String> stopptags, HashSet<String> stoppwords) throws IOException {
		
		
		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter('\t').withQuote(null);
		File csvdir = new File("/media/zerr/BA0E0E3E0E0DF3E3/brexittweets");
		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(csvdir, csvin)),
				StandardCharsets.UTF_8);
		CSVParser parser = new CSVParser(ir, format);
		outdir=new File(outdir,out);
		outdir.mkdirs();
		FileWriter fw=new FileWriter(new File(outdir,out+".txt"));
	//	FileWriter fwhashtags=new FileWriter(new File(new File(outdir,out),out+"_hash.txt"));
		
		nextrec:
		for (CSVRecord record : parser){
			
			String text=record.get("tweetext");
			String textb=text; 
			text=removeLinks(text);
			
			Pattern MY_PATTERN = Pattern.compile("#([\\w\\d]+)");
			Matcher mat = MY_PATTERN.matcher(text.toLowerCase());
			StringBuilder sb=new StringBuilder();
			while (mat.find()) {
				String hashtag = mat.group(1);
				
				if(stopptags.contains(hashtag)){continue nextrec;}
				if(sb.length()>0) sb.append(" ");
					sb.append(hashtag);
			}
			String cleantext=text.toLowerCase();
			for(String stoppword:stoppwords)
			{
				if(cleantext.contains(stoppword))
				{
					continue nextrec;
				}
			}
			
			
			if(sb.length()>0)
			{
				//fwhashtags.write(sb.toString()+"\n");
			}
			fw.write(text.replaceAll("#", "ahashtag")+"\n");
			}
		fw.flush();
		fw.close();
	//	fwhashtags.flush();
		//fwhashtags.close();
//takecontrol votein votedremain remainin leavevote ivotedremain bregret nobrexit voteleavetakecontrol remainineu strongertogether bremain ivotedleave takebackcontrol intogether bettertogether voteout
	}

	private String removeLinks(String text) {
		 String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http|):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
	        Matcher m = p.matcher(text);
	        int i = 0;
	        StringBuilder sb=new StringBuilder();
	       int curstartidx=0;
	       String rettext=text;
	        while (m.find()) {
	        	rettext = //text.replaceAll(m.group(i)," ").trim();
	        			text.substring(0,m.start())+text.substring(m.end());
	            i++;
	        }
	        text=rettext;
	        String picpattern="pic\\.twitter\\.com/[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*";
	         p = Pattern.compile(picpattern,Pattern.CASE_INSENSITIVE);
	         m = p.matcher(text);
	        
	         rettext=text;
	        while (m.find()) {
	        	rettext = //text.replaceAll(m.group(i)," ").trim();
	        			text.substring(0,m.start())+text.substring(m.end());
	            i++;
	        }
	        
	        return rettext;
		
	}

	
}
