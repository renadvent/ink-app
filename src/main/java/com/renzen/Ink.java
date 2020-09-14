package com.renzen;

import com.renzen.model.Caster;
import com.renzen.model.Component;
import com.renzen.model.Texture;
import com.renzen.view.Menu;
import com.renzen.view.*;

import javax.swing.*;
import java.awt.*;

public class Ink extends AbstractController {

    private static final long serialVersionUID = 1L;
    final JTabbedPane jtpr = new JTabbedPane();
    final JTabbedPane jtpl = new JTabbedPane();
    final JTabbedPane jtps = new JTabbedPane();
    final Menu menu = new Menu();
    private final Ink_Controller ink_con = new Ink_Controller(this);
    public Texture selected_texture;
    public Component selected_component;
    JFrame frame = new JFrame("Ink");
    public final Canvas_Panel can_pan = new Canvas_Panel(frame, this);
    final Action_Panel act_pan = new Action_Panel(frame, this);
    final Stroke_Panel str_pan = new Stroke_Panel(frame, this);
    Texture start = init_default_model();
    Ink ink = this;
    private Caster selected_caster;

    public Ink() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                create_and_show_default_GUI();
            }
        });
    }

    // MAIN
    public static void main(String[] args) {
        new Ink();
    }

    // GUI
    public void create_and_show_default_GUI() {

        // temp

        addView((act_pan));
        addModel(start);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // top
        frame.getContentPane().add(menu, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(can_pan), BorderLayout.CENTER);

        // left
        frame.getContentPane().add(jtpl, BorderLayout.WEST);
        jtpl.addTab("Actions", new JScrollPane(act_pan));

        // right
        frame.getContentPane().add(jtpr, BorderLayout.EAST);
        jtpr.addTab("Stroke", new JScrollPane(str_pan));


        // bottom
        frame.getContentPane().add(jtps, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

    }

    // GETTERS
    public JFrame frame() {
        return frame;
    }

    public Caster selected_caster() {
        return selected_caster;
    }

    public Texture selected_texture() {
        return selected_texture;
    }

    public Component selected_component() {
        return selected_component;
    }

    public Canvas_Panel can_pan() {
        return can_pan; //ink.can_pan;
    }

    public Action_Panel act_pan() {
        return ink.act_pan;
    }

    // SETTERS
    public void change_selected_caster(Caster v) {

        if (selected_caster() != null) {
            selected_caster().highlighted = false;
        }

        selected_caster = v;

        if (selected_caster() != null) {
            selected_caster().highlighted = true;
        }

    }

    Texture init_default_model() {

        Texture c = new_texture();
        change_selected_texture(c);
        c.load_component("src/main/java/com/renzen/body.jpg");

        return c;
    }

    public void change_selected_texture(Texture c) {
        selected_texture = c;
    }

    Texture new_texture() {
        return new Texture(1280, 1024);
    }

    //-----------------------------------------------------------------

    public class Ink_Controller {

        Ink ink;
        JFrame frame;

        Ink_Controller(Ink ink) {
            this.ink = ink;
            this.frame = ink.frame;
        }
    }
}