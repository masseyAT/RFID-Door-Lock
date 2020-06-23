package motor;

import org.eclipse.paho.client.mqttv3.*;

public class SubscriberCallback implements MqttCallback {

	public static final String userid = "16061659";

	@Override
	public void connectionLost(Throwable cause) {
		// This is called when the connection is lost. We could reconnect here.
	}

	@Override
	// messageArrived method takes in both the topic string and the message to open
	// the door to a certain position
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("Message arrived. Topic: " + topic + "  Message: " + message.toString());
		String doorOpen150 = message.toString();
		double d = Double.parseDouble(doorOpen150);
		// Move motor to open, then shut after pausing
		MotorMover.moveServoTo(d);
		System.out.println("Waiting until motor at position 150");
		waitFor(5);
		MotorMover.moveServoTo(0.0);
		waitFor(2);

		if ((userid + "/LWT").equals(topic)) {
			System.err.println("Sensor gone!");
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// no-op
	}

	public static void waitFor(int numSeconds) {
		// pauses for numSeconds
		try {
			Thread.sleep(numSeconds * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
