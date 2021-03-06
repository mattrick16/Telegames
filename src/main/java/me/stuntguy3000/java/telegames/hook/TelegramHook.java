package me.stuntguy3000.java.telegames.hook;

import lombok.Getter;
import me.stuntguy3000.java.telegames.Telegames;
import me.stuntguy3000.java.telegames.handler.LogHandler;
import me.stuntguy3000.java.telegames.object.Command;
import me.stuntguy3000.java.telegames.object.Game;
import me.stuntguy3000.java.telegames.object.Lobby;
import me.stuntguy3000.java.telegames.object.config.LobbyList;
import me.stuntguy3000.java.telegames.util.ClassGetter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// @author Luke Anderson | stuntguy3000
public class TelegramHook implements Listener {
    @Getter
    private static TelegramBot bot;
    @Getter
    private final Telegames instance;

    public TelegramHook(String authKey, Telegames instance) {
        this.instance = instance;

        bot = TelegramBot.login(authKey);
        bot.startUpdates(false);
        bot.getEventsManager().register(this);
        LogHandler.log("Connected to Telegram.");

        instance.sendToAdmins("Bot has connected, running build #" + Telegames.BUILD);

        this.initializeCommands();
        this.initializeGames();
        this.initializeLobbies();
    }

    private void initializeCommands() {
        List<Class<?>> allCommands = ClassGetter.getClassesForPackage("me.stuntguy3000.java.telegames.command.");
        allCommands.stream().filter(Command.class::isAssignableFrom).forEach(clazz -> {
            try {
                Command command = (Command) clazz.newInstance();
                LogHandler.log("Registered command " + Arrays.toString(command.getNames()));
            } catch (InstantiationException | IllegalAccessException e) {
                LogHandler.log(clazz.getSimpleName() + " failed to instantiate:");
                e.printStackTrace();
            }
        });
    }

    private void initializeGames() {
        List<Class<?>> allGames = ClassGetter.getClassesForPackage("me.stuntguy3000.java.telegames.game.");
        allGames.stream().filter(Game.class::isAssignableFrom).forEach(clazz -> {
            try {
                Game game = (Game) clazz.newInstance();
                if (game.isDevModeOnly() && !Telegames.DEV_MODE) {
                    return;
                }
                getInstance().getGameHandler().registerGame(game);
            } catch (InstantiationException | IllegalAccessException e) {
                LogHandler.log(clazz.getSimpleName() + " failed to instantiate:");
                e.printStackTrace();
            }
        });
    }

    private void initializeLobbies() {
        LobbyList lobbyList = getInstance().getConfigHandler().getLobbyList();

        if (lobbyList != null) {
            for (Map.Entry<String, List<Integer>> lobby : lobbyList.getActiveLobbies().entrySet()) {
                getInstance().getLobbyHandler().createLobby(lobby.getKey(), lobby.getValue());
            }
        }
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        String command = event.getCommand();

        instance.getCommandHandler().executeCommand(command, event);
    }

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {
        User user = event.getMessage().getSender();
        Lobby lobby = Telegames.getInstance().getLobbyHandler().getLobby(user);

        if (lobby != null) {
            lobby.onTextMessageReceived(event);
        }
    }
}
    