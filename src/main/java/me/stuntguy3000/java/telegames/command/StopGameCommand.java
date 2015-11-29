package me.stuntguy3000.java.telegames.command;

import me.stuntguy3000.java.telegames.Telegames;
import me.stuntguy3000.java.telegames.handler.LobbyHandler;
import me.stuntguy3000.java.telegames.object.Command;
import me.stuntguy3000.java.telegames.object.Game;
import me.stuntguy3000.java.telegames.object.Lobby;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.ChatType;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.user.User;

// @author Luke Anderson | stuntguy3000
public class StopGameCommand extends Command {
    public StopGameCommand() {
        super(Telegames.getInstance(), "stopgame", "/startgame Stop a game.");
    }

    public void processCommand(CommandMessageReceivedEvent event) {
        Chat chat = event.getChat();
        User sender = event.getMessage().getSender();
        LobbyHandler lobbyHandler = getInstance().getLobbyHandler();

        if (event.getChat().getType() == ChatType.PRIVATE) {
            Lobby lobby = lobbyHandler.getLobby(sender);
            if (lobby != null) {
                Game lobbyGame = lobby.getCurrentGame();

                if (lobbyGame != null) {
                    lobby.stopGame(false);
                } else {
                    respond(chat, "No game is running!");
                }
            } else {
                respond(chat, "You are not in a Lobby!");
            }
        } else {
            respond(chat, "This command can only be executed via a private message to @TelegamesBot");
        }
    }
}
    