package main.util;

import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.socket.data.Spoil;
import jsclub.codefest.sdk.algorithm.AStarSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PathUtils {
    public enum PILL {
        HAVE_PILL,
        NO_PILL
    }

    public static String shortestPath(Collection<String> paths) {
        return paths.stream()
                    .reduce("", (curr, next) -> (next.length() < curr.length() || curr.isEmpty()) ? next : curr);
    }

    public static String pathToNearestItem(MapInfo mapInfo, Position heroPos, List<Position> restrictedNode, boolean isPill) {
        ArrayList<Position> targets = mapInfo.spoils
                .stream()
                .filter(spoil -> !isPill || spoil.spoil_type == Spoil.PILL)
                .collect(Collectors.toCollection(ArrayList::new));

        Collection<String> paths = AStarSearch.getPathToAllTargets(mapInfo.mapMatrix, restrictedNode, heroPos, targets).values();
        
        return shortestPath(paths);
    }

    public static String pathToNearestHuman(MapInfo mapInfo, Position heroPos, List<Position> restrictedNode, PILL havePill) {
        ArrayList<Position> targets = mapInfo.human
                                        .stream()
                                        .filter(human -> human.curedRemainTime == 0 && (havePill == PILL.HAVE_PILL || !human.infected))
                                        .map(human -> human.position)
                                        .collect(Collectors.toCollection(ArrayList::new));

        Collection<String> paths = AStarSearch.getPathToAllTargets(mapInfo.mapMatrix, restrictedNode, heroPos, targets).values();
        
        return shortestPath(paths);
    }
    public static String pathToNearestBalk(MapInfo mapInfo, Position heroPos, List<Position> restrictedNode) {
        for (int direction = 1; direction <= 4; direction++) {
            final int DIR = direction;
            if (mapInfo.balk.stream().anyMatch(pos -> heroPos.nextPosition(DIR, 1).equals(pos)))
                return "";
        }
        ArrayList<Position> targets = new ArrayList<>(mapInfo.balk);

        Collection<String> paths = AStarSearch.getPathToAllTargets(mapInfo.mapMatrix, restrictedNode, heroPos, targets).values();

        return shortestPath(paths);
    }
}