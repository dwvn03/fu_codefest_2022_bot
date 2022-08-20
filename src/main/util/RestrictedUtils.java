package main.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.Human;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.socket.data.Viruses;

public class RestrictedUtils {
    public static Set<Position> PERMANENT_RESTRICTED_NODE_SET = ConcurrentHashMap.newKeySet();
    public static final boolean BOMB_INCLUDED = true;
    public static final boolean BOMB_NOT_INCLUDED = false;

    public static List<Position> getRestrictedNode(MapInfo mapInfo, Hero hero, Boolean bombIncluded) {
        Set<Position> restrictedNode = ConcurrentHashMap.newKeySet();

        restrictedNode.addAll(getPermanentRestrictedNode(mapInfo));
        restrictedNode.addAll(mapInfo.balk);

        Position enemyPosition = mapInfo.getEnemyPosition(hero);
        restrictedNode.add(enemyPosition);

        if (bombIncluded) {
            addExplosionNode(restrictedNode, mapInfo.getBombList());
        }

        if (mapInfo.getPlayerByKey(hero.getPlayerID()).pill == 0) {
            addInfectedNode(restrictedNode, mapInfo);
        }

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

    public static void addExplosionNode(Set<Position> restrictedNode, List<Position> explodedNode) {
        restrictedNode.addAll(explodedNode);
    }
    public static void addExplosionNode(List<Position> restrictedNode, List<Position> explodedNode) {
        restrictedNode.addAll(explodedNode);
    }

    public static void addInfectedNode(Set<Position> restrictedNode, MapInfo mapInfo) {
        for (Viruses virus : mapInfo.viruses) {
            restrictedNode.add(virus.position);
            restrictedNode.add(virus.position.nextPosition(virus.direction, 1));
        }

        for (Human human : mapInfo.getDhuman()) {
            restrictedNode.add(human.position);
            restrictedNode.add(human.position.nextPosition(human.direction, 1));
        }
    }
}
