package G12.main.entities.turrets;

import javafx.scene.image.ImageView;

/**
 * A basic turret.
 *
 *
 * @version 0.1.0
 */
public class BasicTurret extends TurretTemplate {

    // Fields
    private ImageView _sprite = new ImageView("file:src/main/resources/turrets/BasicTowerSprite.png");

    public BasicTurret(double x, double y, int rotation, int range, int damage, int cost) {
        super(x, y, rotation, range, damage, cost);
    }
}
