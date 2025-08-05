package edu.knox.knoxcraftmod.client;

enum ToroCommand 
{
    FORWARD("forward"),
    BACK("back"),
    TURN_LEFT("turnLeft"),
    LEFT("left"),
    TURN_RIGHT("turnRight"),
    RIGHT("right"),
    UP("up"),
    DOWN("down"),
    NOP("nop"),
    SET_BLOCK("setBlock"),
    ;

    private final String id;

    ToroCommand(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
