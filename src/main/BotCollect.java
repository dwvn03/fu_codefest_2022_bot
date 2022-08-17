package main;

import io.socket.emitter.Emitter.Listener;
import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;
import jsclub.codefest.sdk.model.Hero;

import main.constant.GameConfig;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;


public class BotCollect {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    public static int[] dx = {-1, 0, 1, 0};
    public static int[] dy = {0, -1, 0, 1};
    public static int[][] visit, matrix;
    public static List<Position> valid_cells = new ArrayList<>();
    public static List<Position> restrictNode = new ArrayList<>();
    public static int n, m;
    public static void main(String[] args) {
        // Creating a new Hero object with name `player1-xxx` and game id
        // `GameConfig.GAME_ID`.
        Hero player1 = new Hero("player1-xxx", GameConfig.GAME_ID);

        Listener onTickTackListener = objects -> {
            // This is getting the game information from the server.
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo = gameInfo.getMapInfo();

            // This is getting the current position of the player and the enemy position.
            StringBuilder path = new StringBuilder();
            matrix = mapInfo.mapMatrix;
            n = mapInfo.size.rows;
            m = mapInfo.size.cols;
            Position currentPosition = mapInfo.getCurrentPosition(player1);
            Position enemyPosition = mapInfo.getEnemyPosition(player1);
            Player bot = new Player(), botEnemy = new Player();;
            for(Player i : mapInfo.players)
                if(i.currentPosition == currentPosition)
                    bot = i;
                else botEnemy = i;

            /// adding restrict node
            restrictNode = new ArrayList<>();
            for(int i = 0; i < n; ++i)
                for(int j = 0; j < m; ++j)
                    if(mapInfo.mapMatrix[i][j] != 0)
                        restrictNode.add(new Position(j, i));
            for(Viruses cell : mapInfo.viruses)
                restrictNode.add(cell.position);
            for(Bomb cell : mapInfo.bombs)
                if(cell.remainTime == 0)
                    restrictNode.add(cell);
            if(bot.pill == 0)
                for(Human cell : mapInfo.human)
                    if(cell.infected)
                        restrictNode.add(cell.position);

            /// making valid_cells
            valid_cells = new ArrayList<>();
            visit = new int[n][m];
            for(int i = 0; i < n; ++i)
                for(int j = 0; j < m; ++j)
                    visit[i][j] = 0;
            dfs(currentPosition.getRow(), currentPosition.getCol());

            /// making path
            boolean ok = true;
            for(Bomb cell : mapInfo.bombs)
                if(cell.playerId == bot.id) {
                    ok = false;
                    for(int i = 1; i <= cell.remainTime; ++i)
                        path.append("x");
                    break;
                }
            if(ok) {
                String pathPoint = "";
                for (Spoil cell : mapInfo.spoils)
                    if (cell.spoil_type == 5) {
                        String tmpPath = AStarSearch.aStarSearch(matrix, restrictNode, currentPosition, cell);
                        if (!tmpPath.isEmpty()) {
                            if (pathPoint.length() == 0 || tmpPath.length() < pathPoint.length())
                                pathPoint = tmpPath;
                        }
                    }
                for (Human cell : mapInfo.human)
                    if (cell.infected && bot.pill > 0) {
                        String tmpPath = AStarSearch.aStarSearch(matrix, restrictNode, currentPosition, cell.position);
                        if (!tmpPath.isEmpty()) {
                            if (pathPoint.length() == 0 || tmpPath.length() < pathPoint.length())
                                pathPoint = tmpPath;
                        }
                    }
                if (pathPoint.length() != 0)
                    path.append(pathPoint);
                else {
                    /// destroy balk
                    if (valid_cells.isEmpty())
                        path.append("x");
                    else {
                        path.append("b");
                        Position safest_cell = currentPosition;
                        int maxDistance = 0;
                        for (Position cell : valid_cells)
                            if (mahattanDistance(currentPosition, cell) > maxDistance) {
                                maxDistance = mahattanDistance(currentPosition, cell);
                                safest_cell = cell;
                            }
                        String tmpPath = AStarSearch.aStarSearch(matrix, restrictNode, currentPosition, safest_cell);
                        path.append(tmpPath);
                        path.append("xxxx");
                    }
                }
            }
            System.out.println(valid_cells.size());


            player1.move(path.toString());
        };

        // This is the code that connects the player to the server.
        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer(SERVER_URL);
    }

    private static void dfs(int x, int y) {
        visit[x][y] = 1;
        valid_cells.add(new Position(y, x));
        for(int i = 0; i < 4; ++i) {
            int u = x + dx[i];
            int v = y + dy[i];
            if(0 <= u && u < n && 0 <= v && v < m && visit[u][v] == 0 && (!restrictNode.contains(new Position(u, v)))) {
                if(matrix[u][v] != 1 && matrix[u][v] != 2 && matrix[u][v] != 6 && matrix[u][v] != 7)
                    dfs(u, v);
            }
        }
    }

    private static int mahattanDistance(Position x, Position y) {
        return  abs(x.getRow() - y.getRow()) + abs(x.getCol() - y.getCol());
    }

    private  static List<Position> get_bombs_in_range(Position currentPosition, List<Bomb> Bombs, int power) {
        List<Position> bombs_in_range = new ArrayList<>();
        for(Bomb cell : Bombs) {
            if(mahattanDistance(cell, currentPosition) <= power * 2)
                bombs_in_range.add(new Position(cell.getCol(), cell.getRow()));
        }
        return bombs_in_range;
    }

    private static Position get_safest_cell(Position currentPosition, int power, List<Position> cells, List<Position> bombs) {
        Position nearest = new Bomb(0, 0);
        int distance = power * 2;
        for(Position cell : bombs) {
            int tmpDist = mahattanDistance(currentPosition, new Position(cell.getRow(), cell.getCol()));
            if (tmpDist < distance) {
                nearest = cell;
                distance = tmpDist;
            }
        }

        distance = 0;
        Position safest_cell = new Position(0, 0);
        for(Position cell : cells) {
            int tmpDist = mahattanDistance(nearest, cell);
            if (tmpDist > distance) {
                safest_cell = cell;
                distance = tmpDist;
            }
        }
        return safest_cell;
    }
}

