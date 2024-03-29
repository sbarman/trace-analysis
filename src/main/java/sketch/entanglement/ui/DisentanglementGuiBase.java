/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DisentanglementGuiBase.java
 *
 * Created on Oct 18, 2010, 11:52:17 PM
 */

package sketch.entanglement.ui;

/**
 *
 * @author shaon
 */
public class DisentanglementGuiBase extends javax.swing.JDialog {

    /** Creates new form DisentanglementGuiBase */
    public DisentanglementGuiBase(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chooseScrollPane = new javax.swing.JScrollPane();
        chooseList = new javax.swing.JList();
        chooseLabel = new javax.swing.JLabel();
        createButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        partitionScrollPane = new javax.swing.JScrollPane();
        partitionList = new javax.swing.JList();
        partitionLabel = new javax.swing.JLabel();
        refineButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        chooseScrollPane.setViewportView(chooseList);

        chooseLabel.setText("Choose statements");

        createButton.setText("Create Partition   >>>");

        removeButton.setText("<<<   Remove Partitions");

        partitionScrollPane.setViewportView(partitionList);

        partitionLabel.setText("Entanglement Partitions");

        refineButton.setText("Disentangle choose statements");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(refineButton, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(chooseScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(createButton, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)))
                            .addComponent(chooseLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(partitionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(partitionLabel))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseLabel)
                    .addComponent(partitionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chooseScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .addComponent(partitionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addComponent(createButton)
                        .addGap(18, 18, 18)
                        .addComponent(removeButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DisentanglementGuiBase dialog = new DisentanglementGuiBase(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel chooseLabel;
    private javax.swing.JList chooseList;
    private javax.swing.JScrollPane chooseScrollPane;
    private javax.swing.JButton createButton;
    private javax.swing.JLabel partitionLabel;
    private javax.swing.JList partitionList;
    private javax.swing.JScrollPane partitionScrollPane;
    private javax.swing.JButton refineButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    protected javax.swing.JList getChooseList() {
        return chooseList;
    }

    protected javax.swing.JList getPartitionList() {
        return partitionList;
    }

    protected javax.swing.JButton getCreateButton() {
        return createButton;
    }

    protected javax.swing.JButton getRemoveButton() {
        return removeButton;
    }

    protected javax.swing.JButton getRefineButton() {
        return refineButton;
    }
}
