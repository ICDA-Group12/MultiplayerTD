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
import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.Spawns;
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

import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;
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
            new Point2D(704, 404)
    };
    public Button mk2_btn;
    public Button mk1_btn;
    public Button plane1_btn;

    private static String role;
    public TextField joincodefield;
    public TextField sessionCode;

    private boolean timerExpired = false;
    private boolean clientsDoneSpawning = true;
    private boolean isDragging = false;
    private Entity draggedEntity = null;
    private Entity nextEntity = null;

    private String tier = "";
    private Entity bullet = null;


    // pSpaces
    private static String uri;
    private static String spaceName;
    private static String errorMsg;
    private static SpaceRepository repository;
    public static Space gameSpace = null;
    public static PlayerType playerID = null;
    private static ArrayList<PlayerType> playersInGame = new ArrayList<>();
    public Parent root;

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(768);
        settings.setHeight(574);
        settings.setTitle("Tower Defense");
        //settings.setVersion("0.1");
        settings.setMainMenuEnabled(true);
        settings.addEngineService(CustomService.class);

        settings.setSceneFactory(new SceneFactory() {
            @NotNull
            @Override
            public FXGLMenu newMainMenu() {
                return new MyMenu(MenuType.MAIN_MENU);
            }

        });

    }

    public static class CustomService extends EngineService {
        @Override
        public void onInit() {
            System.out.println("onInit()");
        }

        @Override
        public void onExit() {
//            try {
//                gameOver();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            if (playerID != PlayerType.PLAYER1 && playerID != null) {
                try {
                    gameSpace.put("leavingGame", playerID);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("onExit()");
            System.exit(0);
        }
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

        try {
//            BufferedReader teminalInput = new BufferedReader(new InputStreamReader(System.in));
//            role = teminalInput.readLine();
//            String role = "server";
            if(role.equalsIgnoreCase("server")){
                uri = "tcp://localhost:31415/?keep";
                gameSpace = new SequentialSpace();
                repository = new SpaceRepository();
                repository.add(spaceName, gameSpace);
                repository.addGate(uri);
                System.out.println("Connected to game space as server");
                gameSpace.put("gold", 1000);
                gameSpace.put("lives", 10);
                gold = 1000;
                System.out.println("Gold: " + gold);

                playerID = PlayerType.PLAYER1;
                playersInGame.add(playerID);
                gameSpace.put("players", playersInGame);
            } else {
                System.out.println("Connecting to server..." + uri);
                gameSpace = new RemoteSpace(uri);

                System.out.println("Connected to game space as client");

                gameSpace.put("newPlayer", PlayerType.PLAYER1);
                Object [] getPlayers = gameSpace.get(new ActualField("newPlayer"), new FormalField(String.class), new ActualField("answer"));
                switch (getPlayers[1].toString()){
                    case "PLAYER2":
                        playerID = PlayerType.PLAYER2;
                        System.out.println("Player " + getPlayers[1] + " joined the game");
                        break;
                    case "PLAYER3":
                        playerID = PlayerType.PLAYER3;
                        System.out.println("Player " + getPlayers[1] + " joined the game");
                        break;
                    case "PLAYER4":
                        playerID = PlayerType.PLAYER4;
                        System.out.println("Player " + getPlayers[1] + " joined the game");
                        break;
                    default:
                        errorMsg = "No available player";
                        return;
                }


//                Object [] getPlayers = gameSpace.get(new ActualField("players"),new FormalField(ArrayList.class));
//                ArrayList<Object> players = (ArrayList<Object>) getPlayers[1];
//                getAvailablePlayer(players);
//                if (playerID == null){
//                    System.out.println("No available player");
//                    return;
//                }
//
//                gameSpace.put("newPlayer", playerID);


            }

        } catch (InterruptedException | IOException ex) {
            System.out.println("no respository set up");
            return;
        }

        getGameWorld().addEntityFactory(new CustomEntityFactory());

        spawnEnemies(1,10);


    }

    private static void spawnEnemies(double delay, int limit) {
        if (playerID == PlayerType.PLAYER1){
            run(()-> {
                spawn("EnemyMK1", 0,390);
                Tuple t = new Tuple("spawn", "EnemyMK1", new Point2D(0, 390));
                try {
                    sendToAllPlayersOnline(t);
                } catch (InterruptedException e) {
                }

                return null;
            }, Duration.seconds(delay), limit);


        }
    }

    private PlayerType getAvailablePlayer() {

        System.out.println("Checking for available player");
        //get first available player
        for (PlayerType player : PlayerType.values()) {
            System.out.println("Checking player " + player);
            if (!playersInGame.contains(player)) {
                playersInGame.add(player);
                System.out.println("Player " + player + " joined the game");
                return player;
            }
        }

        return null;
    }

    @Override
    protected void initUI() {
        loadScene("Level1Nice.fxml");

        Button startRound = null;
        if (playerID == PlayerType.PLAYER1) {
            startRound = new Button("Start Next Round");

            startRound.setTranslateY(100);
            startRound.setOnAction(e -> {
                spawnEnemies(0.5, 10);
            });
            getGameScene().addUINode(startRound);
        }

        Button quitGame = new Button("Quit Game");
        quitGame.setOnAction(e -> {
            try {
                gameOver();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        getGameScene().addUINode(quitGame);

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

                if (!isDragging && clientsDoneSpawning) {
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

                                    if (playerID == PlayerType.PLAYER1){
                                        try {
                                            nextEntity = draggedEntity;
                                            sendToAllPlayersOnline(new Tuple("spawn",tier, draggedEntity.getCenter()));
                                        } catch (InterruptedException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                        clientsDoneSpawning = false;
                                        timerExpired = false;
                                        getGameTimer().runOnceAfter(() -> {
                                            timerExpired = true;
                                        }, Duration.seconds(1));

                                    }else {
                                        try {
                                            gameSpace.put(PlayerType.PLAYER1, new Tuple("spawn",tier, draggedEntity.getCenter()));
                                        } catch (InterruptedException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }
                                    isDragging = false;
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
                response = gameSpace.getp(new ActualField("newPlayer"), new ActualField(playerID));
                if (response != null) {
                    PlayerType playerToSendTo = getAvailablePlayer();
                    if (playerToSendTo != null) {
                        System.out.println("New player joined");
                        gameSpace.put("newPlayer", playerToSendTo.toString(), "answer");
                        getGameWorld().getEntitiesByType(EntityType.ENEMY, EntityType.BULLET, EntityType.TURRETMK1, EntityType.TURRETMK2).forEach(enemy -> {

                            Tuple t = null;
                            if (enemy.getType() == EntityType.ENEMY) {
                                t = new Tuple("spawn", "EnemyMK1", enemy.getCenter());
                            } else if (enemy.getType() == EntityType.BULLET) {
                                t = new Tuple("spawn", "BulletMK1", enemy.getCenter(), enemy.getComponent(ProjectileComponent.class).getDirection());
                            } else if (enemy.getType() == EntityType.TURRETMK1) {
                                t = new Tuple("spawn", "TurretMK1static", enemy.getCenter());
                            } else if (enemy.getType() == EntityType.TURRETMK2) {
                                t = new Tuple("spawn", "TurretMK2static", enemy.getCenter());
                            }
                            try {
                                gameSpace.put(playerToSendTo, t);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        gameSpace.put("newPlayer", "no space", "answer");
                    }
                }

                response = gameSpace.getp(new ActualField("leavingGame"), new FormalField(PlayerType.class));
                if (response != null) {
                    PlayerType player = (PlayerType) response[1];
                    System.out.println("Player " + player + " left the game");
                    playersInGame.remove(player);
                }

                if (!clientsDoneSpawning){
                    boolean allPlayersDone = false;
                    // get "done" from all players in playersInGame
                    if(playersInGame.size() == 1){
                        clientsDoneSpawning = true;
                        timerExpired = false;
                        System.out.println("gg");
                        spawn(tier, nextEntity.getCenter());
                    } else {
                        allPlayersDone = true;
                        for (PlayerType player : playersInGame) {
                            if (player != playerID) {
                                response = gameSpace.queryp(new ActualField("Done"), new ActualField(player));
                                if (response == null && timerExpired) {
                                    System.out.println("Player " + player + " did not respond");
                                    playersInGame.remove(player);
                                    clientsDoneSpawning = false;
                                    allPlayersDone = false;
                                    break;
                                }else {
                                    gameSpace.get(new ActualField("Done"), new ActualField(player));
                                }
                            }
                        }
                    }
                    if(allPlayersDone){
                        clientsDoneSpawning = true;
                        timerExpired = false;
                        spawn(tier, nextEntity.getCenter());
                    }
                }

                Object[] lives = gameSpace.queryp(new ActualField("lives"), new FormalField(Integer.class));
                if (lives != null) {
                    int tempLives = (int) lives[1];
                    if (tempLives <= 0) {
                        System.out.println("Game Over");
                        gameOver();

                    }
                }
            }

            if (gameSpace != null) {
                //System.out.println("Received from other player");
                response = gameSpace.getp(new ActualField(playerID),  new FormalField(Tuple.class));
                if (response != null) {
                    Tuple t = (Tuple) response[1];


                    if (playerID != PlayerType.PLAYER1) {
                        switch (t.getElementAt(0).toString()){
                            case "spawn":
                                String entityType = t.getElementAt(1).toString();
                                Point2D entityPos = (Point2D) t.getElementAt(2);

                                switch (entityType) {
                                    case "EnemyMK1":
                                        spawn(entityType, entityPos.getX(), entityPos.getY());
                                        break;

                                    case "TurretMK1static":
                                        if (playerID == PlayerType.PLAYER1){
                                            spawn("TurretMK1static", entityPos);
                                        }else {
                                            spawn("TurretMK1", entityPos);
                                        }
                                        gameSpace.put("Done", playerID);
                                        break;

                                    case "TurretMK2static":
                                        if (playerID == PlayerType.PLAYER1){
                                            spawn("TurretMK2static", entityPos);
                                        }else {
                                            spawn("TurretMK2", entityPos);
                                        }
                                        gameSpace.put("Done", playerID);
                                        break;

                                    case "BulletMK1":
                                        System.out.println("BulletMK1");
                                        Point2D direction = (Point2D) t.getElementAt(3);
                                        bullet = spawn(entityType, entityPos);
                                        bullet.addComponent(new ProjectileComponent(direction, 200));
                                        break;
                                }
                        }
                    }else {

                        try {
                            nextEntity = spawn(t.getElementAt(1).toString(), (Point2D) t.getElementAt(2));
                            tier = t.getElementAt(1).toString();
                            nextEntity.removeFromWorld();
                            sendToAllPlayersOnline(t);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        clientsDoneSpawning = false;
                        timerExpired = false;
                        getGameTimer().runOnceAfter(() -> {
                            timerExpired = true;
                        }, Duration.seconds(1));
                    }

                }
            }

            if (gameSpace != null && gameSpace.queryp(new ActualField("gold"), new ActualField(gold)) == null) {
                response = gameSpace.queryp(new ActualField("gold"), new FormalField(Integer.class));
                if (response != null){
                    int tempGold = (int) response[1];
                    if (tempGold != gold){
                        gold = tempGold;
                        System.out.println("Gold: " + gold);
                    }
                    if (gold <= 0){
                        gameOver();
                    }
                }
            }


            if (draggedEntity != null && isDragging) {
                // overwrote draggedEntity's position
                //draggedEntity.getComponent(PosistionComponent.class).setPosistion(getInput().getMousePositionWorld());
                draggedEntity.setPosition(getInput().getMouseXWorld()- draggedEntity.getWidth() / 2, getInput().getMouseYWorld() - draggedEntity.getHeight() / 2);

            }


            if (playerID == PlayerType.PLAYER1) {
                getGameWorld().getEntitiesByType(EntityType.TURRETMK1, EntityType.TURRETMK2).forEach(this::updateSpecificTurretTarget);
            }

        } catch (InterruptedException e) {
            getGameController().gotoMainMenu();
        }
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

    public static void sendToAllPlayersOnline(Tuple fields) throws InterruptedException {
        //get all players from gameSpace

        for (PlayerType player : PlayerType.values()) {
            if (playersInGame.contains(player) && player != playerID) {
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


                if (playerID == PlayerType.PLAYER1){
                    updateSpecificTurretTarget(Bullet.getComponent(StoreEntityParentComponent.class).getParentEntity());
                }
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

    private static void gameOver() throws InterruptedException {
        System.out.println(errorMsg);
        if (playerID != null){

            if (playerID != PlayerType.PLAYER1) {
                gameSpace.put("leavingGame", playerID);
            }

            if (playerID == PlayerType.PLAYER1){
                gameSpace = null;
                repository.closeGate(uri);
                repository.shutDown();
                repository = null;

            }

            playerID = null;
            System.out.println("PlayerID set to null");
            getGameController().gotoMainMenu();


        } else {
            gameSpace = null;
            System.out.println("no playerID");
            getGameController().gotoMainMenu();
        }



    }

    public static void main(String[] args) {
        launch(args);
    }

    public void joinGameButton(ActionEvent actionEvent) {
        String joinCode = joincodefield.getText();
        if (!joinCode.isEmpty()) {
            System.out.println("Joining game..." + joinCode);
            role = "client";
            uri = "tcp://localhost:31415/" + joinCode + "?keep";
            gameSpace = null;
            FXGL.getGameController().startNewGame();
        }
        joincodefield.setPromptText("Put a code dumbass");
    }

    public void newGameButton(ActionEvent actionEvent) {
        String createCode = sessionCode.getText();
        if (!createCode.isEmpty()){
            spaceName = createCode;
            System.out.println(createCode);
            System.out.println("Starting new game...");
            role = "server";
            FXGL.getGameController().startNewGame();
        }
        sessionCode.setPromptText("Put a code dumbass");
    }

    public void pickUpTurretMK2(MouseDragEvent mouseDragEvent) {
    }

    public void image_clicked(MouseEvent mouseEvent) {
    }

    public void pickUpPlane(MouseDragEvent mouseDragEvent) {
    }
}
