package de.l3s.sz.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class ProfileSelector {
	Hashtable<String, Boolean> usersclass = new Hashtable<>();

	public static void main(String[] args) {
		ProfileSelector p = new ProfileSelector();
		try {
			p.readProfiles();

			p.split(new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/"), "profiles.csv", "poor.csv", "reach.csv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ok");
	}

	private void split(File dir, String infilestr, String outfile1str, String outfile2str) throws IOException {
		File infile=new File(infilestr);
	
		
		
CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter('\t').withQuote(null);
		
	
		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(dir,infilestr)),
				StandardCharsets.UTF_8);
		CSVParser parser = new CSVParser(ir, format);
		List<CSVRecord> towork = new ArrayList<>();

		List<CSVRecord> class1=new ArrayList<>();
		List<CSVRecord> class2=new ArrayList<>();
		
		BufferedWriter writer1 = Files.newBufferedWriter(Paths.get(new File(dir,outfile1str).toString()));
		BufferedWriter writer2 = Files.newBufferedWriter(Paths.get(new File(dir,outfile2str).toString()));
		
		String[] header=new String[parser.getHeaderMap().keySet().size()];
		int i=0;
		for(String k:parser.getHeaderMap().keySet())
		{
			header[i++]=k;
		}
				
			

        CSVPrinter csvPrinter1 = new CSVPrinter(writer1,CSVFormat.EXCEL.withDelimiter('\t').withQuote(null).withHeader(header));
        
        CSVPrinter csvPrinter2 = new CSVPrinter(writer2, CSVFormat.EXCEL.withDelimiter('\t').withQuote(null).withHeader(header));
    
		HashSet<String> authors=new HashSet<>();
		
		for (CSVRecord record : parser) {
			String screenname = (record.get("screenname")).trim();
	String namelink="https://twitter.com/"+screenname;
	namelink=namelink.toLowerCase();

			
	if(usersclass.get(namelink)==null){continue;}
	
	authors.add(namelink);
	
			if(usersclass.get(namelink))
			{
				class1.add(record);
				csvPrinter1.printRecord(record);   
			}else
			{
				class2.add(record);
				csvPrinter2.printRecord(record); 
			}
		}
		System.out.println("in outtables table there are "+authors.size()+" authors.");
		
		FileWriter fw=new FileWriter(new File(dir,"testauthors.csv"));
		String authorsstr=authors.toString().replaceAll(",", "\n");
		fw.write(authorsstr);
		fw.flush();
		fw.close();
	
		 csvPrinter1.flush();  
		 csvPrinter2.flush();
		 csvPrinter1.close();
		 csvPrinter2.close();
	}

	private void readProfiles() throws IOException {
		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter('\t').withQuote(null);

		File csvdir = new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/");
		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(csvdir, "TwitterAccounts_list.csv")),
				StandardCharsets.UTF_8);
		CSVParser parser = new CSVParser(ir, format);
		List<CSVRecord> towork = new ArrayList<>();

		int cntauthors=0;
		for (CSVRecord record : parser) {
			String screenname = (record.get("Profil Twitter"));
			screenname=screenname.toLowerCase();
			
			String screenclass = (record.get("0-under av; 1 - above av"));

			usersclass.put(screenname.trim(), Integer.parseInt(screenclass) > 0);
			cntauthors++;
		}
		System.out.println("in the first table there are "+cntauthors+" authors.");

	}

}
