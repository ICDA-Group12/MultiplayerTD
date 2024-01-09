/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package G12.main;

import G12.main.entities.EntityType;
import G12.main.entities.PlayerType;
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
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.jspace.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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

    // pSpaces
    private String uri;
    private SpaceRepository repository;
    private Space gameSpace;
    private PlayerType playerID;
    private int playerCounter = 1;

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(800);
        settings.setHeight(600);
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
        });

        turretMK2Button.setOnAction(e -> {
            turretMK1 = false;
        });
    }

    // Resource path
    @Override
    protected void initGame() {

        BufferedReader terminalInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter your role: ");
        String role;
        try {
            role = terminalInput.readLine();

            if (role.equals("server")) {
                uri = "tcp://localhost:31415/?keep";
                playerID = PlayerType.PLAYER1;
                gameSpace = new SequentialSpace();
                repository = new SpaceRepository();
                repository.add("game", gameSpace);
                repository.addGate(uri);
                System.out.println("Connected to game space");
                gameSpace.put("gold", 1000);
            } else {
                playerID = PlayerType.PLAYER2;
                uri = "tcp://localhost:31415/game?keep";
                gameSpace = new RemoteSpace(uri);
                System.out.println("Connected to game space");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        // 1. get input service
        Input input = FXGL.getInput();
        getGameWorld().addEntityFactory(new Factory());

        input.addAction(new UserAction("Manual Spawn") {
            @Override
            protected void onActionBegin() {
                String tier = "";
                Entity tempTurret = null;

                if (selected) {
                    if (turretMK1) {
                        // Spawn a turret at the mouse position and add it to the list of turrets
                        tempTurret = spawn("TurretMK1", FXGL.getInput().getMousePositionWorld());
                        tier = "TurretMK1";
                    } else {
                        // Spawn a turret at the mouse position and add it to the list of turrets
                        tempTurret = spawn("TurretMK2", FXGL.getInput().getMousePositionWorld());
                        tier = "TurretMK2";
                    }
                    try {
                        gameSpace.put(getOppositePlayer(playerID), "spawn", tier, tempTurret.getCenter());
                        //gameSpace.put(getOppositePlayer(playerID), "newTurret", tempTurret);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    tier = "EnemyMK1";
                    tempTurret = spawn("EnemyMK1", FXGL.getInput().getMousePositionWorld());
                    try {
                        gameSpace.put(getOppositePlayer(playerID), "spawn", tier, tempTurret.getCenter());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("Type Switch") {
            @Override
            protected void onActionBegin() {
                selected = !selected;
                if (selected) {
                    System.out.println("Selected");
                } else {
                    System.out.println("Deselected");
                }
            }

        }, KeyCode.T);
    }

    @Override
    protected void onUpdate(double tpf) {
        Object [] response = null;
        try {
            if(gameSpace.queryp(new ActualField(playerID), new FormalField(String.class), new FormalField(String.class), new FormalField(Point2D.class)) != null) {
                response = gameSpace.get(new ActualField(playerID), new FormalField(String.class), new FormalField(String.class), new FormalField(Point2D.class));
                switch ((String) response[1]){
                    case "spawn":
                        String entityType = (String) response[2];
                        Point2D entityPos = (Point2D) response[3];
                        spawn(entityType, entityPos);
                        break;

                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getGameWorld().getEntitiesByType(EntityType.TURRETMK1, EntityType.TURRETMK2).forEach(this::updateSpecificTurretTarget);

        // if(turrets == null) return;
        /*
        for (Entity turret : turrets) {
            updateSpecificTurretTarget(turret);
        }

         */


    }
    private void sendToAllPlayer(List<Object> arguments, PlayerType currentPlayer){

        // Send all arguments to all players except the current player


    }

    private PlayerType getOppositePlayer(PlayerType currentPlayer){

        if (currentPlayer == PlayerType.PLAYER1){
            return PlayerType.PLAYER2;
        }

        return PlayerType.PLAYER1;

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
                // enemies.remove(Enemy);
                updateSpecificTurretTarget(Bullet.getComponent(StoreEntityParentComponent.class).getParentEntity());
                Bullet.removeFromWorld();
            }
        });
    }

    protected void updateSpecificTurretTarget(Entity turret) {

        final double[] minDistance = {1000000000};
        final Entity[] closestEnemy = {null};

        getGameWorld().getEntitiesByType(EntityType.ENEMY).forEach(enemy -> {
            double distance = turret.distance(enemy);
            if (distance < minDistance[0]) {
                minDistance[0] = distance;
                closestEnemy[0] = enemy;
            }
        });

        if (closestEnemy[0] != null) {
            double angle = Math.atan2(closestEnemy[0].getY() - turret.getY(), closestEnemy[0].getX() - turret.getX());
            // Slowly rotate to the enemy
            turret.setRotation(angle * 180 / Math.PI);
        }

        turret.getComponent(ShootingComponent.class).updateTarget(closestEnemy[0]);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
