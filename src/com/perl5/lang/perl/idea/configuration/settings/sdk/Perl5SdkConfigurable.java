/*
 * Copyright 2015-2017 Alexandr Evstigneev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perl5.lang.perl.idea.configuration.settings.sdk;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.PerlSdkTable;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.messages.MessageBusConnection;
import com.perl5.PerlBundle;
import com.perl5.PerlIcons;
import com.perl5.lang.perl.idea.configuration.settings.sdk.wrappers.Perl5RealSdkWrapper;
import com.perl5.lang.perl.idea.configuration.settings.sdk.wrappers.Perl5SdkWrapper;
import com.perl5.lang.perl.idea.configuration.settings.sdk.wrappers.Perl5TextSdkWrapper;
import com.perl5.lang.perl.idea.sdk.PerlSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class Perl5SdkConfigurable implements UnnamedConfigurable, ProjectJdkTable.Listener {
  public static final Perl5SdkWrapper DISABLE_PERL_ITEM = new Perl5TextSdkWrapper(PerlBundle.message("perl.settings.disable.perl.support"));
  public static final Perl5SdkWrapper NOT_SELECTED_ITEM = new Perl5TextSdkWrapper(PerlBundle.message("perl.settings.sdk.not.selected"));

  private Perl5SdkPanel myPanel;
  @NotNull
  private MessageBusConnection myConnection;
  @NotNull
  private Perl5SdkManipulator mySdkManipulator;
  private boolean myChange = false;

  public Perl5SdkConfigurable(@NotNull Perl5SdkManipulator sdkManipulator) {
    myConnection = ApplicationManager.getApplication().getMessageBus().connect();
    mySdkManipulator = sdkManipulator;
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    if (myPanel == null) {
      initPanel();
    }
    return myPanel.getMainPanel();
  }

  private void initPanel() {
    myPanel = new Perl5SdkPanel();

    // combo box
    JComboBox<Perl5SdkWrapper> sdkComboBox = myPanel.getSdkComboBox();
    //noinspection unchecked
    sdkComboBox.setModel(new CollectionComboBoxModel<>(mySdkManipulator.getAllSdkWrappers()));
    sdkComboBox.setRenderer(new ColoredListCellRenderer<Perl5SdkWrapper>() {
      @Override
      protected void customizeCellRenderer(@NotNull JList<? extends Perl5SdkWrapper> list,
                                           Perl5SdkWrapper wrapper,
                                           int index,
                                           boolean selected,
                                           boolean hasFocus) {
        if (wrapper != null) {
          wrapper.customizeRenderer(this);
        }
      }
    });

    myConnection.subscribe(PerlSdkTable.PERL_TABLE_TOPIC, this);

    // add sdk button
    DefaultActionGroup panelActionGroup = myPanel.getActionGroup();
    panelActionGroup.add(new DumbAwareAction(PerlBundle.message("perl.interpreter.new")) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        SdkConfigurationUtil.selectSdkHome(PerlSdkType.INSTANCE, Perl5SdkConfigurable.this::addSdk);
      }

      @Override
      public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
      }
    });
    panelActionGroup.add(new DumbAwareAction(PerlBundle.message("perl.interpreter.edit")) {
      @Override
      public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(getSelectedSdk() != null);
      }

      @Override
      public void actionPerformed(AnActionEvent e) {
        renameSdk(e);
      }
    });
    panelActionGroup.add(new DumbAwareAction(PerlBundle.message("perl.interpreter.delete")) {
      @Override
      public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(getSelectedSdk() != null);
      }

      @Override
      public void actionPerformed(AnActionEvent e) {
        removeSdk(e);
      }
    });
  }

  private void updateSdkModel(@Nullable Perl5SdkWrapper itemToSelect) {
    JComboBox<Perl5SdkWrapper> sdkComboBox = myPanel.getSdkComboBox();
    List<Perl5SdkWrapper> allItems = mySdkManipulator.getAllSdkWrappers();
    if (itemToSelect == null || !allItems.contains(itemToSelect)) {
      itemToSelect = allItems.isEmpty() ? null : allItems.get(0);
    }
    sdkComboBox.setModel(new CollectionComboBoxModel<>(allItems, itemToSelect));
  }

  @Nullable
  public Sdk getSelectedSdk() {
    Perl5SdkWrapper selectedWrapper = getSelectedSdkWrapper();
    return selectedWrapper instanceof Perl5RealSdkWrapper ? ((Perl5RealSdkWrapper)selectedWrapper).getSdk() : null;
  }

  @Nullable
  public Perl5SdkWrapper getSelectedSdkWrapper() {
    ComboBox<Perl5SdkWrapper> sdkComboBox = myPanel.getSdkComboBox();
    return sdkComboBox == null ? null : (Perl5SdkWrapper)sdkComboBox.getSelectedItem();
  }

  private void addSdk(@NotNull String home) {
    PerlSdkType sdkType = PerlSdkType.INSTANCE;
    String newSdkName = SdkConfigurationUtil.createUniqueSdkName(sdkType,
                                                                 home,
                                                                 Arrays.asList(PerlSdkTable.getInstance().getAllJdks()));
    final ProjectJdkImpl newSdk = new ProjectJdkImpl(newSdkName, sdkType);
    newSdk.setHomePath(home);
    sdkType.setupSdkPaths(newSdk);
    // should we check for version string?
    myChange = true;
    PerlSdkTable.getInstance().addJdk(newSdk);
  }

  private void removeSdk(AnActionEvent e) {
    Sdk selectedSdk = getSelectedSdk();
    if (selectedSdk != null) {
      PerlSdkTable.getInstance().removeJdk(selectedSdk);
    }
  }

  private void renameSdk(AnActionEvent e) {
    Sdk selectedSdk = getSelectedSdk();
    if (selectedSdk == null) {
      return;
    }
    PerlSdkTable perlSdkTable = PerlSdkTable.getInstance();
    Messages.showInputDialog(
      myPanel.getSdkComboBox(),
      PerlBundle.message("perl.rename.sdk.text"),
      PerlBundle.message("perl.rename.sdk.title"),
      PerlIcons.PERL_LANGUAGE_ICON,
      selectedSdk.getName(),
      new InputValidator() {
        @Override
        public boolean checkInput(String inputString) {
          if (StringUtil.isEmpty(inputString)) {
            return false;
          }
          Sdk perlSdk = perlSdkTable.findJdk(inputString);
          return perlSdk == null || perlSdk.equals(selectedSdk);
        }

        @Override
        public boolean canClose(String inputString) {
          if (StringUtil.equals(selectedSdk.getName(), inputString)) {
            return true;
          }
          SdkModificator modificator = selectedSdk.getSdkModificator();
          modificator.setName(inputString);
          assert modificator instanceof Sdk;
          perlSdkTable.updateJdk(selectedSdk, (Sdk)modificator);
          myPanel.getSdkComboBox().repaint();
          return true;
        }
      }
    );
  }

  private boolean isSdkModified() {
    return !mySdkManipulator.getCurrentSdkWrapper().equals(getSelectedSdkWrapper());
  }


  @Override
  public boolean isModified() {
    return isSdkModified();
  }

  @Override
  public void apply() {
    if (isSdkModified()) {
      mySdkManipulator.setSdk(getSelectedSdk());
    }
  }

  @Override
  public void reset() {
    myPanel.getSdkComboBox().setSelectedItem(mySdkManipulator.getCurrentSdkWrapper());
  }

  @Override
  public void disposeUIResources() {
    myConnection.deliverImmediately();
    myConnection.disconnect();
    myPanel = null;
  }

  @Override
  public void jdkAdded(@NotNull Sdk sdk) {
    Perl5SdkWrapper itemToSelect = getSelectedSdkWrapper();
    if (myChange) {
      myChange = false;
      itemToSelect = new Perl5RealSdkWrapper(sdk);
    }
    updateSdkModel(itemToSelect);
  }

  @Override
  public void jdkRemoved(@NotNull Sdk sdk) {
    updateSdkModel(getSelectedSdkWrapper());
  }

  @Override
  public void jdkNameChanged(@NotNull Sdk jdk, @NotNull String previousName) {
  }
}
