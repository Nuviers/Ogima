package com.example.ogima.model;

public class DataModel {
    //Dados que serão enviado junto da notificação

    private String name;
    private String age;

    public DataModel(String name, String age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
