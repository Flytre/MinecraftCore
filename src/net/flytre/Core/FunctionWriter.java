package net.flytre.Core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FunctionWriter {

    public static String name = "flytre_custom_items";
    public static String dataLoc = name + "/data";
    public static String currentLoc = name + "/data";


    static void deleteOld() {
        FileHandler.deleteDirectory(name);
    }

    public static void setName(String newName) {
        name = newName;
        dataLoc = name + "/data";
    }


     static void createDatapack() {
        FileHandler.createDatapack(name);
    }

     static void makeTickJSON() {
        FileHandler.setOutput(dataLoc + "/minecraft/tags/functions/tick.json");

        FileHandler.print("{\n" +
                "  \"replace\": false,\n" +
                "  \"values\": [\n" +
                "  ]\n" +
                "}");
    }

     static void makeLoadJSON() {
        FileHandler.setOutput(dataLoc + "/minecraft/tags/functions/load.json");

        FileHandler.print("{\n" +
                "  \"replace\": false,\n" +
                "  \"values\": [\n" +
                "    \"flytre:init_items\"\n" +
                "  ]\n" +
                "}");
    }

     public static void makeFunction(String name) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/" + name + ".mcfunction");

        FileHandler.print("####################################################################################################");

        FileHandler.print("#Automatically Generated File");

        FileHandler.print("#Created Using: Flytre's Custom Item Generator");

         DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
         FileHandler.print("#Created: " + dateFormat.format(new Date()));

        FileHandler.print("####################################################################################################");

        FileHandler.print("");

    }


     public static void addStatment(String func, String statment) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/"+func+".mcfunction");

        FileHandler.print(statment);
    }

    public static void setLoc(String loc) {
        currentLoc = loc;
    }

    public static void section(boolean large) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/"+currentLoc+".mcfunction");

        if(!large)
            FileHandler.print("");
        else
            for(int i = 0; i < 3; i++)
                FileHandler.print("");
    }

    public static void state(String statment) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/"+currentLoc+".mcfunction");

        FileHandler.print(statment);
    }

    public static void comment(String comment) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/"+currentLoc+".mcfunction");

        FileHandler.print("#" + comment);
    }

    public static void scomment(String comment) {

        section(false);
        comment(comment);
    }


     public static void addObj(String name) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/init_items.mcfunction");

        FileHandler.print("scoreboard objectives add " + name + " dummy");
    }

     static void addObj(String name, String criteria) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/init_items.mcfunction");

        FileHandler.print("scoreboard objectives add " + name + " " + criteria);

    }

    static void addObjComment(String comment) {
        FileHandler.setOutput(dataLoc + "/flytre/functions/init_items.mcfunction");
        FileHandler.print("#" + comment);
    }


}
