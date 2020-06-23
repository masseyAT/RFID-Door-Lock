
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import java.io.*;
import java.sql.*;


/**
 * Servlet implementation class sensorToDB
 */
@WebServlet("/Server")
public class Server extends HttpServlet {

	private static final long serialVersionUID = 1L;

	Gson gson = new Gson();
	RFIDdata lastSensor = new RFIDdata("lastSensor", "unknown", false, "unknown");
	Connection conn = null;
	Statement stmt;
	

	// init is run once once upon server loading
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.out.println("Sensor to DB server is up and running\n");
	}

	// Load database driver , establish connection and pass in
	// database login requirements
	private void getConnection() {

		String user = "masseya";
		String password = "krIsderm2";
		String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

		// Load the database driver
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.out.println(e);
		}
		// Connection with user login
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("DEBUG: Connection to database successful");
			stmt = conn.createStatement();
		} catch (SQLException se) {
			System.out.println(se);
		}
	}

	private void closeConnection() {
		try {
			conn.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
/*
	public void destroy() {
		try {
		} catch (Exception e) {
			System.out.println(e);
		}
	}*/

	public Server() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		// Declare a RFIDData object to hold the incoming data
		RFIDdata rfidData = new RFIDdata("unknown", "unknown", false, "unknown");

		// Check to see whether the client is requesting data or sending it
		String getdata = request.getParameter("getdata");

		// If no data, client is sending data
		if (getdata == null) {

			String rfidDataJsonString = request.getParameter("RFIDdata");

			// If invalid JSON or RFIDData constructor not met
			if (rfidDataJsonString != null) {
				System.out.println("Debug: JSON String received: " + rfidDataJsonString);
				// Convert the JSON string to an object of type RFIDData
				rfidData = gson.fromJson(rfidDataJsonString, RFIDdata.class);
				System.out.println("Debug: RFIDData object: " + rfidData);
				// Update sensor values and send back response
				PrintWriter out = response.getWriter();

				// VALID Database check, pass in RFIDData object and return
				// if either valid or not
				System.out.println("Valid database check!");
				String returnedValidRFID = validTagDatabaseCheck(rfidData);
				System.out.println("Debug: RFIDData Validated JSON string returned from database: " + returnedValidRFID);
				rfidData = gson.fromJson(returnedValidRFID, RFIDdata.class);
				System.out.println("Send to attempts database to log attempt of entry to door");
				// ATTEMPTS Database entry, this is ran after the valid database check as
				// attempts is updated in that method
				updateAttemptsDatabase(rfidData);
				out.println(updateSensorValues(rfidData));
				sendJSONString(response);
				out.close();
			}
		} else {
			System.out.println("No Data on last known tag scanned");
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	// This method inserts every tag scan on the RFID scanner, with the data and
	// time added for better security. The valid column will be updated to show all
	// scan attempts and whether they were successful
	private void updateAttemptsDatabase(RFIDdata rfidData) {
		try {
			// Insert statement with all 4 RFIDData variables
			String updateAttemptsSQL = "insert into attempts(tagid, readerid, dateScanned, valid) " + "values('"
					+ rfidData.getTagid() + "','" + rfidData.getReaderid() + "'," + "now()," + rfidData.isValid()
					+ ");";

			System.out.println("DEBUG: Update Attmepts Database SQL statement: " + updateAttemptsSQL);
			getConnection();
			stmt.executeUpdate(updateAttemptsSQL);
			closeConnection();
			System.out.println("DEBUG: Update attempts Database successful ");
		} catch (SQLException se) {
			// Problem with update, return failure message
			System.out.println(se);
			System.out.println("\nDEBUG: Update error - see error trace above for help. ");
			return;
		}
		return;
	}

	// Takes RFIDData object tagID and readerID and checks them against all entry's
	// inside the validTags database. If both variables are the same as a row
	// inside the database. Then the valid variable will be set as true. All other
	// Variables needed for RFIDdata are set and the object is returned. If the tag
	// is not valid, the object is passed back with the valid set a false.
	private String validTagDatabaseCheck(RFIDdata tag) {
		String validTagSQL = "select * from validtags where tagid='" + tag.getTagid() + "' AND readerid='"
				+ tag.getReaderid() + "';";
		ResultSet rs;

		RFIDdata rfidData = new RFIDdata("oneSensor", "unknown", false, "unknown");
		rfidData.setTagid(tag.getTagid());
		rfidData.setReaderid(tag.getReaderid());
		rfidData.setDoorid(tag.getDoorid());
		rfidData.setValid(false);
		try {
			// Create a result set of selected values
			getConnection();
			rs = stmt.executeQuery(validTagSQL);

			// Iterate over the result set
			while (rs.next()) {
				// If its valid, goes to check door for doorid
				rfidData.setDoorid(checkDoor(rs.getString("roomID")));
				rfidData.setValid(true);
			}
		} catch (SQLException ex) {
			System.out.println("Error in SQL " + ex.getMessage());
		}
		// Close connection to database
		closeConnection();

		String validTagsJson = gson.toJson(rfidData);
		return validTagsJson;
	}

	// Database check to make sure doorID is valid with roomID. Will make sure the
	// tag corresponds to the right room and will only open the one lock
	public String checkDoor(String roomID) {

		String selectSQL = "select * from doorChecker where roomid = '" + roomID + "';";
		ResultSet rs;
		String doorID = "";

		try {
			getConnection();
			rs = stmt.executeQuery(selectSQL);

			while (rs.next()) {
				doorID = rs.getString("doorid");
			}
		} catch (SQLException ex) {
			System.out.println("Error in sql" + ex.getMessage());
		}

		closeConnection();
		return doorID;
	}

	private String updateSensorValues(RFIDdata oneSensor) {
		// all ok, update last known values and return
		lastSensor = oneSensor;
		System.out
				.println("DEBUG : Last sensor was " + oneSensor.getTagid() + ", with value " + oneSensor.getReaderid());
		return "";
	}

	private void sendJSONString(HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		String json = gson.toJson(lastSensor);
		PrintWriter out = response.getWriter();
		out.println(json);
		out.close();
	}

}
