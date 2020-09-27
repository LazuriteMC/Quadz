package bluevista.fpvracing.server;

import bluevista.fpvracing.server.entities.DroneEntity;
import bluevista.fpvracing.network.config.ConfigS2C;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.items.DroneSpawnerItem;
import bluevista.fpvracing.server.items.GogglesItem;
import bluevista.fpvracing.server.items.TransmitterItem;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import java.io.*;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class Commands {

    private static class NumberArgumentType implements ArgumentType<Number> {

        static final String NAME = "number";

        @Override
        public Number parse(StringReader reader) {
            final int start = reader.getCursor();
            while (reader.canRead() && StringReader.isAllowedNumber(reader.peek())) {
                reader.skip();
            }
            final String string = reader.getString().substring(start, reader.getCursor());

            try {
                return NumberFormat.getInstance().parse(string);
            } catch (ParseException e) {
                reader.setCursor(start);
                e.printStackTrace();
                return null; // 0?
            }
        }

        @Override
        public Collection<String> getExamples() {
            return null; // no examples, figure it out :)
        }

        public static Number getNumber(CommandContext<ServerCommandSource> context, final String name) {
            return context.getArgument(name, Number.class);
        }
    }

    private static final String COLON_SPACE = ": ";
    private static JsonObject MESSAGES = null;

    public static void registerCommands() {

        try {
            MESSAGES = (JsonObject) new JsonParser().parse(new InputStreamReader(Commands.class.getResourceAsStream("/fpvracing.messages.json")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArgumentTypes.register(NumberArgumentType.NAME, NumberArgumentType.class, new ConstantArgumentSerializer(NumberArgumentType::new));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal(ServerInitializer.MODID)

            .then(CommandManager.literal(Config.HELP)
                .executes(context -> help(context, ServerInitializer.MODID)))

            .then(CommandManager.literal(Config.CONFIG)

                .then(CommandManager.literal(Config.HELP)
                    .executes(context -> help(context, Config.CONFIG)))

                .then(CommandManager.literal(Config.CONTROLLER_ID)
                    .then(CommandManager.argument(Config.CONTROLLER_ID, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.CONTROLLER_ID)))
                    .executes(context -> getConfigValue(context, Config.CONTROLLER_ID)))

                .then(CommandManager.literal(Config.THROTTLE)
                    .then(CommandManager.argument(Config.THROTTLE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.THROTTLE)))
                    .executes(context -> getConfigValue(context, Config.THROTTLE)))

                .then(CommandManager.literal(Config.PITCH)
                    .then(CommandManager.argument(Config.PITCH, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.PITCH)))
                    .executes(context -> getConfigValue(context, Config.PITCH)))

                .then(CommandManager.literal(Config.YAW)
                    .then(CommandManager.argument(Config.YAW, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.YAW)))
                    .executes(context -> getConfigValue(context, Config.YAW)))

                .then(CommandManager.literal(Config.ROLL)
                    .then(CommandManager.argument(Config.ROLL, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.ROLL)))
                    .executes(context -> getConfigValue(context, Config.ROLL)))

                .then(CommandManager.literal(Config.DEADZONE)
                    .then(CommandManager.argument(Config.DEADZONE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.DEADZONE)))
                    .executes(context -> getConfigValue(context, Config.DEADZONE)))

                .then(CommandManager.literal(Config.THROTTLE_CENTER_POSITION)
                    .then(CommandManager.argument(Config.THROTTLE_CENTER_POSITION, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.THROTTLE_CENTER_POSITION)))
                    .executes(context -> getConfigValue(context, Config.THROTTLE_CENTER_POSITION)))

                .then(CommandManager.literal(Config.RATE)
                    .then(CommandManager.argument(Config.RATE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.RATE)))
                    .executes(context -> getConfigValue(context, Config.RATE)))

                .then(CommandManager.literal(Config.SUPER_RATE)
                    .then(CommandManager.argument(Config.SUPER_RATE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.SUPER_RATE)))
                    .executes(context -> getConfigValue(context, Config.SUPER_RATE)))

                .then(CommandManager.literal(Config.EXPO)
                    .then(CommandManager.argument(Config.EXPO, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.EXPO)))
                    .executes(context -> getConfigValue(context, Config.EXPO)))

                .then(CommandManager.literal(Config.INVERT_THROTTLE)
                    .then(CommandManager.argument(Config.INVERT_THROTTLE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.INVERT_THROTTLE)))
                    .executes(context -> getConfigValue(context, Config.INVERT_THROTTLE)))

                .then(CommandManager.literal(Config.INVERT_PITCH)
                    .then(CommandManager.argument(Config.INVERT_PITCH, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.INVERT_PITCH)))
                    .executes(context -> getConfigValue(context, Config.INVERT_PITCH)))

                .then(CommandManager.literal(Config.INVERT_YAW)
                    .then(CommandManager.argument(Config.INVERT_YAW, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.INVERT_YAW)))
                    .executes(context -> getConfigValue(context, Config.INVERT_YAW)))

                .then(CommandManager.literal(Config.INVERT_ROLL)
                    .then(CommandManager.argument(Config.INVERT_ROLL, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.INVERT_ROLL)))
                    .executes(context -> getConfigValue(context, Config.INVERT_ROLL)))

                .then(CommandManager.literal(Config.CAMERA_ANGLE)
                    .then(CommandManager.argument(Config.CAMERA_ANGLE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.CAMERA_ANGLE)))
                    .executes(context -> getConfigValue(context, Config.CAMERA_ANGLE)))

                .then(CommandManager.literal(Config.FIELD_OF_VIEW)
                    .then(CommandManager.argument(Config.FIELD_OF_VIEW, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.FIELD_OF_VIEW)))
                    .executes(context -> getConfigValue(context, Config.FIELD_OF_VIEW)))

                .then(CommandManager.literal(Config.BAND)
                    .then(CommandManager.argument(Config.BAND, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.BAND)))
                    .executes(context -> getConfigValue(context, Config.BAND)))

                .then(CommandManager.literal(Config.CHANNEL)
                    .then(CommandManager.argument(Config.CHANNEL, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.CHANNEL)))
                    .executes(context -> getConfigValue(context, Config.CHANNEL)))

                .then(CommandManager.literal(Config.MASS)
                    .then(CommandManager.argument(Config.MASS, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.MASS)))
                    .executes(context -> getConfigValue(context, Config.MASS)))

                .then(CommandManager.literal(Config.DRAG_COEFFICIENT)
                    .then(CommandManager.argument(Config.DRAG_COEFFICIENT, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.DRAG_COEFFICIENT)))
                    .executes(context -> getConfigValue(context, Config.DRAG_COEFFICIENT)))

                .then(CommandManager.literal(Config.SIZE)
                    .then(CommandManager.argument(Config.SIZE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.SIZE)))
                    .executes(context -> getConfigValue(context, Config.SIZE)))

                .then(CommandManager.literal(Config.THRUST)
                    .then(CommandManager.argument(Config.THRUST, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.THRUST)))
                    .executes(context -> getConfigValue(context, Config.THRUST)))

                .then(CommandManager.literal(Config.THRUST_CURVE)
                    .then(CommandManager.argument(Config.THRUST_CURVE, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.THRUST_CURVE)))
                    .executes(context -> getConfigValue(context, Config.THRUST_CURVE)))

                .then(CommandManager.literal(Config.DAMAGE_COEFFICIENT)
                    .then(CommandManager.argument(Config.DAMAGE_COEFFICIENT, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.DAMAGE_COEFFICIENT)))
                    .executes(context -> getConfigValue(context, Config.DAMAGE_COEFFICIENT)))

