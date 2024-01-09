package G12.main.entities.entityFunctions;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

/**
 * Allows an entity to store its parent entity.
 * @Author: s215521 (s215521@dtu.dk)
 */
public class StoreEntityParentComponent extends Component {

    Entity parentEntity;

    public StoreEntityParentComponent(Entity parentEntity) {
        this.parentEntity = parentEntity;
    }

    public Entity getParentEntity() {
        return parentEntity;
    }
}
