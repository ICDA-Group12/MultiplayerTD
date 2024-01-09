package G12.main.depricated;

import G12.main.Factory;
import G12.main.entities.EntityType;
import G12.main.entities.StoreEntityParentComponent;
import G12.main.entities.entityFunctions.ShootingComponent;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.audio.Audio;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.geometry.Point2D;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getPhysicsWorld;

public class TempClient extends GameApplication {

    protected List<Entity> enemies = new ArrayList<>(); // Create a list of enemies
    protected List<Entity> turrets = new ArrayList<>(); // Create a list of turrets

    // pSpaces
    String uri = "tcp://localhost:31415/game?keep";
    Space gameSpace = null;
    Object[] response = null;
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1200);
        settings.setHeight(900);
        settings.setTitle("Tower Defense Client");
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new Factory());

        System.out.println("Init game");
        try {
            gameSpace = new RemoteSpace(uri);
            System.out.println("Connected to game space");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onUpdate(double tpf) {

        try {
            if(gameSpace.queryp(new ActualField("newTurret"), new FormalField(String.class), new FormalField(Point2D.class)) != null) {
                response = gameSpace.get(new ActualField("newTurret"), new FormalField(String.class), new FormalField(Point2D.class));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(response != null) {
            String turretType = (String) response[1];
            Point2D turretPosition = (Point2D) response[2];
            System.out.println("Turret type: " + turretType);
            System.out.println("Turret position: " + turretPosition);
            Entity turret = spawn(turretType, turretPosition);
            turrets.add(turret);
            response = null;
        }

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
