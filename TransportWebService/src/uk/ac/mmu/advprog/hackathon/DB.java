package uk.ac.mmu.advprog.hackathon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Handles database access from within your web service
 * @author You, Mainly!
 */
public class DB implements AutoCloseable {
	
	//allows us to easily change the database used
	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/NaPTAN.db";
	
	//allows us to re-use the connection between queries if desired
	private Connection connection = null;
	
	/**
	 * Creates an instance of the DB object and connects to the database
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
		}
		catch (SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Returns the number of entries in the database, by counting rows
	 * @return The number of entries in the database, or -1 if empty
	 */
	public int getNumberOfEntries() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM Stops");
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}

	/**
	 * Returns the number of stops in the database in a particular locality, by counting all rows with locality name specified in URL
	 * @return The number of stops in the database in a particular locality, or -1 if empty
	 */
	public int getStopCount(String locality) {
		int result = -1;
		try {
			PreparedStatement s = connection.prepareStatement("SELECT COUNT(*) AS Number FROM Stops WHERE LocalityName = ?");
			s.setString(1, locality);
			ResultSet results = s.executeQuery();
			while(results.next()) { 
				result = results.getInt(results.findColumn("Number"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	/**
	 * Returns a JSON array of stops of a specified stop type in a specific locality, by counting all rows with locality name and stop type specified in URL
	 * @param Locality the location of the stop
	 * @param Type the type of transport
	 * @return Returns JSON array of stops
	 */
	public JSONArray getStopList(String locality, String type) {
		JSONArray stopArray = new JSONArray();
		try {
			PreparedStatement s = connection.prepareStatement("SELECT * FROM Stops WHERE LocalityName = ? AND StopType = ?");
			s.setString(1, locality);
			s.setString(2, type);
			ResultSet results = s.executeQuery();
			while(results.next()) { 
				JSONObject stop = new JSONObject();
				stop.put("name",results.getString("CommonName"));
				stop.put("locality", results.getString("LocalityName"));
				
				JSONObject location = new JSONObject();
				String indicator = results.getString("Indicator");
				String bearing = results.getString("Bearing");
				String street = results.getString("Street");
				String landmark = results.getString("Landmark");
				if(indicator == null || indicator.isEmpty()) {
					indicator = " ";
				}
				if(bearing == null || bearing.isEmpty()) {
					bearing = " ";
				}
				if(street == null || street.isEmpty()) {
					street = " ";
				}
				if(landmark == null || landmark.isEmpty()) {
					landmark = " ";
				}
				location.put("indicator", indicator);
				location.put("bearing", bearing);
				location.put("street", street);
				location.put("landmark", landmark);
				
				stop.put("location", location);
				stop.put("type", results.getString("StopType"));
				
				stopArray.put(stop);
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return stopArray;
	}

	
	/**
	 * Closes the connection to the database, required by AutoCloseable interface.
	 */
	@Override
	public void close() {
		try {
			if ( !connection.isClosed() ) {
				connection.close();
			}
		}
		catch(SQLException sqle) {
			error(sqle);
		}
	}

	/**
	 * Prints out the details of the SQL error that has occurred, and exits the programme
	 * @param sqle Exception representing the error that occurred
	 */
	private void error(SQLException sqle) {
		System.err.println("Problem Opening Database! " + sqle.getClass().getName());
		sqle.printStackTrace();
		System.exit(1);
	}
}
