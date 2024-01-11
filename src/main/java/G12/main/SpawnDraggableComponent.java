package G12.main;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.input.Input;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class SpawnDraggableComponent extends Component {
    private boolean isDragging = true;


    private double offsetX = 0.0;
    private double offsetY = 0.0;

    public SpawnDraggableComponent(){
        Input input = FXGL.getInput();
        input.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && isDragging) {
                System.out.println("Released");
                setDragging(false);
            }
        });

    }
    @Override
    public void onAdded() {
        super.onAdded();
        offsetX = FXGL.getInput().getMouseXWorld() - entity.getX();
        offsetY = FXGL.getInput().getMouseYWorld() - entity.getY();
    }

    @Override
    public void onUpdate(double tpf) {
        super.onUpdate(tpf);


        if (isDragging) {
            entity.setPosition(FXGL.getInput().getMouseXWorld() - offsetX, FXGL.getInput().getMouseYWorld() - offsetY);
        }
    }

    void setDragging(boolean dragging) {
        isDragging = dragging;
    }

}
