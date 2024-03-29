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
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.almasb.fxgl.dsl.FXGL.*;


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
    private String msg = "";
    public TextField joincodefield;
    public TextField sessionCode;
    public ListView<String> chatBox;
    public TextField chatField;
    OurController controller;

    private int enemisSpawned = 10;
    private double spawnrate = 0.5;

    private boolean timerExpired = false;
    private boolean canSpawnNewTower = true;
    private boolean clientsDoneSpawning = true;
    private boolean isDragging = false;
    private Entity draggedEntity = null;
    private Entity nextEntity = null;

    private String tier = "";
    private Point2D spawnPoint = null;
    private Entity bullet = null;


    // pSpaces
    private static String uri;
    private static String spaceName;
    private static String errorMsg;
    private static SpaceRepository repository;
    public static Space gameSpace = null;
    public static PlayerType playerID = null;
    private static ArrayList<PlayerType> playersInGame;
    public Parent root;

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(1000);
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
        public void onExit() {
            if (playerID != PlayerType.PLAYER1 && playerID != null) {
                try {
                    gameSpace.put("leavingGame", playerID);
                } catch (InterruptedException e) {
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

        
        if(role.equalsIgnoreCase("server")){
            uri = "tcp://localhost:31415/?keep";
            gameSpace = new SequentialSpace();
            repository = new SpaceRepository();
            repository.add(spaceName, gameSpace);
            repository.addGate(uri);
            System.out.println("Connected to game space as server");
            try {
                gameSpace.put("gold", 1000);
                gameSpace.put("lives", 10);
                playersInGame = new ArrayList<>();
                playerID = PlayerType.PLAYER1;
                playersInGame.add(playerID);
                gameSpace.put("players", playersInGame);
                
            } catch (InterruptedException e) {
                errorMsg = "Could not connect to game space";
                System.out.println(errorMsg);
                return;
            }
            new Thread(() -> {
                try {
                    while (true) {
                        Object[] response = gameSpace.get(new ActualField("leavingGame"), new FormalField(PlayerType.class));
                        if (response != null) {
                            PlayerType player = (PlayerType) response[1];
                            System.out.println("Player " + player + " left the game");
                            playersInGame.remove(player);
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();


        } else {
            System.out.println("Connecting to server..." + uri);
            try {
                gameSpace = new RemoteSpace(uri);
            } catch (IOException e) {
                errorMsg = "Could not connect to repository";
                System.out.println(errorMsg);
                return;
            }

            System.out.println("Connected to game space as client");

            Object[] getResponse = new Object[0];
            try {
                gameSpace.put("newPlayer", PlayerType.PLAYER1);
                System.out.println("Waiting for response");
                getResponse = gameSpace.get(new ActualField("newPlayer"), new FormalField(String.class), new ActualField("answer"));
                System.out.println("Response received");
            } catch (InterruptedException e) {
                errorMsg = "Could not connect to game space";
                System.out.println(errorMsg);
                getGameController().gotoMainMenu();
            }
            System.out.println(getResponse[1].toString());
            switch (getResponse[1].toString()) {
                case "PLAYER2":
                    playerID = PlayerType.PLAYER2;
                    System.out.println("Player " + getResponse[1] + " joined the game");
                    break;
                case "PLAYER3":
                    playerID = PlayerType.PLAYER3;
                    System.out.println("Player " + getResponse[1] + " joined the game");
                    break;
                case "PLAYER4":
                    playerID = PlayerType.PLAYER4;
                    System.out.println("Player " + getResponse[1] + " joined the game");
                    break;
                default:
                    errorMsg = "No available player";
                    return;
            }

        }

        getGameWorld().addEntityFactory(new CustomEntityFactory());


    }

    private static void spawnEnemies(double delay, int limit) {
        run(()-> {
            if (playerID == PlayerType.PLAYER1) {
                spawn("EnemyMK1", 0,390);
                Tuple t = new Tuple("spawn", "EnemyMK1", new Point2D(0, 390));
                try {
                    sendToAllPlayersOnline(t);
                } catch (InterruptedException e) {
                }
            }
        }, Duration.seconds(delay), limit);
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

        if (playerID == PlayerType.PLAYER1) {
            Button startRound = new Button("Start Next Round");

            startRound.setTranslateY(30);
            startRound.setOnAction(e -> {
                spawnEnemies(spawnrate, enemisSpawned);
                enemisSpawned += 5;
                spawnrate -= 0.1;
                if (spawnrate <= 0.1) {
                    spawnrate = 0.1;
                }
            });
            getGameScene().addUINode(startRound);
        }
        chatBox = new ListView<>();
        chatBox.setTranslateX(780);
        chatBox.setPrefHeight(550);
        chatBox.setMaxWidth(210);
        chatBox.setPrefWidth(210);

        chatField = new TextField();
        chatField.setTranslateX(780);
        chatField.setTranslateY(550);
        chatField.setPrefWidth(210);
        chatField.setMaxWidth(210);
        chatField.setPromptText("Type here");


        chatField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (!chatField.getText().isEmpty()) {

                    if (playerID == PlayerType.PLAYER1) {
                        msg = chatField.getText();
                        showMessage(playerID);
                        try {
                            sendToAllPlayersOnline(new Tuple("chat", msg, playerID));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }else {
                        try {
                            gameSpace.put(PlayerType.PLAYER1, new Tuple("chat", chatField.getText(), playerID));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });

        Button quitGame = new Button("Quit Game");
        quitGame.setOnAction(e -> {
            try {
                gameOver();
            } catch (InterruptedException ex) {
                getGameController().gotoGameMenu();
            }
        });
        Text goldAmount = addVarText("gold", 500, 20);
        Text livesAmount = addVarText("lives", 450, 20);
        getGameScene().addUINodes(quitGame, chatBox, chatField);

    }

    private void showMessage(PlayerType player) {
        chatBox.getItems().add(player + ": " + msg);
        chatBox.scrollTo(chatBox.getItems().size()-1);
        chatField.clear();
        chatField.setPromptText("Type here");
    }

    @Override
    protected void initInput() {
        super.initInput();

        // 1. get input service
        Input input = FXGL.getInput();

        input.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && canSpawnNewTower) {
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
                                    draggedEntity.removeFromWorld();

                                    if (playerID == PlayerType.PLAYER1){
                                        try {
                                            spawnPoint = draggedEntity.getCenter();
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
                                            canSpawnNewTower = false;
                                        } catch (InterruptedException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }
                                    isDragging = false;
                                }else {
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
                System.out.println("Not connected to gamespace");
                getGameController().gotoGameMenu();
            }
            return;
        }
            //System.out.println("Player 1");
        try {
            if (playerID == PlayerType.PLAYER1) {
                response = gameSpace.getp(new ActualField("newPlayer"), new ActualField(playerID));
                if (response != null) {
                    if (playersInGame.size() < 4) {
                        PlayerType playerToSendTo = getAvailablePlayer();
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
//
//                response = gameSpace.getp(new ActualField("leavingGame"), new FormalField(PlayerType.class));
//                if (response != null) {
//                    PlayerType player = (PlayerType) response[1];
//                    System.out.println("Player " + player + " left the game");
//                    playersInGame.remove(player);
//                }

                if (!clientsDoneSpawning){
                    boolean allPlayersDone = false;
                    // get "done" from all players in playersInGame
                    if(playersInGame.size() == 1){
                        clientsDoneSpawning = true;
                        timerExpired = false;
                        System.out.println("gg");
                        spawn(tier, spawnPoint);
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
                                }else if (response == null){
                                    allPlayersDone = false;
                                    System.out.println("Player " + player + " has not responded yet");
                                    break;
                                }else {
                                    System.out.println("Player " + player + " responded");
                                }
                            }
                        }
                    }
                    if(allPlayersDone){
                        System.out.println("All players done");
                        for (PlayerType player : playersInGame) {
                            if (player != playerID) {
                                gameSpace.get(new ActualField("Done"), new ActualField(player));
                            }
                        }
                        clientsDoneSpawning = true;
                        timerExpired = false;
                        spawn(tier, spawnPoint);
                    }
                }


            }
            Object[] lives = gameSpace.queryp(new ActualField("lives"), new FormalField(Integer.class));
            if (lives != null) {
                int tempLives = (int) lives[1];
                set("lives", tempLives);
                if (tempLives <= 0 && playerID == PlayerType.PLAYER1) {
                    System.out.println("Game Over");
                    gameOver();

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
//                                    if (playerID == PlayerType.PLAYER1){
//                                        spawn("TurretMK1static", entityPos);
//                                    }else {
//                                        spawn("TurretMK1", entityPos);
//                                    }
                                    spawn("TurretMK1static", entityPos);
                                    System.out.println("repsoning");
                                    gameSpace.put("Done", playerID);
                                    canSpawnNewTower = true;
                                    break;

                                case "TurretMK2static":
//                                    if (playerID == PlayerType.PLAYER1){
//                                        spawn("TurretMK2static", entityPos);
//                                    }else {
//                                        spawn("TurretMK2", entityPos);
//                                    }
                                    spawn("TurretMK2static", entityPos);
                                    gameSpace.put("Done", playerID);
                                    canSpawnNewTower = true;
                                    break;

                                case "BulletMK1":
                                    System.out.println("BulletMK1");
                                    Point2D direction = (Point2D) t.getElementAt(3);
                                    bullet = spawn(entityType, entityPos);
                                    bullet.addComponent(new ProjectileComponent(direction, 200));
                                    break;
                            }
                            break;
                        case "rotate":
                            break;
                        case "chat":
                            msg = t.getElementAt(1).toString();
                            showMessage((PlayerType) t.getElementAt(2));
                            break;
                    }
                }else {
                    switch (t.getElementAt(0).toString()) {
                        case "spawn":
                            try {
                                spawnPoint = (Point2D) t.getElementAt(2);
                                tier = t.getElementAt(1).toString();
                                sendToAllPlayersOnline(t);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            clientsDoneSpawning = false;
                            timerExpired = false;
                            getGameTimer().runOnceAfter(() -> {
                                timerExpired = true;
                            }, Duration.seconds(1));
                            break;
                        case "chat":
                            sendToAllPlayersOnline(t);
                            msg = t.getElementAt(1).toString();
                            showMessage((PlayerType) t.getElementAt(2));

                    }
                }
            }
        }

        if (gameSpace != null && gameSpace.queryp(new ActualField("gold"), new ActualField(gold)) == null) {
            response = gameSpace.queryp(new ActualField("gold"), new FormalField(Integer.class));
            if (response != null){
                int tempGold = (int) response[1];
                if (tempGold != geti("gold")){
                    set("gold", tempGold);
                    System.out.println("Gold: " + gold);
                }
                if (geti("gold") <= 0){
                    gameOver();
                }
            }
        }


        if (draggedEntity != null && isDragging) {
            // overwrote draggedEntity's position
            //draggedEntity.getComponent(PosistionComponent.class).setPosistion(getInput().getMousePositionWorld());
            draggedEntity.setPosition(getInput().getMouseXWorld()- draggedEntity.getWidth() / 2, getInput().getMouseYWorld() - draggedEntity.getHeight() / 2);

        }


//            if (playerID == PlayerType.PLAYER1) {
//                getGameWorld().getEntitiesByType(EntityType.TURRETMK1, EntityType.TURRETMK2).forEach(this::updateSpecificTurretTarget);
//            }
        getGameWorld().getEntitiesByType(EntityType.TURRETMK1, EntityType.TURRETMK2).forEach(this::updateSpecificTurretTarget);

        } catch (InterruptedException e) {
            getGameController().gotoGameMenu();
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
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("gold", 1000);
        vars.put("lives", 10);
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
                int health = Enemy.getComponent(HealthIntComponent.class).getValue();
                health -= 1;
                if (health <= 0) {
                    Enemy.removeFromWorld();
                    try {
                        Object [] res = gameSpace.get(new ActualField("gold"), new FormalField(Integer.class));
                        if (res != null){
                            int gold = (int) res[1] + 10;
                            set("gold", gold);
                            gameSpace.put("gold", geti("gold"));
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // enemies.remove(Enemy);
                }
                else {
                    Enemy.getComponent(HealthIntComponent.class).setValue(health);
                }
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

        if (playerID == PlayerType.PLAYER1) {
            turret.getComponent(ShootingComponent.class).updateTarget(closestEnemy[0]);
        }
    }

    private static void gameOver() throws InterruptedException {
        System.out.println(errorMsg);
        if (playerID != null){

            if (playerID != PlayerType.PLAYER1) {
                gameSpace.put("leavingGame", playerID);
            }

            if (playerID == PlayerType.PLAYER1){
                repository.remove(spaceName);
                gameSpace = null;
                repository.closeGate(uri);
                repository.shutDown();
                repository = null;


            }

            playerID = null;
            getGameController().gotoMainMenu();


        } else {
            gameSpace = null;
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
        joincodefield.setPromptText("Put a code bro");
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
        sessionCode.setPromptText("Put a code bro");
    }



}
