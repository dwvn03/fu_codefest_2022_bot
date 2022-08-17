package main;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;
import jsclub.codefest.sdk.model.Hero;

import main.constant.GameConfig;

import java.util.*;


public class Bot {
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

            /// making targets list
            targets = new ArrayList<>();
            int n = mapInfo.size.rows;
            int m = mapInfo.size.cols;
            visit = new int[n][m];
            for(int i = 0; i < n; ++i)
                for(int j = 0; j < m; ++j) {
                    visit[i][j] = 0;
                    if(mapInfo.mapMatrix[i][j] == 1)
                        restrictNode.add(new Position(i, j));
                }
            boolean containWall = true;
            dfs(currentPosition.getRow(), currentPosition.getCol(), n, m, restrictNode);
            for(Position cell : targets) {
                int x = cell.getRow();
                int y = cell.getCol();
                if (0 <= x && x < n && 0 <= y && y < m && mapInfo.mapMatrix[x][y] == 5)
                    containWall = false;
            }


            /// choosing best path
            Map<Position, String> ok = AStarSearch.getPathToAllTargets(mapInfo.mapMatrix, restrictNode, currentPosition, targets);
            String path = new String();
            if(containWall) {
                for (Position cell : targets) {
                    if (ok.containsKey(cell)) {
                        if(cell == currentPosition)
                            continue;
                        String tmpPath = ok.get(cell);
                        int x = cell.getRow();
                        int y = cell.getCol();
                        for(Position cell1 : targets)
                            if(cell1 != cell) {
                                boolean hudge = true;
                                int u = cell1.getRow();
                                int v = cell1.getCol();
                                if (u == x && (y - bot.power + 1 <= v && v <= y))
                                    hudge = false;
                                else if(u == x && (y <= v && v <= y + bot.power - 1))
                                    hudge = false;
                                else if(x <= u && u <= x + bot.power - 1 && v == y)
                                    hudge = false;
                                else if(x - bot.power + 1 <= u && u <= x && v == y)
                                    hudge = false;
                                if(hudge) {
                                    String path2 = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictNode, cell, cell1);
                                    if(path.isEmpty() || path2.length()+tmpPath.length()+1 < path.length()) {
                                        path = tmpPath + "b" + path2;
                                    }
                                }
                            }
                    }
                }
            } else {
                for (Position cell : targets) {
                    if(ok.containsKey(cell) && mapInfo.mapMatrix[cell.getRow()][cell.getCol()] == 5) {
                        String tmpPath = ok.get(cell);
                        if(path.isEmpty() || tmpPath.length() < path.length())
                            path = tmpPath;
                    }
                }
                if(path.isEmpty()) {
                    for(Position cell : targets)
                        if(ok.containsKey(cell)){
                            if(path.isEmpty() || ok.get(cell).length() < path.length())
                                path = ok.get(cell);
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

