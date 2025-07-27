package edu.knox.knoxcraftmod.command;

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

    
    
}

