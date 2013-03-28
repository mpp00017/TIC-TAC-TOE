/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author juan
 */
public class JPanelConFondo extends JPanel {
 
    private Image imagen;

    public JPanelConFondo() {
    }

    public JPanelConFondo(String nombreImagen) {
        if (nombreImagen != null) {
            imagen = new ImageIcon(getClass().getResource(nombreImagen)).getImage();
        }
    }
    
    public JPanelConFondo(Image imagenInicial) {
        if (imagenInicial != null) {
            imagen = imagenInicial;
        }
    }

    public void setImagen(String nombreImagen) {
        if (nombreImagen != null) {
            imagen = new ImageIcon(getClass().getResource(nombreImagen)).getImage();
        } else {
            imagen = null;
        }

        repaint();
    }

    public void setImagen(Image nuevaImagen) {
        imagen = nuevaImagen;

        repaint();
    }
 
    @Override
    public void paint(Graphics g) {
        g.drawImage(imagen, 0, 0, getWidth(), getHeight(),
                        this);
 
        setOpaque(false);
        super.paint(g);
    }
}


