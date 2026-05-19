package br.mackenzie;

public class GameManager {
    private static GameManager instance;
    private Difficulty currentDifficulty = Difficulty.NORMAL;

    private GameManager() {}

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public Difficulty getDifficulty() {
        return currentDifficulty;
    }

    public void setDifficulty(Difficulty d) {
        currentDifficulty = d;
    }
}
