package br.mackenzie;

public enum Difficulty {
    EASY(false, false),
    NORMAL(true, false),
    HARD(true, true);

    public final boolean hasEnemies;
    public final boolean enemiesShoot;

    Difficulty(boolean hasEnemies, boolean enemiesShoot) {
        this.hasEnemies = hasEnemies;
        this.enemiesShoot = enemiesShoot;
    }
}
