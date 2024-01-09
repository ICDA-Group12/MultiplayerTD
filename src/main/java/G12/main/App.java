/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package G12.main;

import G12.main.entityFunctions.ShootingComponent;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import dev.DeveloperWASDControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;


/**
 * Shows how to use collision handlers and define hitboxes for entities.
 * For collisions to work, entities must have:
 * 1. a type
 * 2. a hit box
 * 3. a CollidableComponent (added by calling collidable() on entity builder)
 * 4. a collision handler
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class App extends GameApplication {

    protected boolean selected = false;
    protected boolean turrettype = false;
    protected List<Entity> enemies; // Create a list of enemies
    protected List<Entity> turrets;
    // Create a list of turrets


    public enum Type {
        TURRET, ENEMY, BULLET
    }

    @Override
    protected void initSettings(GameSettings settings) {

        settings.setWidth(1200);
        settings.setHeight(900);
        settings.setTitle("Tower Defense");

    }

    // Resource path
    @Override
    protected void initGame() {




        // 1. get input service
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Print") {
            @Override
            protected void onActionBegin() {
                turrettype = !turrettype;
                if(turrettype) {
                    System.out.println("turrettype");
                } else {
                    System.out.println("!turrettype");
                }
            }
        }, KeyCode.R);

        // 2. add key/mouse bound actions
        // when app is running press F to see output to console
        input.addAction(new UserAction("Print Line") {
            @Override
            protected void onActionBegin() {

                if (selected) {
                    if(turrettype) {
                        FXGL.entityBuilder()
                                .type(Type.TURRET)
                                .at(FXGL.getInput().getMouseXWorld(), FXGL.getInput().getMouseYWorld())
                                .viewWithBBox("turrets/BasicTowerSprite.png")
                                .with(new ShootingComponent(0.8, 150, ShootingComponent.BulletType.NORMAL))
                                .buildAndAttach();
                    } else {
                        FXGL.entityBuilder()
                                .type(Type.TURRET)
                                .at(FXGL.getInput().getMouseXWorld(), FXGL.getInput().getMouseYWorld())
                                .viewWithBBox("turrets/BasicTowerUpgradeSprite.png")
                                .with(new ShootingComponent(0.8, 150, ShootingComponent.BulletType.FIRE))
                                .buildAndAttach();
                    }
                } else {
                    FXGL.entityBuilder()
                            .type(Type.ENEMY)
                            .at(FXGL.getInput().getMouseXWorld(), FXGL.getInput().getMouseYWorld())
                            .bbox(new HitBox(BoundingShape.box(20,40)))
                            .viewWithBBox("enemies/EnemyMK1Sprite.png")
                            .with(new DeveloperWASDControl())
                            .collidable()
                            .buildAndAttach();
                }

                turrets = getGameWorld().getEntitiesByType(Type.TURRET);
                enemies = getGameWorld().getEntitiesByType(Type.ENEMY);
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

        if(enemies == null || turrets == null) {
            return;
        }

        // For each turret rotate to the nearest enemy
        for (Entity turret : turrets) {
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

    }

    @Override
    protected void initPhysics() {
        // the order of entities is determined by
        // the order of their types passed into this method
        FXGL.onCollision(Type.ENEMY, Type.BULLET, (enemy, bullet) -> System.out.println("On Collision"));

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(Type.ENEMY, Type.BULLET) {
            @Override
            protected void onCollisionBegin(Entity Enemy, Entity Bullet) {
                Enemy.removeFromWorld();
                Bullet.removeFromWorld();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
