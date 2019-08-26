package bodega.interfaces;
import bodega.Busqueda;
import bodega.ConectorSesion;
import java.util.Timer;

/**
 *
 * @author luisGonzalez
 */
public class Principal extends javax.swing.JFrame {

    private ConectorSesion login;
    
    public Principal() {
        initComponents();
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPadre = new javax.swing.JDesktopPane();
        funcionamiento = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        funcionamiento.setText("Empezar!");
        funcionamiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                funcionamientoActionPerformed(evt);
            }
        });

        panelPadre.setLayer(funcionamiento, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout panelPadreLayout = new javax.swing.GroupLayout(panelPadre);
        panelPadre.setLayout(panelPadreLayout);
        panelPadreLayout.setHorizontalGroup(
            panelPadreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPadreLayout.createSequentialGroup()
                .addGap(116, 116, 116)
                .addComponent(funcionamiento, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );
        panelPadreLayout.setVerticalGroup(
            panelPadreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPadreLayout.createSequentialGroup()
                .addContainerGap(139, Short.MAX_VALUE)
                .addComponent(funcionamiento)
                .addGap(137, 137, 137))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPadre)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPadre)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void funcionamientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_funcionamientoActionPerformed
        Timer timer = new Timer();
        Busqueda busqueda = new Busqueda();
        busqueda.busqueda();
        timer.schedule(busqueda, 0, 3000);

    }//GEN-LAST:event_funcionamientoActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton funcionamiento;
    private javax.swing.JDesktopPane panelPadre;
    // End of variables declaration//GEN-END:variables
}
