package G12.main.entities.entityFunctions;

import G12.main.entities.PlayerType;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import org.jspace.ActualField;
import org.jspace.FormalField;

import static G12.main.App.*;

public class MoveEnemyComponent extends Component {

    private int currentPathIndex = 0;

    @Override
    public void onUpdate(double tpf) {
        try {
            move();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void move() throws InterruptedException {
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
                if (playerID == PlayerType.PLAYER1) {
                    Object[] lives = gameSpace.get(new ActualField("lives"), new FormalField(Integer.class));
                    gameSpace.put("lives", (int) lives[1] - 1);
                }
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
