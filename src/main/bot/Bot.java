package main.bot;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.socket.data.Bomb;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.ArrayList;

import main.constant.GameConfig;
import main.util.RestrictedDetails;

public class Bot {
    public static void main(String[] args) {
        Hero player1 = new Hero(GameConfig.PLAYER1_ID, GameConfig.GAME_ID);

        Listener onTickTackListener = objects -> {
            long startTime = System.nanoTime();

            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo = gameInfo.getMapInfo();

            Position currentPosition = mapInfo.getCurrentPosition(player1);
            Position enemyPosition = mapInfo.getEnemyPosition(player1);

            for (Bomb b : mapInfo.bombs) {
                System.out.println("Bomb :" + b.remainTime);
            }

            ArrayList<Position> restrictedPosition = RestrictedDetails.getRestrictedNode(mapInfo, player1);
            String path = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictedPosition, currentPosition, enemyPosition);
            // String path = RandomPlayer.getRandomPath(10);
            player1.move(path);

            long endTime = System.nanoTime();
            System.out.println((endTime - startTime) / 1000000);
        };

        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer(GameConfig.SERVER_URL);
    }
}
