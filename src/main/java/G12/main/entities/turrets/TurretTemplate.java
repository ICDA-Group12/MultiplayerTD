package G12.main.entities.turrets;

import G12.main.AbstractTypes.Coordinate;
import javafx.scene.image.ImageView;

/**
 * TurretTemplate
 * Abstract class for turret templates.
 *
 * @version 0.1.0
 * @see Coordinate
 */
public abstract class TurretTemplate {

    // Fields
    private double _x;
    private double _y;
    private ImageView _sprite = new ImageView("file:src/main/resources/turrets/TurretMK1.png");
    private final int _range;
    private final int _cost;
    private final int _fireRate;
    private int _rotation;

    TurretTemplate(double x, double y, int rotation, int range, int damage, int cost) {
        this._x = x;
        this._y = y;
        this._range = range;
        this._fireRate = damage;
        this._cost = cost;
        this._rotation = rotation;
    }

    public ImageView getSprite () {
        return _sprite;
    }

    public void setPosition(double x, double y) {
        this._x = x;
        this._y = y;
        _sprite.setX(x);
        _sprite.setY(y);
    }
}