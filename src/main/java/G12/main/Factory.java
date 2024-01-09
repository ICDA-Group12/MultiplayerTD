package G12.main;

import G12.main.entities.EntityType;
import G12.main.entities.entityFunctions.ShootingComponent;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Factory implements EntityFactory {

    @Spawns("TurretMK1")
    public Entity TurretMK1(SpawnData data) {
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
            .type(EntityType.TURRETMK2)
            .at(data.getX() - view.getWidth() / 2, data.getY() - view.getHeight() / 2)
            .viewWithBBox(new ImageView(view))
            .with(new ShootingComponent(0.5, 200, ShootingComponent.BulletType.NORMAL))
            .collidable()
            .build();
    }

    @Spawns("EnemyMK1")
    public Entity EnemyMK1(SpawnData data) {
        Image view = new Image("assets/textures/enemies/EnemyMK1.png");

        return new EntityBuilder(data)
            .type(EntityType.ENEMY)
            .at(data.getX() - view.getWidth() / 2, data.getY() - view.getHeight() / 2)
            .viewWithBBox(new ImageView(view))
            .collidable()
            .build();
    }
}
