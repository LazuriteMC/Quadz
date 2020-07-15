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
	public static float RATE;
	public static float SUPER_RATE;
	public static float EXPO;
	public static int INVERT_PITCH;
	public static int INVERT_YAW;
	public static int INVERT_ROLL;

	public static void setControllerId(int controllerId) {
		CONTROLLER_ID = controllerId;
	}

	public static void setThrottle(int throttleChannel) {
		THROTTLE_NUM = throttleChannel;
	}

	public static void setPitch(int pitchChannel) {
		PITCH_NUM = pitchChannel;
	}

	public static void setYaw(int yawChannel) {
		YAW_NUM = yawChannel;
	}

	public static void setRoll(int rollChannel) {
		ROLL_NUM = rollChannel;
	}

	public static void setDeadzone(float deadzone) {
		DEADZONE = deadzone;
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

	public static float getBetaflightAxis(int axis, float rate, float expo, float superRate) { // logistic yo
		return (float) BetaflightHelper.calculateRates(getAxis(axis), rate, expo, superRate);
	}
}