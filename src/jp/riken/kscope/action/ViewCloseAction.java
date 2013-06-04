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

import jp.riken.kscope.Application;
import jp.riken.kscope.Message;
import jp.riken.kscope.service.AppController;

/**
 * 表示ーファイルを閉じるアクション
 * @author riken
 */
public class ViewCloseAction extends ActionBase {

    /** すべてのファイルタブを閉じるフラグ */
    private boolean closeAll = false;

    /**
     * コンストラクタ
     * @param controller	アプリケーションコントローラ
     */
    public ViewCloseAction(AppController controller) {
        super(controller);
    }

    /**
     * コンストラクタ
     * @param controller	アプリケーションコントローラ
     * @param all 			true=すべてのソースファイルタブを閉じる
     */
    public ViewCloseAction(AppController controller, boolean all) {
        super(controller);
        this.closeAll = all;
    }

    /**
     * アクション発生イベント
     * @param event		イベント情報
     */
    @Override
    public void actionPerformed(ActionEvent event) {

    	// ステータスバー表示メッセージ
    	String message = null;
        // ソースファイルタブのクローズ
        if (this.closeAll) {
            // すべてのソースファイルタブを閉じる
            this.controller.getMainframe().getPanelSourceView().closeAllTabs();
            message = Message.getString("mainmenu.view.close-all-file"); //すべて閉じる
        }
        else {
            // アクティブなソースファイルタブを閉じる
            this.controller.getMainframe().getPanelSourceView().closeTabComponent();
            message = Message.getString("mainmenu.view.closefile"); //ファイルを閉じる
        }
        
        Application.status.setMessageMain(message);
    }

}
