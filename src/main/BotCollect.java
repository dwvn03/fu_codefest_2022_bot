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
            String path = new String();
            matrix = mapInfo.mapMatrix;
            n = mapInfo.size.rows;
            m = mapInfo.size.cols;
            Position currentPosition = mapInfo.getCurrentPosition(player1);
            Position enemyPosition = mapInfo.getEnemyPosition(player1);
            Player bot = new Player(), botEnemy = new Player();
            ;
            for (Player i : mapInfo.players)
                if (i.currentPosition == currentPosition)
                    bot = i;
                else botEnemy = i;
            int range = max(bot.power, botEnemy.power);

            /// adding restrict node
            restrictNode = new ArrayList<>();
            restrictNode.addAll(mapInfo.balk);
            restrictNode.addAll(mapInfo.walls);
            restrictNode.addAll(mapInfo.bombs);
            for(Viruses cell : mapInfo.viruses)
                restrictNode.add(cell.position);
            if(bot.pill == 0)
                for(Human cell : mapInfo.human)
                    if(cell.infected)
                        restrictNode.add(cell.position);

            /// making valid_cells
            valid_cells = new ArrayList<>();
            visit = new int[n][m];
            matrix = new int[n][m];

            for(int i = 0; i < n; ++i)
                for(int j = 0; j < m; ++j) {
                    visit[i][j] = 0;
                    matrix[i][j] = mapInfo.mapMatrix[i][j];
                }
            dfs(currentPosition.getRow(), currentPosition.getCol());

            /// making path
            boolean check = false;
            if(bot.delay == 0) {
                for(Spoil cell : mapInfo.spoils)
                    if(valid_cells.contains(cell)) {
                        check = true;
                        String tmpPath = AStarSearch.aStarSearch(matrix, restrictNode, currentPosition, cell);
                    }
                player1.move(path);
            }
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
            if(0 <= u && u < n && 0 <= v && v < m && visit[u][v] == 0 && matrix[u][v] == 0 && (!restrictNode.contains(new Position(v, u))))
                    dfs(u, v);
        }
    }
    private static int mahattanDistance(Position x, Position y) {
        return  abs(x.getRow() - y.getRow()) + abs(x.getCol() - y.getCol());
    }
}
