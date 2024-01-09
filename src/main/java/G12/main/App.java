/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package G12.main;

import G12.main.entities.EntityType;
import G12.main.entities.StoreEntityParentComponent;
import G12.main.entities.entityFunctions.ShootingComponent;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.audio.Audio;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;


/**
 * Main class for the game.
 * @Author: Group 12
 */
public class App extends GameApplication {

    protected boolean turretMK1 = false;
    protected boolean turretMK2 = false;
    protected boolean selected = true;
    protected List<Entity> enemies = new ArrayList<>(); // Create a list of enemies
    protected List<Entity> turrets = new ArrayList<>();
    // Create a list of turrets

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(1200);
        settings.setHeight(900);
        settings.setTitle("Tower Defense");

    }

    @Override
    protected void initUI() {
        Button turretMK1Button = new Button("Turret MK1");
        Button turretMK2Button = new Button("Turret MK2");

        FXGL.addUINode(turretMK1Button, 100, 100);
        FXGL.addUINode(turretMK2Button, 100, 200);

        turretMK1Button.setOnAction(e -> {
            turretMK1 = true;
            turretMK2 = false;
        });

        turretMK2Button.setOnAction(e -> {
            turretMK1 = false;
            turretMK2 = true;
        });
    }

    // Resource path
    @Override
    protected void initGame() {

        // 1. get input service
        Input input = FXGL.getInput();
        getGameWorld().addEntityFactory(new Factory());

        // 2. add key/mouse bound actions
        // when app is running press F to see output to console
        input.addAction(new UserAction("Print Line") {
            @Override
            protected void onActionBegin() {

                if (selected) {
                    if(turretMK1) {
                        // Spawn a turret at the mouse position and add it to the list of turrets
                        turrets.add(spawn("TurretMK1", FXGL.getInput().getMousePositionWorld()));
                    }
                    if(turretMK2) {
                        turrets.add(spawn("TurretMK2", FXGL.getInput().getMousePositionWorld()));
                    }
                } else {
                    // Spawn an enemy at the mouse position and add it to the list of enemies
                    enemies.add(spawn("EnemyMK1", FXGL.getInput().getMousePositionWorld()));
                }
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("Type Switch") {
            @Override
            protected void onActionBegin() {
                selected = !selected;
                if(selected) {
                    System.out.println("Selected");
                } else {
                    System.out.println("Deselected");
                }
            }
        }, KeyCode.T);
    }

    @Override
    protected void onUpdate(double tpf) {

        if(turrets == null) return;

        for (Entity turret : turrets) {
            updateSpecificTurretTarget(turret);
        }
    }

    @Override
    protected void initPhysics() {
        // the order of entities is determined by
        // the order of their types passed into this method
        FXGL.onCollision(EntityType.ENEMY, EntityType.BULLET, (enemy, bullet) -> System.out.println("On Collision"));
        Audio hitSound = getAssetLoader().loadSound("Hit.wav").getAudio();

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.ENEMY, EntityType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity Enemy, Entity Bullet) {
                // Play sound
                hitSound.play();
                Enemy.removeFromWorld();
                enemies.remove(Enemy);
                updateSpecificTurretTarget(Bullet.getComponent(StoreEntityParentComponent.class).getParentEntity());
                Bullet.removeFromWorld();
            }
        });
    }

    protected void updateSpecificTurretTarget(Entity turret) {
        if(enemies == null) return;

        double minDistance = 1000000000;
        Entity closestEnemy = null;
        for (Entity enemy : enemies) {
            double distance = turret.distance(enemy);
            if (distance < minDistance) {
                minDistance = distance;
                closestEnemy = enemy;
            }
        }
        if (closestEnemy != null) {
            double angle = Math.atan2(closestEnemy.getY() - turret.getY(), closestEnemy.getX() - turret.getX());
            // Slowly rotate to the enemy
            turret.setRotation(angle * 180 / Math.PI);
        }

        turret.getComponent(ShootingComponent.class).updateTarget(closestEnemy);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
