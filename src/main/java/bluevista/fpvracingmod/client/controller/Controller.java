package bluevista.fpvracingmod.client.controller;

import bluevista.fpvracingmod.client.math.BetaflightHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;

@Environment(EnvType.CLIENT)
public class Controller {

	public static int CONTROLLER_ID;
	public static int THROTTLE_NUM;
	public static int PITCH_NUM;
	public static int YAW_NUM;
	public static int ROLL_NUM;
	public static float DEADZONE;
	public static int THROTTLE_CENTER_POSITION;
	public static float RATE;
	public static float SUPER_RATE;
	public static float EXPO;
	public static int INVERT_THROTTLE;
	public static int INVERT_PITCH;
	public static int INVERT_YAW;
	public static int INVERT_ROLL;

	public static void setValue(String key, int value) {
		switch(key) {
			case "controllerID":
				setControllerId(value);
				break;
			case "throttleNum":
				setThrottleNum(value);
				break;
			case "pitchNum":
				setPitchNum(value);
				break;
			case "yawNum":
				setYawNum(value);
				break;
			case "rollNum":
				setRollNum(value);
				break;
			case "throttleCenterPosition":
				setThrottleCenterPosition(value);
				break;
			case "invertThrottle":
				setInvertThrottle(value);
				break;
			case "invertPitch":
				setInvertPitch(value);
				break;
			case "invertYaw":
				setInvertYaw(value);
				break;
			case "invertRoll":
				setInvertRoll(value);
				break;
			default:
				break;
		}
	}

	public static void setValue(String key, float value) {
		switch(key) {
			case "deadzone":
				setDeadzone(value);
				break;
			case "rate":
				setRate(value);
				break;
			case "superRate":
				setSuperRate(value);
				break;
			case "expo":
				setExpo(value);
				break;
			default:
				break;
		}
	}

	public static void setControllerId(int controllerId) {
		CONTROLLER_ID = controllerId;
	}

	public static void setThrottleNum(int throttleChannel) {
		THROTTLE_NUM = throttleChannel;
	}

	public static void setPitchNum(int pitchChannel) {
		PITCH_NUM = pitchChannel;
	}

	public static void setYawNum(int yawChannel) {
		YAW_NUM = yawChannel;
	}

	public static void setRollNum(int rollChannel) {
		ROLL_NUM = rollChannel;
	}

	public static void setDeadzone(float deadzone) {
		DEADZONE = deadzone;
	}

	public static void setThrottleCenterPosition(int throttleCenterPosition) {
		THROTTLE_CENTER_POSITION = throttleCenterPosition;
	}

	public static void setRate(float rate) {
		RATE = rate;
	}

	public static void setSuperRate(float superRate) {
		SUPER_RATE = superRate;
	}

	public static void setExpo(float expo) {
		EXPO = expo;
	}

	public static void setInvertThrottle(int invertThrottle) {
		INVERT_THROTTLE = invertThrottle;
	}

	public static void setInvertPitch(int invertPitch) {
		INVERT_PITCH = invertPitch;
	}

	public static void setInvertYaw(int invertYaw) {
		INVERT_YAW = invertYaw;
	}

	public static void setInvertRoll(int invertRoll) {
		INVERT_ROLL = invertRoll;
	}

	public static float getAxis(int axis) {
		return glfwGetJoystickAxes(CONTROLLER_ID).get(axis);
	}

	public static boolean controllerExists() {
		try {
			glfwGetJoystickAxes(CONTROLLER_ID).get();
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public static float getBetaflightAxis(int axis, float rate, float expo, float superRate) { // logistic yo
		return (float) BetaflightHelper.calculateRates(getAxis(axis), rate, expo, superRate);
	}
}