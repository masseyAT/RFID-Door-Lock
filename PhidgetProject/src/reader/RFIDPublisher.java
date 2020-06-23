package reader;

import org.eclipse.paho.client.mqttv3.*;

public class RFIDPublisher {

	public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";
	public static final String userid = "16061659";
	public static final String TOPIC_MOTOR = userid + "/motor";

	private MqttClient client;

	public RFIDPublisher() {

		try {
			client = new MqttClient(BROKER_URL, userid);
			// Create MQTT session
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setWill(client.getTopic(userid + "/LWT"), "I'm gone :(".getBytes(), 0, false);
			client.connect(options);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// Method used to publish MQTT to the lock subscriber.
	// The doorID is passed into the method and added onto the end of the topic,
	// which the motor subscribes to
	// and will allow only that motor to open the connected door.
	public void publishMotor(String doorID) throws MqttException {
		final MqttTopic motorTopic = client.getTopic(TOPIC_MOTOR + doorID);
		System.out.println("Debug: Message being created for TOPIC_MOTOR: " + motorTopic.getName());

		// Message is published over MQTT with the topic including userID and roomID.
		// MotorMessage passed over a number which the motor uses to open to that range.
		final String motorMessage = 150 + "";

		motorTopic.publish(new MqttMessage(motorMessage.getBytes()));

		System.out.println("Published data over MQTT. Topic: " + motorTopic.getName() + "   Message: " + motorMessage);
	}

}
