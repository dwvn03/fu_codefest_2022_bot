package main;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;
import jsclub.codefest.sdk.model.Hero;

import main.constant.GameConfig;

import java.util.*;


public class PlayerX {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    public static int[] dx = {-1, 0, 1, 0};
    public static int[] dy = {0, -1, 0, 1};
    public static int[][] visit;

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

            Player bot;
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
            ArrayList<Position> targets = new ArrayList<>();
            int n = mapInfo.size.rows;
            int m = mapInfo.size.cols;
            for(int i = 0; i < n; ++i)
                for(int j = 0; j < m; ++j)
                    visit[i][j] = 0;
            Deque<Position> dq = new LinkedList<>();
            dq.addFirst(currentPosition);
            boolean containWall = true;
            while(dq.size() != 0)
            {
                targets.add(dq.getFirst());
                int x = dq.getFirst().getRow();
                int y = dq.getFirst().getCol();
                dq.removeFirst();
                for(int i = 0; i < 4; ++i)
                {
                    int u = x + dx[i];
                    int v = y + dy[i];
                    if(0 <= u && u < n && 0 <= v && v < m && visit[u][v] == 0 && reachable(new Position(u, v), restrictNode)) {
                        if(mapInfo.mapMatrix[u][v] != 1) {
                            dq.addLast(new Position(u, v));
                            if(mapInfo.mapMatrix[u][v] != 1 && mapInfo.mapMatrix[u][v] != 0)
                                containWall = false;
                        }
                    }
                }
            }

            /// choosing best path
            Map<Position, String> ok = AStarSearch.getPathToAllTargets(mapInfo.mapMatrix, restrictNode, currentPosition, targets);
            String path = new String();
            if(containWall) {
                for (Position cell : targets) {
                    if (ok.containsKey(cell)) {
                        String tmpPath = ok.get(cell);
                        for(Position cell1 : targets)
                            if(cell1 != cell) {
                                AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictNode, cell, cell1);
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
                if(path.isEmpty())
                    path = ok.get(targets.get(1));
            }

            player1.move(path);
        };

        // This is the code that connects the player to the server.
        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer(SERVER_URL);
    }

    private static Boolean reachable(Position n, List<Position> restrictNode) {
        if(restrictNode.contains(n))
            return false;
        return true;
    }
}

