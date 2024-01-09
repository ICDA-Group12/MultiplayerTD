/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package G12.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
public class PhysicsSample extends GameApplication {

    private enum Type {
        PLAYER, ENEMY
    }

    @Override
    protected void initSettings(GameSettings settings) { }

    // Resource path
    @Override
    protected void initGame() {
        FXGL.entityBuilder()
                .type(Type.PLAYER)
                .at(100, 100)
                .view("TurretMK1.png")
                .buildAndAttach();

        FXGL.entityBuilder()
                .type(Type.ENEMY)
                .at(200, 100)
                // 1. OR let the view generate it from view data
                .viewWithBBox(new Rectangle(40, 40, Color.RED))
                // 2. make it collidable
                .collidable()
                .buildAndAttach();
    }

    @Override
    protected void initPhysics() {
        // the order of entities is determined by
        // the order of their types passed into this method
        FXGL.onCollision(Type.PLAYER, Type.ENEMY, (player, enemy) -> System.out.println("On Collision"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
