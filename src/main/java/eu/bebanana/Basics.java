package eu.bebanana;

import static spark.Spark.get;

public class Basics {
    private Basics(){}

    public static void init() {
        get("/isup", (req, res) -> "true");
    }

    public static void init(String hook) {
        init();
        Logger.hook = hook;
    }
}
