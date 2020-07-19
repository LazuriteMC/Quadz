package bluevista.fpvracingmod.server.commands;

import bluevista.fpvracingmod.client.controller.Controller;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class FPVRacing {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("fpvracing")
                    .then(CommandManager.literal("controllerID")
                            .then(CommandManager.argument("controllerIDValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setControllerID)))

                    .then(CommandManager.literal("throttleNum")
                            .then(CommandManager.argument("throttleNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setThrottleNum)))

                    .then(CommandManager.literal("pitchNum")
                            .then(CommandManager.argument("pitchNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setPitchNum)))

                    .then(CommandManager.literal("yawNum")
                            .then(CommandManager.argument("yawNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setYawNum)))

                    .then(CommandManager.literal("rollNum")
                            .then(CommandManager.argument("rollNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setRollNum)))

                    .then(CommandManager.literal("deadzone")
                            .then(CommandManager.argument("deadzoneValue", FloatArgumentType.floatArg(0, 1))
                                    .executes(FPVRacing::setDeadzone)))

                    .then(CommandManager.literal("throttleCenterPosition")
                            .then(CommandManager.argument("throttleCenterPositionValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setThrottleCenterPosition)))

                    .then(CommandManager.literal("rate")
                            .then(CommandManager.argument("rateValue", FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setRate)))

                    .then(CommandManager.literal("superRate")
                            .then(CommandManager.argument("superRateValue", FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setSuperRate)))

                    .then(CommandManager.literal("expo")
                            .then(CommandManager.argument("expoValue", FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setExpo)))

                    .then(CommandManager.literal("invertThrottle")
                            .then(CommandManager.argument("invertThrottleValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertThrottle)))

                    .then(CommandManager.literal("invertPitch")
                            .then(CommandManager.argument("invertPitchValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertPitch)))

                    .then(CommandManager.literal("invertYaw")
                            .then(CommandManager.argument("invertYawValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertYaw)))

                    .then(CommandManager.literal("invertRoll")
                            .then(CommandManager.argument("invertRollValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertRoll))));
        });
    }

    // The int returned is used as an exit code. Is it needed?

    private static int setControllerID(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "controllerIDValue");
        if (Controller.CONTROLLER_ID != value) {
            Controller.setControllerId(IntegerArgumentType.getInteger(context, "controllerIDValue"));
            return 1;
        }
        return 0;
    }

    private static int setThrottleNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "throttleNumValue");
        if (Controller.THROTTLE_NUM != value) {
            Controller.setThrottleNum(value);
            return 1;
        }
        return 0;
    }

    private static int setPitchNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "pitchNumValue");
        if (Controller.PITCH_NUM != value) {
            Controller.setPitchNum(value);
            return 1;
        }
        return 0;
    }

    private static int setYawNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "yawNumValue");
        if (Controller.YAW_NUM != value) {
            Controller.setYawNum(value);
            return 1;
        }
        return 0;
    }

    private static int setRollNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "rollNumValue");
        if (value != Controller.ROLL_NUM) {
            Controller.setRollNum(value);
            return 1;
        }
        return 0;
    }

    private static int setDeadzone(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "deadzoneValue");
        if (Controller.DEADZONE != value) {
            Controller.setDeadzone(value);
            return 1;
        }
        return 0;
    }

    private static int setThrottleCenterPosition(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "throttleCenterPositionValue");
        if (Controller.THROTTLE_CENTER_POSITION != value) {
            Controller.setThrottleCenterPosition(value);
            return 1;
        }
        return 0;
    }

    private static int setRate(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "rateValue");
        if (Controller.RATE != value) {
            Controller.setRate(value);
            return 1;
        }
        return 0;
    }

    private static int setSuperRate(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "superRateValue");
        if (Controller.SUPER_RATE != value) {
            Controller.setSuperRate(value);
            return 1;
        }
        return 0;
    }

    private static int setExpo(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "expoValue");
        if (Controller.EXPO != value) {
            Controller.setExpo(value);
            return 1;
        }
        return 0;
    }

    private static int setInvertThrottle(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertThrottleValue");
        if (Controller.INVERT_THROTTLE != value) {
            Controller.setInvertThrottle(value);
            return 1;
        }
        return 0;
    }

    private static int setInvertPitch(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertPitchValue");
        if (Controller.INVERT_PITCH != value) {
            Controller.setInvertPitch(value);
            return 1;
        }
        return 0;
    }

    private static int setInvertYaw(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertYawValue");
        if (Controller.INVERT_YAW != value) {
            Controller.setInvertYaw(value);
            return 1;
        }
        return 0;
    }

    private static int setInvertRoll(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertRollValue");
        if (Controller.INVERT_ROLL != value) {
            Controller.setInvertRoll(value);
            return 1;
        }
        return 0;
    }

}
