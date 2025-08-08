package edu.knox.knoxcraftmod.client;

enum TerpCommand 
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

    TerpCommand(String id) {
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
