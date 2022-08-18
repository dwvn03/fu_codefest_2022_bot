package main.util;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.Bomb;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;

public class RestrictedDetails {
    public static Set<Position> PERMANENT_RESTRICTED_NODE_SET = ConcurrentHashMap.newKeySet();
    
    public static ArrayList<Position> getRestrictedNode(MapInfo mapInfo, Hero hero) {
        Set<Position> restrictedNode = ConcurrentHashMap.newKeySet();

        restrictedNode.addAll(getPermanentRestrictedNode(mapInfo));
        restrictedNode.addAll(mapInfo.balk);

        Position enemyPosition = mapInfo.getEnemyPosition(hero);
        restrictedNode.add(enemyPosition);

        addExplodedNode(restrictedNode, mapInfo);

        return new ArrayList<>(restrictedNode);
    }

    public static Set<Position> getPermanentRestrictedNode(MapInfo mapInfo) {
        if (PERMANENT_RESTRICTED_NODE_SET.size() == 0) {
            PERMANENT_RESTRICTED_NODE_SET.addAll(mapInfo.teleportGate);
            PERMANENT_RESTRICTED_NODE_SET.addAll(mapInfo.walls);
            PERMANENT_RESTRICTED_NODE_SET.addAll(mapInfo.quarantinePlace);
        }

        return PERMANENT_RESTRICTED_NODE_SET;
    }

    public static void addExplodedNode(Set<Position> restrictedNode, MapInfo mapInfo) {
        for (Bomb bomb : mapInfo.bombs) {
            int bombPower = mapInfo.getPlayerByKey(bomb.playerId).power;
            int bombCol = bomb.getCol();
            int bombRow = bomb.getRow();

            for (int i = -1 * bombPower; i <= bombPower; i++) {
                restrictedNode.add(
                    new Position(bombCol + i, bombRow)
                );
                restrictedNode.add(
                    new Position(bombCol, bombRow + i)
                );
            }
        }
    }

}
