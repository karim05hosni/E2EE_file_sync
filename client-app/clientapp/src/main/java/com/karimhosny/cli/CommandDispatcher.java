package com.karimhosny.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.karimhosny.cli.commands.contracts.ICommand;

public class CommandDispatcher {

    private final Map<String, ICommand> commands = new HashMap<>();

    public void register(ICommand command) {
        commands.put(command.getName(), command);
    }

    public void dispatch(String input) {
        // 1. Split user input into words
        String[] parts = input.trim().split("\\s+");

        // 2. First word = command name
        String commandName = parts[0];

        // 3. The rest = command arguments
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        // 4. Find the registered command
        ICommand cmd = commands.get(commandName);

        // 5. Run it (if exists), else print error
        if (cmd != null) {
            cmd.execute(args);
        } else {
            System.out.println("Unknown command: " + commandName);
        }
    }

}
