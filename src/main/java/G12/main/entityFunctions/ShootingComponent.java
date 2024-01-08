package G12.main.entityFunctions;

import G12.main.App;
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
 */
public class ShootingComponent extends Component{

    public enum BulletType {
        NORMAL, FIRE
    }

    /**
     * Fire rate in shots per second.
     */
    protected double fireRate;
    protected double speed;
    protected Entity target;
    protected BulletType bulletType;
    List<Entity> bullets = new ArrayList<>();
    protected String bulletSprite;

    public ShootingComponent(double fireRate, double speed, BulletType bulletType) {
        this.fireRate = fireRate;
        this.speed = speed;
        this.bulletType = bulletType;

        switch (bulletType) {
            case NORMAL:
                bulletSprite = "bullets/NormalBulletSprite.png";
                break;
            case FIRE:
                bulletSprite = "bullets/FireBulletSprite.png";
                break;
        }

        FXGL.getGameTimer().runAtInterval(this::shoot, Duration.seconds(1));
    }

    public void updateTarget(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }

    public void shoot() {

        if(target != null) {
            Point2D fromTarget = entity.getCenter();
            Point2D toTarget = target.getCenter();
            Point2D direction = toTarget.subtract(fromTarget).normalize();

            bullets.add(FXGL.entityBuilder()
                    .type(App.Type.BULLET)
                    .at(entity.getPosition())
                    .viewWithBBox(bulletSprite)
                    .with(new ProjectileComponent(direction, speed))
                    .with(new OffscreenCleanComponent())
                    .collidable()
                    .buildAndAttach());
        }
    }
}
