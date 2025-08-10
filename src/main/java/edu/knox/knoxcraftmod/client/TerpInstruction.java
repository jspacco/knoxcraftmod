package edu.knox.knoxcraftmod.client;

class TerpInstruction 
    {
        @SuppressWarnings("unused")
        private final TerpCommand command;
        @SuppressWarnings("unused")
        private final TerpBlockType blockType;

        TerpInstruction(TerpCommand command, TerpBlockType terpBlockType) {
            this.command = command;
            this.blockType = terpBlockType;
        }

        TerpInstruction(TerpCommand command) {
            this(command, null);
        }
    }
