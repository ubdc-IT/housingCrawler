import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * getHousingData.java
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

public class getHousingData {

	static ArrayList<String> outheaders = new ArrayList<String>(); // output headers
	static ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>(); 
	private static ArrayList<String> locations = null;
	private static housingCrawler.crawlTypes ct;
	private static String nestoria_url = "http://api.nestoria.co.uk/";

	/*
	 * This method takes a list of locations and a crawler type and then queries the API
	 */
	public static csvWrapper getHousing(ArrayList<String> locationArray,housingCrawler.crawlTypes cT) {
		ct = cT;
		locations = locationArray;


		String charset = "UTF-8"; // URL string encoding
		String paramEncoding = "json"; // api output encoding
		String paramListType = ""; // buy, rent or share type
		switch (ct) {
		case B: 
			paramListType = "buy";
			break;
		case R:
			paramListType = "rent";
			break;
		case S:
			paramListType = "share";
			break;
		}
		String paramAction = "search_listings"; // api action
		String paramCountry = "uk"; // target country
		String paramNoRes = "50"; // number of results per page (50max)
		csvWrapper dataout = null; // initialise return value


		for (int i=0;i<locations.size();i++) {
			String paramPlace = locations.get(i); // location to search for listings
			System.out.println(paramPlace);
			long totalPages = 0;
			int pageNo = 1; // page number to offset results

			try {
				String query = String.format("encoding=%s&place_name=%s&listing_type=%s&" +
						"action=%s&country=%s&number_of_results=%s", 
						URLEncoder.encode(paramEncoding, charset), URLEncoder.encode(paramPlace, charset),
						URLEncoder.encode(paramListType, charset),URLEncoder.encode(paramAction, charset),
						URLEncoder.encode(paramCountry,charset),URLEncoder.encode(paramNoRes, charset));

				// leave a second between API calls
				try {
					Thread.sleep(1000);                 //1000 milliseconds is one second.
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				URLConnection connection = new URL(nestoria_url + "api" + "?" + query).openConnection();
				connection.setRequestProperty("Accept-Charset", charset);
				InputStream response = connection.getInputStream();

				HttpURLConnection conn = (HttpURLConnection)connection;
				int status = conn.getResponseCode();
				//System.out.println(status);
				/*
				for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
					System.out.println(header.getKey() + "=" + header.getValue());
				}
				 */
				// set up reader for parsing response from webserver
				BufferedReader reader = new BufferedReader(new InputStreamReader(response, charset));
				String json = reader.readLine(); // get the json response (should be one line)

				JSONParser parser = new JSONParser(); // create json parser to access data structure
				Object resultObject = parser.parse(json); // parse structure

				JSONObject obj = (JSONObject)resultObject;
				//System.out.println(obj);
				// access response metadata to find total number of pages
				JSONObject Resp = (JSONObject)obj.get("response");
				Object totalResults;
				reader.close();
				if ((totalResults = (Object)Resp.get("total_results"))!=null) {

					totalPages = (long) (Resp.get("total_pages"));
					//System.out.println(totalPages);

					// get data from first page
					JSONArray listings = (JSONArray) Resp.get("listings");
					for (int j=0;j<listings.size();j++) {
						JSONObject listing = (JSONObject) listings.get(j);
						ArrayList<String> datarow = new ArrayList<String>();
						// add output headers from 1st Listing
						if (pageNo==1 && j==0 && i==0) {
							System.out.println("here only once?");
							outheaders.addAll(listing.keySet());
							System.out.println(outheaders.size());
						}
						// get values
						for (int k=0;k<outheaders.size();k++) {
							if ((listing.keySet()).contains(outheaders.get(k))) {
								datarow.add(String.valueOf(listing.get(outheaders.get(k))));
								//System.out.println("unit = "+outheaders.get(k)+"\tvalue = "+datarow.get(k));
							} else {
								datarow.add("");
							}
						}
						// add row to table structure
						rows.add(datarow);
					}
					pageNo++;

					for (int j=1;j<totalPages;j++) {
						// get data from subsequent pages	
						// add page offset to query
						// leave a second between API calls
						try {
							Thread.sleep(1000);                 //1000 milliseconds is one second.
						} catch(InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
						connection = new URL(nestoria_url+"api"+"?"+query+"&page="+
								URLEncoder.encode(String.valueOf(pageNo), charset)).openConnection();
						connection.setRequestProperty("Accept-Charset", charset);
						response = connection.getInputStream();

						conn = (HttpURLConnection)connection;
						status = conn.getResponseCode();
						//System.out.println(status);
						/*
					for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
						System.out.println(header.getKey() + "=" + header.getValue());
					}
						 */
						// set up reader for parsing response from webserver
						reader = new BufferedReader(new InputStreamReader(response, charset));
						json = reader.readLine(); // get the json response (should be one line)

						parser = new JSONParser(); // create json parser to access data structure
						resultObject = parser.parse(json); // parse structure

						obj = (JSONObject)resultObject;
						//System.out.println(obj);
						// access response object
						Resp = (JSONObject)obj.get("response");
						// get array of listings
						listings = (JSONArray) Resp.get("listings");
						for (int l=0;l<listings.size();l++) {
							JSONObject listing = (JSONObject) listings.get(l);
							ArrayList<String> datarow = new ArrayList<String>();

							// get values
							for (int k=0;k<outheaders.size();k++) {
								if ((listing.keySet()).contains(outheaders.get(k))) {
									datarow.add(String.valueOf(listing.get(outheaders.get(k))));
									//System.out.println("unit = "+outheaders.get(k)+"\tvalue = "+datarow.get(k));
								} else {
									datarow.add("");
								}
							}
							// add row to table structure
							rows.add(datarow);
						}
						pageNo++;
					}
					reader.close();
				} 
			} catch (IOException | ParseException e) {
				e.printStackTrace();
				return dataout;
			}
		}
		dataout = new csvWrapper(outheaders,rows);
		return dataout;
	}
}
class csvWrapper {
	ArrayList<String> headers;
	ArrayList<ArrayList<String>> data;

	public csvWrapper(ArrayList<String> h, ArrayList<ArrayList<String>> d) {
		headers = h;
		data = d;
	}
}