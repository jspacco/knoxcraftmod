package edu.knox.knoxcraftmod.command;

import net.minecraft.util.Mth;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public Direction turnLeft() {
        return switch (this) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
        };
    }

    public Direction turnRight() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    public Direction opposite() {
        return this.turnLeft().turnLeft();
    }

    public static Direction fromOrdinal(int i) {
        return values()[i % values().length];
    }

    public float toYaw() {
        return switch (this) {
            case NORTH -> 180f;
            case EAST -> -90f;
            case SOUTH -> 0f;
            case WEST -> 90f;
        };
    }

    public static Direction fromDegrees(float yRot) {
        float rot = Mth.wrapDegrees(yRot);
        if (rot >= -45 && rot < 45) {
            return Direction.SOUTH;
        } else if (rot >= 45 && rot < 135) {
            return Direction.WEST;
        } else if (rot >= -135 && rot < -45) {
            return Direction.EAST;
        } else {
            return Direction.NORTH;
        }
    }
    
}

