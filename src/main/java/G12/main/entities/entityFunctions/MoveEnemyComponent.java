package G12.main.entities.entityFunctions;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

import static G12.main.App.pathCoordinates;

public class MoveEnemyComponent extends Component {

    private int currentPathIndex = 0;

    @Override
    public void onUpdate(double tpf) {
        move();
    }

    public void move(){
        Point2D center = entity.getCenter();
        if (currentPathIndex < pathCoordinates.length - 2){
            //System.out.println(currentPathIndex);
            switch (currentPathIndex){
                case 0:
                case 2:
                    if (center.getX() >= pathCoordinates[currentPathIndex+1].getX()){
                        nextWayPoint();
                    }
                    break;
                case 1:
                    if (center.getY() <= pathCoordinates[currentPathIndex+1].getY()){
                        nextWayPoint();
                    }
                    break;
                case 3:
                    if (center.getY() >= pathCoordinates[currentPathIndex+1].getY()){
                        nextWayPoint();
                    }
                    break;
                default:
            }

        }else{
            if (center.getX() >= pathCoordinates[currentPathIndex + 1].getX()){
                currentPathIndex = 0;
                entity.removeFromWorld();
            }

        }
    }

    private void nextWayPoint() {
        if (currentPathIndex < pathCoordinates.length - 2){
            currentPathIndex++;
            Point2D currentPoint = pathCoordinates[currentPathIndex];
            Point2D nextPoint = pathCoordinates[currentPathIndex+1];
            // Calculate the direction vector from current to next point
            Point2D direction = nextPoint.subtract(currentPoint).normalize();

            entity.getComponent(PhysicsComponent.class).setLinearVelocity(direction.multiply(100));

        }
    }

}
