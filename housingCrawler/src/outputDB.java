import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

/*
 * outputDB.java
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

public class outputDB {
	static Statement stmt;
	static Connection con;

	public static int writetoDB(csvWrapper c,housingCrawler.crawlTypes t) {

		// time stamp for crawl time
		Date ts = new Date();
		// get data from wrapper
		ArrayList<ArrayList<String>> rows = c.data;

		// load database driver
		try {
			Class.forName("org.postgresql.Driver"); //Or any other driver
			String url = "jdbc:postgresql://DBHOSTIP:PORT/DBNAME"; // 

			con = DriverManager.getConnection(url,"USERNAME","PASSWORD");
		}
		catch( SQLException x ){
			System.out.println( "Couldn’t get connection!" );
			x.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println( "Unable to load the driver class!" );
			e.printStackTrace();
		}

		// set the table name by switch on crawlTypes
		String dbtable = "";
		switch (t) {
		case B:	
			dbtable = "sales";
			break;
		case R:
			dbtable = "rentals";
			break;
		case S:
			dbtable = "shares";
			break;
		}
		// create the string for inserting
		String insertString;
		Object[] rowString;
		for (int i=0;i<rows.size();i++) {
			String rowtoDBformat = "";
			rowString = rows.get(i).toArray();
			for (int k=0;k<rowString.length;k++) {
				String stripSingleQuotes;
				if (((String)rowString[k]).length()==0) {
					stripSingleQuotes = "null";
				} else {
					stripSingleQuotes = ((String)rowString[k]).replace("\'", "");
				}
				if (stripSingleQuotes != "null") {
					rowtoDBformat = rowtoDBformat.concat("\'"+stripSingleQuotes+"\',");
				} else {
					rowtoDBformat = rowtoDBformat.concat(stripSingleQuotes+",");
				}
			}
			System.out.println(i);
			System.out.println(rowtoDBformat);
			insertString = String.format("insert into %s values(%s, %s)",dbtable,"\'"+ts+"\'",
					rowtoDBformat.substring(0, rowtoDBformat.length()-1));
			System.out.println(insertString);
			// insert to table 
			try {
				stmt = con.createStatement();
				stmt.executeUpdate(insertString);
				stmt.close();
			} catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
				System.out.println(insertString);
				return 0;
			}
		}


		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

}
