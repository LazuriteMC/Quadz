package bluevista.fpvracingmod.client.controller;

import bluevista.fpvracingmod.client.ClientInitializer;
import bluevista.fpvracingmod.client.math.helper.BetaflightHelper;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;


public class Controller {

	public static int CONTROLLER_ID;
	public static int THROTTLE_NUM;
	public static int PITCH_NUM;
	public static int YAW_NUM;
	public static int ROLL_NUM;

	public static void setControllerId() {
		if (ClientInitializer.getConfig().getValue("controllerID") != null) {
			CONTROLLER_ID = Integer.parseInt(ClientInitializer.getConfig().getValue("controllerID"));
		} else {
			CONTROLLER_ID = 0;
		}
	}

	public static void setThrottle() {
		if (ClientInitializer.getConfig().getValue("throttle") != null) {
			THROTTLE_NUM = Integer.parseInt(ClientInitializer.getConfig().getValue("throttle"));
		} else {
			THROTTLE_NUM = 0; // default throttle channel
		}
	}

	public static void setPitch() {
		if (ClientInitializer.getConfig().getValue("pitch") != null) {
			PITCH_NUM = Integer.parseInt(ClientInitializer.getConfig().getValue("pitch"));
		} else {
			PITCH_NUM = 0; // default pitch channel
		}
	}

	public static void setYaw() {
		if (ClientInitializer.getConfig().getValue("yaw") != null) {
			YAW_NUM = Integer.parseInt(ClientInitializer.getConfig().getValue("yaw"));
		} else {
			YAW_NUM = 0; // default yaw channel
		}
	}

	public static void setRoll() {
		if (ClientInitializer.getConfig().getValue("roll") != null) {
			ROLL_NUM = Integer.parseInt(ClientInitializer.getConfig().getValue("roll"));
		} else {
			ROLL_NUM = 0; // default roll channel
		}
	}

	public static float getAxis(int axis) {
		return glfwGetJoystickAxes(CONTROLLER_ID).get(axis);
	}

	public float getBetaflightAxis(int axis, float rate, float expo, float superRate) { // logistic yo
		return (float) BetaflightHelper.calculateRates(getAxis(axis), rate, expo, superRate) / 10;
	}
}