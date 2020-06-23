package reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.eclipse.paho.client.mqttv3.MqttException;
import com.google.gson.Gson;
import com.phidget22.PhidgetException;
import com.phidget22.RFID;
import com.phidget22.RFIDTagEvent;
import com.phidget22.RFIDTagListener;
import com.phidget22.RFIDTagLostEvent;
import com.phidget22.RFIDTagLostListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import reader.RFIDPublisher;

public class CardReader {

	// Create new RFIDPublisher object, found in reader package
	RFIDPublisher publisher = new RFIDPublisher();
	RFID rfid = new RFID();
	Gson gson = new Gson();

	// Makeshift rfidTag - variables ( tagId, readerId)
	RFIDdata rfidTag = new RFIDdata("Tester", "123456");

	// Declare String to hold representation of rfidTag object data
	String rfidTagJson = new String();
	int reader;

	// Address of server which will receive rfidTag
	public static String rfidTagServerURL = "http://localhost:8081/PhidgetProjectServer/Server";
	// 
	public static String sURL = "http://localhost:8081/PhidgetProjectServer/Server?getdata=true";

	public static void main(String[] args) throws PhidgetException {
		new CardReader();

	}

	public CardReader() throws PhidgetException {

		// Make the RFID PhidgetScanner able to detect loss or gain of an RFID tag
		rfid.addTagListener(new RFIDTagListener() {
			public void onTag(RFIDTagEvent e) {

				// Get tagID and readerID to create RFIDdata object
				String tagID = e.getTag();
				String readerID = String.valueOf(reader);

				System.out.println("Tag read: " + tagID + " ReaderId: " + readerID);

				try {

					// Create RFIDdata object
					RFIDdata rfidTag = new RFIDdata(tagID, readerID);
					// Convert java object into JSON using GSON library
					rfidTagJson = gson.toJson(rfidTag);
					// Debug: If JSON string is correctly structured and includes right data
					System.out.println("What is being sent to server: " + rfidTagJson);

					// Pass the JSON string into the sendToServer method
					// This is the start of the tag being validated server side
					// Then return if string valid or not
					String validCheck = sendToServer(rfidTagJson);
					System.out.println("Valid check String returned from database: " + validCheck);
					RFIDdata currentTag = gson.fromJson(validCheck, RFIDdata.class);

					// Checks string sent back from server, to determine whether to unlock door
					// Variable valid would of been set by server. If true then use publish to send
					// over MQTT with the doorID. This is to allow only one door being opened by the
					// tag read.
					if (validCheck.contains("true")) {

						System.out.println("Door open, welcome!");
						publisher.publishMotor(currentTag.getDoorid());
					} else {
						System.out.println("Invalid tag - No entry!!!");
					}

				} catch (MqttException mqtte) {
					mqtte.printStackTrace();
				}

			}
		});
		// Alert to show when tag is removed from reader
		rfid.addTagLostListener(new RFIDTagLostListener() {
			// What to do when a tag is lost
			public void onTagLost(RFIDTagLostEvent e) {
				// optional print, used as debug here
				System.out.println("DEBUG: Tag lost: " + e.getTag());
			}
		});

		// Open and start detecting RFIDtags. Wait 5 seconds for device to respond
		rfid.open(5000);
		// Generic device information, useful for testing
		System.out.println("Device Name " + rfid.getDeviceName());
		System.out.println("Serial Number " + rfid.getDeviceSerialNumber());
		System.out.println("Device Version " + rfid.getDeviceVersion());
		reader = rfid.getDeviceSerialNumber();

		rfid.setAntennaEnabled(true);

		try {
			System.out.println("\n\nCard Reader open for 2 minuites - please scan RFID tag\n\n");
			Thread.sleep(120000);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			rfid.close();
			System.out.println("RFID scanner turning off!");
		}

	}

	// Method used to obtain the RFIDdata object, convert it to JSON format,
	// Attach onto a URL to be sent and received by server.
	// JSON is used to allow easier parsing on the server, useful to allow database
	// insertion
	public String sendToServer(String rfidTagJson) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;

		// Replace invalid URL characters from JSON string
		try {
			rfidTagJson = URLEncoder.encode(rfidTagJson, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		// Create URL string with relevant RFIDTag data and JSON format
		String fullURL = rfidTagServerURL + "?RFIDdata=" + rfidTagJson;
		System.out.println("Sending data to: " + fullURL); // DEBUG confirmation message
		String line;
		String result = "";
		try {
			url = new URL(fullURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			// Request response from server to enable URL to be opened
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
