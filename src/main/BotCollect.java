package main;

import io.socket.emitter.Emitter.Listener;
import io.socket.emitter.Emitter;
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

            Player bot = new Player(), botEnemy = new Player();;
            for(Player i : mapInfo.players)
                if(i.currentPosition == currentPosition)
                    bot = i;
                else botEnemy = i;

            /// adding restrict node
            List<Position> restrictNode = new ArrayList<>();
            boolean havePill = false;
            for(Viruses i : mapInfo.viruses)
                restrictNode.add(i.position);
            for(Spoil i : mapInfo.spoils)
                if(i.spoil_type != 5)
                    restrictNode.add(new Position(i.getRow(), i.getCol()));
                else
                    havePill = true;
            restrictNode.addAll(mapInfo.quarantinePlace);
            restrictNode.addAll(mapInfo.teleportGate);
            restrictNode.addAll(mapInfo.balk);

            /// making path
            String path = "";
            int n = mapInfo.size.rows;
            int m = mapInfo.size.cols;
            int x = currentPosition.getRow();
            int y = currentPosition.getCol();

            if(havePill) {
                for(Spoil cell : mapInfo.spoils) {
                    if (cell.spoil_type == 5) {
                        String tmpPath = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictNode, currentPosition, cell);
                        if (tmpPath.isEmpty())
                            continue;
                        if (path.length() == 0 || path.length() > tmpPath.length())
                            path = tmpPath;
                    }
                }
            } else if(bot.pill > 0) {
                for(Human cell : mapInfo.human) {
                    String tmpPath = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictNode, currentPosition, cell.position);
                    if(tmpPath.isEmpty())
                        continue;
                    if(path.length() == 0 || path.length() > tmpPath.length())
                        path = tmpPath;
                }
            }
            if(path.length() == 0) {
                for(Position cell : mapInfo.blank) {
                    if(!notAttack(cell, currentPosition, bot.power))
                        continue;
                    String tmpPath = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictNode, currentPosition, cell);
                    if(tmpPath.isEmpty())
                        continue;
                    if(path.length() == 0 || tmpPath.length()+1 <= path.length())
                        path = "b" + tmpPath;
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

    private  static  Boolean notAttack(Position end, Position sta, int power) {
        if(end.getRow() == sta.getRow())
            return !(sta.getCol()-power <= end.getCol() && end.getCol() <= sta.getCol()+power);
        if(end.getCol() == sta.getCol())
            return !(sta.getRow()-power <= end.getRow() && end.getRow() <= sta.getRow()+power);
        return  true;
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

