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
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.GameView;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.audio.Audio;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jspace.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

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
            new Point2D(252, 404),
            new Point2D(252, 404),
            new Point2D(252, 240),
            new Point2D(442, 240),
            new Point2D(442, 404),
            new Point2D(800, 404)
    };
    public Button mk2_btn;
    public Button mk1_btn;
    public Button plane1_btn;

    private static String role;

    private boolean isDragging = false;
    private Entity draggedEntity = null;

    private String tier = "";
    private Entity tempTurret = null;


    // pSpaces
    private String uri;
    private SpaceRepository repository;
    private Space gameSpace;
    private PlayerType playerID = null;
    public Parent root;

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(768);
        settings.setHeight(574);
        settings.setTitle("Tower Defense");
        //settings.setVersion("0.1");
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);

        settings.setSceneFactory(new SceneFactory() {
            @NotNull
            @Override
            public FXGLMenu newMainMenu() {
                return new MyMenu(MenuType.MAIN_MENU);
            }

        });

    }
    private int gold = 0;

    private void loadScene(String fxmlFileName) {
        try {


            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/" + fxmlFileName));
            root = fxmlLoader.load();
            FXGL.getGameScene().clearUINodes();
            //FXGL.getGameScene().addUINode(root);


            getGameScene().addGameView(new GameView(root, -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Resource path
    @Override
    protected void initGame() {

        //FXGL.getEventBus().addEventHandler(new EventListener());
        System.out.println("Server or client?");
        try {
//            BufferedReader teminalInput = new BufferedReader(new InputStreamReader(System.in));
//            role = teminalInput.readLine();
//            String role = "server";
            if(role.equalsIgnoreCase("server")){
                uri = "tcp://localhost:31415/?keep";
                gameSpace = new SequentialSpace();
                repository = new SpaceRepository();
                repository.add("game", gameSpace);
                repository.addGate(uri);
                System.out.println("Connected to game space as server");
                gameSpace.put("gold", 1000);
                gameSpace.put("lives", 10);
                gold = 1000;
                System.out.println("Gold: " + gold);


                ArrayList<Object> players = new ArrayList<>();
                playerID = PlayerType.PLAYER1;
                players.add(playerID);
                gameSpace.put("players", players);


            } else {
                uri = "tcp://localhost:31415/game?keep";
                gameSpace = new RemoteSpace(uri);
                System.out.println("Connected to game space as client");
                gameSpace.put("newPlayer");


                Object [] getPlayers = gameSpace.get(new ActualField("players"),new FormalField(ArrayList.class));
                ArrayList<Object> players = (ArrayList<Object>) getPlayers[1];
                getAvailablePlayer(players);
                if (playerID == null){
                    System.out.println("No available player");
                    return;
                }


            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException | IOException ex) {
            return;
        }

        getGameWorld().addEntityFactory(new CustomEntityFactory());

        if (playerID == PlayerType.PLAYER1){
            run(()-> {
                spawn("EnemyMK1", 0,390);
                Tuple t = new Tuple("spawn", "EnemyMK1", new Point2D(0, 390));
                try {
                    sendToAllPlayersOnline(t, playerID);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }, Duration.seconds(0.5));

        }


    }

    private void getAvailablePlayer(ArrayList<Object> players) throws InterruptedException {

        //get first available player
        for (PlayerType player : PlayerType.values()) {
            if (!players.contains(player.toString())) {
                playerID = player;
                players.add(playerID);
                System.out.println("Player " + playerID + " joined the game");
                break;
            }
        }

        gameSpace.put("players", players);
    }

    @Override
    protected void initUI() {
        loadScene("Level1Nice.fxml");


    }

    @Override
    protected void initInput() {
        super.initInput();

        // 1. get input service
        Input input = FXGL.getInput();


        input.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
//                System.out.println(e.getTarget());
                Object[] gold;
                int goldAmount;

                if (!isDragging) {
                    if (e.getTarget().getClass() == Button.class) {
                        Button btn = (Button) e.getTarget();
                        if (Objects.equals(btn.getId(), "mk1_btn")) {
                            tier = "TurretMK1static";
                            draggedEntity = spawn("TurretMK1", FXGL.getInput().getMousePositionWorld());
                            //System.out.println("mk1_btn clicked");
                            isDragging = true;
                            //                    turretMK1 = true;
                            //spawn("TurretMK1", FXGL.getInput().getMousePositionWorld());
                        } else if (Objects.equals(btn.getId(), "mk2_btn")) {
                            tier = "TurretMK2static";
                            //System.out.println("mk2_btn clicked");
                            draggedEntity = spawn("TurretMK2", FXGL.getInput().getMousePositionWorld());
                            isDragging = true;
                        }
                    }
                }else {
                    double x = e.getX();
                    double y = e.getY();

                    GridPane images = (GridPane) root.getChildrenUnmodifiable().get(0);
                    for( Node child: images.getChildrenUnmodifiable()) {
                        if( child instanceof ImageView) {
                            ImageView imageView = (ImageView) child;
                            boolean url = imageView.getImage().getUrl().endsWith("towerDefense_tile024.png");
                            if (x >= imageView.getLayoutX() && x <= (imageView.getLayoutX() + 63)
                                    && y >= imageView.getLayoutY() && y <= (imageView.getLayoutY() + 63)){
                                if (url) {
                                    try {
                                        gold = gameSpace.get(new ActualField("gold"), new FormalField(Integer.class));
                                        goldAmount = (int) gold[1];
                                        if (goldAmount < 100) {
                                            System.out.println("Not enough gold");
                                            gameSpace.put("gold", goldAmount);
                                            draggedEntity.removeFromWorld();
                                            isDragging = false;
                                            break;
                                        }else {
                                            gameSpace.put("gold", goldAmount - 100);
                                        }
                                    } catch (InterruptedException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    System.out.println("GOOD TO GO");
                                    //show coordinates for imageview and x and y
                                    System.out.println("imageView.getX(), " + imageView.getLayoutX() + "");
                                    System.out.println("imageView.getY(), " + imageView.getLayoutY() + "");
                                    System.out.println("y, " + y + "");
                                    System.out.println("x, " + x + "");
                                    draggedEntity.removeFromWorld();
                                    spawn(tier, e.getX(), e.getY());
                                    isDragging = false;
                                    try {
                                        sendToAllPlayers(new Tuple("spawn",tier, new Point2D(e.getX(), e.getY())), playerID);
                                    } catch (InterruptedException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }else {
                                    System.out.println("NOT GOOD TO GO");
                                    draggedEntity.removeFromWorld();
                                    isDragging = false;
                                    break;
                                }
                            }

                        }
                    }


                }
            }

        });


    }


    @Override
    protected void onUpdate(double tpf) {
        Object [] response = null;

        if (playerID == null){
            System.out.println("No available player");
            try {
                gameOver();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
            //System.out.println("Player 1");
        try {


            if (playerID == PlayerType.PLAYER1) {
                response = gameSpace.getp(new ActualField("newPlayer"));
                if (response != null) {
                    System.out.println("New player joined");
                }
            }

            if(gameSpace.queryp(new ActualField(playerID), new FormalField(Tuple.class)) != null) {
                //System.out.println("Received from other player");
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

            response = gameSpace.getp(new ActualField("gold"), new FormalField(Integer.class));
            if (response != null){
                int tempGold = (int) response[1];
                if (tempGold != gold){
                    gold = tempGold;
                    System.out.println("Gold: " + gold);
                }
                //System.out.println("Gold: " + gold);
                gameSpace.put("gold", gold);
                if (gold <= 0){
                    gameOver();
                }
            }


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }



        if (draggedEntity != null && isDragging) {
            // overwrote draggedEntity's position
            //draggedEntity.getComponent(PosistionComponent.class).setPosistion(getInput().getMousePositionWorld());
            draggedEntity.setPosition(getInput().getMousePositionWorld().subtract(draggedEntity.getHeight() / 2, draggedEntity.getWidth() / 2));

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

        System.out.println("Sending to all other players... " + tier);
        for (PlayerType player : PlayerType.values()) {
            if (player != currentPlayer) {
                // Assuming pSpace supports putting multiple values at once
                gameSpace.put(player, fields);
            }
        }

    }

    private void sendToAllPlayersOnline(Tuple fields, PlayerType playerID) throws InterruptedException {
        //get all players from gameSpace
        Object [] getPlayers = gameSpace.get(new ActualField("players"),new FormalField(ArrayList.class));
        ArrayList<Object> players = (ArrayList<Object>) getPlayers[1];
        gameSpace.put("players", players);
        for (PlayerType player : PlayerType.values()) {
            if (players.contains(player.toString()) && player != playerID) {
                //System.out.println("Sending to " + player);
                // Assuming pSpace supports putting multiple values at once
                gameSpace.put(player, fields);
            }
        }

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

    private void gameOver() throws InterruptedException {
        if (playerID != null){
            Object [] tuple = gameSpace.get(new ActualField("players"), new FormalField(ArrayList.class));
            ArrayList<Object> players = (ArrayList<Object>) tuple[1];
            // remove player from list
            players.remove(playerID.toString());
            System.out.println("Player " + playerID + " left the game");
            gameSpace.put("players", players);
            FXGL.getGameController().gotoMainMenu();

        }else {
            FXGL.getGameController().gotoMainMenu();
        }



    }

    public static void main(String[] args) {
        launch(args);
    }

    public void joinGameButton(ActionEvent actionEvent) {
        System.out.println("Joining game...");
        role = "client";
        FXGL.getGameController().startNewGame();;
    }

    public void newGameButton(ActionEvent actionEvent) {
        System.out.println("Starting new game...");
        role = "server";
        FXGL.getGameController().startNewGame();
    }

    public void pickUpTurretMK2(MouseDragEvent mouseDragEvent) {
    }

    public void image_clicked(MouseEvent mouseEvent) {
    }

    public void pickUpPlane(MouseDragEvent mouseDragEvent) {
    }
}
