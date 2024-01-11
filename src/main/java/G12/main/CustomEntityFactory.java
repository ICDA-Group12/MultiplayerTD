package G12.main;

import G12.main.entities.EntityType;
import G12.main.entities.entityFunctions.MoveEnemyComponent;
import G12.main.entities.entityFunctions.ShootingComponent;
import G12.main.entities.entityFunctions.StoreEntityParentComponent;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CustomEntityFactory implements EntityFactory {

    @Spawns("TurretMK1")
    public Entity TurretMK1(SpawnData data) {
        Image view = new Image("assets/textures/turrets/TurretMK1.png");

        return new EntityBuilder(data)
           .type(EntityType.SPRITE)
           .at(data.getX() - view.getWidth() / 2, data.getY() - view.getHeight() / 2)
           .viewWithBBox(new ImageView(view))
           .collidable()
           .build();
    }
    @Spawns("TurretMK1static")
    public Entity TurretMK1static(SpawnData data) {
        Image view = new Image("assets/textures/turrets/TurretMK1.png");

        return new EntityBuilder(data)
                .type(EntityType.TURRETMK1)
                .at(data.getX() - view.getWidth() / 2, data.getY() - view.getHeight() / 2)
                .viewWithBBox(new ImageView(view))
                .with(new ShootingComponent(1, 200, ShootingComponent.BulletType.NORMAL))
                .collidable()
                .build();
    }

    @Spawns("TurretMK2")
    public Entity TurretMK2(SpawnData data) {
        Image view = new Image("assets/textures/turrets/TurretMK2.png");

        return new EntityBuilder(data)
            .type(EntityType.SPRITE)
            .at(data.getX() - view.getWidth() / 2, data.getY() - view.getHeight() / 2)
            .viewWithBBox(new ImageView(view))
//                .with(new PosistionComponent())
//                .with(new SpawnDraggableComponent())
            .collidable()
            .build();
    }
    @Spawns("TurretMK2static")
    public Entity TurretMK2static(SpawnData data) {
        Image view = new Image("assets/textures/turrets/TurretMK2.png");

        return new EntityBuilder(data)
                .type(EntityType.TURRETMK2)
                .at(data.getX() - view.getWidth() / 2, data.getY() - view.getHeight() / 2)
                .viewWithBBox(new ImageView(view))
                .with(new ShootingComponent(0.5, 200, ShootingComponent.BulletType.NORMAL))
                .collidable()
                .build();
    }

    @Spawns("BulletMK1")
    public Entity BulletMK1(SpawnData data) {
        Image view = new Image("assets/textures/bullets/BulletMK1.png");

        return new EntityBuilder(data)
                .type(EntityType.BULLET)
                .viewWithBBox(new ImageView(view))
                .with(new OffscreenCleanComponent())
                .collidable()
                .buildAndAttach();
    }

    @Spawns("EnemyMK1")
    public Entity EnemyMK1(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);
        physics.setOnPhysicsInitialized(() -> physics.setLinearVelocity(100, 0));
        Image view = new Image("assets/textures/enemies/EnemyMK1.png");
        return new EntityBuilder(data)
                .type(EntityType.ENEMY)
                .at(data.getX() - view.getWidth() / 2, data.getY() - view.getHeight() / 2)
                .viewWithBBox(new ImageView(view))
                .with(new OffscreenCleanComponent())
                .with(physics)
                .with(new MoveEnemyComponent())
                .collidable()
                .build();
    }
}
