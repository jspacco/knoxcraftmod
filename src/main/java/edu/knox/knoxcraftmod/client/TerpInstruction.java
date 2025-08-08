package edu.knox.knoxcraftmod.client;

class TerpInstruction 
    {
        @SuppressWarnings("unused")
        private final TerpCommand command;
        @SuppressWarnings("unused")
        private final TerpBlockType blockType;

        TerpInstruction(TerpCommand command, TerpBlockType toroBlockType) {
            this.command = command;
            this.blockType = toroBlockType;
        }

        TerpInstruction(TerpCommand command) {
            this(command, null);
        }
    }
