import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

/*
 * outputCSV.java
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

public class outputCSV {

	static File out = null;
	static int hflag = 0;

	public static int writetoCSV(csvWrapper c,housingCrawler.crawlTypes t) {
		Date ts = new Date();
		System.out.println("^ "+ts);
		long tsf = System.currentTimeMillis() / 1000L; 
		ArrayList<String> headers = c.headers;
		ArrayList<ArrayList<String>> rows = c.data;
		String loc = housingCrawler.getLocString();
		switch (t) {
		case B:	
			out = new File("outputBuy-"+loc+"-"+tsf+".csv");
			break;
		case R:
			out = new File("outputRent-"+loc+"-"+tsf+".csv");
			break;
		case S:
			out = new File("outputShare-"+loc+"-"+tsf+".csv");
			break;
		}
		if (out.exists())
			hflag = 1;
		PrintWriter q;
		try {
			q = new PrintWriter(new BufferedWriter(new FileWriter(out, true)));
			int j;
			// add headers to a new file
			if (hflag==0) {
				for (int i = 0; i < headers.size(); i++) {
					if (i == 0)
						q.print("Crawl Time"+",");
					if (i != 0) 
						q.print(",");
					q.print(headers.get(i));
				}
				q.println();
			}
			// add data
			for (int i = 0; i < rows.size(); i++) {
				for (j = 0; j < headers.size(); j++) {
					if (j == 0)
						q.print(ts+",");
					if (j != 0)
						q.print(",");
					q.print("\""+rows.get(i).get(j)+"\""); // this might break TODO
				}
				q.println();
			}
			q.close();
		} catch (IOException | IndexOutOfBoundsException e) {
			// give warning
			System.out.println("cannot create output file: " + out.getName());
			return 0;
		}
		return 1;
	} 
}
