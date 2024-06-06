package cn.powernukkitx.codegen;

public class ConstGen {
    public static void main(String[] args) {
        String input = """
                0 => ON_FIRE
                1 => SNEAKING
                2 => RIDING
                3 => SPRINTING
                4 => USING_ITEM
                5 => INVISIBLE
                6 => TEMPTED
                7 => IN_LOVE
                8 => SADDLED
                9 => POWERED
                10 => IGNITED
                11 => BABY
                12 => CONVERTING
                13 => CRITICAL
                14 => CAN_SHOW_NAME
                15 => ALWAYS_SHOW_NAME
                16 => NO_AI
                17 => SILENT
                18 => WALL_CLIMBING
                19 => CAN_CLIMB
                20 => CAN_SWIM
                21 => CAN_FLY
                22 => CAN_WALK
                23 => RESTING
                24 => SITTING
                25 => ANGRY
                26 => INTERESTED
                27 => CHARGED
                28 => TAMED
                29 => ORPHANED
                30 => LEASHED
                31 => SHEARED
                32 => GLIDING
                33 => ELDER
                34 => MOVING
                35 => BREATHING
                36 => CHESTED
                37 => STACKABLE
                38 => SHOW_BOTTOM
                39 => STANDING
                40 => SHAKING
                41 => IDLING
                42 => CASTING
                43 => CHARGING
                44 => WASD_CONTROLLED
                45 => CAN_POWER_JUMP
                46 => CAN_DASH
                47 => LINGERING
                48 => HAS_COLLISION
                49 => HAS_GRAVITY
                50 => FIRE_IMMUNE
                51 => DANCING
                52 => ENCHANTED
                53 => RETURN_TRIDENT
                54 => CONTAINER_IS_PRIVATE
                55 => IS_TRANSFORMING
                56 => DAMAGE_NEARBY_MOBS
                57 => SWIMMING
                58 => BRIBED
                59 => IS_PREGNANT
                60 => LAYING_EGG
                61 => RIDER_CAN_PICK
                62 => TRANSITION_SITTING
                63 => EATING
                64 => LAYING_DOWN
                65 => SNEEZING
                66 => TRUSTING
                67 => ROLLING
                68 => SCARED
                69 => IN_SCAFFOLDING
                70 => OVER_SCAFFOLDING
                71 => DESCEND_THROUGH_BLOCK
                72 => BLOCKING
                73 => TRANSITION_BLOCKING
                74 => BLOCKED_USING_SHIELD
                75 => BLOCKED_USING_DAMAGED_SHIELD
                76 => SLEEPING
                77 => WANTS_TO_WAKE
                78 => TRADE_INTEREST
                79 => DOOR_BREAKER
                80 => BREAKING_OBSTRUCTION
                81 => DOOR_OPENER
                82 => IS_ILLAGER_CAPTAIN
                83 => STUNNED
                84 => ROARING
                85 => DELAYED_ATTACK
                86 => IS_AVOIDING_MOBS
                87 => IS_AVOIDING_BLOCK
                88 => FACING_TARGET_TO_RANGE_ATTACK
                89 => HIDDEN_WHEN_INVISIBLE
                90 => IS_IN_UI
                91 => STALKING
                92 => EMOTING
                93 => CELEBRATING
                94 => ADMIRING
                95 => CELEBRATING_SPECIAL
                97 => RAM_ATTACK
                98 => PLAYING_DEAD
                99 => IN_ASCENDABLE_BLOCK
                100 => OVER_DESCENDABLE_BLOCK
                101 => CROAKING
                102 => EAT_MOB
                103 => JUMP_GOAL_JUMP
                104 => EMERGING
                105 => SNIFFING
                106 => DIGGING
                107 => SONIC_BOOM
                108 => HAS_DASH_COOLDOWN
                109 => PUSH_TOWARDS_CLOSEST_SPACE
                110 => SCENTING
                111 => RISING
                112 => FEELING_HAPPY
                113 => SEARCHING
                114 => CRAWLING
                115 => TIMER_FLAG_1
                116 => TIMER_FLAG_2
                117 => TIMER_FLAG_3
                118 => BODY_ROTATION_BLOCKED
                """;
        output(input, "");
    }

    public static void output(String input, String prefix) {
        String[] split = input.split("\n");
        for (var s : split) {
            String[] split1 = s.trim().split(" => ");
            System.out.println("public static final int %s = dynamic(%s);".formatted(prefix + split1[1], split1[0]));
        }
    }
}
