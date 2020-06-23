package motor;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import utils.Utils;

public class MotorSubscriber {

	public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";
	public static final String userid = "16061659";

	String clientId = Utils.getMacAddress() + "-sub";
	private MqttClient mqttClient;

	public MotorSubscriber() {

		try {
			mqttClient = new MqttClient(BROKER_URL, clientId);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void start() {
		try {
			mqttClient.setCallback(new SubscriberCallback());
			mqttClient.connect();

			String doorID = MotorMover.retrieveDoorID();
			System.out.println("Debug: Current doorID is: " + doorID);
			// Subscribe to correct topic with doorID added on to allow
			// only that to open
			final String topic = userid + "/motor" + doorID;
			mqttClient.subscribe(topic);

			System.out.println("Debug: Subscriber is now listening to " + topic);

		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String... args) {
		final MotorSubscriber subscriber = new MotorSubscriber();
		subscriber.start();
	}

}
