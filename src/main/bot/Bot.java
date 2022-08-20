package main.bot;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Player;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.ArrayList;
import java.util.List;

import main.constant.GameConfig;
import main.util.*;

public class Bot {
    public static void main(String[] args) {
        Hero hero = new Hero(GameConfig.PLAYER1_ID, GameConfig.GAME_ID);
        TimestampManager tm = new TimestampManager();

        Listener onTickTackListener = objects -> {
            // long startTime = System.nanoTime();
            ArrayList<String> paths = new ArrayList<>();

            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo = gameInfo.getMapInfo();

            tm.call(gameInfo.timestamp, 300L, () -> {
                List<Position> restrictedNode = RestrictedUtils.getRestrictedNode(mapInfo, hero, RestrictedUtils.BOMB_NOT_INCLUDED);
                
                Player me = mapInfo.getPlayerByKey(hero.getPlayerID());
                Position heroPos = me.currentPosition;
                // Position enemyPos = mapInfo.getEnemyPosition(hero);

                if (PlayerUtils.isQuarantined(mapInfo, heroPos)) {
                    System.out.println("Quarantined");
                } else if (PlayerUtils.isOnExplosionNode(mapInfo, heroPos)) {
                    // ne bom
                    hero.move(RandomPlayer.getRandomPath(10));
                    System.out.println("Run");

                } else {    
                    RestrictedUtils.addExplosionNode(restrictedNode, mapInfo.getBombList());            
                    String pathToSpoil = PathUtils.pathToNearestItem(mapInfo, heroPos, restrictedNode, true);

                    if (pathToSpoil.isEmpty()) {
                        pathToSpoil = PathUtils.pathToNearestItem(mapInfo, heroPos, restrictedNode, false);
                    }

                    PlayerUtils.addNonEmpty(paths, pathToSpoil);
                    
                    String pathToHuman = me.pill > 0
                                        ? PathUtils.pathToNearestHuman(mapInfo, heroPos, restrictedNode, PathUtils.PILL.HAVE_PILL)
                                        : PathUtils.pathToNearestHuman(mapInfo, heroPos, restrictedNode, PathUtils.PILL.NO_PILL);

                    PlayerUtils.addNonEmpty(paths, pathToHuman);
                
                    if (paths.isEmpty()) {
                        String pathToBalk = PathUtils.pathToNearestBalk(mapInfo, heroPos, restrictedNode);
                        PlayerUtils.addNonEmpty(paths, pathToBalk + "b");
                    }

                    hero.move(
                        PathUtils.shortestPath(paths)
                    );
                }
            });
            
            // long endTime = System.nanoTime();
            // System.out.println((endTime - startTime) / 1000000);
        };

        hero.setOnTickTackListener(onTickTackListener);
        hero.connectToServer(GameConfig.SERVER_URL);
    }
}