//                .then(CommandManager.literal(Config.CRASH_MOMENTUM_THRESHOLD)
//                    .then(CommandManager.argument(Config.CRASH_MOMENTUM_THRESHOLD, new NumberArgumentType())
//                        .executes(context -> setConfigValue(context, Config.CRASH_MOMENTUM_THRESHOLD)))
//                    .executes(context -> getConfigValue(context, Config.CRASH_MOMENTUM_THRESHOLD)))

                .then(CommandManager.literal(Config.GRAVITY)
                    .then(CommandManager.argument(Config.GRAVITY, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.GRAVITY)))
                    .executes(context -> getConfigValue(context, Config.GRAVITY)))

                .then(CommandManager.literal(Config.BLOCK_RADIUS)
                    .then(CommandManager.argument(Config.BLOCK_RADIUS, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.BLOCK_RADIUS)))
                    .executes(context -> getConfigValue(context, Config.BLOCK_RADIUS)))

                .then(CommandManager.literal(Config.AIR_DENSITY)
                    .then(CommandManager.argument(Config.AIR_DENSITY, new NumberArgumentType())
                        .executes(context -> setConfigValue(context, Config.AIR_DENSITY)))
                    .executes(context -> getConfigValue(context, Config.AIR_DENSITY)))

                .then(CommandManager.literal(Config.WRITE)
                    .executes(context -> config(context, Config.WRITE)))

                .then(CommandManager.literal(Config.REVERT)
                    .executes(context -> config(context, Config.REVERT)))

                )

            .then(CommandManager.literal(Config.DRONE)

                .then(CommandManager.literal(Config.HELP)
                    .executes(context -> help(context, Config.DRONE)))

                .then(CommandManager.literal(Config.BAND)
                    .then(CommandManager.argument(Config.BAND, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.BAND)))
                    .executes(context -> getDroneValue(context, Config.BAND)))

                .then(CommandManager.literal(Config.CHANNEL)
                    .then(CommandManager.argument(Config.CHANNEL, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.CHANNEL)))
                    .executes(context -> getDroneValue(context, Config.CHANNEL)))

                .then(CommandManager.literal(Config.CAMERA_ANGLE)
                    .then(CommandManager.argument(Config.CAMERA_ANGLE, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.CAMERA_ANGLE)))
                    .executes(context -> getDroneValue(context, Config.CAMERA_ANGLE)))

                .then(CommandManager.literal(Config.FIELD_OF_VIEW)
                    .then(CommandManager.argument(Config.FIELD_OF_VIEW, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.FIELD_OF_VIEW)))
                    .executes(context -> getDroneValue(context, Config.FIELD_OF_VIEW)))

                .then(CommandManager.literal(Config.MASS)
                    .then(CommandManager.argument(Config.MASS, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.MASS)))
                    .executes(context -> getDroneValue(context, Config.MASS)))

                .then(CommandManager.literal(Config.DRAG_COEFFICIENT)
                    .then(CommandManager.argument(Config.DRAG_COEFFICIENT, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.DRAG_COEFFICIENT)))
                    .executes(context -> getDroneValue(context, Config.DRAG_COEFFICIENT)))

                .then(CommandManager.literal(Config.SIZE)
                    .then(CommandManager.argument(Config.SIZE, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.SIZE)))
                    .executes(context -> getDroneValue(context, Config.SIZE)))

                .then(CommandManager.literal(Config.THRUST)
                    .then(CommandManager.argument(Config.THRUST, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.THRUST)))
                    .executes(context -> getDroneValue(context, Config.THRUST)))

                .then(CommandManager.literal(Config.THRUST_CURVE)
                    .then(CommandManager.argument(Config.THRUST_CURVE, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.THRUST_CURVE)))
                    .executes(context -> getDroneValue(context, Config.THRUST_CURVE)))

                .then(CommandManager.literal(Config.DAMAGE_COEFFICIENT)
                    .then(CommandManager.argument(Config.DAMAGE_COEFFICIENT, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.DAMAGE_COEFFICIENT)))
                    .executes(context -> getDroneValue(context, Config.DAMAGE_COEFFICIENT)))

