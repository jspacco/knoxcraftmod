package edu.knox.knoxcraftmod.client;

class ToroInstruction 
    {
        @SuppressWarnings("unused")
        private final ToroCommand command;
        @SuppressWarnings("unused")
        private final ToroBlockType blockType;

        ToroInstruction(ToroCommand command, ToroBlockType toroBlockType) {
            this.command = command;
            this.blockType = toroBlockType;
        }

        ToroInstruction(ToroCommand command) {
            this(command, null);
        }
    }
