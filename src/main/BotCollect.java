package main;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;
import jsclub.codefest.sdk.model.Hero;

import main.constant.GameConfig;

import java.util.*;


public class BotCollect {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    public static int[] dx = {-1, 0, 1, 0};
    public static int[] dy = {0, -1, 0, 1};
    public static int[][] visit;
    public static ArrayList<Position> targets;
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

            Player bot = new Player();
            for(Player i : mapInfo.players)
                if(i.currentPosition == currentPosition)
                    bot = i;

            /// adding restrict node
            List<Position> restrictNode = new ArrayList<>();
            for(Viruses i : mapInfo.viruses)
                restrictNode.add(i.position);
            for(Spoil i : mapInfo.spoils)
                if(i.spoil_type != 5)
                    restrictNode.add(new Position(i.getRow(), i.getCol()));
            restrictNode.addAll(mapInfo.quarantinePlace);
            restrictNode.addAll(mapInfo.teleportGate);
            restrictNode.addAll(mapInfo.balk);
            restrictNode.addAll(mapInfo.getBombList());

            /// making path
            String path = "b";
            int n = mapInfo.size.rows;
            int m = mapInfo.size.cols;
            int x = currentPosition.getRow();
            int y = currentPosition.getCol();
            if(!mapInfo.spoils.isEmpty()) {
                for(Spoil cellx : mapInfo.spoils){
                    if(cellx.spoil_type != 5)
                        continue;;
                    Position cell = new Position(cellx.getRow(), cellx.getCol());
                    String tmpPath = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictNode, currentPosition, cell);
                    if(!tmpPath.isEmpty()) {
                        int u = cell.getRow();
                        int v = cell.getCol();
                        boolean hudge = true;
                        if (u == x && (y - bot.power + 2 <= v && v <= y))
                            hudge = false;
                        else if(u == x && (y <= v && v <= y + bot.power + 2))
                            hudge = false;
                        else if(x <= u && u <= x + bot.power + 2 && v == y)
                            hudge = false;
                        else if(x - bot.power + 2 <= u && u <= x && v == y)
                            hudge = false;
                        if(hudge) {
                            if(path.length() == 1 || path.length() < tmpPath.length()+1)
                                path = "b" + tmpPath;
                        }
                    }
                }
            }
            for(Position cell : mapInfo.blank) {
                String tmpPath = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictNode, currentPosition, cell);
                if(tmpPath.isEmpty() == false) {
                    int u = cell.getRow();
                    int v = cell.getCol();
                    boolean hudge = true;
                    if (u == x && (y - bot.power + 2 <= v && v <= y))
                        hudge = false;
                    else if(u == x && (y <= v && v <= y + bot.power + 2))
                        hudge = false;
                    else if(x <= u && u <= x + bot.power + 2 && v == y)
                        hudge = false;
                    else if(x - bot.power + 2 <= u && u <= x && v == y)
                        hudge = false;
                    if(hudge) {
                        if(path.length() == 1 || path.length() < tmpPath.length()+1)
                            path = "b" + tmpPath;
                    }
                }
            }

            player1.move(path);
        };

        // This is the code that connects the player to the server.
        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer(SERVER_URL);
    }

    private static Boolean reachable(Position n, List<Position> restrictNode) {
        return !restrictNode.contains(n);
    }

    private static void dfs(int x, int y, int n, int m, List<Position> restrictNode){
        visit[x][y] = 1;
        targets.add(new Position(x, y));
        for(int i = 0; i < 4; ++i) {
            int u = x + dx[i];
            int v = y + dy[i];
            if(0 <= u && u < n && 0 <= v && v < m){
                if(visit[u][v] == 0) {
                    if(reachable(new Position(u, v), restrictNode))
                        dfs(u, v, n, m, restrictNode);
                }
            }
        }
    }
}

