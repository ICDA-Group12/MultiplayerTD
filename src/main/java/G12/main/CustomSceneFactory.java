package G12.main;

import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.StartupScene;
import com.almasb.fxgl.ui.UI;
import com.almasb.fxgl.ui.UIController;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameScene;

public class CustomSceneFactory extends SceneFactory {

    @Override
    public StartupScene newStartup(int width, int height) {
        try {
            return new MainMenuScene(width, height);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MainMenuScene extends StartupScene {
        public MainMenuScene(int appWidth, int appHeight) throws IOException {
            super(appWidth, appHeight);

            UI fxmlut = getAssetLoader().loadUI("mainMenu.fxml", new UIController() {
                @Override
                public void init() {

                }
            });

            getGameScene().addUI(fxmlut);

        }
    }
}
