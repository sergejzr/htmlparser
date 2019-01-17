package de.l3s.sz.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Diagnostics {
	public Diagnostics(String[] args) {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		Diagnostics dg = new Diagnostics(args);
		try {
			dg.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
void parseFile(File f) throws IOException
{

	
	FileReader fr=new FileReader(f);
	BufferedReader br=new BufferedReader(fr);
	String line = null;
	StringBuilder sb=new StringBuilder();
	
	
	while(null !=(line=br.readLine()))
	{
		sb.append(line);
		sb.append("\n");
	}
	
	
	String txt = sb.toString().replaceAll("(<.+exclusivity=')|('.+>)", "");
	
	txt=txt.replaceAll("(<.+>)|('>)", "");
	txt=txt.replaceAll("\n\n", "\n");
	
	
	String words=sb.toString().replaceAll("(<word.+'>)", "");
	words=words.replaceAll("(<\\/word>\\n)", " ");
	words=words.replaceAll("(<.+>)", "");	
	words=words.replaceAll("\n\n", "\n").replaceAll("\n\n", "\n");
	
	ArrayList<HashSet<String>> div=new ArrayList<>();
	for(String s:txt.split("\n"))
	{
		s=s.trim();
		if(s.length()==0) {continue;}
		
		String tokens[]=s.split("\\s+");
		HashSet<String> se=new HashSet<>(Arrays.asList(tokens));
		div.add(se);
		
	}
	;
	
	Double sum=0.0;
	int cnt=0;
	for(String s:txt.split("\n"))
	{
		s=s.trim();
		if(s.length()<1){continue;}
		Double d=Double.parseDouble(s);
		sum+=d;
		cnt++;
	}
	System.out.println(f.getName()+"\t"+(sum/cnt)+"\t"+JaccardDiversity.getRDJ(div));
	
	
	fr.close();
	
	
	
}
	private void run() throws IOException {
		File dir=new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/diagnose/");
		for(File subdir:dir.listFiles())
		{
			for(File f:subdir.listFiles())
			{
				if(f.getName().endsWith(".xml"))
				{
					parseFile(f);
					
				}
			}
		}
		
	}

}
