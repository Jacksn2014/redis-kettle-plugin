package org.pentaho.di.trans.steps.redisfinddel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class RedisFindDelDialog extends BaseStepDialog implements StepDialogInterface {
    private static Class<?> PKG = RedisFindDelMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

    private RedisFindDelMeta input;
    private boolean gotPreviousFields = false;
    private RowMetaInterface previousFields;

    private Label wlKeyField;
    private TextVar wKeyField;
    private FormData fdlKeyField, fdKeyField;

    private Label wlValueField;
    private TextVar wValueField;
    private FormData fdlValueField, fdValueField;

    private Label wlMasterName;
    private TextVar wMasterName;
    private FormData fdlMasterName, fdMasterName;

    private Composite wServersComp;
    private Label wlServers;
    private TableView wServers;
    private FormData fdlServers, fdServers;

    public RedisFindDelDialog(Shell parent, Object in, TransMeta tr, String sname) {
        super(parent, (BaseStepMeta) in, tr, sname);
        input = (RedisFindDelMeta) in;
    }

    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                input.setChanged();
            }
        };
        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "RedisFindDelDialog.Shell.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "RedisFindDelDialog.Stepname.Label"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // Key field
        wlKeyField = new Label(shell, SWT.RIGHT);
        wlKeyField.setText(BaseMessages.getString(PKG, "RedisFindDelDialog.KeyField.Label"));
        props.setLook(wlKeyField);
        fdlKeyField = new FormData();
        fdlKeyField.left = new FormAttachment(0, 0);
        fdlKeyField.right = new FormAttachment(middle, -margin);
        fdlKeyField.top = new FormAttachment(wStepname, margin);
        wlKeyField.setLayoutData(fdlKeyField);
        wKeyField = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wKeyField);
        wKeyField.addModifyListener(lsMod);
        fdKeyField = new FormData();
        fdKeyField.left = new FormAttachment(middle, 0);
        fdKeyField.top = new FormAttachment(wStepname, margin);
        fdKeyField.right = new FormAttachment(100, 0);
        wKeyField.setLayoutData(fdKeyField);
        //key type fiels

        // Value field
        wlValueField = new Label(shell, SWT.RIGHT);
        wlValueField.setText(BaseMessages.getString(PKG, "RedisFindDelDialog.ValueField.Label"));
        props.setLook(wlValueField);
        fdlValueField = new FormData();
        fdlValueField.left = new FormAttachment(0, 0);
        fdlValueField.right = new FormAttachment(middle, -margin);
        fdlValueField.top = new FormAttachment(wKeyField, margin);
        wlValueField.setLayoutData(fdlValueField);
        wValueField = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wValueField);
        wValueField.addModifyListener(lsMod);
        fdValueField = new FormData();
        fdValueField.left = new FormAttachment(middle, 0);
        fdValueField.top = new FormAttachment(wKeyField, margin);
        fdValueField.right = new FormAttachment(100, 0);
        wValueField.setLayoutData(fdValueField);

        
        // Master Name
        wlMasterName = new Label(shell, SWT.RIGHT);
        wlMasterName.setText(BaseMessages.getString(PKG, "RedisFindDelDialog.MasterName.Label"));
        props.setLook(wlMasterName);
        fdlMasterName = new FormData();
        fdlMasterName.left = new FormAttachment(0, 0);
        fdlMasterName.right = new FormAttachment(middle, -margin);
        fdlMasterName.top = new FormAttachment(wValueField, margin);
        wlMasterName.setLayoutData(fdlMasterName);
        wMasterName = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wMasterName);
        wMasterName.addModifyListener(lsMod);
        fdMasterName = new FormData();
        fdMasterName.left = new FormAttachment(middle, 0);
        fdMasterName.top = new FormAttachment(wValueField, margin);
        fdMasterName.right = new FormAttachment(100, 0);
        wMasterName.setLayoutData(fdMasterName);

        ColumnInfo[] colinf =
                new ColumnInfo[]{
                        new ColumnInfo(BaseMessages.getString(PKG, "RedisFindDelDialog.HostName.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "RedisFindDelDialog.Port.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),
                        new ColumnInfo(BaseMessages.getString(PKG, "RedisFindDelDialog.Auth.Column"),
                                ColumnInfo.COLUMN_TYPE_TEXT, false),};

        // Servers
        wServersComp = new Composite(wMasterName, SWT.NONE);
        props.setLook(wServersComp);

        FormLayout fileLayout = new FormLayout();
        fileLayout.marginWidth = 3;
        fileLayout.marginHeight = 3;
        wServersComp.setLayout(fileLayout);

        wlServers = new Label(shell, SWT.RIGHT);
        wlServers.setText(BaseMessages.getString(PKG, "RedisFindDelDialog.Servers.Label"));
        props.setLook(wlServers);
        fdlServers = new FormData();
        fdlServers.left = new FormAttachment(0, 0);
        fdlServers.right = new FormAttachment(middle / 4, -margin);
        fdlServers.top = new FormAttachment(wMasterName, margin);
        wlServers.setLayoutData(fdlServers);

        wServers =
                new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | 3, colinf, 4/* FieldsRows */, lsMod,
                        props);

        fdServers = new FormData();
        fdServers.left = new FormAttachment(middle / 4, 0);
        fdServers.top = new FormAttachment(wMasterName, margin * 2);
        fdServers.right = new FormAttachment(100, 0);
        // fdServers.bottom = new FormAttachment( 100, 0 );
        wServers.setLayoutData(fdServers);

        // Some buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[]{wOK, wCancel}, margin, wServers);

        // Add listeners
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };

        wStepname.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // Set the shell size, based upon previous time...
        setSize();

        getData();
        input.setChanged(changed);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return stepname;
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData() {
        if (!Const.isEmpty(input.getTableName())) {
            wKeyField.setText(input.getTableName());
        }
        if (!Const.isEmpty(input.getValueFieldName())) {
            wValueField.setText(input.getValueFieldName());
        }
        if (!Const.isEmpty(input.getMasterName())) {
        	wMasterName.setText(input.getMasterName());
        }

        int i = 0;
        Set<Map<String,String>> servers = input.getServers();
        if (servers != null) {
        	Iterator<Map<String, String>> iterator = servers.iterator();
            while (iterator.hasNext()) {
            	Map<String,String> addr=iterator.next();
                TableItem item = wServers.table.getItem(i);
                int col = 1;

                item.setText(col++, addr.get("hostname"));
                item.setText(col++, addr.get("port"));
                item.setText(col++, addr.get("auth"));
                i++;
            }
        }

        wServers.setRowNums();
        wServers.optWidth(true);

        wStepname.selectAll();
        wStepname.setFocus();
    }

    private void cancel() {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    private void ok() {
        if (Const.isEmpty(wStepname.getText()))
            return;

        stepname = wStepname.getText(); // return value
        input.setTableName(wKeyField.getText());
        input.setValueFieldName(wValueField.getText());
        input.setMasterName(wMasterName.getText());

        int nrServers = wServers.nrNonEmpty();

        input.allocate(nrServers);

        Set<Map<String,String>> servers = input.getServers();

        for (int i = 0; i < nrServers; i++) {
            TableItem item = wServers.getNonEmpty(i);
            Map<String,String> wServersmap=new HashMap<String, String>();
            wServersmap.put("hostname", item.getText(1));
            wServersmap.put("port", item.getText(2));
            wServersmap.put("auth", item.getText(3));
            servers.add(wServersmap);
        }
        input.setServers(servers);

        dispose();
    }

    private void getFieldsInto(CCombo fieldCombo) {
        try {
            if (!gotPreviousFields) {
                previousFields = transMeta.getPrevStepFields(stepname);
            }

            String field = fieldCombo.getText();

            if (previousFields != null) {
                fieldCombo.setItems(previousFields.getFieldNames());
            }

            if (field != null)
                fieldCombo.setText(field);
            gotPreviousFields = true;

        } catch (KettleException ke) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "RedisFindDelDialog.FailedToGetFields.DialogTitle"),
                    BaseMessages.getString(PKG, "RedisFindDelDialog.FailedToGetFields.DialogMessage"), ke);
        }
    }
}
