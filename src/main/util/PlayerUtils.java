package main.util;

import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;

import java.util.ArrayList;
public class PlayerUtils {
    public static void addNonEmpty(ArrayList<String> paths, String toBeAdded) {
        if (!toBeAdded.isEmpty()) 
            paths.add(toBeAdded);
    } 

    public static boolean isOnExplosionNode(MapInfo mapInfo, Position heroPos) {
        return mapInfo.getBombList().stream()
                                        .anyMatch(pos -> pos.getCol() == heroPos.getCol() && pos.getRow() == heroPos.getRow());
    } 

    public static boolean isQuarantined(MapInfo mapInfo, Position heroPos) {
        return mapInfo.quarantinePlace.stream()
                                        .anyMatch(pos -> pos.getCol() == heroPos.getCol() && pos.getRow() == heroPos.getRow());
    } 
}