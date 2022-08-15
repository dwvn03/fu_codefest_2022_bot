package main;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.util.GameUtil;
import jsclub.codefest.sdk.model.Hero;

import main.constant.GameConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


public class Player {
    final static String SERVER_URL = "https://codefest.jsclub.me/";

    public static void main(String[] args) {
        // Creating a new Hero object with name `player1-xxx` and game id
        // `GameConfig.GAME_ID`.
        Hero player1 = new Hero("player1-xxx", GameConfig.GAME_ID);

        Listener onTickTackListener = objects -> {
            // This is getting the game information from the server.
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo = gameInfo.getMapInfo();

            // This is getting the current position of the player and the enemy position.
            Position currentPosition = mapInfo.getCurrentPosition(player1);
            Position enemyPosition = mapInfo.getEnemyPosition(player1);

            // This is the A* algorithm. It is used to find the shortest path between two
            // points.
            Set<Position> restrictPosition = new HashSet<>();
//            restrictPosition.
            //
            String path = AStarSearch.aStarSearch(mapInfo.mapMatrix, new ArrayList<>(restrictPosition), currentPosition, enemyPosition);

            // Sending the path to the server.
            player1.move(path);
        };

        // This is the code that connects the player to the server.
        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer(SERVER_URL);
    }
}
