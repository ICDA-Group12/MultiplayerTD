/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package G12.main;

import G12.main.entities.EntityType;
import G12.main.entities.PlayerType;
import G12.main.entities.entityFunctions.StoreEntityParentComponent;
import G12.main.entities.entityFunctions.ShootingComponent;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.GameView;
import com.almasb.fxgl.audio.Audio;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.jspace.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;
import static com.almasb.fxgl.dsl.FXGL.run;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;


/**
 * Main class for the game.
 * @Author: Group 12
 */
public class App extends GameApplication {
    public static final Point2D[] pathCoordinates= {
            new Point2D(300, 390),
            new Point2D(277, 380),
            new Point2D(277, 200),
            new Point2D(450, 200),
            new Point2D(450, 390),
            new Point2D(800, 390)
    };

    protected boolean turretMK1 = false;
    protected boolean turretMK2 = false;
    protected boolean selected = true;

    // pSpaces
    private String uri;
    private SpaceRepository repository;
    private Space gameSpace;
    private PlayerType playerID;

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(768);
        settings.setHeight(574);
        settings.setTitle("Tower Defense");

    }

    private void loadScene(String fxmlFileName) {
        try {


            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/" + fxmlFileName));
            Parent root = fxmlLoader.load();
            FXGL.getGameScene().clearUINodes();
            //FXGL.getGameScene().addUINode(root);


            getGameScene().addGameView(new GameView(root, -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initUI() {
        //loadScene("Level1Nice.fxml");
        loadScene("MainMenu.fxml");

        Button serverButton = new Button("Host Game");
        Button clientButton = new Button("Join Game");

        Button turretMK1Button = new Button("Turret MK1");
        Button turretMK2Button = new Button("Turret MK2");

        FXGL.addUINode(turretMK1Button, 100, 100);
        FXGL.addUINode(turretMK2Button, 100, 200);

        serverButton.setOnAction(e -> {
        });




        clientButton.setOnAction(e -> {
        });

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
        System.out.println("Server or client?");
        try {
            BufferedReader teminalInput = new BufferedReader(new InputStreamReader(System.in));
            String role = teminalInput.readLine();

            if(role.equals("server")){
                uri = "tcp://localhost:31415/?keep";
                gameSpace = new SequentialSpace();
                repository = new SpaceRepository();
                repository.add("game", gameSpace);
                repository.addGate(uri);
                System.out.println("Connected to game space");
                gameSpace.put("gold", 1000);
                gameSpace.put("lives", 10);
                gameSpace.put("players", 1);
                playerID = PlayerType.PLAYER1;


            } else if (role.equals("client")){
                uri = "tcp://localhost:31415/game?keep";
                gameSpace = new RemoteSpace(uri);
                System.out.println("Connected to game space");

                Object [] getPlayers = gameSpace.get(new ActualField("players"),new FormalField(Integer.class));
                int playerCounter = (int) getPlayers[1];
                if (playerCounter == 4){
                    System.out.println("Game is full");
                    System.exit(0);
                }
                switch (playerCounter){
                    case 1:
                        playerID = PlayerType.PLAYER2;
                        break;
                    case 2:
                        playerID = PlayerType.PLAYER3;
                        break;
                    case 3:
                        playerID = PlayerType.PLAYER4;
                        break;
                }
                gameSpace.put("players", playerCounter+1);
            }

        } catch (InterruptedException | IOException ex) {
            throw new RuntimeException(ex);
        }

        FXGL.image("level1.png", 800, 600);

        // Set the background image
        //FXGL.getGameScene().setBackgroundRepeat(FXGL.image("level1.png", 800, 600));
        getGameScene().setUIMouseTransparent(true);

        // 1. get input service
        Input input = FXGL.getInput();
        getGameWorld().addEntityFactory(new Factory());

        
        run(()-> {
            spawn("EnemyMK1", 0,390);

        }, Duration.seconds(0.5));

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
                        //gameSpace.put(getOppositePlayer(playerID), "spawn", tier, tempTurret.getCenter());
                        //gameSpace.put(getOppositePlayer(playerID), "newTurret", tempTurret);
                        Tuple t = new Tuple("spawn", tier, tempTurret.getCenter());
                        sendToAllPlayers(t, playerID);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    tier = "EnemyMK1";
                    tempTurret = spawn("EnemyMK1", FXGL.getInput().getMousePositionWorld());
                    try {
                        //gameSpace.put(getOppositePlayer(playerID), "spawn", tier, tempTurret.getCenter());
                        Tuple t = new Tuple("spawn", tier, tempTurret.getCenter());
                        sendToAllPlayers(t, playerID);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, MouseButton.BACK);

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
            if(gameSpace.queryp(new ActualField(playerID), new FormalField(Tuple.class)) != null) {
                response = gameSpace.get(new ActualField(playerID),  new FormalField(Tuple.class));
                Tuple t = (Tuple) response[1];
                switch (t.getElementAt(0).toString()){
                    case "spawn":
                        String entityType = t.getElementAt(1).toString();
                        Point2D entityPos = (Point2D) t.getElementAt(2);
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
    private void sendToAllPlayers(Tuple fields, PlayerType currentPlayer) throws InterruptedException {

        System.out.println("Sending to all other players...");
        for (PlayerType player : PlayerType.values()) {
            if (player != currentPlayer) {
                // Assuming pSpace supports putting multiple values at once
                gameSpace.put(player, fields);
            }
        }

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
        getPhysicsWorld().setGravity(0,0);
        FXGL.onCollision(EntityType.ENEMY, EntityType.BULLET, (enemy, bullet) -> System.out.println("On Collision"));
        Audio hitSound = getAssetLoader().loadSound("Hit.wav").getAudio();

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.ENEMY, EntityType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity Enemy, Entity Bullet) {
                // Play sound
                // hitSound.play();
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

    public void pickUpTurretMK2(MouseDragEvent mouseDragEvent) {
        spawn("TurretMK2", mouseDragEvent.getX(), mouseDragEvent.getY());
    }

    public void pickUpPlane(MouseDragEvent mouseDragEvent) {
    }

    public void pickUpTurretMK1(MouseDragEvent mouseDragEvent) {
        spawn("TurretMK1", mouseDragEvent.getX(), mouseDragEvent.getY());
    }

    public void joinGameButton(ActionEvent actionEvent) {
        System.out.println("Joining game...");
    }

    public void newGameButton(ActionEvent actionEvent) {
        System.out.println("Starting new game...");
        loadScene("Level1Nice.fxml");
    }
}
