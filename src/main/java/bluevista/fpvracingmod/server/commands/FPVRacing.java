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
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;

public class FPVRacing {

    // These are static strings that aren't available for access from Config (i.e. Config.THROTTLE_NUM)
    private static final String DRONE_BAND = "droneBand";
    private static final String DRONE_CHANNEL = "droneChannel";
    private static final String DRONE_CAMERA_ANGLE = "droneCameraAngle";
    private static final String GOGGLES_BAND = "gogglesBand";
    private static final String GOGGLES_CHANNEL = "gogglesChannel";
    private static final String DEFAULT_CAMERA_ANGLE = "defaultCameraAngle";
    private static final String DEFAULT_BAND = "defaultBand";
    private static final String DEFAULT_CHANNEL = "defaultChannel";
    private static final String WRITE_CONFIG = "writeConfig";

    private static final MutableText DRONE_ERROR_MESSAGE = new TranslatableText("Must be holding a ")
            .append(new TranslatableText("item.fpvracing.drone_spawner_item"))
            .append(" or a bound ")
            .append(new TranslatableText("item.fpvracing.transmitter_item"));

    private static final MutableText GOGGLES_ERROR_MESSAGE = new TranslatableText("Must be holding or wearing ")
            .append(new TranslatableText("item.fpvracing.goggles_item"));

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal(ServerInitializer.MODID)
                    .then(CommandManager.literal(Config.CONTROLLER_ID)
                            .then(CommandManager.argument(Config.CONTROLLER_ID, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.CONTROLLER_ID)))
                            .executes(context -> getConfigValue(context, Config.CONTROLLER_ID)))

                    .then(CommandManager.literal(Config.THROTTLE)
                            .then(CommandManager.argument(Config.THROTTLE, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.THROTTLE)))
                            .executes(context -> getConfigValue(context, Config.THROTTLE)))

                    .then(CommandManager.literal(Config.PITCH)
                            .then(CommandManager.argument(Config.PITCH, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.PITCH)))
                            .executes(context -> getConfigValue(context, Config.PITCH)))

                    .then(CommandManager.literal(Config.PITCH)
                            .then(CommandManager.argument(Config.PITCH, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.PITCH)))
                            .executes(context -> getConfigValue(context, Config.PITCH)))

                    .then(CommandManager.literal(Config.ROLL)
                            .then(CommandManager.argument(Config.ROLL, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.ROLL)))
                            .executes(context -> getConfigValue(context, Config.ROLL)))

                    .then(CommandManager.literal(Config.DEADZONE)
                            .then(CommandManager.argument(Config.DEADZONE, FloatArgumentType.floatArg(0, 1))
                                    .executes(context -> setConfigValue(context, Config.DEADZONE)))
                            .executes(context -> getConfigValue(context, Config.DEADZONE)))

                    .then(CommandManager.literal(Config.THROTTLE_CENTER_POSITION)
                            .then(CommandManager.argument(Config.THROTTLE_CENTER_POSITION, IntegerArgumentType.integer(0, 1))
                                    .executes(context -> setConfigValue(context, Config.THROTTLE_CENTER_POSITION)))
                            .executes(context -> getConfigValue(context, Config.THROTTLE_CENTER_POSITION)))

                    .then(CommandManager.literal(Config.RATE)
                            .then(CommandManager.argument(Config.RATE, FloatArgumentType.floatArg(0)) // max value?
                                    .executes(context -> setConfigValue(context, Config.RATE)))
                            .executes(context -> getConfigValue(context, Config.RATE)))

                    .then(CommandManager.literal(Config.SUPER_RATE)
                            .then(CommandManager.argument(Config.SUPER_RATE, FloatArgumentType.floatArg(0)) // max value?
                                    .executes(context -> setConfigValue(context, Config.SUPER_RATE)))
                            .executes(context -> getConfigValue(context, Config.SUPER_RATE)))

                    .then(CommandManager.literal(Config.EXPO)
                            .then(CommandManager.argument(Config.EXPO, FloatArgumentType.floatArg(0)) // max value?
                                    .executes(context -> setConfigValue(context, Config.EXPO)))
                            .executes(context -> getConfigValue(context, Config.EXPO)))

                    .then(CommandManager.literal(Config.INVERT_THROTTLE)
                            .then(CommandManager.argument(Config.INVERT_THROTTLE, IntegerArgumentType.integer(0, 1))
                                    .executes(context -> setConfigValue(context, Config.INVERT_THROTTLE)))
                            .executes(context -> getConfigValue(context, Config.INVERT_THROTTLE)))

                    .then(CommandManager.literal(Config.INVERT_PITCH)
                            .then(CommandManager.argument(Config.INVERT_PITCH, IntegerArgumentType.integer(0, 1))
                                    .executes(context -> setConfigValue(context, Config.INVERT_PITCH)))
                            .executes(context -> getConfigValue(context, Config.INVERT_PITCH)))

                    .then(CommandManager.literal(Config.INVERT_YAW)
                            .then(CommandManager.argument(Config.INVERT_YAW, IntegerArgumentType.integer(0, 1))
                                    .executes(context -> setConfigValue(context, Config.INVERT_YAW)))
                            .executes(context -> getConfigValue(context, Config.INVERT_YAW)))

                    .then(CommandManager.literal(Config.INVERT_ROLL)
                            .then(CommandManager.argument(Config.INVERT_ROLL, IntegerArgumentType.integer(0, 1))
                                    .executes(context -> setConfigValue(context, Config.INVERT_ROLL)))
                            .executes(context -> getConfigValue(context, Config.INVERT_ROLL)))

                    .then(CommandManager.literal(DRONE_BAND) // different literal
                            .then(CommandManager.argument(Config.BAND, IntegerArgumentType.integer(0))
                                    .executes(context -> setDroneValue(context, Config.BAND)))
                            .executes(context -> getDroneValue(context, Config.BAND)))

                    .then(CommandManager.literal(DRONE_CHANNEL) // different literal
                            .then(CommandManager.argument(Config.CHANNEL, IntegerArgumentType.integer(0))
                                    .executes(context -> setDroneValue(context, Config.CHANNEL)))
                            .executes(context -> getDroneValue(context, Config.CHANNEL)))

                    .then(CommandManager.literal(DRONE_CAMERA_ANGLE) // different literal
                            .then(CommandManager.argument(Config.CAMERA_ANGLE, IntegerArgumentType.integer(0))
                                    .executes(context -> setDroneValue(context, Config.CAMERA_ANGLE)))
                            .executes(context -> getDroneValue(context, Config.CAMERA_ANGLE)))

                    .then(CommandManager.literal(GOGGLES_BAND) // different literal
                            .then(CommandManager.argument(Config.BAND, IntegerArgumentType.integer(0))
                                    .executes(context -> setGogglesValue(context, Config.BAND)))
                            .executes(context -> getGogglesValue(context, Config.BAND)))

                    .then(CommandManager.literal(GOGGLES_CHANNEL) // different literal
                            .then(CommandManager.argument(Config.CHANNEL, IntegerArgumentType.integer(0))
                                    .executes(context -> setGogglesValue(context, Config.CHANNEL)))
                            .executes(context -> getGogglesValue(context, Config.CHANNEL)))

                    .then(CommandManager.literal(DEFAULT_CAMERA_ANGLE) // different literal
                            .then(CommandManager.argument(Config.CAMERA_ANGLE, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.CAMERA_ANGLE)))
                            .executes(context -> getConfigValue(context, Config.CAMERA_ANGLE)))

                    .then(CommandManager.literal(DEFAULT_BAND) // different literal
                            .then(CommandManager.argument(Config.BAND, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.BAND)))
                            .executes(context -> getConfigValue(context, Config.BAND)))

                    .then(CommandManager.literal(DEFAULT_CHANNEL) // different literal
                            .then(CommandManager.argument(Config.CHANNEL, IntegerArgumentType.integer(0))
                                    .executes(context -> setConfigValue(context, Config.CHANNEL)))
                            .executes(context -> getConfigValue(context, Config.CHANNEL)))

                    .then(CommandManager.literal(WRITE_CONFIG)
                            .executes(FPVRacing::writeConfig)));
        });
    }

    private static int setConfigValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config config = ServerInitializer.serverPlayerConfigs.get(player.getUuid());

            Number value = null;

            if (Config.INT_KEYS.contains(key)) {
                value = IntegerArgumentType.getInteger(context, key);
            } else if (Config.FLOAT_KEYS.contains(key)) {
                value = FloatArgumentType.getFloat(context, key);
            }

            if (value == null || config.getOption(key).equals(value)) {
                getConfigValue(context, key);
                return 0;
            } else {
                config.setOption(key, value);
                ConfigS2C.send(player, key);
                getConfigValue(context, key);
                return 1;
            }

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getConfigValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config config = ServerInitializer.serverPlayerConfigs.get(player.getUuid());

            player.sendMessage(new TranslatableText(key + ": " + config.getOption(key)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDroneValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            int value = IntegerArgumentType.getInteger(context, key);
            final ItemStack stack = player.getMainHandStack();

            if (stack.getItem() instanceof DroneSpawnerItem) {
                if (DroneSpawnerItem.getValue(stack, key) == value) {
                    getDroneValue(context, key);
                    return 0;
                } else {
                    DroneSpawnerItem.setBand(stack, value);
                    getDroneValue(context, key);
                    return 1;
                }
            } else if (stack.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(stack, player);
                if (drone == null || drone.getValue(key) == value) {
                    getDroneValue(context, key);
                    return 0;
                } else {
                    drone.setValue(key, value);
                    getDroneValue(context, key);
                    return 1;
                }
            }
            player.sendMessage(DRONE_ERROR_MESSAGE, false);
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getDroneValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack stack = player.getMainHandStack();

            if (stack.getItem() instanceof DroneSpawnerItem) {
                player.sendMessage(new TranslatableText(key + ": " + DroneSpawnerItem.getValue(stack, key)), false);
                return 1;
            } else if (stack.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(stack, player);
                if (drone != null) {
                    player.sendMessage(new TranslatableText(key + ": " + drone.getValue(key)), false);
                    return 1;
                }
            }
            player.sendMessage(DRONE_ERROR_MESSAGE, false);
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setGogglesValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            int value = IntegerArgumentType.getInteger(context, key);
            final ItemStack stack = player.getMainHandStack();
            final ItemStack helmet = player.inventory.armor.get(3);

            if (stack.getItem() instanceof GogglesItem) {
                if (GogglesItem.getValue(stack, key) == value) {
                    getGogglesValue(context, key);
                    return 0;
                } else {
                    GogglesItem.setValue(stack, key, value);
                    getGogglesValue(context, key);
                    return 1;
                }
            } else if (helmet.getItem() instanceof GogglesItem) {
                if (GogglesItem.getValue(helmet, key) == value) {
                    getGogglesValue(context, key);
                    return 0;
                } else {
                    GogglesItem.setValue(stack, key, value);
                    getGogglesValue(context, key);
                    return 1;
                }
            }
            player.sendMessage(GOGGLES_ERROR_MESSAGE, false);
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getGogglesValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack stack = player.getMainHandStack();
            final ItemStack helmet = player.inventory.armor.get(3);

            if (stack.getItem() instanceof GogglesItem) {
                player.sendMessage(new TranslatableText(key + ": " + GogglesItem.getValue(stack, key)), false);
                return 1;
            } else if (helmet.getItem() instanceof GogglesItem) {
                player.sendMessage(new TranslatableText(key + ": " + GogglesItem.getValue(stack, key)), false);
                return 1;
            }
            player.sendMessage(GOGGLES_ERROR_MESSAGE, false);
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int writeConfig(CommandContext<ServerCommandSource> context) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            ConfigS2C.send(player, WRITE_CONFIG);
            player.sendMessage(new TranslatableText("Successfully wrote config"), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // TODO: Revert command
}
