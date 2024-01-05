package G12.main.entities.balloons;

import G12.main.AbstractTypes.Coordinate;

/**
 * BalloonTemplate
 * Abstract class for balloon templates.
 *
 * @version 0.1.0
 * @see Coordinate
 */
public abstract class BalloonTemplate {

    private Coordinate _position;
    private int health;
    private int speed;

    public BalloonTemplate(int x, int y, int health, int speed) {
        this._position = new Coordinate(x, y);
        this.health = health;
        this.speed = speed;
    }
}
