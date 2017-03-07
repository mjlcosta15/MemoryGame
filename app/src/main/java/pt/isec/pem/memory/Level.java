package pt.isec.pem.memory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Level implements Serializable {

    private String pathImageLevel;
    private String levelName;
    private List<String> imagesPath;
    private List<String> intruders;
    private int colums;
    private int nPairs;
    private boolean defaultLevel;

    public Level(String name, boolean defaultLevel){
        imagesPath = new ArrayList<>();
        intruders = new ArrayList<>();
        this.levelName = name;
        this.defaultLevel = defaultLevel;
    }

    public void addImage(String imgSource){
        imagesPath.add(imgSource);
    }

    public List<String> getImagesPath() {
        return imagesPath;
    }

    public void setImagesPath(List<String> imagesPath) {
        this.imagesPath = imagesPath;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getPathImageLevel() {
        return pathImageLevel;
    }

    public void setPathImageLevel(String pathImageLevel) {
        this.pathImageLevel = pathImageLevel;
    }

    public int getColums() { return colums; }

    public void setColums(int colums) { this.colums = colums;}

    public int getnPairs() {
        return nPairs;
    }

    public void setnPairs(int nPairs) {
        this.nPairs = nPairs;
    }

    public List<String> getIntruders() {
        return intruders;
    }

    public void setIntruders(List<String> intruders) {
        this.intruders = intruders;
    }

    public boolean isDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(boolean defaultLevel) {
        this.defaultLevel = defaultLevel;
    }
}
