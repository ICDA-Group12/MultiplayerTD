/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package G12.main;

import G12.main.entities.EntityType;
import G12.main.entities.entityFunctions.MoveEnemyComponent;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import dev.DeveloperWASDControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jspace.*;

import java.io.IOException;
import java.net.UnknownHostException;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;

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

        getGameWorld().addEntityFactory(new CustomEntityFactory());

        Entity player = FXGL.entityBuilder()
                .type(Type.PLAYER)
                .at(100, 100)
                .viewWithBBox(new Rectangle(40, 40, Color.BLACK))
                .with(new DeveloperWASDControl())
                .collidable()
                .buildAndAttach();

        Entity enemy = spawn("EnemyMK1", 100, 200);

        SpaceRepository repository = new SpaceRepository();
        String uri = "tcp://localhost:31415/?keep";
        Space gameSpace = new SequentialSpace();
        repository.addGate(uri);
        repository.add("game", gameSpace);

        try {
            Space remote = new RemoteSpace("tcp://localhost:31415/game?keep");

            gameSpace.put(enemy);
            Object[] recived = remote.get(new FormalField(Entity.class));

            Entity player2 = (Entity) recived[0];
            System.out.println(player2.getX() + " " + player2.getY());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
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
