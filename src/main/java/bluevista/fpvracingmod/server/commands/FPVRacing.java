package bluevista.fpvracingmod.server.commands;

import bluevista.fpvracingmod.client.controller.Controller;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.GogglesItem;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class FPVRacing {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("fpvracing")
                    .then(CommandManager.literal("controllerID")
                            .then(CommandManager.argument("controllerIDValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setControllerID))
                            .executes(FPVRacing::getControllerID))

                    .then(CommandManager.literal("throttleNum")
                            .then(CommandManager.argument("throttleNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setThrottleNum))
                            .executes(FPVRacing::getThrottleNum))

                    .then(CommandManager.literal("pitchNum")
                            .then(CommandManager.argument("pitchNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setPitchNum))
                            .executes(FPVRacing::getPitchNum))

                    .then(CommandManager.literal("yawNum")
                            .then(CommandManager.argument("yawNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setYawNum))
                            .executes(FPVRacing::getYawNum))

                    .then(CommandManager.literal("rollNum")
                            .then(CommandManager.argument("rollNumValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setRollNum))
                            .executes(FPVRacing::getRollNum))

                    .then(CommandManager.literal("deadzone")
                            .then(CommandManager.argument("deadzoneValue", FloatArgumentType.floatArg(0, 1))
                                    .executes(FPVRacing::setDeadzone))
                            .executes(FPVRacing::getDeadzone))

                    .then(CommandManager.literal("throttleCenterPosition")
                            .then(CommandManager.argument("throttleCenterPositionValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setThrottleCenterPosition))
                            .executes(FPVRacing::getThrottleCenterPosition))

                    .then(CommandManager.literal("rate")
                            .then(CommandManager.argument("rateValue", FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setRate))
                            .executes(FPVRacing::getRate))

                    .then(CommandManager.literal("superRate")
                            .then(CommandManager.argument("superRateValue", FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setSuperRate))
                            .executes(FPVRacing::getSuperRate))

                    .then(CommandManager.literal("expo")
                            .then(CommandManager.argument("expoValue", FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setExpo))
                            .executes(FPVRacing::getExpo))

                    .then(CommandManager.literal("invertThrottle")
                            .then(CommandManager.argument("invertThrottleValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertThrottle))
                            .executes(FPVRacing::getInvertThrottle))

                    .then(CommandManager.literal("invertPitch")
                            .then(CommandManager.argument("invertPitchValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertPitch))
                            .executes(FPVRacing::getInvertPitch))

                    .then(CommandManager.literal("invertYaw")
                            .then(CommandManager.argument("invertYawValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertYaw))
                            .executes(FPVRacing::getInvertYaw))

                    .then(CommandManager.literal("invertRoll")
                            .then(CommandManager.argument("invertRollValue", IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertRoll))
                            .executes(FPVRacing::getInvertRoll))

                    .then(CommandManager.literal("droneBand")
                            .then(CommandManager.argument("droneBandValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setDroneBand))
                            .executes(FPVRacing::getDroneBand))

                    .then(CommandManager.literal("droneChannel")
                            .then(CommandManager.argument("droneChannelValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setDroneChannel))
                            .executes(FPVRacing::getDroneChannel))

                    .then(CommandManager.literal("cameraAngle")
                            .then(CommandManager.argument("angleValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setCameraAngle))
                            .executes(FPVRacing::getCameraAngle))

                    .then(CommandManager.literal("gogglesBand")
                            .then(CommandManager.argument("gogglesBandValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setGogglesBand))
                            .executes(FPVRacing::getGogglesBand))

                    .then(CommandManager.literal("gogglesChannel")
                            .then(CommandManager.argument("gogglesChannelValue", IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setGogglesChannel))
                            .executes(FPVRacing::getGogglesChannel)));
        });
    }

    // The int returned is used as an exit code. Is it needed?

    private static int setControllerID(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "controllerIDValue");
        if (Controller.CONTROLLER_ID != value) {
            Controller.setControllerId(value);
            return 1;
        }
        return 0;
    }

    private static int getControllerID(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("controllerID: " + Controller.CONTROLLER_ID), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setThrottleNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "throttleNumValue");
        if (Controller.THROTTLE_NUM != value) {
            Controller.setThrottleNum(value);
            return 1;
        }
        return 0;
    }

    private static int getThrottleNum(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("throttleNum: " + Controller.THROTTLE_NUM), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setPitchNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "pitchNumValue");
        if (Controller.PITCH_NUM != value) {
            Controller.setPitchNum(value);
            return 1;
        }
        return 0;
    }

    private static int getPitchNum(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("pitchNum: " + Controller.PITCH_NUM), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setYawNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "yawNumValue");
        if (Controller.YAW_NUM != value) {
            Controller.setYawNum(value);
            return 1;
        }
        return 0;
    }

    private static int getYawNum(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("yawNum: " + Controller.YAW_NUM), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setRollNum(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "rollNumValue");
        if (value != Controller.ROLL_NUM) {
            Controller.setRollNum(value);
            return 1;
        }
        return 0;
    }

    private static int getRollNum(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("rollNum: " + Controller.ROLL_NUM), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDeadzone(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "deadzoneValue");
        if (Controller.DEADZONE != value) {
            Controller.setDeadzone(value);
            return 1;
        }
        return 0;
    }

    private static int getDeadzone(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("deadzone: " + Controller.DEADZONE), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setThrottleCenterPosition(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "throttleCenterPositionValue");
        if (Controller.THROTTLE_CENTER_POSITION != value) {
            Controller.setThrottleCenterPosition(value);
            return 1;
        }
        return 0;
    }

    private static int getThrottleCenterPosition(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("throttleCenterPosition: " + Controller.THROTTLE_CENTER_POSITION), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setRate(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "rateValue");
        if (Controller.RATE != value) {
            Controller.setRate(value);
            return 1;
        }
        return 0;
    }

    private static int getRate(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("rate: " + Controller.RATE), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setSuperRate(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "superRateValue");
        if (Controller.SUPER_RATE != value) {
            Controller.setSuperRate(value);
            return 1;
        }
        return 0;
    }

    private static int getSuperRate(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("superRate: " + Controller.SUPER_RATE), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setExpo(CommandContext<ServerCommandSource> context) {
        final float value = FloatArgumentType.getFloat(context, "expoValue");
        if (Controller.EXPO != value) {
            Controller.setExpo(value);
            return 1;
        }
        return 0;
    }

    private static int getExpo(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("expo: " + Controller.EXPO), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertThrottle(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertThrottleValue");
        if (Controller.INVERT_THROTTLE != value) {
            Controller.setInvertThrottle(value);
            return 1;
        }
        return 0;
    }

    private static int getInvertThrottle(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("invertThrottle: " + Controller.INVERT_THROTTLE), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertPitch(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertPitchValue");
        if (Controller.INVERT_PITCH != value) {
            Controller.setInvertPitch(value);
            return 1;
        }
        return 0;
    }

    private static int getInvertPitch(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("invertPitch: " + Controller.INVERT_PITCH), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertYaw(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertYawValue");
        if (Controller.INVERT_YAW != value) {
            Controller.setInvertYaw(value);
            return 1;
        }
        return 0;
    }

    private static int getInvertYaw(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("invertYaw: " + Controller.INVERT_YAW), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertRoll(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "invertRollValue");
        if (Controller.INVERT_ROLL != value) {
            Controller.setInvertRoll(value);
            return 1;
        }
        return 0;
    }

    private static int getInvertRoll(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getPlayer().sendMessage(new TranslatableText("invertRoll: " + Controller.INVERT_ROLL), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDroneBand(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "droneBandValue");
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setBand(selectedItem, value);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int getDroneBand(CommandContext<ServerCommandSource> context) {
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                context.getSource().getPlayer().sendMessage(new TranslatableText("droneBand: " + DroneSpawnerItem.getBand(selectedItem)), false);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int setDroneChannel(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "droneChannelValue");
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setChannel(selectedItem, value);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int getDroneChannel(CommandContext<ServerCommandSource> context) {
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                context.getSource().getPlayer().sendMessage(new TranslatableText("droneChannel: " + DroneSpawnerItem.getChannel(selectedItem)), false);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int setCameraAngle(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "angleValue");
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setCameraAngle(selectedItem, value);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int getCameraAngle(CommandContext<ServerCommandSource> context) {
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                context.getSource().getPlayer().sendMessage(new TranslatableText("cameraAngle: " + DroneSpawnerItem.getCameraAngle(selectedItem)), false);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int setGogglesBand(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "gogglesBandValue");
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                GogglesItem.setBand(selectedItem, value, context.getSource().getPlayer());
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int getGogglesBand(CommandContext<ServerCommandSource> context) {
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                context.getSource().getPlayer().sendMessage(new TranslatableText("gogglesBand: " + GogglesItem.getBand(selectedItem)), false);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int setGogglesChannel(CommandContext<ServerCommandSource> context) {
        final int value = IntegerArgumentType.getInteger(context, "gogglesChannelValue");
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                GogglesItem.setChannel(selectedItem, value, context.getSource().getPlayer());
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int getGogglesChannel(CommandContext<ServerCommandSource> context) {
        try {
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                context.getSource().getPlayer().sendMessage(new TranslatableText("gogglesChannel: " + GogglesItem.getChannel(selectedItem)), false);
                return 1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
