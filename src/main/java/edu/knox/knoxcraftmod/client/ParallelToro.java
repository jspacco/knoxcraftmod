package edu.knox.knoxcraftmod.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import edu.knox.knoxcraftmod.client.*;

public class ParallelToro {

    private String programName;
    private String description;

    public ParallelToro(String programName, String description) {
        if (programName == null || description == null) {
            throw new IllegalArgumentException("programName and description cannot be null!");
        }
        this.programName = programName;
        this.description = description;
    }

    // each element of the list is the instructions for a thread
    private List<AbstractToro> threads = new LinkedList<>();

    public void addThread(Consumer<AbstractToro> c) {
        AbstractToro t = new AbstractToro(new ArrayList<>()) {};
        threads.add(t);
        c.accept(t);
    }

    public List<List<ToroInstruction>> getAllThreads() {
        return threads.stream()
            .map(AbstractToro::getInstructions)
            .toList();
    }

    public String getProgramName() {
        return programName;
    }

    public String getDescription() {
        return description;
    }
}
