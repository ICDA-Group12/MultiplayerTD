package G12.main;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

import static com.almasb.fxgl.dsl.FXGL.getUIFactoryService;
import static javafx.beans.binding.Bindings.when;

public class MyMenu extends FXGLMenu {
    public MyMenu(@NotNull MenuType type) {
        super(type);
        getContentRoot().getChildren().add(loadScene("MainMenu.fxml"));

    }

    private Parent loadScene(String fxmlFileName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/" + fxmlFileName));
            return fxmlLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
