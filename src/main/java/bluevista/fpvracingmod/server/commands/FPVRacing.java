package bluevista.fpvracingmod.server.commands;

import bluevista.fpvracingmod.server.entities.DroneEntity;
import bluevista.fpvracingmod.network.ConfigS2C;
import bluevista.fpvracingmod.config.Config;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.items.DroneSpawnerItem;
import bluevista.fpvracingmod.server.items.GogglesItem;
import bluevista.fpvracingmod.server.items.TransmitterItem;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class FPVRacing {

    private static final String DRONE_BAND = "droneBand";
    private static final String DRONE_CHANNEL = "droneChannel";
    private static final String CAMERA_ANGLE = "cameraAngle";
    private static final String GOGGLES_BAND = "gogglesBand";
    private static final String GOGGLES_CHANNEL = "gogglesChannel";
    private static final String DEFAULT_CAMERA_ANGLE = "defaultCameraAngle";
    private static final String DEFAULT_BAND = "defaultBand";
    private static final String DEFAULT_CHANNEL = "defaultChannel";
    private static final String WRITE_CONFIG = "writeConfig";

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal(ServerInitializer.MODID)
                    .then(CommandManager.literal(Config.CONTROLLER_ID)
                            .then(CommandManager.argument(Config.CONTROLLER_ID, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setControllerId))
                            .executes(FPVRacing::getControllerId))

                    .then(CommandManager.literal(Config.THROTTLE_NUM)
                            .then(CommandManager.argument(Config.THROTTLE_NUM, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setThrottleNum))
                            .executes(FPVRacing::getThrottleNum))

                    .then(CommandManager.literal(Config.PITCH_NUM)
                            .then(CommandManager.argument(Config.PITCH_NUM, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setPitchNum))
                            .executes(FPVRacing::getPitchNum))

                    .then(CommandManager.literal(Config.PITCH_NUM)
                            .then(CommandManager.argument(Config.PITCH_NUM, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setYawNum))
                            .executes(FPVRacing::getYawNum))

                    .then(CommandManager.literal(Config.ROLL_NUM)
                            .then(CommandManager.argument(Config.ROLL_NUM, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setRollNum))
                            .executes(FPVRacing::getRollNum))

                    .then(CommandManager.literal(Config.DEADZONE)
                            .then(CommandManager.argument(Config.DEADZONE, FloatArgumentType.floatArg(0, 1))
                                    .executes(FPVRacing::setDeadzone))
                            .executes(FPVRacing::getDeadzone))

                    .then(CommandManager.literal(Config.THROTTLE_CENTER_POSITION)
                            .then(CommandManager.argument(Config.THROTTLE_CENTER_POSITION, IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setThrottleCenterPosition))
                            .executes(FPVRacing::getThrottleCenterPosition))

                    .then(CommandManager.literal(Config.RATE)
                            .then(CommandManager.argument(Config.RATE, FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setRate))
                            .executes(FPVRacing::getRate))

                    .then(CommandManager.literal(Config.SUPER_RATE)
                            .then(CommandManager.argument(Config.SUPER_RATE, FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setSuperRate))
                            .executes(FPVRacing::getSuperRate))

                    .then(CommandManager.literal(Config.EXPO)
                            .then(CommandManager.argument(Config.EXPO, FloatArgumentType.floatArg(0)) // max value?
                                    .executes(FPVRacing::setExpo))
                            .executes(FPVRacing::getExpo))

                    .then(CommandManager.literal(Config.INVERT_THROTTLE)
                            .then(CommandManager.argument(Config.INVERT_THROTTLE, IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertThrottle))
                            .executes(FPVRacing::getInvertThrottle))

                    .then(CommandManager.literal(Config.INVERT_PITCH)
                            .then(CommandManager.argument(Config.INVERT_PITCH, IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertPitch))
                            .executes(FPVRacing::getInvertPitch))

                    .then(CommandManager.literal(Config.INVERT_YAW)
                            .then(CommandManager.argument(Config.INVERT_YAW, IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertYaw))
                            .executes(FPVRacing::getInvertYaw))

                    .then(CommandManager.literal(Config.INVERT_ROLL)
                            .then(CommandManager.argument(Config.INVERT_ROLL, IntegerArgumentType.integer(0, 1))
                                    .executes(FPVRacing::setInvertRoll))
                            .executes(FPVRacing::getInvertRoll))

                    .then(CommandManager.literal(DRONE_BAND)
                            .then(CommandManager.argument(DRONE_BAND, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setDroneBand))
                            .executes(FPVRacing::getDroneBand))

                    .then(CommandManager.literal(DRONE_CHANNEL)
                            .then(CommandManager.argument(DRONE_CHANNEL, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setDroneChannel))
                            .executes(FPVRacing::getDroneChannel))

                    .then(CommandManager.literal(CAMERA_ANGLE)
                            .then(CommandManager.argument(CAMERA_ANGLE, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setCameraAngle))
                            .executes(FPVRacing::getCameraAngle))

                    .then(CommandManager.literal(GOGGLES_BAND)
                            .then(CommandManager.argument(GOGGLES_BAND, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setGogglesBand))
                            .executes(FPVRacing::getGogglesBand))

                    .then(CommandManager.literal(GOGGLES_CHANNEL)
                            .then(CommandManager.argument(GOGGLES_CHANNEL, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setGogglesChannel))
                            .executes(FPVRacing::getGogglesChannel))

                    .then(CommandManager.literal(DEFAULT_CAMERA_ANGLE)
                            .then(CommandManager.argument(DEFAULT_CAMERA_ANGLE, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setDefaultCameraAngle))
                            .executes(FPVRacing::getDefaultCameraAngle))

                    .then(CommandManager.literal(DEFAULT_BAND)
                            .then(CommandManager.argument(DEFAULT_BAND, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setDefaultBand))
                            .executes(FPVRacing::getDefaultBand))

                    .then(CommandManager.literal(DEFAULT_CHANNEL)
                            .then(CommandManager.argument(DEFAULT_CHANNEL, IntegerArgumentType.integer(0))
                                    .executes(FPVRacing::setDefaultChannel))
                            .executes(FPVRacing::getDefaultChannel))

                    .then(CommandManager.literal(WRITE_CONFIG)
                            .executes(FPVRacing::writeConfig)));
        });
    }

    private static int setControllerId(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.CONTROLLER_ID);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.CONTROLLER_ID) != value) {
                serverPlayerConfig.setOption(Config.CONTROLLER_ID, value);
                ConfigS2C.send(player, Config.CONTROLLER_ID);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getControllerId(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.CONTROLLER_ID + ": " + serverPlayerConfig.getOption(Config.CONTROLLER_ID)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setThrottleNum(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.THROTTLE_NUM);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.THROTTLE_NUM) != value) {
                serverPlayerConfig.setOption(Config.THROTTLE_NUM, value);
                ConfigS2C.send(player, Config.THROTTLE_NUM);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getThrottleNum(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.THROTTLE_NUM + ": " + serverPlayerConfig.getOption(Config.THROTTLE_NUM)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setPitchNum(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.PITCH_NUM);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.PITCH_NUM) != value) {
                serverPlayerConfig.setOption(Config.PITCH_NUM, value);
                ConfigS2C.send(player, Config.PITCH_NUM);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getPitchNum(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.PITCH_NUM + ": " + serverPlayerConfig.getOption(Config.PITCH_NUM)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setYawNum(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.YAW_NUM);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.YAW_NUM) != value) {
                serverPlayerConfig.setOption(Config.YAW_NUM, value);
                ConfigS2C.send(player, Config.YAW_NUM);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getYawNum(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.YAW_NUM + ": " + serverPlayerConfig.getOption(Config.YAW_NUM)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            return -1;
        }
    }

    private static int setRollNum(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.ROLL_NUM);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.ROLL_NUM) != value) {
                serverPlayerConfig.setOption(Config.ROLL_NUM, value);
                ConfigS2C.send(player, Config.ROLL_NUM);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getRollNum(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.ROLL_NUM + ": " + serverPlayerConfig.getOption(Config.ROLL_NUM)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDeadzone(CommandContext<ServerCommandSource> context) {
        try {
            final float value = FloatArgumentType.getFloat(context, Config.DEADZONE);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getFloatOption(Config.DEADZONE) != value) {
                serverPlayerConfig.setOption(Config.DEADZONE, value);
                ConfigS2C.send(player, Config.DEADZONE);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getDeadzone(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.DEADZONE + ": " + serverPlayerConfig.getOption(Config.DEADZONE)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setThrottleCenterPosition(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.THROTTLE_CENTER_POSITION);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.THROTTLE_CENTER_POSITION) != value) {
                serverPlayerConfig.setOption(Config.THROTTLE_CENTER_POSITION, value);
                ConfigS2C.send(player, Config.THROTTLE_CENTER_POSITION);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getThrottleCenterPosition(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.THROTTLE_CENTER_POSITION + ": " + serverPlayerConfig.getOption(Config.THROTTLE_CENTER_POSITION)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setRate(CommandContext<ServerCommandSource> context) {
        try {
            final float value = FloatArgumentType.getFloat(context, Config.RATE);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getFloatOption(Config.RATE) != value) {
                serverPlayerConfig.setOption(Config.RATE, value);
                ConfigS2C.send(player, Config.RATE);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getRate(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.RATE + ": " + serverPlayerConfig.getOption(Config.RATE)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setSuperRate(CommandContext<ServerCommandSource> context) {
        try {
            final float value = FloatArgumentType.getFloat(context, Config.SUPER_RATE);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getFloatOption(Config.SUPER_RATE) != value) {
                serverPlayerConfig.setOption(Config.SUPER_RATE, value);
                ConfigS2C.send(player, Config.SUPER_RATE);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getSuperRate(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.SUPER_RATE + ": " + serverPlayerConfig.getOption(Config.SUPER_RATE)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setExpo(CommandContext<ServerCommandSource> context) {
        try {
            final float value = FloatArgumentType.getFloat(context, Config.EXPO);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getFloatOption(Config.EXPO) != value) {
                serverPlayerConfig.setOption(Config.EXPO, value);
                ConfigS2C.send(player, Config.EXPO);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getExpo(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.EXPO + ": " + serverPlayerConfig.getOption(Config.EXPO)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertThrottle(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.INVERT_THROTTLE);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.INVERT_THROTTLE) != value) {
                serverPlayerConfig.setOption(Config.INVERT_THROTTLE, value);
                ConfigS2C.send(player, Config.INVERT_THROTTLE);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getInvertThrottle(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.INVERT_THROTTLE + ": " + serverPlayerConfig.getOption(Config.INVERT_THROTTLE)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertPitch(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.INVERT_PITCH);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.INVERT_PITCH) != value) {
                serverPlayerConfig.setOption(Config.INVERT_PITCH, value);
                ConfigS2C.send(player, Config.INVERT_PITCH);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getInvertPitch(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.INVERT_PITCH + ": " + serverPlayerConfig.getOption(Config.INVERT_PITCH)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertYaw(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.INVERT_YAW);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.INVERT_YAW) != value) {
                serverPlayerConfig.setOption(Config.INVERT_YAW, value);
                ConfigS2C.send(player, Config.INVERT_YAW);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getInvertYaw(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.INVERT_YAW + ": " + serverPlayerConfig.getOption(Config.INVERT_YAW)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setInvertRoll(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, Config.INVERT_ROLL);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.INVERT_ROLL) != value) {
                serverPlayerConfig.setOption(Config.INVERT_ROLL, value);
                ConfigS2C.send(player, Config.INVERT_ROLL);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getInvertRoll(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(Config.INVERT_ROLL + ": " + serverPlayerConfig.getOption(Config.INVERT_ROLL)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDroneBand(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, DRONE_BAND);
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setBand(selectedItem, value);
                return 1;
            } else if(selectedItem.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(selectedItem, context.getSource().getPlayer());
                if(drone != null) {
                    drone.setBand(value);
                    return 1;
                }
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getDroneBand(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                player.sendMessage(new TranslatableText(DRONE_BAND + ": " + DroneSpawnerItem.getBand(selectedItem)), false);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDroneChannel(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, DRONE_CHANNEL);
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setChannel(selectedItem, value);
                return 1;
            } else if(selectedItem.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(selectedItem, context.getSource().getPlayer());
                if(drone != null) {
                    drone.setChannel(value);
                    return 1;
                }
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getDroneChannel(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                player.sendMessage(new TranslatableText(DRONE_CHANNEL + ": " + DroneSpawnerItem.getChannel(selectedItem)), false);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setCameraAngle(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, CAMERA_ANGLE);
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                DroneSpawnerItem.setCameraAngle(selectedItem, value);
                return 1;
            } else if(selectedItem.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(selectedItem, context.getSource().getPlayer());
                if(drone != null) {
                    drone.setCameraAngle(value);
                    return 1;
                }
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getCameraAngle(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof DroneSpawnerItem) {
                player.sendMessage(new TranslatableText(CAMERA_ANGLE + ": " + DroneSpawnerItem.getCameraAngle(selectedItem)), false);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setGogglesBand(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, GOGGLES_BAND);
            final ServerPlayerEntity player = context.getSource().getPlayer();

            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                GogglesItem.setBand(selectedItem, value, player);
                return 1;
            }

            final ItemStack hat = context.getSource().getPlayer().inventory.armor.get(3);
            if(hat.getItem() instanceof GogglesItem) {
                GogglesItem.setBand(hat, value, context.getSource().getPlayer());
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getGogglesBand(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                player.sendMessage(new TranslatableText(GOGGLES_BAND + ": " + GogglesItem.getBand(selectedItem)), false);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setGogglesChannel(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final int value = IntegerArgumentType.getInteger(context, GOGGLES_CHANNEL);

            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                GogglesItem.setBand(selectedItem, value, player);
                return 1;
            }

            final ItemStack hat = context.getSource().getPlayer().inventory.armor.get(3);
            if(hat.getItem() instanceof GogglesItem) {
                GogglesItem.setChannel(hat, value, context.getSource().getPlayer());
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getGogglesChannel(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack selectedItem = context.getSource().getPlayer().getMainHandStack();
            if (selectedItem.getItem() instanceof GogglesItem) {
                player.sendMessage(new TranslatableText(GOGGLES_CHANNEL + ": " + GogglesItem.getChannel(selectedItem)), false);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDefaultCameraAngle(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, DEFAULT_CAMERA_ANGLE);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.CAMERA_ANGLE) != value) {
                serverPlayerConfig.setOption(Config.CAMERA_ANGLE, value);
                ConfigS2C.send(player, Config.CAMERA_ANGLE);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getDefaultCameraAngle(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(DEFAULT_CAMERA_ANGLE + ": " + serverPlayerConfig.getOption(Config.CAMERA_ANGLE)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDefaultBand(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, DEFAULT_BAND);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.BAND) != value) {
                serverPlayerConfig.setOption(Config.BAND, value);
                ConfigS2C.send(player, Config.BAND);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getDefaultBand(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(DEFAULT_BAND + ": " + serverPlayerConfig.getOption(Config.BAND)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDefaultChannel(CommandContext<ServerCommandSource> context) {
        try {
            final int value = IntegerArgumentType.getInteger(context, DEFAULT_CHANNEL);
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            if (serverPlayerConfig.getIntOption(Config.CHANNEL) != value) {
                serverPlayerConfig.setOption(Config.CHANNEL, value);
                ConfigS2C.send(player, Config.CHANNEL);
                return 1;
            }
            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getDefaultChannel(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config serverPlayerConfig = ServerInitializer.serverPlayerConfigs.get(player.getUuid());
            player.sendMessage(new TranslatableText(DEFAULT_CHANNEL + ": " + serverPlayerConfig.getOption(Config.CHANNEL)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int writeConfig(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            ConfigS2C.send(player, WRITE_CONFIG);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // TODO: Revert command
}