//                .then(CommandManager.literal(Config.CRASH_MOMENTUM_THRESHOLD)
//                    .then(CommandManager.argument(Config.CRASH_MOMENTUM_THRESHOLD, new NumberArgumentType())
//                        .executes(context -> setDroneValue(context, Config.CRASH_MOMENTUM_THRESHOLD)))
//                    .executes(context -> getDroneValue(context, Config.CRASH_MOMENTUM_THRESHOLD)))

                .then(CommandManager.literal(Config.RATE)
                    .then(CommandManager.argument(Config.RATE, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.RATE)))
                    .executes(context -> getDroneValue(context, Config.RATE)))

                .then(CommandManager.literal(Config.SUPER_RATE)
                    .then(CommandManager.argument(Config.SUPER_RATE, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.SUPER_RATE)))
                    .executes(context -> getDroneValue(context, Config.SUPER_RATE)))

                .then(CommandManager.literal(Config.EXPO)
                    .then(CommandManager.argument(Config.EXPO, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.EXPO)))
                    .executes(context -> getDroneValue(context, Config.EXPO)))

                .then(CommandManager.literal(Config.NO_CLIP)
                    .then(CommandManager.argument(Config.NO_CLIP, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.NO_CLIP)))
                    .executes(context -> getDroneValue(context, Config.NO_CLIP)))

                .then(CommandManager.literal(Config.GOD_MODE)
                    .then(CommandManager.argument(Config.GOD_MODE, new NumberArgumentType())
                        .executes(context -> setDroneValue(context, Config.GOD_MODE)))
                    .executes(context -> getDroneValue(context, Config.GOD_MODE)))

                )

            .then(CommandManager.literal(Config.GOGGLES)

                .then(CommandManager.literal(Config.HELP)
                    .executes(context -> help(context, Config.GOGGLES)))

                .then(CommandManager.literal(Config.BAND)
                    .then(CommandManager.argument(Config.BAND, new NumberArgumentType())
                        .executes(context -> setGogglesValue(context, Config.BAND)))
                    .executes(context -> getGogglesValue(context, Config.BAND)))

                .then(CommandManager.literal(Config.CHANNEL)
                    .then(CommandManager.argument(Config.CHANNEL, new NumberArgumentType())
                        .executes(context -> setGogglesValue(context, Config.CHANNEL)))
                    .executes(context -> getGogglesValue(context, Config.CHANNEL)))

                )
        ));
    }

    private static int setConfigValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(player.getUuid());

            Number value = NumberArgumentType.getNumber(context, key);

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
            final Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(player.getUuid());

            player.sendMessage(new LiteralText(key + COLON_SPACE + config.getOption(key)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setDroneValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            Number value = NumberArgumentType.getNumber(context, key);
            final ItemStack stack = player.getMainHandStack();

            if (stack.getItem() instanceof DroneSpawnerItem) {
                if (value.equals(DroneSpawnerItem.getTagValue(stack, key))) {
                    getDroneValue(context, key);
                    return 0;
                } else {
                    DroneSpawnerItem.setTagValue(stack, key, value);
                    getDroneValue(context, key);
                    return 1;
                }
            } else if (stack.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(stack, player);
                if (drone == null || value.equals(drone.getConfigValues(key))) {
                    getDroneValue(context, key);
                    return 0;
                } else {
                    drone.setConfigValues(key, value);
                    getDroneValue(context, key);
                    return 1;
                }
            }

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(Config.ERROR).getAsJsonObject(Config.DRONE).toString()), false);
            }
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
                player.sendMessage(new LiteralText(key + COLON_SPACE + DroneSpawnerItem.getTagValue(stack, key)), false);
                return 1;
            } else if (stack.getItem() instanceof TransmitterItem) {
                DroneEntity drone = TransmitterItem.droneFromTransmitter(stack, player);
                if (drone != null) {
                    player.sendMessage(new LiteralText(key + COLON_SPACE + drone.getConfigValues(key)), false);
                    return 1;
                }
            }

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(Config.ERROR).getAsJsonObject(Config.DRONE).toString()), false);
            }
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int setGogglesValue(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            Number value = NumberArgumentType.getNumber(context, key);
            final ItemStack stack = player.getMainHandStack();
            final ItemStack helmet = player.inventory.armor.get(3);

            if (stack.getItem() instanceof GogglesItem) {
                if (value.equals(GogglesItem.getTagValue(stack, key))) {
                    getGogglesValue(context, key);
                    return 0;
                } else {
                    GogglesItem.setTagValue(stack, key, value);
                    getGogglesValue(context, key);
                    return 1;
                }
            } else if (helmet.getItem() instanceof GogglesItem) {
                if (value.equals(GogglesItem.getTagValue(helmet, key))) {
                    getGogglesValue(context, key);
                    return 0;
                } else {
                    GogglesItem.setTagValue(helmet, key, value);
                    getGogglesValue(context, key);
                    return 1;
                }
            }

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(Config.ERROR).getAsJsonObject(Config.GOGGLES).toString()), false);
            }
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
                player.sendMessage(new LiteralText(key + COLON_SPACE + GogglesItem.getTagValue(stack, key)), false);
                return 1;
            } else if (helmet.getItem() instanceof GogglesItem) {
                player.sendMessage(new LiteralText(key + COLON_SPACE + GogglesItem.getTagValue(helmet, key)), false);
                return 1;
            }

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(Config.ERROR).getAsJsonObject(Config.GOGGLES).toString()), false);
            }
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int config(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            ConfigS2C.send(player, key);

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(Config.SUCCESS).getAsJsonObject(key).toString()), false);
            }
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int help(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            if (MESSAGES != null) {
                final String message = MESSAGES.getAsJsonObject(Config.HELP).getAsJsonArray(key).toString();
                player.sendMessage(Text.Serializer.fromJson(message), false);
                return 1;
            } else {
                return -1;
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
