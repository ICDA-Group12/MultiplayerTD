package G12.main.entities.entityFunctions;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.TriggerListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import static G12.main.App.root;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;

public class SpawnDraggableComponent extends Component {
    private boolean isDragging = true;
    private String path = "file:/C:/Users/skakm/IdeaProjects/Java/MultiplayerTD/target/classes/";


    private double offsetX = 0.0;
    private double offsetY = 0.0;

    public SpawnDraggableComponent(){
        Input input = FXGL.getInput();
        input.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && isDragging) {
                setDragging(false);
                double x = e.getX();
                double y = e.getY();

                GridPane images = (GridPane) root.getChildrenUnmodifiable().get(0);
                System.out.println(images.getChildrenUnmodifiable().size());
                for( Node child: images.getChildrenUnmodifiable()) {
                    if( child instanceof ImageView) {
                        ImageView imageView = (ImageView) child;
                        String url = imageView.getImage().getUrl();
                        if (x >= imageView.getLayoutX() && x <= (imageView.getLayoutX() + 64)
                                && y >= imageView.getLayoutY() && y <= (imageView.getLayoutY() + 64)){
                            System.out.println(url);
                            if(url.equals(path + "Default_size/towerDefense_tile024.png")) {
                                System.out.println("GOOD TO GO");
                                //show coordinates for imageview and x and y
                                System.out.println("imageView.getX(), " + imageView.getLayoutX() + "");
                                System.out.println("imageView.getY(), " + imageView.getLayoutY() + "");
                                System.out.println("y, " + y + "");
                                System.out.println("x, " + x + "");
                                entity.removeFromWorld();
                                spawn("TurretMK1static", e.getX(), e.getY());
                            }else {
                                System.out.println("NOT GOOD TO GO");
                                entity.removeFromWorld();

                                break;
                            }
                        }

                    }
                }
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
