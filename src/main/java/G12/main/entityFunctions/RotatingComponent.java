package G12.main.entityFunctions;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

public class RotatingComponent extends Component {

    // degrees per second
    protected double rotationSpeed;
    // Target to follow
    protected Entity target;

    public RotatingComponent(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void newTarget(Entity target) {
        this.target = target;
    }
    @Override
    public void onUpdate(double tpf) {
        // Use rotationSpeed to rotate the entity
        entity.setRotation(entity.getRotation() + rotationSpeed * tpf);
    }

}
