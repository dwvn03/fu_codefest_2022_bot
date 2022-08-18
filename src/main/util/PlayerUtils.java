package main.util;

import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Player;

import java.util.List;

public class PlayerUtils {
    public static Player getPlayer(MapInfo mapInfo, Hero hero, boolean isSelf) {
        List<Player> players = mapInfo.players;
        int selfIdx = 0;

        if (players.get(1).id.equals(hero.getPlayerID())) {
            selfIdx = 1;
        }

        return isSelf ? players.get(selfIdx) : players.get(1 - selfIdx);
    }

    public static int getPlayerPower(MapInfo mapInfo, Hero hero, boolean isSelf) {
        return getPlayer(mapInfo, hero, isSelf).power;
    }

    public static String evadeBomb(MapInfo mapInfo) {
        

        return "";
    }
}
