package de.l3s.sz.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class TweetsPerPerson {
	Hashtable<String, Boolean> usersclass = new Hashtable<>();

	public static void main(String[] args) {
		TweetsPerPerson p = new TweetsPerPerson();
		try {
			p.readProfiles();

			p.split(new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/fullprofiles_clean/"), "profiles.txt",
					new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/tweetsperperson"),
					new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/tweetsperpersongrouped"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ok");
	}

	private void split(File dir, String infilestr, File outdir, File outdirgrouped) throws IOException {
		outdir.mkdirs();
		outdirgrouped.mkdirs();

		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter('\t').withQuote(null);

		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(dir, infilestr)),
				StandardCharsets.UTF_8);
		CSVParser parser = new CSVParser(ir, format);

		Hashtable<String, ArrayList<CSVRecord>> tweets = new Hashtable<>();

		for (CSVRecord record : parser) {
			String screenname = (record.get("screenname")).trim();
			String namelink = "https://twitter.com/" + screenname;
			namelink = namelink.toLowerCase();

			if (usersclass.get(namelink) == null) {
				continue;
			}

			ArrayList<CSVRecord> conti = tweets.get(screenname);
			if (conti == null) {

				tweets.put(screenname, conti = new ArrayList<>());
			}
			conti.add(record);

		}

		String[] header = new String[parser.getHeaderMap().keySet().size()];
		int h = 0;
		for (String k : parser.getHeaderMap().keySet()) {
			header[h++] = k;
		}

		Calendar cal = Calendar.getInstance();

		for (String screename : tweets.keySet()) {
			FileWriter fweachtweet = new FileWriter(new File(outdir, screename + ".txt"));

			Md5Crypt.apr1Crypt(screename.getBytes());
			ArrayList<CSVRecord> conti = tweets.get(screename);

			ArrayList<CSVRecord> sorted = new ArrayList<>();
			sorted.addAll(conti);
			Collections.sort(sorted, new Comparator<CSVRecord>() {

				@Override
				public int compare(CSVRecord o1, CSVRecord o2) {
					try {
						Date date1 = getTwitterDate(o1.get("tweetdatetime"));
						Date date2 = getTwitterDate(o2.get("tweetdatetime"));
						return date1.before(date2) ? -1 : date1.after(date2) ? 1 : 0;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return 0;
				}
			});

			

			FileWriter fw = new FileWriter(new File(outdirgrouped, screename + ".txt"));
			Date batchdate;
			
			for (int i = 0; i < sorted.size();) {

				CSVRecord currecord = sorted.get(i);

				try {
					Date curdate = getTwitterDay(currecord.get("tweetdatetime"));

					fweachtweet.write(currecord.get("tweetid") + "\t");
					fweachtweet.write(currecord.get("tweetext"));
					fweachtweet.write("\n");

					fw.write(getDaystring(curdate) + "\t");

					cal.setTime(curdate);
					cal.add(Calendar.DATE, 5);
					batchdate = cal.getTime();

					for (int y = i; y < sorted.size(); y++) {
						CSVRecord runtweet = sorted.get(y);
						Date rundate = getTwitterDay(runtweet.get("tweetdatetime"));
						if (rundate.equals(batchdate) || rundate.before(batchdate)) {
							fw.write(runtweet.get("tweetext") + " ");
						} else {
							break;
						}

					}
					
					cal.setTime(curdate);
					cal.add(Calendar.DATE, 1);
					Date nextday = cal.getTime();
					
					do
					{
						currecord = sorted.get(i);
						curdate = getTwitterDay(currecord.get("tweetdatetime"));
						i++;
					}while(i < sorted.size()&&curdate.before(nextday));
					
					fw.write("\n");

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
	
			fw.flush();
			fw.close();
		
			
			fweachtweet.flush();
			fweachtweet.close();
			parser.close();
		}

	}

	private String getDaystring(Date curdate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(curdate);
		int iyear = cal.get(Calendar.YEAR);
		int imonth = cal.get(Calendar.MONTH) + 1;
		int iday = cal.get(Calendar.DAY_OF_MONTH);

		String month = imonth < 10 ? ("0" + imonth) : imonth + "";
		String day = iday < 10 ? ("0" + iday) : iday + "";
		String year = (iyear + "").substring(2);
		return day + "." + month + "." + year;
	}

	private Date getTwitterDay(String date) throws ParseException {

		Date nt = getTwitterDate(date);

		return getTwitterDay(nt);
	}

	private Date getTwitterDay(Date nt) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(nt);
		int iyear = cal.get(Calendar.YEAR);
		int imonth = cal.get(Calendar.MONTH) + 1;
		int iday = cal.get(Calendar.DAY_OF_MONTH);

		String month = imonth < 10 ? ("0" + imonth) : imonth + "";
		String day = iday < 10 ? ("0" + iday) : iday + "";
		String year = (iyear + "").substring(2);

		return getTwitterDate(day + "." + month + "." + year + " 00:00:00");
	}

	private void readProfiles() throws IOException {
		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter('\t').withQuote(null);

		File csvdir = new File("/home/zerr/ownCloud/AnträgeOwncloud/RDAT/");
		InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(csvdir, "TwitterAccounts_list.csv")),
				StandardCharsets.UTF_8);
		CSVParser parser = new CSVParser(ir, format);

		int cntauthors = 0;
		for (CSVRecord record : parser) {
			String screenname = (record.get("Profil Twitter"));
			screenname = screenname.toLowerCase();

			String screenclass = (record.get("0-under av; 1 - above av"));

			usersclass.put(screenname.trim(), Integer.parseInt(screenclass) > 0);
			cntauthors++;
		}
		parser.close();
		System.out.println("in the first table there are " + cntauthors + " authors.");

	}

	public static Date getTwitterDate(String date) throws ParseException {

		final String TWITTER = "dd.MM.yy HH:mm:ss";
		SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
		sf.setLenient(true);
		return sf.parse(date);
	}
}
