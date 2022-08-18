package main.bot;

import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.model.Hero;
import main.constant.GameConfig;

import java.util.Random;

public class RandomPlayer {
    public static String getRandomPath(int length) {
        Random rand = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int random_integer = rand.nextInt(5);
            sb.append("1234b".charAt(random_integer));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        Hero randomPlayer = new Hero(GameConfig.PLAYER2_ID, GameConfig.GAME_ID);

        Emitter.Listener onTickTackListener = objects -> {
            randomPlayer.move(getRandomPath(10));
        };

        randomPlayer.setOnTickTackListener(onTickTackListener);
        randomPlayer.connectToServer(GameConfig.SERVER_URL);
    }
}
