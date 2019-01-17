package de.l3s.sz.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class TextByUser {

	public static void main(String[] args) {
		TextByUser tbu = new TextByUser();
		try {
			tbu.run(new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/"), "poor.csv", "poor");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void run(File dir, String infilestr, String outdir1) throws IOException {
		File outdir = new File(dir, outdir1);
		outdir.mkdirs();

		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter(',').withQuote(null);

		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(dir, infilestr)),
				StandardCharsets.UTF_8);
		
		CSVParser parser = new CSVParser(ir, format);

		Hashtable<String, List<String>> texts = new Hashtable<>();

		for (CSVRecord record : parser) {
			String screenname = record.get("userid");

			List<String> list = texts.get(screenname);
			if (list == null) {
				texts.put(screenname, list = new ArrayList<>());
			}
			list.add(record.get("tweetext"));
		}
		
		for(String userid:texts.keySet())
		{
			FileWriter fw=new FileWriter(new File(new File(dir,outdir1),userid+",txt"));
			
			for(String txt:texts.get(userid))
			{
				fw.write(txt+"\n");
			}
			
			fw.close();
		}
		parser.close();

	}

}
