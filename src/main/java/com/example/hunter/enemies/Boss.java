/*
This is the Boss abstract class, it is a subclass of the abstract enemy superclass.
This class has the game's bosses as subclasses of it.
*/

package com.example.hunter.enemies;

import com.example.hunter.GameController;
import com.example.hunter.Player;

public abstract class Boss extends Enemy {
    GameController gameController;
    public Boss(double x, double y, double speed, int initialHealth, GameController gameController) {
        super(x, y, speed, initialHealth);
        this.gameController = gameController;
    }
    // The receiveDamage method allows the player to hurt the enemy
    @Override
    public void receiveDamage(int damage, Player player) {
        super.receiveDamage(damage, player);
        if (this.health <= 0) {
            onBossDefeated();
        }else{
            applyKnockback(player);
        }
    }
    // This method tells the GameController class that the boss has been defeated
    // so that the game can move to the next age.
    public void onBossDefeated() {
        // Notify GameController when the boss is defeated
        if (this.gameController != null) {
            this.gameController.setBossDefeated(true);
        }
    }
    // The update method is a loop that keeps track of the enemy's state.
    @Override
    public void update(Player player) {
    }
}