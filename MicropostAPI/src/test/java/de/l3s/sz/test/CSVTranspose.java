package de.l3s.sz.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class CSVTranspose {
	public CSVTranspose(String[] args) {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		CSVTranspose csv = new CSVTranspose(args);
		try {
			csv.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void run() throws IOException {

		FileReader tfr = new FileReader("/home/zerr/tmp/output-topic-keys_100.keys");

		BufferedReader br = new BufferedReader(tfr);
		String line = null;
		Vector<String> topics = new Vector<>();

		while (null != (line = br.readLine())) {
			if (line.trim().startsWith("#")) {
				continue;
			}
			String linearr[] = line.split("\t");

			topics.add(linearr[2]);
		}
		br.close();

		FileReader fr;

		fr = new FileReader(
				"/home/zerr/tmp/personaltweets_infered_100/tweetsperpersongrouped/A_avni_O_100_infered.txt");
		br = new BufferedReader(fr);
		FileWriter fw = new FileWriter("/home/zerr/tmp/out.txt");

		line = null;

		try {
			int cntlines = 0;
			ArrayList<String[]> lines = new ArrayList<>();
			while (null != (line = br.readLine())) {
				if (line.trim().startsWith("#")) {
					continue;
				}
				String[] linearr = line.split("\t");
				Double sum = 0.;
				for (int i = 2; i < linearr.length; i++) {
					sum += Double.parseDouble(linearr[i]);
				}

				linearr[0] = sum + "";
				lines.add(linearr);

			}

			Collections.sort(lines, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return (int) (Double.parseDouble(o1[0]) - Double.parseDouble(o2[0]));
				}
			});

			
			fw.write("\t");

			
			for (int i = 0; i < lines.size(); i++) {

				fw.write(lines.get(i)[1]);
				fw.write("\t");

			}
			fw.write("\n");
			
			
			for (int y = 0; y < topics.size(); y++) {
				fw.write(topics.get(y));
				fw.write("\t");

			
				for (int i = 0; i < lines.size(); i++) {

					fw.write(lines.get(i)[y + 2]);
					fw.write("\t");

				}
				fw.write("\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
