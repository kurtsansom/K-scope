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
import java.io.File;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import jp.riken.kscope.Application;
import jp.riken.kscope.Message;
import jp.riken.kscope.common.FRAME_VIEW;
import jp.riken.kscope.data.CodeLine;
import jp.riken.kscope.data.Program;
import jp.riken.kscope.data.SourceFile;
import jp.riken.kscope.properties.ProgramProperties;
import jp.riken.kscope.service.AppController;
import jp.riken.kscope.utils.StringUtils;
import jp.riken.kscope.utils.SwingUtils;

/**
 * ソースファイルを開くアクションクラス
 * @author RIKEN
 *
 */
public class FileOpenSourceFileAction extends ActionBase {

    /** ソースファイル取得先ビュー */
    private FRAME_VIEW view;

    /**
     * コンストラクタ
     * @param controller	アプリケーションコントローラ
     * @param view 			ソースファイル取得先ビュー
     */
    public FileOpenSourceFileAction(AppController controller, FRAME_VIEW view) {
        super(controller);
        this.view = view;
    }

    /**
     * アクションが実行可能であるかチェックする.<br/>
     * アクションの実行前チェック、メニューのイネーブルの切替を行う。<br/>
     * @return		true=アクションが実行可能
     */
    @Override
    public boolean validateAction() {

        CodeLine line = getSelectedCodeLine();
        if (line == null) return false;
        SourceFile source = line.getSourceFile();
        if (source == null) return false;
        ProgramProperties properties = this.controller.getPropertiesExtension();
        Program program = properties.getMatchProgram(source.getPath());

        return program != null;
    }


    /**
     * 現在選択されているコード行を取得する
     * @return    選択コード行
     */
    private CodeLine getSelectedCodeLine() {

        CodeLine line = null;
        if (this.view == FRAME_VIEW.SOURCE_VIEW) {
            line = this.controller.getMainframe().getPanelSourceView().getSelectedCodeLine();
        }
        else if (this.view == FRAME_VIEW.EXPLORE_VIEW) {
            CodeLine[] lines = this.controller.getMainframe().getPanelExplorerView().getSelectedCodeLines();
            if (lines != null && lines.length > 0) {
                line = lines[0];
            }
        }
        return line;
    }


    /**
     * ソースファイルを開くイベント
     * @param event		イベント情報
     */
    @Override
    public void actionPerformed(ActionEvent event) {

        // ステータスメッセージ
        final String message = Message.getString("mainmenu.file.program"); //外部ツールで開く
        Application.status.setMessageMain(message);

        // 実行チェック
        CodeLine line = getSelectedCodeLine();
        if (line == null) {
            this.controller.getErrorInfoModel().addErrorInfo(Message.getString("fileopensourcefileaction.openfile.notarget.errinfo")); //ファイルが選択されていません。
            Application.status.setMessageMain(message +
            		Message.getString("action.common.error.status")); //:エラー
            return;
        }
        SourceFile source = line.getSourceFile();
        int startline = line.getStartLine();
        if (source == null || source.getFile() == null) {
            this.controller.getErrorInfoModel().addErrorInfo(
                    line,
                    Message.getString("fileopensourcefileaction.openfile.filenotget.errinfo")); //ファイルを取得できませんでした。
            Application.status.setMessageMain(message + ":" +
            		Message.getString("action.common.error.status")); //:エラー
            return;
        }
        File file = source.getFile();
        if (!file.isAbsolute()) {
            file = new File(this.controller.getProjectModel().getProjectFolder().getAbsolutePath() + File.separator + file.getPath());
        }
        if (!file.exists()) {
            this.controller.getErrorInfoModel().addErrorInfo(
                    line,
                    Message.getString("fileopensourcefileaction.openfile.notexist.errinfo")); //ファイルが存在しません。
            Application.status.setMessageMain(message + ":" +
            		Message.getString("action.common.error.status")); //:エラー
            return;
        }

        ProgramProperties properties = this.controller.getPropertiesExtension();
        Program program = properties.getMatchProgram(source.getPath());
        if (program == null) {
            this.controller.getErrorInfoModel().addErrorInfo(
                    line,
                    Message.getString("fileopensourcefileaction.openfile.noprogram.errinfo")); //外部ツールが設定されていません。
            Application.status.setMessageMain(message +
            		Message.getString("action.common.error.status")); //:エラー
            return;
        }

        String option = program.getOption();
        String filename = file.getAbsolutePath();
        // 起動引数
        String[] args = getProgramArguments(option, filename, startline);
        if (args != null && args.length > 0) {
            // 起動引数が設定されていれば、ソースファイルは起動引数に含まれる。
            filename = null;
        }
        String programname = program.getExename();
        if (program.isRelation()) {
            programname = null;
        }
        // 外部プログラムの実行
        String errMsg = SwingUtils.processOpenProgram(filename, programname, args);
        if (errMsg != null && !errMsg.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this.controller.getMainframe(),
                    errMsg,
                    Message.getString("dialog.common.error"), //エラー
                    JOptionPane.ERROR_MESSAGE);
            Application.status.setMessageMain(message +
            		Message.getString("action.common.error.status")); //:エラー
            return;
        }
        Application.status.setMessageMain(message +
        		Message.getString("action.common.done.status")); //完了
        return;
    }

    /**
     * 起動引数を取得する
     * @param option		起動引数
     * @param filename		起動ソースファイル名
     * @param startline		行番号
     * @return		起動引数
     */
    private String[] getProgramArguments(String option, String filename, int startline) {
        if (option == null || option.isEmpty()) return null;

        List<String> args = new ArrayList<String>();
        String[] opts = StringUtils.tokenizerDelimit(option, " ");
        if (opts == null) return null;
        boolean isadd = false;
        for (String arg : opts) {
	        int posfile = arg.indexOf("%F");
	        int posline = arg.indexOf("%L");
	        if (posfile >= 0) {
	        	arg = arg.replaceAll("%F", filename);
	        	isadd = true;
	        }
	        if (posline >= 0) {
	        	arg = arg.replaceAll("%L", String.valueOf(startline));
	        }
            args.add(arg);
        }
        if (!isadd) {
            args.add(filename);
        }
        if (args.size() <= 0) return null;

        return args.toArray(new String[0]);
    }

}
