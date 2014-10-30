/*
 * K-scope
 * Copyright 2012-2013 RIKEN, Japan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.riken.kscope.action;

import java.awt.event.ActionEvent;

import jp.riken.kscope.common.ANALYSIS_PANEL;
import jp.riken.kscope.model.ProjectModel;
import jp.riken.kscope.model.PropertiesTableModel;
import jp.riken.kscope.service.AppController;
import jp.riken.kscope.service.ProjectService;

/**
 * プロジェクトプロパティアクション
 * @author RIKEN
 */
public class ProjectPropertyAction extends ActionBase {

    /**
     * コンストラクタ
     * @param controller	アプリケーションコントローラ
     */
    public ProjectPropertyAction(AppController controller) {
        super(controller);
    }

    /**
     * アクション発生イベント
     * @param event		イベント情報
     */
    @Override
    public void actionPerformed(ActionEvent event) {

        // プロパティの設定を行う。
        setProperties();

        // プロパティタブをアクティブにする
        this.controller.getMainframe().getPanelAnalysisView().setSelectedPanel(ANALYSIS_PANEL.PROPARTIES);
    }

    /**
     * プロジェクトのプロパティの設定を行う。
     */
    public void setProperties() {

        // プロパティ設定モデルの取得する
        PropertiesTableModel model = this.controller.getPropertiesTableModel();

        // プロジェクトのプロパティの取得を行う
        ProjectModel project = this.controller.getProjectModel();
        ProjectService service = new ProjectService(project);
        service.setProperties(model);

    }
}
