package de.jigp.plugin.configuration;

import de.jigp.plugin.GeneratorPluginContext;
import de.jigp.plugin.configuration.TypeToTextMapping.Entry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

public class TypeToTextPanel extends JPanel implements ActionListener, ItemListener {
    private Object[][] initializerRowData;
    private JTable tableVariableInitializers;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton removeButton;
    private JCheckBox isEnabled;
    private boolean isEnabledState = true;
    private GridBagConstraints constraints;
    private JPanel buttonPanel;
    private JScrollPane tablePane;

    public TypeToTextPanel(TypeToTextMapping mapping, String activationText) {
        initLayout();
        createEnableCheckbox(mapping, activationText);
        createTablePanel(mapping);
        createButtonPanel();

        setXY(0, 0);
        this.add(isEnabled, constraints);

        setXY(0, 1);
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.add(tablePane, constraints);

        setXY(0, 2);
        this.add(buttonPanel, constraints);
        this.updateUI();
    }

    private void setXY(int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
    }

    private void createButtonPanel() {
        buttonPanel = new JPanel();
        addButton = new JButton("new");
        addButton.addActionListener(this);
        removeButton = new JButton("remove selected");
        removeButton.addActionListener(this);
        this.buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
    }

    private void initLayout() {
        setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.weightx = 1.0;
    }

    private void createEnableCheckbox(TypeToTextMapping mapping, String activationText) {
        isEnabled = new JCheckBox(activationText);
        isEnabled.setEnabled(mapping != null ? mapping.isMappingActive : isEnabledState);
        isEnabled.addItemListener(this);
    }

    private void createTablePanel(TypeToTextMapping mapping) {
        createInitializerTableRowData(mapping);
        tableModel = createTableModel();
        tableVariableInitializers = new JTable(tableModel);
        setTableColumnWidths();

        tablePane = new JScrollPane(tableVariableInitializers);
    }


    private void createInitializerTableRowData(TypeToTextMapping mapping) {
        if (mapping == null) {
            //TODO wrong initialization
            mapping = GeneratorPluginContext.getConfiguration().variableInitializers;
        }

        initializerRowData = new Object[mapping.size()][3];
        int i = 0;
//         TODO sort it
        for (Entry entry : mapping.entries()) {
            initializerRowData[i++] = new Object[]{entry.type, entry.text, (Boolean) entry.isAddRemoveMethodRequested};
        }
    }

    protected DefaultTableModel createTableModel() {
        String[] columNames = {"type (full qualified)", "initializer expression", "generate add/remove"};
        DefaultTableModel defaultTableModel = new DefaultTableModel(initializerRowData, columNames) {
            public boolean isCellEditable(int row, int col) {
                return true;
            }

            @Override
            public Class<?> getColumnClass(int i) {
                if (i == 2) {
                    return Boolean.class;
                }
                return super.getColumnClass(i);
            }
        };


        return defaultTableModel;
    }

    private void setTableColumnWidths() {
        tableVariableInitializers.getColumnModel().getColumn(0).setPreferredWidth(900);
        tableVariableInitializers.getColumnModel().getColumn(1).setPreferredWidth(900);
        tableVariableInitializers.getColumnModel().getColumn(2).setPreferredWidth(100);
    }

    public TypeToTextMapping getMapping() {
        TypeToTextMapping mapping = new TypeToTextMapping();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            mapping.put(
                    (String) tableModel.getValueAt(i, 0),
                    (String) tableModel.getValueAt(i, 1),
                    (Boolean) tableModel.getValueAt(i, 2));
        }
        mapping.setMappingActive(isEnabledState);
        return mapping;
    }

    private void deleteRows(int[] rows) {
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            tableModel.removeRow(rows[i]);
        }
    }

    private void addNewRow() {
        tableModel.insertRow(0, new Object[]{"new type", "new initializer expression", Boolean.FALSE});
    }


    public void init(TypeToTextMapping typeToTextMapping) {
        createInitializerTableRowData(typeToTextMapping);
        tableModel = createTableModel();
        tableVariableInitializers.setModel(tableModel);
        tableVariableInitializers.updateUI();
        isEnabledState = typeToTextMapping.isMappingActive;
        isEnabled.setSelected(isEnabledState);

    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource().equals(addButton)) {
            addNewRow();
        } else if (actionEvent.getSource().equals(removeButton)) {
            deleteRows(tableVariableInitializers.getSelectedRows());
        }


    }

    public void itemStateChanged(ItemEvent itemEvent) {
        if (itemEvent.getItemSelectable().equals(isEnabled)) {
            isEnabledState = itemEvent.getStateChange() == ItemEvent.SELECTED;
        }
    }
}
