package motor;

import com.phidget22.PhidgetException;
import com.phidget22.RCServo;

public class MotorMover {

	static RCServo servo = null;

	public static RCServo getInstance() {
		System.out.println("Motor Waiting");
		if (servo == null) {
			servo = PhidgetMotorMover();
		}
		return servo;
	}

	// This method will create a new instance of a servo board and will listen to
	// motor changes.
	private static RCServo PhidgetMotorMover() {

		try {
			System.out.println("Constructing MotorMover");
			servo = new RCServo();
			// Start listening for motor interaction
			servo.open(2000);
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
		return servo;
	}

	public static String retrieveDoorID() {
		try {
			MotorMover.getInstance();
			return Integer.toString(servo.getDeviceSerialNumber());
		} catch (PhidgetException e) {
			e.printStackTrace();
			return "Cannot retrieve door ID!";
		}
	}

	// Motor position is passed through on the end of the MQTT message
	// sent from RFIDPublisher
	public static void moveServoTo(double motorPosition) {
		try {
			// Get the servo that is available
			MotorMover.getInstance();
			System.out.println("Lock opening: Moving to lock position " + motorPosition);
			servo.setMaxPosition(210.0);
			servo.setTargetPosition(motorPosition);
			servo.setEngaged(true);
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
