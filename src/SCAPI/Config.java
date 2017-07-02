package SCAPI;

import bwapi.Game;

public class Config {

    // Debug state
    private static boolean DEBUG = false;

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public static boolean getDebug() {
        return DEBUG;
    }

    // Game Ref
    private static Game game = null;

    public static void setGameRef(Game game) {
        Config.game = game;
    }

    public static Game getGameRef() {
        return Config.game;
    }

}
