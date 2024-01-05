package G12.main.entities;

import G12.main.AbstractTypes.Coordinate;

/**
 * TurretTemplate
 * Abstract class for turret templates.
 *
 * @version 0.1.0
 * @see Coordinate
 */
public abstract class TurretTemplate {

    // Fields
    private final Coordinate position;
    private final int _range;
    private final int _cost;
    private final int _reloadTime;

    public TurretTemplate(int x, int y, int range, int reloadTime, int cost) {
        this.position = new Coordinate(x, y);
        this._range = range;
        this._reloadTime = reloadTime;
        this._cost = cost;
    }

    public Coordinate getPosition() {
        return position;
    }

}