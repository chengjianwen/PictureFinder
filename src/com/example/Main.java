/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example;

import component.PictureFinder.ImageIndicatorController;
import component.PictureFinder.Indicator;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author chengjianwen
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        ImageIndicatorController newRoot = new ImageIndicatorController();
        newRoot.getComboBox().getItems().add(0, new Indicator("封面照片", 500, 500));

        Scene scene = new Scene(newRoot);
        
        primaryStage.setTitle("图片剪刀手 (版本: 0.2)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
