package dev.lazurite.fpvracing.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.network.packet.ConfigCommandS2C;
import dev.lazurite.fpvracing.network.packet.ConfigValueS2C;
import dev.lazurite.fpvracing.physics.PhysicsWorld;
import dev.lazurite.fpvracing.server.entity.FlyableEntity;
import dev.lazurite.fpvracing.server.entity.PhysicsEntity;
import dev.lazurite.fpvracing.server.item.GogglesItem;
import dev.lazurite.fpvracing.server.item.QuadcopterItem;
import dev.lazurite.fpvracing.server.item.TransmitterItem;
import dev.lazurite.fpvracing.network.tracker.Config;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import java.io.*;

public class Commands {
    private static final String COLON_SPACE = ": ";
    private static JsonObject MESSAGES = null;

    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    public static final String REVERT = "revert";
    public static final String WRITE = "write";
    public static final String HELP = "help";
    public static final String CONFIG = "config";

    public static final String GOGGLES = "goggles";
    public static final String DRONE = "drone";

    public static final String BAND = "band";
    public static final String CHANNEL = "channel";

    public static void register() {
        try {
            MESSAGES = (JsonObject) new JsonParser().parse(new InputStreamReader(Commands.class.getResourceAsStream("/fpvracing.messages.json")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal(ServerInitializer.MODID)

            .then(CommandManager.literal(HELP)
                .executes(context -> help(context, ServerInitializer.MODID)))

            .then(CommandManager.literal(CONFIG)

                .then(CommandManager.literal(HELP)
                    .executes(context -> help(context, CONFIG)))

                .then(CommandManager.literal(InputTick.CONTROLLER_ID.getName())
                    .then(CommandManager.argument(InputTick.CONTROLLER_ID.getName(), IntegerArgumentType.integer(0))
                        .executes(context -> setConfigValue(context, InputTick.CONTROLLER_ID)))
                    .executes(context -> getConfigValue(context, InputTick.CONTROLLER_ID)))

                .then(CommandManager.literal(InputTick.THROTTLE.getName())
                    .then(CommandManager.argument(InputTick.THROTTLE.getName(), IntegerArgumentType.integer(0))
                        .executes(context -> setConfigValue(context, InputTick.THROTTLE)))
                    .executes(context -> getConfigValue(context, InputTick.THROTTLE)))

                .then(CommandManager.literal(InputTick.PITCH.getName())
                    .then(CommandManager.argument(InputTick.PITCH.getName(), IntegerArgumentType.integer(0))
                        .executes(context -> setConfigValue(context, InputTick.PITCH)))
                    .executes(context -> getConfigValue(context, InputTick.PITCH)))

                .then(CommandManager.literal(InputTick.YAW.getName())
                    .then(CommandManager.argument(InputTick.YAW.getName(), IntegerArgumentType.integer(0))
                        .executes(context -> setConfigValue(context, InputTick.YAW)))
                    .executes(context -> getConfigValue(context, InputTick.YAW)))

                .then(CommandManager.literal(InputTick.ROLL.getName())
                    .then(CommandManager.argument(InputTick.ROLL.getName(), IntegerArgumentType.integer(0))
                        .executes(context -> setConfigValue(context, InputTick.ROLL)))
                    .executes(context -> getConfigValue(context, InputTick.ROLL)))

                .then(CommandManager.literal(InputTick.DEADZONE.getName())
                    .then(CommandManager.argument(InputTick.DEADZONE.getName(), FloatArgumentType.floatArg(0.0f, 1.0f))
                        .executes(context -> setConfigValue(context, InputTick.DEADZONE)))
                    .executes(context -> getConfigValue(context, InputTick.DEADZONE)))

                .then(CommandManager.literal(InputTick.THROTTLE_CENTER_POSITION.getName())
                    .then(CommandManager.argument(InputTick.THROTTLE_CENTER_POSITION.getName(), BoolArgumentType.bool())
                        .executes(context -> setConfigValue(context, InputTick.THROTTLE_CENTER_POSITION)))
                    .executes(context -> getConfigValue(context, InputTick.THROTTLE_CENTER_POSITION)))

                .then(CommandManager.literal(QuadcopterEntity.RATE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.RATE.getKey().getName(), FloatArgumentType.floatArg(0.0f, 2.0f))
                        .executes(context -> setConfigValue(context, QuadcopterEntity.RATE.getKey())))
                    .executes(context -> getConfigValue(context, QuadcopterEntity.RATE.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.SUPER_RATE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.SUPER_RATE.getKey().getName(), FloatArgumentType.floatArg(0.0f, 2.0f))
                        .executes(context -> setConfigValue(context, QuadcopterEntity.SUPER_RATE.getKey())))
                    .executes(context -> getConfigValue(context, QuadcopterEntity.SUPER_RATE.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.EXPO.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.EXPO.getKey().getName(), FloatArgumentType.floatArg(0.0f, 2.0f))
                        .executes(context -> setConfigValue(context, QuadcopterEntity.EXPO.getKey())))
                    .executes(context -> getConfigValue(context, QuadcopterEntity.EXPO.getKey())))

                .then(CommandManager.literal(InputTick.INVERT_THROTTLE.getName())
                    .then(CommandManager.argument(InputTick.INVERT_THROTTLE.getName(), BoolArgumentType.bool())
                        .executes(context -> setConfigValue(context, InputTick.INVERT_THROTTLE)))
                    .executes(context -> getConfigValue(context, InputTick.INVERT_THROTTLE)))

                .then(CommandManager.literal(InputTick.INVERT_PITCH.getName())
                    .then(CommandManager.argument(InputTick.INVERT_PITCH.getName(), BoolArgumentType.bool())
                        .executes(context -> setConfigValue(context, InputTick.INVERT_PITCH)))
                    .executes(context -> getConfigValue(context, InputTick.INVERT_PITCH)))

                .then(CommandManager.literal(InputTick.INVERT_YAW.getName())
                    .then(CommandManager.argument(InputTick.INVERT_YAW.getName(), BoolArgumentType.bool())
                        .executes(context -> setConfigValue(context, InputTick.INVERT_YAW)))
                    .executes(context -> getConfigValue(context, InputTick.INVERT_YAW)))

                .then(CommandManager.literal(InputTick.INVERT_ROLL.getName())
                    .then(CommandManager.argument(InputTick.INVERT_ROLL.getName(), BoolArgumentType.bool())
                        .executes(context -> setConfigValue(context, InputTick.INVERT_ROLL)))
                    .executes(context -> getConfigValue(context, InputTick.INVERT_ROLL)))

                .then(CommandManager.literal(QuadcopterEntity.CAMERA_ANGLE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.CAMERA_ANGLE.getKey().getName(), IntegerArgumentType.integer(-10, 100))
                        .executes(context -> setConfigValue(context, QuadcopterEntity.CAMERA_ANGLE.getKey())))
                    .executes(context -> getConfigValue(context, QuadcopterEntity.CAMERA_ANGLE.getKey())))

                .then(CommandManager.literal(FlyableEntity.FIELD_OF_VIEW.getKey().getName())
                    .then(CommandManager.argument(FlyableEntity.FIELD_OF_VIEW.getKey().getName(), IntegerArgumentType.integer(40, 170))
                        .executes(context -> setConfigValue(context, FlyableEntity.FIELD_OF_VIEW.getKey())))
                    .executes(context -> getConfigValue(context, FlyableEntity.FIELD_OF_VIEW.getKey())))

//                .then(CommandManager.literal(BAND)
//                    .then(CommandManager.argument(BAND, StringArgumentType.word())
//                        .executes(context -> setConfigValue(context, FlyableEntity.FREQUENCY.getKey())))
//                    .executes(context -> getConfigValue(context, FlyableEntity.FREQUENCY.getKey())))
//
//                .then(CommandManager.literal(CHANNEL)
//                    .then(CommandManager.argument(CHANNEL, IntegerArgumentType.integer(1, 8))
//                        .executes(context -> setConfigValue(context, FlyableEntity.FREQUENCY.getKey())))
//                    .executes(context -> getConfigValue(context, FlyableEntity.FREQUENCY.getKey())))

                .then(CommandManager.literal(PhysicsEntity.MASS.getKey().getName())
                    .then(CommandManager.argument(PhysicsEntity.MASS.getKey().getName(), FloatArgumentType.floatArg())
                        .executes(context -> setConfigValue(context, PhysicsEntity.MASS.getKey())))
                    .executes(context -> getConfigValue(context, PhysicsEntity.MASS.getKey())))

                .then(CommandManager.literal(PhysicsEntity.DRAG_COEFFICIENT.getKey().getName())
                    .then(CommandManager.argument(PhysicsEntity.DRAG_COEFFICIENT.getKey().getName(), FloatArgumentType.floatArg())
                        .executes(context -> setConfigValue(context, PhysicsEntity.DRAG_COEFFICIENT.getKey())))
                    .executes(context -> getConfigValue(context, PhysicsEntity.DRAG_COEFFICIENT.getKey())))

                .then(CommandManager.literal(PhysicsEntity.SIZE.getKey().getName())
                    .then(CommandManager.argument(PhysicsEntity.SIZE.getKey().getName(), IntegerArgumentType.integer(1, 24))
                        .executes(context -> setConfigValue(context, PhysicsEntity.SIZE.getKey())))
                    .executes(context -> getConfigValue(context, PhysicsEntity.SIZE.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.THRUST.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.THRUST.getKey().getName(), IntegerArgumentType.integer(0))
                        .executes(context -> setConfigValue(context, QuadcopterEntity.THRUST.getKey())))
                    .executes(context -> getConfigValue(context, QuadcopterEntity.THRUST.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.THRUST_CURVE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.THRUST_CURVE.getKey().getName(), FloatArgumentType.floatArg(0.0f, 1.0f))
                        .executes(context -> setConfigValue(context, QuadcopterEntity.THRUST_CURVE.getKey())))
                    .executes(context -> getConfigValue(context, QuadcopterEntity.THRUST_CURVE.getKey())))

                .then(CommandManager.literal(PhysicsWorld.GRAVITY.getName())
                    .then(CommandManager.argument(PhysicsWorld.GRAVITY.getName(), FloatArgumentType.floatArg())
                        .executes(context -> setConfigValue(context, PhysicsWorld.GRAVITY)))
                    .executes(context -> getConfigValue(context, PhysicsWorld.GRAVITY)))

                .then(CommandManager.literal(PhysicsWorld.BLOCK_RADIUS.getName())
                    .then(CommandManager.argument(PhysicsWorld.BLOCK_RADIUS.getName(), IntegerArgumentType.integer(1, 4))
                        .executes(context -> setConfigValue(context, PhysicsWorld.BLOCK_RADIUS)))
                    .executes(context -> getConfigValue(context, PhysicsWorld.BLOCK_RADIUS)))

                .then(CommandManager.literal(PhysicsWorld.AIR_DENSITY.getName())
                    .then(CommandManager.argument(PhysicsWorld.AIR_DENSITY.getName(), FloatArgumentType.floatArg())
                        .executes(context -> setConfigValue(context, PhysicsWorld.AIR_DENSITY)))
                    .executes(context -> getConfigValue(context, PhysicsWorld.AIR_DENSITY)))

                .then(CommandManager.literal(WRITE)
                    .executes(context -> sendConfigCommand(context, WRITE)))

                .then(CommandManager.literal(REVERT)
                    .executes(context -> sendConfigCommand(context, REVERT)))

                )

            .then(CommandManager.literal(DRONE)

                .then(CommandManager.literal(HELP)
                    .executes(context -> help(context, DRONE)))

//                .then(CommandManager.literal(Config.BAND)
//                    .then(CommandManager.argument(Config.BAND, new NumberArgumentType())
//                        .executes(context -> setFlyableValue(context, Config.BAND)))
//                    .executes(context -> getFlyableValue(context, Config.BAND)))
//
//                .then(CommandManager.literal(Config.CHANNEL)
//                    .then(CommandManager.argument(Config.CHANNEL, new NumberArgumentType())
//                        .executes(context -> setFlyableValue(context, Config.CHANNEL)))
//                    .executes(context -> getFlyableValue(context, Config.CHANNEL)))

                .then(CommandManager.literal(QuadcopterEntity.CAMERA_ANGLE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.CAMERA_ANGLE.getKey().getName(), IntegerArgumentType.integer(-10, 100))
                        .executes(context -> setFlyableValue(context, QuadcopterEntity.CAMERA_ANGLE.getKey())))
                    .executes(context -> getFlyableValue(context, QuadcopterEntity.CAMERA_ANGLE.getKey())))

                .then(CommandManager.literal(FlyableEntity.FIELD_OF_VIEW.getKey().getName())
                    .then(CommandManager.argument(FlyableEntity.FIELD_OF_VIEW.getKey().getName(), IntegerArgumentType.integer(40, 170))
                        .executes(context -> setFlyableValue(context, FlyableEntity.FIELD_OF_VIEW.getKey())))
                    .executes(context -> getFlyableValue(context, FlyableEntity.FIELD_OF_VIEW.getKey())))

                .then(CommandManager.literal(PhysicsEntity.MASS.getKey().getName())
                    .then(CommandManager.argument(PhysicsEntity.MASS.getKey().getName(), FloatArgumentType.floatArg())
                            .executes(context -> setFlyableValue(context, PhysicsEntity.MASS.getKey())))
                    .executes(context -> getFlyableValue(context, PhysicsEntity.MASS.getKey())))

                .then(CommandManager.literal(PhysicsEntity.DRAG_COEFFICIENT.getKey().getName())
                    .then(CommandManager.argument(PhysicsEntity.DRAG_COEFFICIENT.getKey().getName(), FloatArgumentType.floatArg())
                            .executes(context -> setFlyableValue(context, PhysicsEntity.DRAG_COEFFICIENT.getKey())))
                    .executes(context -> getFlyableValue(context, PhysicsEntity.DRAG_COEFFICIENT.getKey())))

                .then(CommandManager.literal(PhysicsEntity.SIZE.getKey().getName())
                    .then(CommandManager.argument(PhysicsEntity.SIZE.getKey().getName(), IntegerArgumentType.integer(1, 24))
                            .executes(context -> setFlyableValue(context, PhysicsEntity.SIZE.getKey())))
                    .executes(context -> getFlyableValue(context, PhysicsEntity.SIZE.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.THRUST.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.THRUST.getKey().getName(), IntegerArgumentType.integer(0))
                            .executes(context -> setFlyableValue(context, QuadcopterEntity.THRUST.getKey())))
                    .executes(context -> getFlyableValue(context, QuadcopterEntity.THRUST.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.THRUST_CURVE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.THRUST_CURVE.getKey().getName(), FloatArgumentType.floatArg(0.0f, 1.0f))
                            .executes(context -> setFlyableValue(context, QuadcopterEntity.THRUST_CURVE.getKey())))
                    .executes(context -> getFlyableValue(context, QuadcopterEntity.THRUST_CURVE.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.RATE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.RATE.getKey().getName(), FloatArgumentType.floatArg(0.0f, 2.0f))
                        .executes(context -> setFlyableValue(context, QuadcopterEntity.RATE.getKey())))
                    .executes(context -> getFlyableValue(context, QuadcopterEntity.RATE.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.SUPER_RATE.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.SUPER_RATE.getKey().getName(), FloatArgumentType.floatArg(0.0f, 2.0f))
                        .executes(context -> setFlyableValue(context, QuadcopterEntity.SUPER_RATE.getKey())))
                    .executes(context -> getFlyableValue(context, QuadcopterEntity.SUPER_RATE.getKey())))

                .then(CommandManager.literal(QuadcopterEntity.EXPO.getKey().getName())
                    .then(CommandManager.argument(QuadcopterEntity.EXPO.getKey().getName(), FloatArgumentType.floatArg(0.0f, 2.0f))
                        .executes(context -> setFlyableValue(context, QuadcopterEntity.EXPO.getKey())))
                    .executes(context -> getFlyableValue(context, QuadcopterEntity.EXPO.getKey())))

                .then(CommandManager.literal(FlyableEntity.NO_CLIP.getKey().getName())
                    .then(CommandManager.argument(FlyableEntity.NO_CLIP.getKey().getName(), BoolArgumentType.bool())
                        .executes(context -> setFlyableValue(context, FlyableEntity.NO_CLIP.getKey())))
                    .executes(context -> getFlyableValue(context, FlyableEntity.NO_CLIP.getKey())))

                .then(CommandManager.literal(FlyableEntity.GOD_MODE.getKey().getName())
                    .then(CommandManager.argument(FlyableEntity.GOD_MODE.getKey().getName(), BoolArgumentType.bool())
                        .executes(context -> setFlyableValue(context, FlyableEntity.GOD_MODE.getKey())))
                    .executes(context -> getFlyableValue(context, FlyableEntity.GOD_MODE.getKey())))

                )

            .then(CommandManager.literal(GOGGLES)

                .then(CommandManager.literal(HELP)
                    .executes(context -> help(context, GOGGLES)))

//                .then(CommandManager.literal(Config.BAND)
//                    .then(CommandManager.argument(Config.BAND, new NumberArgumentType())
//                        .executes(context -> setFlyableValue(context, Config.BAND)))
//                    .executes(context -> getGogglesValue(context, Config.BAND)))
//
//                .then(CommandManager.literal(Config.CHANNEL)
//                    .then(CommandManager.argument(Config.CHANNEL, new NumberArgumentType())
//                        .executes(context -> setFlyableValue(context, Config.CHANNEL)))
//                    .executes(context -> getGogglesValue(context, Config.CHANNEL)))
//
                ))
        );
    }

    protected static <T> int setConfigValue(CommandContext<ServerCommandSource> context, Config.Key<T> key) {
        try {
            final Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(context.getSource().getPlayer().getUuid());
            final T value = context.getArgument(key.getName(), key.getType().getClassType());

            if (value == null || config.getValue(key).equals(value)) {
                getConfigValue(context, key);
                return 0;
            } else {
                config.setValue(key, value);
                ConfigValueS2C.send(context.getSource().getPlayer(), key.getName(), value.toString());
                getConfigValue(context, key);
                return 1;
            }

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected static int getConfigValue(CommandContext<ServerCommandSource> context, Config.Key<?> key) {
        try {
            final Config config = ServerInitializer.SERVER_PLAYER_CONFIGS.get(context.getSource().getPlayer().getUuid());
            final ServerPlayerEntity player = context.getSource().getPlayer();
            player.sendMessage(new LiteralText(key.getName() + COLON_SPACE + config.getValue(key)), false);
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected static <T> int setFlyableValue(CommandContext<ServerCommandSource> context, Config.Key<T> key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final T value = context.getArgument(key.getName(), key.getType().getClassType());

            final ItemStack stack = player.getMainHandStack();
            final CompoundTag stackTag = player.getMainHandStack().getOrCreateSubTag(ServerInitializer.MODID);

            if (stack.getItem() instanceof QuadcopterItem) {
                if (value.equals(key.getType().fromTag(stackTag, key.getName()))) {
                    getFlyableValue(context, key);
                    return 0;
                } else {
                    key.getType().toTag(stackTag, key.getName(), value);
                    getFlyableValue(context, key);
                    return 1;
                }
            } else if (stack.getItem() instanceof TransmitterItem) {
                FlyableEntity flyable = TransmitterItem.flyableEntityFromTransmitter(stack, player);

                if (flyable == null || value.equals(flyable.getValue(key))) {
                    getFlyableValue(context, key);
                    return 0;
                } else {
                    flyable.setValue(key, value);
                    getFlyableValue(context, key);
                    return 1;
                }
            }

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(ERROR).getAsJsonObject(DRONE).toString()), false);
            }
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected static int getFlyableValue(CommandContext<ServerCommandSource> context, Config.Key<?> key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack stack = player.getMainHandStack();
            final CompoundTag stackTag = stack.getOrCreateSubTag(ServerInitializer.MODID);

            if (stack.getItem() instanceof QuadcopterItem) {
                player.sendMessage(new LiteralText(key.getName() + COLON_SPACE + key.getType().fromTag(stackTag, key.getName())), false);
                return 1;
            } else if (stack.getItem() instanceof TransmitterItem) {
                FlyableEntity flyable = TransmitterItem.flyableEntityFromTransmitter(stack, player);

                if (flyable != null) {
                    player.sendMessage(new LiteralText(key.getName() + COLON_SPACE + flyable.getValue(key)), false);
                    return 1;
                }
            }

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(ERROR).getAsJsonObject(DRONE).toString()), false);
            }
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected static <T> int setGogglesValue(CommandContext<ServerCommandSource> context, Config.Key<T> key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final T value = context.getArgument(key.getName(), key.getType().getClassType());

            final ItemStack stack = player.getMainHandStack();
            final ItemStack helmet = player.inventory.armor.get(3);

            final CompoundTag stackTag = player.getMainHandStack().getOrCreateSubTag(ServerInitializer.MODID);
            final CompoundTag helmetTag = player.inventory.armor.get(3).getOrCreateSubTag(ServerInitializer.MODID);

            if (stack.getItem() instanceof GogglesItem) {
                if (value.equals(key.getType().fromTag(stackTag, key.getName()))) {
                    getGogglesValue(context, key);
                    return 0;
                } else {
                    key.getType().toTag(stackTag, key.getName(), value);
                    getGogglesValue(context, key);
                    return 1;
                }
            } else if (helmet.getItem() instanceof GogglesItem) {
                if (value.equals(key.getType().fromTag(helmetTag, key.getName()))) {
                    getGogglesValue(context, key);
                    return 0;
                } else {
                    key.getType().toTag(helmetTag, key.getName(), value);
                    getGogglesValue(context, key);
                    return 1;
                }
            }

            return 0;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

   protected static int getGogglesValue(CommandContext<ServerCommandSource> context, Config.Key<?> key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            final ItemStack stack = player.getMainHandStack();
            final ItemStack helmet = player.inventory.armor.get(3);

            if (stack.getItem() instanceof GogglesItem) {
                CompoundTag tag = stack.getOrCreateSubTag(ServerInitializer.MODID);
                player.sendMessage(new LiteralText(key.getName() + COLON_SPACE + key.getType().fromTag(tag, key.getName())), false);
                return 1;
            } else if (helmet.getItem() instanceof GogglesItem) {
                CompoundTag tag = helmet.getOrCreateSubTag(ServerInitializer.MODID);
                player.sendMessage(new LiteralText(key.getName() + COLON_SPACE + key.getType().fromTag(tag, key.getName())), false);
                return 1;
            }

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(ERROR).getAsJsonObject(GOGGLES).toString()), false);
            }
            return -1;

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected static int sendConfigCommand(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            ConfigCommandS2C.send(player, key);

            if (MESSAGES != null) {
                player.sendMessage(Text.Serializer.fromJson(MESSAGES.getAsJsonObject(SUCCESS).getAsJsonObject(key).toString()), false);
            }
            return 1;
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected static int help(CommandContext<ServerCommandSource> context, String key) {
        try {
            final ServerPlayerEntity player = context.getSource().getPlayer();
            if (MESSAGES != null) {
                final String message = MESSAGES.getAsJsonObject(HELP).getAsJsonArray(key).toString();
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
