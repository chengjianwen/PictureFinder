
/*
 * 该程序基于Javafx ImageView的Viewport实现了了图像文件的剪裁功能。
 * Javafx提供图像处理（主要是显示）的类包括两个：Image和ImageView。其中前者负责存储图像数据，后者提供显示图像。
 * ImageView对图像的局显函数有两个：Clip和Viewport。Clip是继承自Node，它是对所显示内容进行遮盖，只显示指定范围的内容，而ViewPort则直接对Image数据进行控制，所以效率更高，特别是在批处理过程中，由于没有ImageView的参与，性能大大提高。
 */
package component.PictureFinder;

import java.io.FileInputStream;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Formatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javax.imageio.ImageIO;
/**
 * @author chengjianwen
 */
public class ImageIndicatorController extends AnchorPane implements Initializable {
    @FXML private ListView          clippedListView;        // 剪裁照片列表
    @FXML private CheckBox          autoClipCheckBox;       // 打开文件时自动就剪裁
    @FXML private Button            openButton;             // 打开按钮
    @FXML private Button            clipButton;             // 剪裁按钮
    @FXML private CheckBox          autoDeleteCheckBox;     // 保存后自动删除
    @FXML private Button            saveButton;             // 保存按钮
    @FXML private RadioMenuItem     saveCurrentRadioMenuItem;    // 保存当前
    @FXML private RadioMenuItem     saveAllRadioMenuItem;        // 保存所有
    @FXML private Button            deleteButton;           // 删除按钮
    @FXML private RadioMenuItem     deleteCurrentRadioMenuItem;    // 删除当前
    @FXML private RadioMenuItem     deleteAllRadioMenuItem;        // 删除所有
    @FXML private Button            zoomInButton;           // 放大按钮
    @FXML private Button            zoomOutButton;          // 缩小按钮
    @FXML private CheckBox          autoScaleCheckBox;      // 自动缩放模式    
    @FXML private CheckBox          autoLocationCheckBox;   // 自动定位模式
    @FXML private RadioMenuItem     widthFirstRadioMenuItem;             // 宽度优先
    @FXML private RadioMenuItem     heightFirstRadioMenuItem;            // 高度优先
    @FXML private ComboBox          indicatorComboBox;      // 剪裁器下拉框
    @FXML private GridPane          zoomGridPane;           // 手动位置定位按钮
    @FXML private RadioButton       leftRadioButton;        // 左
    @FXML private RadioButton       rightRadioButton;       // 右
    @FXML private RadioButton       topRadioButton;         // 上
    @FXML private RadioButton       bottomRadioButton;      // 下
    @FXML private RadioButton       centerRadioButton;      // 中
    @FXML private RadioButton       leftTopRadioButton;     // 左上
    @FXML private RadioButton       rightTopRadioButton;    // 右上
    @FXML private RadioButton       leftBottomRadioButton;  // 左下
    @FXML private RadioButton       rightBottomRadioButton; // 右下

    @FXML private ScrollPane        scroller;               // 滚动器，对原始图像进行滚动
    @FXML private ImageView         background;             // 背景图，可进行缩放
    @FXML private Rectangle         cover;                  // 背景图图遮布，同背景图大小保持一致，将背景图以半透明方式遮挡
    @FXML private Group             finderGroup;
    private Indicator               indicator;

    public ListView getListView() {
        return this.clippedListView;
    }
    
    public ComboBox getComboBox() {
        return indicatorComboBox;
    }
    
