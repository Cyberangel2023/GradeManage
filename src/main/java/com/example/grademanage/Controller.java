package com.example.grademanage;

import com.example.grademanage.Service.ScoreService;
import com.example.grademanage.Service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final BeanFactory beanFactory = BeanFactory.getInstance();
    @Getter
    private UserService userService;
    @Getter
    private ScoreService scoreService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = beanFactory.getBean("userService");
        scoreService = beanFactory.getBean("scoreService");
    }
}
