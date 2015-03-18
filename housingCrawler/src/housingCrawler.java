import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/*
 * housingCrawler.java
 * 
 * Copyright 2015 Michael Comerford (UBDC) <michael.comerford@glasgow.ac.uk>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * 
 */

public class housingCrawler {

	/**
	 * @param args
	 */
	
	private static File log;
	private static crawlTypes crawlType;
	private static ArrayList<String> locations = new ArrayList<String>();
	private static String locString;
	
	public static void main(String[] args) {
		//start time
		long time = System.currentTimeMillis();
		// Usage
		String usage = "housingCrawler [location(s)] [crawlType]\n"+
				"location(s)\t\teither a single location String or a location file (one location per line)\n"+
				"crawlType\t\t'buy','rent', or 'share'";
		// set up  log file
		DateFormat tsLogFormat = DateFormat.getDateTimeInstance();
		Date tsLog = new Date(time);
		log = new File("housingCrawlerLog.log");
		// start log
		PrintWriter q;
		try {
			q = new PrintWriter(new BufferedWriter(new FileWriter(log, true)));
			q.println(tsLogFormat.format(tsLog)+" : "+"Crawler started");
			if (args.length == 0) {
					System.out.println(usage);
					System.exit(0);
			}
			// get command line args
			for (int i=0;i<args.length;i++) {
				if (i==0) {
					File locs;
					if ((locs = new File(args[i])).isFile()==true) {
						Scanner locFile = new Scanner(locs);
						while (locFile.hasNextLine()) {
							locations.add(locFile.nextLine().trim());
						}
						locFile.close();
					} else {
						locations.add(args[i]);
					}
				} else if (i==1) {
					if (args[i].equals("buy")) {
						crawlType = housingCrawler.crawlTypes.B;
					} else if (args[i].equals("rent")) {
						crawlType = housingCrawler.crawlTypes.R;
					} else if (args[i].equals("share")) {
						crawlType = housingCrawler.crawlTypes.S;
					} else {
						System.out.println("no valid crawlType given");
						System.exit(0);
					}
				}
			}
			// add cmdline input to log and set locString for output files
			q.println(tsLogFormat.format(tsLog)+" : "+"locations="+args[0]);
			setLocString(args[0]);
			q.println(tsLogFormat.format(tsLog)+" : "+"crawlType="+args[1]);
			csvWrapper housingData;
			
			// run the api queries 
			if ((housingData = getHousingData.getHousing(locations,crawlType)) != null) {
				q.println(tsLogFormat.format(tsLog)+" : "+args[1]+" data acquired");
			} else {
				q.println(tsLogFormat.format(tsLog)+" : "+args[1]+" data acquisition error");
				q.close();
				System.exit(0);
			}
			if (outputDB.writetoDB(housingData, crawlType)==1) {
				q.println(tsLogFormat.format(tsLog)+" : "+"data written to database");
			} else {
				q.println(tsLogFormat.format(tsLog)+" : "+"error writing data to database");
				q.close();
				System.exit(0);
			}
			if (outputCSV.writetoCSV(housingData,crawlType)==1) { 
				q.println(tsLogFormat.format(tsLog)+" : "+"output file created/updated");
			} else {
				q.println(tsLogFormat.format(tsLog)+" : "+"error creating/updating output file");
				q.close();
				System.exit(0);
			}
			q.println(tsLogFormat.format(tsLog)+" : "+"crawl finished");
			q.println(tsLogFormat.format(tsLog)+" : "+"execution time " + (System.currentTimeMillis()-time)/1000 +" Secs.");
			q.close();
			
	}catch (IOException e) {
		System.out.println(tsLogFormat.format(tsLog)+" : "+"Error creating/updating log file");
		System.out.println(e.getStackTrace());
		}
	}
	
	public static String getLocString() {
		return locString.substring(0, locString.indexOf("."));
	}
	public static void setLocString(String locString) {
		housingCrawler.locString = locString;
	}

	public enum crawlTypes {
	    B,R,S 
	}
}
