package io.renzen.ink.Services;

import io.renzen.ink.ArtObjects.Caster;
import io.renzen.ink.ArtObjects.RenderShape;
import io.renzen.ink.Controllers.CanvasPanelController;
import io.renzen.ink.ViewPanels.CanvasPanel;
import io.renzen.ink.ViewPanels.JavaFXPanel;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;

/**
 * Gives and set information on the Canvas
 * Used by ActionPanelController
 * and CanvasPanelController to interact
 */

@Controller
public class CanvasService {

    final CanvasPanel canvasPanel;
    final RenderShapeService renderShapeService;
    final CasterService casterService;
    final RenzenService renzenService;
    final BrushService brushService;
    final CanvasPanelController canvasPanelController;
    public JavaFXPanel javaFXPanel;

    public CanvasService(CanvasPanel canvasPanel, RenderShapeService renderShapeService,
                         CasterService casterService, RenzenService renzenService, BrushService brushService
            , CanvasPanelController canvasPanelController) {
        this.canvasPanel = canvasPanel;
        this.renderShapeService = renderShapeService;
        this.casterService = casterService;
        this.renzenService = renzenService;

        this.brushService = brushService;
        this.canvasPanelController = canvasPanelController;
    }

    public void repaintCanvas() {
        canvasPanel.validate();
        canvasPanel.repaint();
    }

