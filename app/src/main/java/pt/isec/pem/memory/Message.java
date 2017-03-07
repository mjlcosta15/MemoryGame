package pt.isec.pem.memory;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String levelName;
    private String message;
    private boolean running; //se o jogo acabou = false
    private boolean yourTurn;
    private boolean faceUp;
    private int theirScore;
    private Level lvl;
    private int nPairs;
    private int firstImagePosition;
    private int secondImagePosition;
    private ArrayList<String> adapterArray;
    private boolean rightPair;
    private boolean aborted;

    public Message(){
        yourTurn = false;
        rightPair = false;
        theirScore = 0;
        running = true;
        firstImagePosition = -1;
        secondImagePosition = -1;
        aborted = false;
    }

    public Level getLvl() {
        return lvl;
    }

    public void setLvl(Level lvl) {
        this.lvl = lvl;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getFirstImagePosition() {
        return firstImagePosition;
    }

    public void setFirstImagePosition(int firstImagePosition) {
        this.firstImagePosition = firstImagePosition;
    }

    public int getSecondImagePosition() {
        return secondImagePosition;
    }

    public void setSecondImagePosition(int secondImagePosition) {
        this.secondImagePosition = secondImagePosition;
    }

    public boolean isYourTurn() {
        return yourTurn;
    }

    public void setYourTurn(boolean yourTurn) {
        this.yourTurn = yourTurn;
    }

    public ArrayList<String> getAdapterArray() {
        return adapterArray;
    }

    public void setAdapterArray(ArrayList<String> adapterArray) {
        this.adapterArray = adapterArray;
    }

    public int getTheirScore() {
        return theirScore;
    }

    public void setTheirScore(int theirScore) {
        this.theirScore = theirScore;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public int getnPairs() {
        return nPairs;
    }

    public void setnPairs(int nPairs) {
        this.nPairs = nPairs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRightPair() {
        return rightPair;
    }

    public void setRightPair(boolean rightPair) {
        this.rightPair = rightPair;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }
}
