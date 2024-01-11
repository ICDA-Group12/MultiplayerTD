/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package G12.main;

import G12.main.entities.EntityType;
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
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

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

    // JavaFX
    public Button mk2_btn;
    public Button mk1_btn;
    public Button plane1_btn;
    public TextField joincodefield;
    public TextField createcodefield;

    // Game
    private boolean isDragging = false;
    private Entity draggedEntity = null;
    private String tier = "";
    private static int playerID = -1;
    private static int playerCount = 0;
    private ArrayList<Integer> usedPlayerIDs = new ArrayList<>();
    private String GameRoom = "";

    // pSpaces
    private static String uri;
    private static SpaceRepository repository;
    private static Space gameSpace = null;
    public Parent root;

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(768);
        settings.setHeight(574);
        settings.setTitle("Tower Defense");
        settings.setVersion("0.1");
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
        System.out.println("Server or client?");
        try {
            if (playerID == 0) { // If this is the server
                uri = "tcp://localhost:31415/?keep";
                gameSpace = new SequentialSpace();
                repository = new SpaceRepository();
                GameRoom = getRandomGameRoom();
                repository.add(GameRoom, gameSpace);
                repository.addGate(uri);
                System.out.println("Server: Created game room: " + GameRoom);
                gameSpace.put("gold", 1000);
                gameSpace.put("lives", 10);
                gold = 1000;
                System.out.println("Gold: " + gold);
                usedPlayerIDs.add(playerID);
            } else {
                gameSpace = new RemoteSpace(uri);
                System.out.println("Client: Joined game room: " + GameRoom);
                gameSpace.put("newPlayer");

                // Wait for playerID ("newPlayer" is the key and is now last in the tuple)
                Object[] response = gameSpace.get(new FormalField(Integer.class), new ActualField("newPlayer"));
                playerID = (int) response[0];

                if(playerID < 0) {
                    System.out.println("No available player");
                    gameOver();
                    return;
                }

//                Object[] getPlayers = gameSpace.get(new ActualField("players"), new FormalField(ArrayList.class));
//                ArrayList<Object> players = (ArrayList<Object>) getPlayers[1];
//                getAvailablePlayer(players);
//                if (playerID == null) {
//                    System.out.println("No available player");
//                    return;
//                }
            }
        } catch (IOException | InterruptedException e) {
            FXGL.getGameController().gotoMainMenu();
        }

        getGameWorld().addEntityFactory(new CustomEntityFactory());
    }

    @Override
    protected void onUpdate(double tpf) {
        Object [] response = null;

        try {
            if (playerID == 0) { // If this is the server
                // Check if new player wants to join
                response = gameSpace.getp(new ActualField("newPlayer"));
                if(response != null) {
                    // Reply with playerID
                    playerCount++;
                    gameSpace.put(playerCount, "newPlayer");
                    // Add playerID to used playerIDs
                    usedPlayerIDs.add(playerCount);
                }

                // Check if player wants to spawn something
                // TODO: Implement handling receiving spawn requests from a client and distributing it to all other clients.
                // TODO: Wait for all clients to reply with a spawn request before spawning the entity.

            }

            if(gameSpace.queryp(new ActualField("Spawn"), new FormalField(Tuple.class)) != null) {
                response = gameSpace.get(new ActualField(playerID),  new FormalField(Tuple.class));
                Tuple t = (Tuple) response[1];
                switch (t.getElementAt(0).toString()){
                    case "Spawn":
                        String entityType = t.getElementAt(1).toString();
                        Point2D entityPos = (Point2D) t.getElementAt(2);

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
    }

    private String getRandomGameRoom() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    @Override
    protected void initUI() {
        loadScene("Level1Nice.fxml");

        Button spawnEnemy = new Button("Next Wave");

        getGameScene().addUINode(spawnEnemy);

        spawnEnemy.setOnAction(e -> {
            if(playerID == 0) {
                spawnNextWave(1, 10);
            }
        });
    }

    private void spawnNextWave(double delay, int amount) {
        run(()-> {
            spawn("EnemyMK1", 0,390);
            Tuple t = new Tuple("Spawn", "EnemyMK1", new Point2D(0, 390));
            try {
                sendToOtherClients(t);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }, Duration.seconds(delay), amount);
    }

    @Override
    protected void initInput() {
        super.initInput();

        // 1. get input service
        Input input = FXGL.getInput();


        input.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Object[] gold;
                int goldAmount;

                if (!isDragging) { // If a turret is not in the process of being placed
                    if (e.getTarget().getClass() == Button.class) {
                        Button btn = (Button) e.getTarget();
                        if (Objects.equals(btn.getId(), "mk1_btn")) { // TurretMK1
                            tier = "TurretMK1static";
                            draggedEntity = spawn("TurretMK1", FXGL.getInput().getMousePositionWorld());
                            isDragging = true;
                        } else if (Objects.equals(btn.getId(), "mk2_btn")) { // TurretMK2
                            tier = "TurretMK2static";
                            draggedEntity = spawn("TurretMK2", FXGL.getInput().getMousePositionWorld());
                            isDragging = true;
                        }
                    }
                }else { // Make the turret follow the mouse
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
                                    sendToServer(new Tuple("Spawn", tier, new Point2D(e.getX(), e.getY())));
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

    private void sendToServer(Tuple t) {
        try {
            gameSpace.put(playerID, t);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToOtherClients(Tuple fields) throws InterruptedException {
        System.out.println("Sending to all other players... " + tier);
        for (int IDs : usedPlayerIDs) {
            if (IDs != playerID) { // Don't send to self
                // Sends specified tuple to all players
                gameSpace.put(IDs, fields);
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
        // TODO: Tell server to remove playerID from used playerIDs

        FXGL.getGameController().gotoMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void joinGameButton(ActionEvent actionEvent) {
        System.out.println("Joining game...");
        String room = joincodefield.getText();
        uri = "tcp://localhost:31415/" + room + "?keep";
        System.out.println("URI: " + uri);
        gameSpace = null;
        FXGL.getGameController().startNewGame();;
    }

    public void newGameButton(ActionEvent actionEvent) {
        System.out.println("Starting new game...");
        playerID = 0;
        FXGL.getGameController().startNewGame();
    }

    public void pickUpTurretMK2(MouseDragEvent mouseDragEvent) {
    }

    public void image_clicked(MouseEvent mouseEvent) {
    }

    public void pickUpPlane(MouseDragEvent mouseDragEvent) {
    }

    public void copyGameRoom(ActionEvent actionEvent) {
        String gameRoom = createcodefield.getText();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(gameRoom);
        clipboard.setContent(content);
    }
}
