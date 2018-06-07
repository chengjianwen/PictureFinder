/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package component.PictureFinder;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/**
 * FXML Controller class
 *
 * 一个图片剪裁组件
 * @author chengjianwen
 */
public class Indicator extends Group implements Initializable {
    @FXML private Rectangle finder;
    @FXML private ImageView viewer;

    private String          name;
    private ImageView       background;
    private BooleanProperty draggable;
    private BooleanProperty resizable;

    final class Delta {
        double X = 0, Y = 0;
    }
    final Delta delta = new Delta();

    public Indicator() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "Indicator.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }    
    }

    public Indicator(String name, double width, double height) {
        this();
        setName(name);
        setWidth(width);
        setHeight(height);
    }
    
    public Indicator(Indicator clone) {
        this(clone.getName(), clone.getWidth(), clone.getHeight());
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {    
        finder.setOnMouseMoved(me -> {
            boolean onWest = false;
            boolean onEast = false;
            boolean onNorth = false;
            boolean onSouth = false;
            if (me.getX() < 2) 
                onWest = true;
            if (me.getY() < 2) 
                onNorth = true;
            if (me.getX() > finder.getWidth() - 2) 
                onEast = true;
            if (me.getY() > finder.getHeight()- 2) 
                onSouth = true;
            if (onWest)
                finder.setCursor(Cursor.W_RESIZE);
            else if (onEast)
                finder.setCursor(Cursor.E_RESIZE);
            else if (onNorth)
                finder.setCursor(Cursor.N_RESIZE);
            else if (onSouth)
                finder.setCursor(Cursor.S_RESIZE);
            else
                finder.setCursor(Cursor.HAND);
        });
        finder.setOnMousePressed(me -> {
            delta.X = me.getX();
            delta.Y = me.getY();
            if (finder.getCursor().equals(Cursor.HAND))
                finder.setCursor(Cursor.CLOSED_HAND);
        });
        finder.setOnMouseDragged(me -> {
            if (getDraggble() && finder.getCursor().equals(Cursor.CLOSED_HAND)) {
                finder.setTranslateX(finder.getTranslateX() + me.getX() - delta.X);
                finder.setTranslateY(finder.getTranslateY() + me.getY() - delta.Y);
            } 
            if (getResizable()) {
                if (finder.getCursor().equals(Cursor.W_RESIZE)) {
                    finder.setWidth(finder.getWidth() - me.getX());
                    finder.setTranslateX(finder.getTranslateX() + me.getX());
                }
                else if (finder.getCursor().equals(Cursor.E_RESIZE)) {
                    finder.setWidth(me.getX());
                } else if (finder.getCursor().equals(Cursor.N_RESIZE)) {
                    finder.setHeight(finder.getHeight()- me.getY());
                    finder.setTranslateY(finder.getTranslateY() + me.getY());
                }
                else if (finder.getCursor().equals(Cursor.S_RESIZE)) {
                    finder.setHeight(me.getY());
                }
            }
        });
        
        finder.setOnMouseReleased((MouseEvent me) -> {
            if (finder.getCursor().equals(Cursor.CLOSED_HAND))
                finder.setCursor(Cursor.HAND);
            
            finder.setLayoutX(finder.getLayoutX() + finder.getTranslateX());
            finder.setLayoutY(finder.getLayoutY() + finder.getTranslateY());
            finder.setTranslateX(0);
            finder.setTranslateY(0);
            finder();
        });
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setWidth(double width) {
        finder.setWidth(width);
    }
    public double getWidth() {
        return finder.getWidth();
    }
    public void setHeight(double height) {
        finder.setHeight(height);
    }
    public double getHeight() {
        return finder.getHeight();
    }
    public void setX(double value) {
        finder.setLayoutX(value);
    }
    public double getX() {
        return finder.getLayoutX();
    }
    public void setY(double value) {
        finder.setLayoutY(value);
    }
    public double getY() {
        return finder.getLayoutY();
    }
    public Bounds getBounds() {
        return finder.getBoundsInParent();
    }
    
    public void setDraggble(boolean draggable) {
        DraggableProperty().set(draggable);
    }
    
    public boolean getDraggble() {
        return DraggableProperty().get();
    }
    
    public BooleanProperty DraggableProperty() {
        if (draggable == null)
            draggable = new SimpleBooleanProperty();
        return draggable;
    }
    
    public void setResizable(boolean resizable) {
        ResizableProperty().set(resizable);
    }
    
    public boolean getResizable() {
        return ResizableProperty().get();
    }
    
    public BooleanProperty ResizableProperty() {
        if (resizable == null) {
            resizable = new SimpleBooleanProperty();
        }
        return resizable;
    }
    public void setBackground(ImageView background) {
        this.background = background;
        this.viewer.setImage(background.getImage());
    }
    public ImageView getBackground() {
        return this.background;
    }
    public Rectangle2D getViewport() {
        return viewer.getViewport();
    }
    public Image getImage() {
        Image image = background.getImage();
        Rectangle2D rectangle2D = viewer.getViewport();
        int x = (int)rectangle2D.getMinX();
        int y = (int)rectangle2D.getMinY();
        int w = (int)rectangle2D.getWidth();
        int h = (int)rectangle2D.getHeight();
        if (x < 0) {
            w += x;
            x = 0;
        }
        if (y < 0) {
            h += y;
            y = 0;
        }
        if (x + w > image.getWidth()) 
            w = (int)image.getWidth() - x;
        if (y + h > image.getHeight())
            h = (int)image.getHeight() - y;

        int[] buffer = new int[w * h];
        image.getPixelReader().getPixels(x, y, w, h, PixelFormat.getIntArgbInstance(), buffer, 0, w);
        
        WritableImage target = new WritableImage((int)rectangle2D.getWidth(), (int)rectangle2D.getHeight());
        target.getPixelWriter().setPixels(
                x - (int)rectangle2D.getMinX(),
                y - (int)rectangle2D.getMinY(),
                w, 
                h, 
                PixelFormat.getIntArgbInstance(),
                buffer, 
                0,
                w);
        double scale = background.getScaleX();
        if (scale != 1) {
            double width = target.getWidth() * scale;
            double height = target.getHeight() * scale;
            BufferedImage bi = SwingFXUtils.fromFXImage(target, null);
            BufferedImage resized = resize(bi, (int)height, (int)width);
            target = SwingFXUtils.toFXImage(resized, null);
        }
        return target;
    }
    private static BufferedImage resize(BufferedImage img, int height, int width) {
        java.awt.Image tmp = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
    // 取景
    public void finder() {
        if (background == null)
            return;
        viewer.setLayoutX(finder.getLayoutX());
        viewer.setLayoutY(finder.getLayoutY());
        viewer.setFitWidth(finder.getWidth());
        viewer.setFitHeight(finder.getHeight());

        Bounds bounds = viewer.getBoundsInParent();
        double x = bounds.getMinX();
        double y = bounds.getMinY();
        double w = viewer.getFitWidth();
        double h = viewer.getFitHeight();
        Rectangle2D viewport = new Rectangle2D(x, y, w, h);
        
        double scale = background.getScaleX();
        double width = background.getBoundsInParent().getWidth() / scale;
        double height = background.getBoundsInParent().getHeight() / scale;
        
        Rectangle2D viewportNew = reverseTranslate(viewport, scale, width, height);
        viewer.setViewport(viewportNew);
    }
    
    // 根据背景图缩放系数scale、宽度width和高度height，对取景位置进行变换
    // 变换的原理就是， 根据ImageView的缩放缩放方式，进行坐标位置逆变换
    // ImageView缩放的方式为：以中心位置为缩放原点进行缩放，所以w和h根据缩放因子直接计算，x、y要根据在背景图中的位置进行计算
    private static Rectangle2D reverseTranslate(Rectangle2D target, double scale, double width, double height) {
        double x = target.getMinX();
        double y = target.getMinY();
        double w = target.getWidth();
        double h = target.getHeight();
        width /= 2;
        height /= 2;
        x = (x - width) / scale + width;
        y = (y - height) / scale + height;
        w = w / scale;
        h = h / scale;
        return new Rectangle2D(x, y, w, h);
    }
    @Override
    public String toString() {
        return String.format("%s %.0f x %.0f", getName(), finder.getWidth(), finder.getHeight());
    }
}
