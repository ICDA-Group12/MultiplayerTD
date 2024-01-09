package G12.main.entities.entityFunctions;

import G12.main.entities.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows an entity to spawn bullets.
 * @Author: s215521 (s215521@dtu.dk)
 */
public class ShootingComponent extends Component{

    public enum BulletType {
        NORMAL, FIRE
    }

    protected double speed;
    protected Entity target;
    protected BulletType bulletType;
    List<Entity> bullets = new ArrayList<>();
    protected String bulletSprite;

    public ShootingComponent(double delay, double speed, BulletType bulletType) {
        this.speed = speed;
        this.bulletType = bulletType;

        switch (bulletType) {
            case NORMAL:
                bulletSprite = "bullets/BulletMK1.png";
                break;
            case FIRE:
                bulletSprite = "bullets/BulletMK2.png";
                break;
        }

        FXGL.getGameTimer().runAtInterval(this::shoot, Duration.seconds(delay));
    }

    public void updateTarget(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }

    public void shoot() {

        if(target == null) return;

        Point2D fromTarget = entity.getCenter();
        Point2D toTarget = target.getCenter();
        Point2D direction = toTarget.subtract(fromTarget).normalize();

        bullets.add(FXGL.entityBuilder()
                .type(EntityType.BULLET)
                .at(entity.getCenter())
                .viewWithBBox(bulletSprite)
                .with(new ProjectileComponent(direction, speed))
                .with(new OffscreenCleanComponent())
                .with(new StoreEntityParentComponent(entity))
                .collidable()
                .buildAndAttach());

    }
}