    public ImageIndicatorController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
"/component/PictureFinder/ImageIndicator.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 剪裁器下拉菜单设置
        indicatorComboBox.setItems(FXCollections.observableArrayList());
        indicatorComboBox.setCellFactory(l -> {
            return new ListCell<Indicator>() {
                @Override
                protected void updateItem(Indicator item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    }
                    else if (item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.toString());
                        setGraphic(null);
                    }
                }
            };
        });
        // 下拉框显示
        Callback<ListView, ListCell> callback = indicatorComboBox.getCellFactory();
        if (callback != null)
            indicatorComboBox.setButtonCell(callback.call(null));
        
        // 选择剪裁器Action
        indicatorComboBox.setOnAction( c -> {
            indicator = new Indicator((Indicator)indicatorComboBox.getSelectionModel().getSelectedItem());
            indicator.setDraggble(true);
            indicator.setResizable(true);
            finderGroup.getChildren().clear();
            finderGroup.getChildren().add(indicator);
            if (background.getImage() != null) {
               indicator.setBackground(background);
               indicator.finder();
            }
        });
        indicatorComboBox.getItems().addListener(new ListChangeListener<Indicator> (){
            @Override
            public void onChanged(ListChangeListener.Change<? extends Indicator> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        indicatorComboBox.getSelectionModel().selectFirst();
                        EventHandler action = indicatorComboBox.getOnAction();
                        if (action != null)
                            action.handle(null);
                    } else if (c.wasRemoved()) {
                        // 无需处理
                    } else if (c.wasUpdated()) {
                        // 无需处理
                    }
                }
            }
        });
        openButton.disableProperty().bind(indicatorComboBox.getSelectionModel().selectedItemProperty().isNull());
        autoClipCheckBox.disableProperty().bind(openButton.disableProperty());
        openButton.setOnAction(c -> {
            FileChooser     fc = new FileChooser();
            fc.setTitle("请选择照片文件");
            FileChooser.ExtensionFilter fileExtensions =
                new FileChooser.ExtensionFilter(
                    "图像文件 (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp");

            fc.setSelectedExtensionFilter(fileExtensions);
            List<File> fileList = fc.showOpenMultipleDialog(null);
            if (fileList != null) {
                for (File f : fileList) {
                    double requestWidth = indicator.getWidth();
                    double requestHeight = indicator.getHeight();
                    try {
                        Image image = new Image(new FileInputStream(f.getAbsoluteFile()));
                        background.setImage(image);
                        background.setScaleX(1);
                        background.setScaleY(1);
                        indicator.setBackground(background);
                        EventHandler action = autoScaleCheckBox.getOnAction();
                        if (action != null)
                            action.handle(null);
                        action = autoLocationCheckBox.getOnAction();
                        if (action != null)
                            action.handle(null);
                        for(Toggle toggle: centerRadioButton.getToggleGroup().getToggles()) {
                            if (toggle.isSelected()) {
                                ButtonBase bt = (ButtonBase)toggle;
                                action = bt.getOnAction();
                                if (action != null)
                                    action.handle(null);
                            }
                        }
                        indicator.finder();
                        if (autoClipCheckBox.isSelected()) {
                            action = this.clipButton.getOnAction();
                            if (action != null)
                                action.handle(null);
                        }
                    } catch (IOException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("无法读取文件: " + f.getName());
                        alert.showAndWait();
                    }
                }
            }
        });
        autoScaleCheckBox.disableProperty().bind(autoClipCheckBox.selectedProperty().not().and(clipButton.disableProperty()));
        clipButton.disableProperty().bind(background.imageProperty().isNull());
        clipButton.setOnAction(c -> {
            Image image = indicator.getImage();
            clippedListView.getItems().add(image);
            clippedListView.getSelectionModel().select(image);
        });
        
        autoDeleteCheckBox.disableProperty().bind(clippedListView.getSelectionModel().selectedItemProperty().isNull());
        saveButton.disableProperty().bind(autoDeleteCheckBox.disableProperty());
        
        saveButton.setOnAction(c -> {
            if (saveAllRadioMenuItem.isSelected())
                saveAll();
            else if (saveCurrentRadioMenuItem.isSelected())
                saveCurrent();
        });
        saveCurrentRadioMenuItem.setOnAction(c -> {
            saveButton.setText("保存(当前)");
            EventHandler action = saveButton.getOnAction();
            if (action != null)
                action.handle(null);
        });
        saveAllRadioMenuItem.setOnAction(c -> {
            saveButton.setText("保存(所有)");
            EventHandler action = saveButton.getOnAction();
            if (action != null)
                action.handle(null);
        });
        
        deleteButton.disableProperty().bind(autoDeleteCheckBox.disableProperty());
        deleteButton.setOnAction(c -> {
            if (this.deleteAllRadioMenuItem.isSelected())
                clippedListView.getItems().clear();
            else if (this.deleteCurrentRadioMenuItem.isSelected()) {
                int index = clippedListView.getSelectionModel().getSelectedIndex();
                clippedListView.getItems().remove(index);
            }
        });
        deleteCurrentRadioMenuItem.setOnAction(c -> {
            deleteButton.setText("删除(当前)");
            EventHandler action = deleteButton.getOnAction();
            if (action != null)
                action.handle(null);
        });
        deleteAllRadioMenuItem.setOnAction(c -> {
            deleteButton.setText("删除(所有)");
            EventHandler action = deleteButton.getOnAction();
            if (action != null)
                action.handle(null);
        });

        // 放大按钮
        zoomInButton.disableProperty().bind(clipButton.disableProperty());
        zoomInButton.setOnAction(c -> {
            
            double scaleX = background.getScaleX();
            double scaleY = background.getScaleY();
            scaleX *= 1.1;
            scaleY *= 1.1;
            background.setScaleX(scaleX);
            background.setScaleY(scaleY);
            //显示
            for(Toggle toggle: centerRadioButton.getToggleGroup().getToggles()) {
                if (toggle.isSelected()) {
                    ((ButtonBase)toggle).getOnAction().handle(null);
                }
            }
            if (autoScaleCheckBox.isSelected())
                autoScaleCheckBox.setSelected(false);
        });
        zoomOutButton.disableProperty().bind(clipButton.disableProperty());
        zoomOutButton.setOnAction(c -> {
            double scaleX = background.getScaleX();
            double scaleY = background.getScaleY();
            scaleX *= 0.9;
            scaleY *= 0.9;
            background.setScaleX(scaleX);
            background.setScaleY(scaleY);
            //显示
            for(Toggle toggle: centerRadioButton.getToggleGroup().getToggles()) {
                if (toggle.isSelected()) {
                    ((ButtonBase)toggle).getOnAction().handle(null);
                }
            }
            if (autoScaleCheckBox.isSelected())
                autoScaleCheckBox.setSelected(false);
        });

        //leftTopRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_up_left.png"))));
        zoomGridPane.disableProperty().bind(autoClipCheckBox.selectedProperty().not().and(clipButton.disableProperty()));
        leftTopRadioButton.setText(null);
        leftTopRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMinX();
                double y = b2.getMinY();
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.TOP_LEFT);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            leftTopRadioButton.setSelected(true);
        });
        //rightBottomRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_down_right.png"))));
        rightBottomRadioButton.setText(null);
        rightBottomRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMaxX() - b1.getWidth();
                double y = b2.getMaxY() - b1.getHeight();
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.BOTTOM_RIGHT);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            rightBottomRadioButton.setSelected(true);
        });
        //topRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_up.png"))));
        topRadioButton.setText(null);
        topRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMinX() + (b2.getWidth() - b1.getWidth()) / 2;
                double y = b2.getMinY();
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.TOP_CENTER);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            topRadioButton.setSelected(true);
        });
        //bottomRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_down.png"))));
        bottomRadioButton.setText(null);
        bottomRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMinX() + (b2.getWidth() - b1.getWidth()) / 2;
                double y = b2.getMaxY() - b1.getHeight();;
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.BOTTOM_CENTER);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            bottomRadioButton.setSelected(true);
        });
        //leftRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_left.png"))));
        leftRadioButton.setText(null);
        leftRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMinX();
                double y = b2.getMinY() + (b2.getHeight()- b1.getHeight()) / 2;
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.CENTER_LEFT);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            leftRadioButton.setSelected(true);
        });
        //rightRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_right.png"))));
        rightRadioButton.setText(null);
        rightRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMaxX() - b1.getWidth();
                double y = b2.getMinY() + (b2.getHeight()- b1.getHeight()) / 2;
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.CENTER_RIGHT);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            rightRadioButton.setSelected(true);
        });
        //leftBottomRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_down_left.png"))));
        leftBottomRadioButton.setText(null);
        leftBottomRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMinX();
                double y = b2.getMaxY() - b1.getHeight();
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.BOTTOM_LEFT);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            leftBottomRadioButton.setSelected(true);
        });
        //rightTopRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_up_right.png"))));
        rightTopRadioButton.setText(null);
        rightTopRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMaxX() - b1.getWidth();
                double y = b2.getMinY();
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.TOP_RIGHT);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            rightTopRadioButton.setSelected(true);
        });
        //centerRadioButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("nav_plain.png"))));
        centerRadioButton.setText(null);
        centerRadioButton.setOnAction(r -> {
            if (indicator != null) {
                Bounds b1 = indicator.getBounds();
                Bounds b2 = background.getBoundsInParent();
                double x = b2.getMinX() + (b2.getWidth() - b1.getWidth()) / 2;
                double y = b2.getMinY() + (b2.getHeight()- b1.getHeight()) / 2;
                indicator.setX(x);
                indicator.setY(y);
            }
            scrollerTo(Pos.CENTER);
            if (autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(false);
            centerRadioButton.setSelected(true);
        });
        autoScaleCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    double scale = 0;
                    {
                        double scaleX = scroller.getViewportBounds().getWidth() / background.getBoundsInLocal().getWidth();
                        double scaleY = scroller.getViewportBounds().getHeight() / background.getBoundsInLocal().getHeight();
                        if (Math.abs(scaleY - 1) > Math.abs(scaleX - 1)) {
                            scale = scaleY;
                        } else
                            scale = scaleX;
                    }
                    background.setScaleX(scale);
                    background.setScaleY(scale);
                    //自动定位
                    if (autoLocationCheckBox.isSelected()) {
                        EventHandler action = autoLocationCheckBox.getOnAction();
                        if (action != null)
                            action.handle(null);
                    } else {
                        for(Toggle toggle: centerRadioButton.getToggleGroup().getToggles()) {
                            if (toggle.isSelected()) {
                                EventHandler action = ((ButtonBase)toggle).getOnAction();
                                if (action != null)
                                    action.handle(null);
                            }
                        }
                    }
                }
            }
        });
        autoLocationCheckBox.disableProperty().bind(autoClipCheckBox.selectedProperty().not().and(clipButton.disableProperty()));
        autoLocationCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue)
                {
                    
                    EventHandler action = null;
                    if (widthFirstRadioMenuItem.isSelected())
                        action = widthFirstRadioMenuItem.getOnAction();
                    else if (heightFirstRadioMenuItem.isSelected())
                        action = heightFirstRadioMenuItem.getOnAction();
                    if (action != null)
                        action.handle(null);
                }
            }
        });
        widthFirstRadioMenuItem.setOnAction(c -> {
            double scale = indicator.getWidth() / background.getBoundsInLocal().getWidth();
            background.setScaleX(scale);
            background.setScaleY(scale); 
            for(Toggle toggle: centerRadioButton.getToggleGroup().getToggles()) {
                if (toggle.isSelected()) {
                    toggle.setSelected(false);
                }
            }
            autoLocationCheckBox.setText("自动定位(宽度优先)");
            if (!autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(true);
        });
        heightFirstRadioMenuItem.setOnAction(c -> {
            double scale = indicator.getHeight()/ background.getBoundsInLocal().getHeight();
            background.setScaleX(scale);
            background.setScaleY(scale); 
            for(Toggle toggle: centerRadioButton.getToggleGroup().getToggles()) {
                if (toggle.isSelected()) {
                    toggle.setSelected(false);
                }
            }
            autoLocationCheckBox.setText("自动定位(高度优先)");
            if (!autoLocationCheckBox.isSelected())
                autoLocationCheckBox.setSelected(true);
        });
        clippedListView.setItems(FXCollections.observableArrayList());
        clippedListView.setCellFactory(param -> new ListCell<Image>() {
            // https://stackoverflow.com/questions/37130122/javafx-how-to-limit-cell-width-to-the-width-of-the-listview
            final ImageView imageView = new ImageView();
            {
                setPrefWidth(0);
                imageView.setPreserveRatio(true);
                imageView.fitWidthProperty().bind(widthProperty());
            }
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                }
                else if (item != null) {
                    setText(null);
                    imageView.setImage(item);
                    setGraphic(imageView);
                }
            }
        });

        // cover大小与background大小保持一致
        background.boundsInParentProperty().addListener((ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) -> {
            cover.setWidth(newValue.getWidth());
            cover.setHeight(newValue.getHeight());
            cover.setLayoutX(newValue.getMinX());
            cover.setLayoutY(newValue.getMinY());
            indicator.finder();
        });
        cover.visibleProperty().bind(clipButton.disableProperty().not());
    }
    // 通过设置scrollpane的hvalue和vvalue，使得indicator在scroll中可见。。
    private void scrollerTo(Pos pos) {
        double vvalue = -1, hvalue = -1;
        if (indicator != null) {
            if (pos == Pos.TOP_LEFT
                    || pos == Pos.CENTER_LEFT
                    || pos == Pos.BOTTOM_LEFT)
                hvalue = 0;
            else if (pos == Pos.TOP_RIGHT
                    || pos == Pos.CENTER_RIGHT
                    || pos == Pos.BOTTOM_RIGHT)
                hvalue = 1;
            if (pos == Pos.TOP_LEFT
                    || pos == Pos.TOP_CENTER
                    || pos == Pos.TOP_RIGHT)
                vvalue = 0;
            else if (pos == Pos.BOTTOM_LEFT
                    || pos == Pos.BOTTOM_CENTER
                    || pos == Pos.BOTTOM_RIGHT)
                vvalue = 1;
            // should be center
            if (vvalue < 0)
                vvalue = 0.5;
            if (hvalue < 0)
                hvalue = 0.5;
            // scrolling values range from 0 to 1
            scroller.setVvalue(vvalue);
            scroller.setHvalue(hvalue);
        }
    }
    
    private void saveCurrent() {
        Image image = (Image)clippedListView.getSelectionModel().getSelectedItem();
        FileChooser     fc = new FileChooser();
        fc.setTitle("请输入照片文件名");
        fc.setInitialFileName("未命名");
        FileChooser.ExtensionFilter fileExtensions =
            new FileChooser.ExtensionFilter(
                "图像文件 (*.png)", "*.png");

        fc.setSelectedExtensionFilter(fileExtensions);
        File f = fc.showSaveDialog(null);
        if (f != null) {
            String filePath = f.getAbsolutePath() + ".png";
            f = new File(filePath);
            BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
            try {
                ImageIO.write(bi, "png", f);
                if (autoDeleteCheckBox.isSelected())
                        clippedListView.getItems().remove(image);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("无法写入文件: " + f.getName());
                alert.showAndWait();
            }
        }
    }
    
    private void saveAll() {
        Integer count = clippedListView.getItems().size();
        String formatter = "%s";
        if (count > 1) {
            int countLength = count.toString().length();
            formatter += String.format("%%0%dd", countLength);
        }
        formatter += ".png";
        FileChooser     fc = new FileChooser();
        fc.setTitle("请输入照片文件名");
        fc.setInitialFileName("未命名");
        FileChooser.ExtensionFilter fileExtensions =
            new FileChooser.ExtensionFilter(
                "图像文件 (*.png)", "*.png");

        fc.setSelectedExtensionFilter(fileExtensions);
        File f = fc.showSaveDialog(null);
        if (f != null) {
            for (int i = 0; i < count; i++) {
                Image image = (Image)clippedListView.getItems().get(count - i - 1);
                String filePath = String.format(formatter, f.getAbsolutePath(), i + 1);
                File f2 = new File(filePath);
                BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
                try {
                    ImageIO.write(bi, "png", f2);
                    if (autoDeleteCheckBox.isSelected())
                        clippedListView.getItems().remove(count - i - 1);
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("无法写入文件: " + f2.getName());
                    alert.showAndWait();
                }
            }
        }
    }
}
