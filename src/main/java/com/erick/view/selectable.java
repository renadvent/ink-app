package com.erick.view;


import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

public class selectable {

    static LinkedList<selectable> list = new LinkedList<selectable>();

    public selectable(){
        System.out.println("selectable added");
        list.add(this);
    }

    LinkedList<Shape> items = new LinkedList<Shape>();
    public Rectangle2D.Double rect = null;
    boolean selected=false;

}