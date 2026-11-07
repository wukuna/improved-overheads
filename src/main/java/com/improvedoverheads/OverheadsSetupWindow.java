package com.improvedoverheads;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

final class OverheadsSetupWindow extends JDialog
{
    private final OverheadsSetups setups;
    private final boolean themes;
    private final OverheadsSetups.Session session;
    private final JTextField name = new JTextField("My setup", 18);
    private final JComboBox<String> saved = new JComboBox<>();
    private final JTextArea json = new JTextArea();
    private final JLabel status = new JLabel(" ");
    private final JButton apply = new JButton("Apply preview");
    private final JButton undo = new JButton("Undo last apply");
    private OverheadsSetups.Preview undoPreview;
    private final DefaultTableModel changes = new DefaultTableModel(new Object[]{"Setting", "Current", "Imported"}, 0)
    {
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
    };
    private Map<String, String> savedValues = new LinkedHashMap<>();
    private OverheadsSetups.Preview preview;

    OverheadsSetupWindow(JComponent owner, OverheadsSetups setups)
    {
        this(owner, setups, false);
    }

    OverheadsSetupWindow(JComponent owner, OverheadsSetups setups, boolean themes)
    {
        super(SwingUtilities.getWindowAncestor(owner), themes ? "Improved Overheads — Hitsplat themes" : "Improved Overheads — Setups",
            Dialog.ModalityType.MODELESS);
        this.setups = setups;
        this.themes = themes;
        name.setText(themes ? "My theme" : "My setup");
        session = setups.session();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel controls = new JPanel();
        controls.setLayout(new javax.swing.BoxLayout(controls, javax.swing.BoxLayout.Y_AXIS));
        JCheckBox replace = new JCheckBox("Replace saved");
        controls.add(row(new JLabel("Name"), name, button("Save current", () ->
        {
            setups.saveCurrent(name.getText(), replace.isSelected(), session, themes);
            refreshSaved(name.getText().trim());
            status.setText("Saved " + name.getText().trim() + ".");
        }), replace));
        saved.setPreferredSize(new Dimension(200, 26));
        javax.swing.DefaultListCellRenderer savedRenderer = new javax.swing.DefaultListCellRenderer();
        savedRenderer.putClientProperty("html.disable", Boolean.TRUE);
        saved.setRenderer(savedRenderer);
        controls.add(row(new JLabel("Saved"), saved, button("Preview saved", () ->
        {
            String selected = (String) saved.getSelectedItem();
            if (selected == null)
            {
                throw new IllegalStateException(themes ? "Choose a saved theme." : "Choose a saved setup.");
            }
            name.setText(selected);
            json.setText(savedValues.get(selected));
            preview();
        }), button("Delete saved", () ->
        {
            String selected = (String) saved.getSelectedItem();
            if (selected != null)
            {
                setups.delete(selected, session, themes);
                refreshSaved(null);
                status.setText("Deleted " + selected + ".");
            }
        })));
        if (themes)
        {
            JComboBox<String> preset = new JComboBox<>(new String[]{"OSRS", "Modern", "Numbers only", "Scrolling"});
            controls.add(row(new JLabel("Start from"), preset, button("Preview preset", () ->
            {
                int selected = preset.getSelectedIndex();
                OverheadsHitsplatStyle style = selected == 0 ? OverheadsHitsplatStyle.OSRS
                    : selected == 2 ? OverheadsHitsplatStyle.NUMBERS : OverheadsHitsplatStyle.MODERN;
                json.setText(setups.themePreset(style, selected == 3 ? OverheadsHitsplatMotion.SCROLL_UP
                    : OverheadsHitsplatMotion.FIXED, session));
                preview();
            })));
        }
        else
        {
            String[] groups = {"My player", "Other players", "NPCs"};
            JComboBox<String> from = new JComboBox<>(groups);
            JComboBox<String> to = new JComboBox<>(groups);
            to.setSelectedIndex(1);
            controls.add(row(new JLabel("Copy visibility"), from, new JLabel("to"), to, button("Preview copy", () ->
            {
                json.setText(setups.copyGroup(from.getSelectedIndex(), to.getSelectedIndex(), session));
                preview();
            })));
        }
        controls.add(row(button("Export current JSON", () ->
        {
            json.setText(setups.exportCurrent(name.getText(), session, themes));
            json.setCaretPosition(0);
            status.setText(themes ? "Current theme exported below." : "Current setup exported below.");
        }), button("Copy JSON", () ->
        {
            if (json.getText().isEmpty())
            {
                json.setText(setups.exportCurrent(name.getText(), session, themes));
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(json.getText()), null);
            status.setText("JSON copied.");
        }), button("Preview import", this::preview)));
        json.setLineWrap(true);
        json.setWrapStyleWord(false);
        ((AbstractDocument) json.getDocument()).setDocumentFilter(new DocumentFilter()
        {
            @Override
            public void insertString(FilterBypass bypass, int offset, String text, AttributeSet attributes) throws BadLocationException
            {
                replace(bypass, offset, 0, text, attributes);
            }

            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes) throws BadLocationException
            {
                if (bypass.getDocument().getLength() - length + (text == null ? 0 : text.length()) > OverheadsSetups.MAX_JSON)
                {
                    invalidatePreview();
                    status.setText("JSON exceeds the import size limit.");
                    return;
                }
                super.replace(bypass, offset, length, text, attributes);
            }
        });
        json.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override public void insertUpdate(DocumentEvent event) { invalidatePreview(); }
            @Override public void removeUpdate(DocumentEvent event) { invalidatePreview(); }
            @Override public void changedUpdate(DocumentEvent event) { invalidatePreview(); }
        });
        JTable table = new JTable(changes);
        javax.swing.table.DefaultTableCellRenderer textRenderer = new javax.swing.table.DefaultTableCellRenderer();
        textRenderer.putClientProperty("html.disable", Boolean.TRUE);
        table.setDefaultRenderer(Object.class, textRenderer);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        JScrollPane input = new JScrollPane(json);
        input.setBorder(BorderFactory.createTitledBorder(themes ? "Hitsplat theme JSON" : "Setup JSON"));
        JScrollPane result = new JScrollPane(table);
        result.setBorder(BorderFactory.createTitledBorder("Changes to apply"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, input, result);
        split.setResizeWeight(0.45);
        split.setDividerLocation(190);
        apply.setEnabled(false);
        apply.addActionListener(event -> act(() ->
        {
            int count = setups.apply(preview);
            undoPreview = setups.reverse(preview);
            undo.setEnabled(count > 0);
            invalidatePreview();
            status.setText("Applied " + count + " changes.");
        }));
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.add(status, BorderLayout.CENTER);
        undo.setEnabled(false);
        undo.addActionListener(event -> act(() ->
        {
            int count = setups.apply(undoPreview);
            undoPreview = null;
            undo.setEnabled(false);
            invalidatePreview();
            status.setText("Restored " + count + " settings.");
        }));
        footer.add(row(undo, apply), BorderLayout.EAST);
        content.add(controls, BorderLayout.NORTH);
        content.add(split, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
        setSize(780, 680);
        setMinimumSize(new Dimension(680, 500));
        setLocationRelativeTo(owner);
        try
        {
            refreshSaved(null);
        }
        catch (RuntimeException ex)
        {
            status.setText(ex.getMessage());
        }
    }

    private void act(Runnable action)
    {
        if (!isDisplayable())
        {
            return;
        }
        try
        {
            setups.check(session);
            action.run();
        }
        catch (RuntimeException ex)
        {
            invalidatePreview();
            status.setText(ex.getMessage() == null ? "Unable to complete the action." : ex.getMessage());
        }
    }

    private JButton button(String label, Runnable action)
    {
        JButton button = new JButton(label);
        button.addActionListener(event -> act(action));
        return button;
    }

    private void preview()
    {
        invalidatePreview();
        preview = setups.preview(json.getText(), session, themes);
        for (OverheadsSetups.Change change : preview.changes)
        {
            changes.addRow(new Object[]{change.label, OverheadsSetups.display(change.before), OverheadsSetups.display(change.after)});
        }
        apply.setEnabled(!preview.changes.isEmpty());
        status.setText(preview.changes.size() + " changes" + (preview.ignored == 0 ? "." : "; " + preview.ignored + " settings skipped."));
    }

    private void invalidatePreview()
    {
        preview = null;
        changes.setRowCount(0);
        apply.setEnabled(false);
    }

    private void refreshSaved(String selection)
    {
        savedValues = setups.saved(session, themes);
        saved.removeAllItems();
        savedValues.keySet().forEach(saved::addItem);
        if (selection != null)
        {
            saved.setSelectedItem(selection);
        }
    }

    private static JPanel row(JComponent... components)
    {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        for (JComponent component : components)
        {
            row.add(component);
        }
        return row;
    }
}