    public void paintOnCanvas() {

//        canvasPanelController.setCanvasPanelCO();

        var brush = brushService.getSelectedBrush();

        var adapter = new MouseAdapter() {
            RenderShape last;
            double lastX;
            double lastY;

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                renderShapeService.addRenderShape(
                        new RenderShape("first click from brush",
                                new Ellipse2D.Double(e.getX() - brush.getSize() / 2, e.getY() - brush.getSize() / 2,
                                        brush.getSize(), brush.getSize()), brush.getColor()));


                repaintCanvas();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                var temp = renderShapeService.addRenderShape(
                        new RenderShape("while dragging",
                                new Ellipse2D.Double(e.getX() - brush.getSize() / 2, e.getY() - brush.getSize() / 2,
                                        brush.getSize(), brush.getSize()), brush.getColor()));


                //TODO working on rendering lines

//                var temp = renderObjectService.addRenderShape(
//                        new RenderShape("firstClick", new Ellipse2D.Double(e.getX() - 50, e.getY() - 50, 100, 100)));
//

                if (last != null) {
                    renderShapeService.addRenderShape(
                            new RenderShape("line between",
                                    new Line2D.Double(lastX, lastY, e.getX(), e.getY()), brush.getColor()));
                }

                last = temp;
                lastX = e.getX();
                lastY = e.getY();

                repaintCanvas();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);


                removeCanvasListeners();
                repaintCanvas();
            }

        };

        canvasPanel.addMouseListener(adapter);
        canvasPanel.addMouseMotionListener(adapter);
    }


    public void toggleShowBackground() {
        canvasPanel.setShowBackground(!canvasPanel.isShowBackground());
        repaintCanvas();
    }

    public void saveFile(File file) {

        BufferedImage bi = new BufferedImage(canvasPanel.getWidth(), canvasPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) bi.getGraphics();
        canvasPanel.printAll(g2d);
        g2d.dispose();

        try {
            ImageIO.write(bi, "png", file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public String SAVE_CANVAS_AND_CREATE_NEW_ARTICLE_ON_RENZEN() {

        //get canvas and save it to a temporary file as a png
        var base = this.canvasPanelController.getCanvasPanelCO().getBaseBuffer();
        var canvasPanelCO = this.canvasPanelController.getCanvasPanelCO();

        //TODO
        //for cases for right now, base buffer will be largest buffer
        //for future, will need to set max canvas size in CanvasPanelCO

        BufferedImage bi = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        //var bi = base.getSubimage(0,0,base.getWidth(),base.getHeight());
        //BufferedImage bi = new BufferedImage(canvasPanelCO.getBaseBuffer().getWidth(), canvasPanelCO.getBaseBuffer().getHeight(), BufferedImage.TYPE_INT_ARGB);


        Graphics2D g2d = (Graphics2D) bi.getGraphics();
        //this.canvasPanelController.getInit().getBaseBuffer().

        //g2d.drawImage(base);
        //canvasPanel.printAll(g2d);

        //if (showBackground) {
        g2d.drawImage(canvasPanelCO.getBaseBuffer(), 0, 0, null);
        //}

        for (var caster : canvasPanelController.getCanvasPanelCOtoRepaint().getCasterCOList()) {
            g2d.drawImage(caster.getStrokeBuffer(), 0, 0, null);
        }

        //draws RenderShapes on screen
        for (RenderShape renderShape : renderShapeService.getRenderShapeArrayList()) {
            g2d.setColor(renderShape.getColor());
            g2d.draw(renderShape.getShape());
        }


        g2d.dispose();

        File file = null;

        try {
            file = File.createTempFile("image", ".png");
//            ImageIO.write(bi, "png", file);
            ImageIO.write(bi, "png", file);
        } catch (Exception exception) {
            exception.printStackTrace();
            return "failed";
        }

        String fileContent = "";

        try {
            fileContent = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        } catch (Exception exception) {
            System.out.println("count get contents");
            exception.printStackTrace();
            return "failed";
        }

        var jacksonResponse = renzenService.UploadArticle(fileContent);


        System.out.println("Trying to open");

        //TODO switch from uploading just an image, to uploading an image that creates a draft

        try {
            OpenArticleInBrowser(jacksonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("could not open");
        }

        return (String) jacksonResponse.get("SASUrl");

    }

    private void OpenArticleInBrowser(HashMap<?, ?> jacksonResponse) throws IOException, URISyntaxException {
        var URL = new java.net.URL(renzenService.getRoot()


                + "/OPEN_ARTICLE_DRAFT_FROM_APP?articleID="
                //+ "/newCreateArticle?image="
                + URLEncoder.encode((String) jacksonResponse.get("articleID"), StandardCharsets.UTF_8)
                + "&token="
                + URLEncoder.encode(renzenService.getAuthToken(), StandardCharsets.UTF_8));

        //opens browser window, logs in, and goes to page to create a post
        Desktop.getDesktop().browse(URL.toURI());
    }

    /**
     * this function will create a click listener
     * that will listen for the given number of clicks on the canvas
     * and then
     */
    public void getClicksFromCanvasPanelAndCreateCaster(String casterName) {

        /**
         * begins events to create a caster
         * "click and drag"
         */

        var adapter = new MouseAdapter() {

            /**
             * shows user it is active
             * @param e
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);

                if (renderShapeService.findByName("firstClick") == null) {
                    renderShapeService.deleteByName("beforeClick");

                    renderShapeService.addRenderShape(new RenderShape("beforeClick",
                            new Ellipse2D.Double(e.getX() - 25, e.getY() - 25, 50, 50)));

                    canvasPanel.validate();
                    canvasPanel.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                renderShapeService.deleteByName("beforeClick");

                /**
                 * start tracking mouse and drawing preview
                 * to current location
                 */

                //System.out.println("PRESSED");
                renderShapeService.addRenderShape(
                        new RenderShape("firstClick", new Ellipse2D.Double(e.getX() - 50, e.getY() - 50, 100, 100)));


                canvasPanel.validate();
                canvasPanel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                renderShapeService.deleteByName("drag");

                RenderShape renderShape = renderShapeService.findByName("firstClick");

                Shape shape = renderShape.getShape();
                Ellipse2D.Double circle = (Ellipse2D.Double) shape;

                renderShapeService.addRenderShape(new RenderShape("drag",
                        new Ellipse2D.Double(e.getX() - 25, e.getY() - 25, 50, 50)));

                renderShapeService.addRenderShape(new RenderShape("drag",
                        new Line2D.Double(circle.getCenterX(), circle.getCenterY(), e.getX(), e.getY())));

                //?????
//                renderObjectService.addRenderShape(
//                        new RenderShape("firstClick", new Ellipse2D.Double(e.getX() - 50, e.getY() - 50, 100, 100)));

                canvasPanel.validate();
                canvasPanel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);

                var firstClick = (Ellipse2D.Double) renderShapeService.findByName("firstClick").getShape();

                //creates caster extending from first click to where mouse was released

                var temp = new Caster(casterName, firstClick.getX(), firstClick.getY(), e.getX(), e.getY());

                //TODO working on here
                temp.setColor(casterService.getCasterColor());
                Caster caster = casterService.save(temp);

                casterService.setSelectedCaster(temp);

                //canvasPanelControllerToActionPanelViewLink.updateActionPanelWithSelectedCaster();


                javaFXPanel.UpdateActionPanelToSelectedCaster();

                renderShapeService.deleteByName("drag");
                renderShapeService.deleteByName("firstClick");

                canvasPanel.validate();
                canvasPanel.repaint();


                removeCanvasListeners();

                /**
                 * end tracking and delete listener
                 * and create new Caster
                 */
            }
        };

        canvasPanel.addMouseListener(adapter);
        canvasPanel.addMouseMotionListener(adapter);
    }

    public void removeCanvasListeners() {

        for (var listener : canvasPanel.getMouseListeners()) {
            canvasPanel.removeMouseListener(listener);
        }

        for (var listener : canvasPanel.getMouseMotionListeners()) {
            canvasPanel.removeMouseMotionListener(listener);
        }
    }


}