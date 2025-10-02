package com.karimhosny.cli.commands.contracts;


public interface ICommand {
    String getName();         // e.g. "login"
    void execute(String[] args);
}
