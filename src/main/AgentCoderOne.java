package main;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;
import jsclub.codefest.sdk.model.Hero;

import main.constant.GameConfig;

import java.io.PipedOutputStream;
import java.util.*;

import static java.lang.Math.*;


public class AgentCoderOne {
    final static String SERVER_URL = "https://codefest.jsclub.me/";

    public static void main(String[] args) {
        // Creating a new Hero object with name `player1-xxx` and game id
        // `GameConfig.GAME_ID`.
        Hero player1 = new Hero("player1-xxx", GameConfig.GAME_ID);

        Listener onTickTackListener = objects -> {
            // This is getting the game information from the server.
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo = gameInfo.getMapInfo();

            int n = mapInfo.size.rows;
            int m = mapInfo.size.cols;

            Position currentPosition = mapInfo.getCurrentPosition(player1);
            Position enemyPosition = mapInfo.getEnemyPosition(player1);

            Player bot = new Player(), botEnemy = new Player();
            for(Player i : mapInfo.players)
                if(i.currentPosition == currentPosition)
                    bot = i;
                else botEnemy = i;

            List<Position> bombs_in_range = get_bombs_in_range(currentPosition, mapInfo.bombs, max(bot.power, botEnemy.power));
            List<Position> surrounding_cells = get_surrounding_cells(n, m, currentPosition);
            List<Position> valid_cells = get_valid_cells(mapInfo.mapMatrix, surrounding_cells);
            if(bombs_in_range.contains(currentPosition)) {
                if(!valid_cells.isEmpty()) {
                    int index = valid_cells.size();
                    Random rand = new Random();
                    index = rand.nextInt(index);
                    Position target = valid_cells.get(index);
                    String path = move_to_cell(currentPosition, target);
                    player1.move(path);
                } else player1.move("x");
            } else if(!bombs_in_range.isEmpty()) {
                if(!valid_cells.isEmpty()) {
                    Position target = get_safest_cell(currentPosition, max(bot.power, botEnemy.power), valid_cells, bombs_in_range);
                    String path = move_to_cell(currentPosition, target);
                    player1.move(path);
                } else player1.move("x");
            } else {
                if(bot.lives > 0)
                    player1.move("b");
                else {
                    Random rand = new Random();
                    StringBuilder path = new StringBuilder();
                    int random_integer = rand.nextInt(5);
                    path.append("1234".charAt(random_integer));
                    player1.move(path.toString());
                }
            }
        };

        // This is the code that connects the player to the server.
        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer(SERVER_URL);
    }

    /// helpers
    private static int mahattanDistance(Position x, Position y) {
        return  abs(x.getRow() - y.getRow()) + abs(x.getCol() - y.getCol());
    }

    private  static List<Position> get_bombs_in_range(Position currentPosition, List<Bomb> Bombs, int power) {
        List<Position> bombs_in_range = new ArrayList<>();
        for(Bomb cell : Bombs) {
            if(mahattanDistance(cell, currentPosition) <= power * 2)
                bombs_in_range.add(new Position(cell.getRow(), cell.getCol()));
        }
        return bombs_in_range;
    }

    private static List<Position> get_surrounding_cells(int n, int m, Position currentPosition) {
        int x = currentPosition.getRow();
        int y = currentPosition.getCol();
        List<Position> all_surrounding_cells = new ArrayList<>();
        all_surrounding_cells.add(new Position(x+1, y));
        all_surrounding_cells.add(new Position(x-1, y));
        all_surrounding_cells.add(new Position(x, y+1));
        all_surrounding_cells.add(new Position(x, y-1));
        List<Position> valid_surrounding_cells = new ArrayList<>();
        for(Position cell : all_surrounding_cells) {
            int u = cell.getRow();
            int v = cell.getCol();
            if(0 <= u && u < n && 0 <= v && v <= m)
                valid_surrounding_cells.add(cell);
        }
        return  valid_surrounding_cells;
    }

    private static List<Position> get_valid_cells(int [][] mapMatrix, List<Position> valid_surrounding_cells) {
        List<Position> valid_cells = new ArrayList<>();
        for(Position cell : valid_surrounding_cells) {
            if(mapMatrix[cell.getRow()][cell.getCol()] == 0)
                valid_cells.add(cell);
        }
        return  valid_cells;
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
            int tmpDist = mahattanDistance(currentPosition, cell);
            if (tmpDist > distance) {
                safest_cell = cell;
                distance = tmpDist;
            }
        }
        return safest_cell;
    }

    private static String move_to_cell(Position sta, Position end) {
        Position diff = new Position(sta.getRow()- end.getRow(),sta.getCol()-end.getCol());
        if(diff == new Position(-1, 0))
            return "1";
        if(diff == new Position(1, 0))
            return "2";
        if(diff == new Position(0, -1))
            return "3";
        if(diff == new Position(0, 1))
            return "4";
        return "x";
    }
}

