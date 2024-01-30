package com.example.hunter.enemies;

import com.example.hunter.GameController;
import com.example.hunter.Player;

public abstract class Boss extends Enemy {
    GameController gameController;
    public Boss(double x, double y, double speed, int initialHealth, GameController gameController) {
        super(x, y, speed, initialHealth);
        this.gameController = gameController;
    }
    @Override
    public void receiveDamage(int damage, Player player) {
        super.receiveDamage(damage, player);
        if (this.health <= 0) {
            onBossDefeated();
        }
    }

    public void onBossDefeated() {
        // Notify GameController when the boss is defeated
        if (this.gameController != null) {
            this.gameController.setBossDefeated(true);
        }
    }


    @Override
    public void update(Player player) {
    }
}

