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
package jp.riken.kscope.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import jp.riken.kscope.Message;
import jp.riken.kscope.common.PROFILERINFO_TYPE;
import jp.riken.kscope.data.CodeLine;
import jp.riken.kscope.data.SourceFile;
import jp.riken.kscope.language.Expression;
import jp.riken.kscope.language.Fortran;
import jp.riken.kscope.language.IBlock;
import jp.riken.kscope.language.Procedure;
import jp.riken.kscope.language.ProcedureUsage;
import jp.riken.kscope.model.ProfilerMeasureModel;
import jp.riken.kscope.model.ProfilerTableBaseModel;
import jp.riken.kscope.profiler.IProfilerReader;
import jp.riken.kscope.profiler.ProfilerDprofData;
import jp.riken.kscope.profiler.ProfilerEprofData;
import jp.riken.kscope.profiler.ProfilerInfo;
import jp.riken.kscope.profiler.ProfilerMeasureInfo;
import jp.riken.kscope.profiler.ProfilerMeasureInfo.MeasureData;
import jp.riken.kscope.profiler.dprof.DProfReader;
import jp.riken.kscope.profiler.eprof.EProfReader;
import jp.riken.kscope.profiler.utils.ProfilerReaderUtil;
import jp.riken.kscope.properties.ProfilerProperties;
import jp.riken.kscope.utils.FileUtils;
import jp.riken.kscope.utils.StringUtils;

/**
 * プロファイラサービス
 * @author RIKEN
 *
 */
public class ProfilerService extends BaseService {
    /** ソースファイル一覧 */
    private SourceFile[] sourceFiles;
    /** プロファイラ情報クラス */
    private ProfilerInfo profilerInfo;
    /** プロファイラモデルリスト */
    private ProfilerTableBaseModel[] profilerModels;
    /** ファイルタイプ:DPRF, EPRF */
    private String fileType;
    /** PAイベント指定値:EPRFのみ */
    private String paEventName;
    /** フォートラン構文解析結果格納データベース. */
    private Fortran fortranDb;
    /** 詳細プロファイラ測定区間情報モデル */
    private ProfilerMeasureModel measureModel;
    /** プロジェクトフォルダ */
    private File projectFolder;
    /** プロファイラプロパティ */
    private ProfilerProperties properties;
    /** 測定区間情報 */
    private ProfilerMeasureInfo measureInfo;

    /**
     * プロファイラデータを読み込む。
     * @param file  		プロファイラデータファイル
     */
    public void loadProfilerDataFile(File file) {
        // プロファイラリーダーを生成する
        IProfilerReader reader = factoryProfilerReader(file);
        if (reader == null) {
            this.addErrorInfo(Message.getString("profilerservice.profilerdatafile.invalidfile")); //プロファイラファイルを特定できませんでした。
            return;
        }

        // ファイルから読み込みを行う
        try {
            reader.readFile(file);

            // 読込データをモデルにセットする
            if (reader instanceof DProfReader) {
                setDprofModel(reader);
            }
            else if (reader instanceof EProfReader) {
                setEprofModel(reader);
            }
            // ファイルタイプ
            fileType = reader.getFileType();
            // PAイベント指定値:EPRFのみ
            paEventName = reader.getPaEventName();

        } catch (IOException ex) {
            ex.printStackTrace();
            this.addErrorInfo(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.addErrorInfo(ex);
        }

        return;
    }

    /**
     * Dprofコスト情報をモデルにセットする
     * @param reader		プロファイラリーダ
     */
    private void setDprofModel(IProfilerReader reader) {
        // 読込ファイル名
        File file = reader.getProfFile();
        String key = file.getName();
        // コスト情報:ラインを取得する
        ProfilerDprofData[] costline = reader.getCostInfoLine();
        {
            // コスト情報タイプ、全体に対する割合をセットし、ソートを行う。
            PROFILERINFO_TYPE type = PROFILERINFO_TYPE.COST_LINE;
            setDprofInfo(costline, type);
            ProfilerTableBaseModel model = getProfilerModel(type);
            if (model != null) {
                model.setProfilerData(key, costline);
            }
        }

        // コスト情報:ループを取得する
        ProfilerDprofData[] costloop = reader.getCostInfoLoop();
        {
            // コスト情報タイプ、全体に対する割合をセットし、ソートを行う。
            PROFILERINFO_TYPE type = PROFILERINFO_TYPE.COST_LOOP;
            setDprofInfo(costloop, type);
            ProfilerTableBaseModel model = getProfilerModel(type);
            if (model != null) {
                model.setProfilerData(key, costloop);
            }
        }

        // コスト情報:手続を取得する
        ProfilerDprofData[] costprocedure = reader.getCostInfoProcedure();
        {
            // コスト情報タイプ、全体に対する割合をセットし、ソートを行う。
            PROFILERINFO_TYPE type = PROFILERINFO_TYPE.COST_PROCEDURE;
            setDprofInfo(costprocedure, type);
            ProfilerTableBaseModel model = getProfilerModel(type);
            if (model != null) {
                model.setProfilerData(key, costprocedure);
            }
        }

        // コールグラフ情報を取得する
        ProfilerDprofData[] callgraph = reader.getDprofCallGraphInfo();
        {
            // コスト情報タイプ
            PROFILERINFO_TYPE type = PROFILERINFO_TYPE.CALLGRAPH;
            setDprofInfo(callgraph, type);
            ProfilerTableBaseModel model = getProfilerModel(type);
            if (model != null) {
                model.setProfilerData(key, callgraph);
            }
        }

        // Dprof情報を設定する
        if (this.profilerInfo != null) {
            this.profilerInfo.putCostLine(key, costline);
            this.profilerInfo.putCostLoop(key, costloop);
            this.profilerInfo.putCostProcedure(key, costprocedure);
            this.profilerInfo.putCallgraph(key, callgraph);
        }

        return;
    }


    /**
     * Eprofイベントカウンタ情報をモデルにセットする
     * @param reader		プロファイラリーダ
     */
    private void setEprofModel(IProfilerReader reader) {
        // 読込ファイル名
        File file = reader.getProfFile();
        String key = file.getName();

        // Eprofイベントカウンタ情報を取得する
        ProfilerEprofData[] eventInfo = reader.getEprofEventCounterInfo();
        if (eventInfo == null || eventInfo.length <= 0) return;
        if (eventInfo[0] == null) return;

        // Eprofの測定区間を取得する。
        for (ProfilerEprofData data : eventInfo) {
            String groupname = data.getSymbol();
            // サブルーチン名から、CALL文ブロックを探索し返す。
            List<IBlock[]> areas = searchProcedureUsage(
                                    groupname,
                                    this.properties.getEprofFunctionStart(),
                                    this.properties.getEprofFunctionEnd());
            if (areas != null) {
                data.setAreas(areas);
            }
        }

        PROFILERINFO_TYPE type = eventInfo[0].getInfoType();
        ProfilerTableBaseModel model = getProfilerModel(type);
        if (model != null) {
            model.setProfilerData(key, eventInfo);
        }


        // EProf情報を設定する
        if (this.profilerInfo != null) {
            this.profilerInfo.putEventCounter(key, eventInfo);
        }
        return;
    }


    /**
     * コスト情報リストにコスト情報タイプ、全体に対する割合をセットし、ソートを行う。
     * @param costinfos		コスト情報リストにコスト
     * @param type			コスト情報タイプ
     */
    private void setDprofInfo(ProfilerDprofData[] costinfos, PROFILERINFO_TYPE type) {

        if (costinfos == null) return;

        // コスト情報タイプを設定する
        for (ProfilerDprofData info : costinfos) {
            info.setInfoType(type);
            CodeLine code = info.getCodeLine();
            if (code == null) continue;
            // ソースファイルをセットする
            SourceFile profsrcfile = info.getSourceFile();
            if (profsrcfile == null) continue;
            // プロファイラ読込時点のソースファイルのパスはプロファイラ作成時のパスであるので、
            // ツールのソースファイルをセットする
            SourceFile toolfile = searchSourceFile(profsrcfile);
            if (toolfile == null) continue;
            info.getCodeLine().setSourceFile(toolfile);

            // コード行情報から、それが属するプログラム単位を探索し返す。
            // IBlock block = searchCodeLine(code);
            // if (block != null) {
            //    info.setBlock(block);
            // }
        }

        if (type != PROFILERINFO_TYPE.CALLGRAPH) {
            // 全体に対する割合を算出する. CALLGRAPFはDProfReader::getDprofCallGraphInfoでセット済み
            calculateRatio(costinfos);
            // サンプリング回数でソートする
            sortCostInfo(costinfos);
        }
    }


    /**
     * プロファイラ読込時点のソースファイルのパスはプロファイラ作成時のパスであるので、
     * ツールのソースファイルをセットする
     * @param profsrcfile			プロファイラソースファイル
     * @return						ツールソースファイル
     */
    private SourceFile searchSourceFile(SourceFile profsrcfile) {
        if (this.sourceFiles == null || this.sourceFiles.length <= 0) return null;
        if (profsrcfile == null) return null;
        if (profsrcfile.getFile() == null) return null;

        // ファイル名のみで検索する
        List<SourceFile> matchFiles = new ArrayList<SourceFile>();
        for (SourceFile file : this.sourceFiles) {
            String name = file.getFile().getName();
            String profname = profsrcfile.getFile().getName();
            // ファイル名を大文字・小文字区別なしで比較する
            if (profname.equalsIgnoreCase(name)) {
                matchFiles.add(file);
            }
        }
        if (matchFiles.size() <= 0) return null;
        if (matchFiles.size() == 1) return matchFiles.get(0);

        // 複数存在するので、パスの比較を行う
        // パスを比較して最も一致しているファイルを返す。
        File[] profpathlist = FileUtils.getPathList(profsrcfile.getFile());
        SourceFile findFile = null;
        int maxmatchpath = 0;
        for (SourceFile file : matchFiles) {
            File[] pathlist = FileUtils.getPathList(file.getFile());
            if (pathlist == null || pathlist.length<= 0) continue;
            int matchpath = 0;
            for (int i=pathlist.length-1, profindex = profpathlist.length-1;
                    i>=0 && profindex>=0;
                    i--,profindex--) {
                if (profpathlist[profindex].getName().equalsIgnoreCase(pathlist[i].getName())) {
                    matchpath++;
                }
            }
            if (maxmatchpath < matchpath) {
                maxmatchpath = matchpath;
                findFile = file;
            }
        }

        return findFile;
    }

    /**
     * 全体に対する割合を算出する
     * @param costinfos		コスト情報リスト
     */
    private void calculateRatio(ProfilerDprofData[] costinfos) {
        if (costinfos == null) return;
        float total = 0.0F;
        int count = 0;
        for (ProfilerDprofData cost : costinfos) {
            total += cost.getSampling();
            count++;
        }

        for (ProfilerDprofData cost : costinfos) {
            float value = cost.getSampling();
            value = value/total;
            cost.setRatio(value);
        }

        return;
    }

    /**
     * コスト情報をサンプリング回数でソースする.
     * サンプリング回数を降順にソートする。
     * @param costinfos		コスト情報
     * @return   ソートコスト情報
     */
    private ProfilerDprofData[] sortCostInfo(ProfilerDprofData[] costinfos) {
        if (costinfos == null) return null;

        // ソースファイルリストのソート
        java.util.Arrays.sort(costinfos,
            new java.util.Comparator<ProfilerDprofData>() {
                public int compare(ProfilerDprofData o1, ProfilerDprofData o2) {
                    ProfilerDprofData src1 = (ProfilerDprofData) o1;
                    ProfilerDprofData src2 = (ProfilerDprofData) o2;
                    float diff = src2.getSampling() - src1.getSampling();
                    if (diff < 0.0) return -1;
                    else if (diff > 0.0) return 1;
                    return 0;
                }
        });
        return costinfos;
    }


    /**
     * ファイルの先頭を読み込み、読み込みを行うリーダーを作成する
     * @param file		読み込みファイル
     * @return			プロファイルリーダ
     */
    private IProfilerReader factoryProfilerReader(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[]  magickey = new byte[]{0x00, 0x00, 0x00, 0x00};
            fis.read(magickey, 0, 4);

            char[] data = new char[]{(char) magickey[0], (char)magickey[1],(char)magickey[2], (char)magickey[3]};
            char[] repdata = new char[]{(char) magickey[3], (char)magickey[2],(char)magickey[1], (char)magickey[0]};
            String key = String.valueOf(data);
            String repkey = String.valueOf(repdata);
            int endian = -1;
            // DPRF
            {
                final String MAGIC_ID = "DPRF";
                if (MAGIC_ID.equalsIgnoreCase(key)) {
                    endian = ProfilerReaderUtil.BIG_ENDIAN;
                }
                else if (MAGIC_ID.equalsIgnoreCase(repkey)) {
                    endian = ProfilerReaderUtil.LITTLE_ENDIAN;
                }
                if (endian != -1) {
                    IProfilerReader reader = new DProfReader();
                    reader.setEndian(endian);
                    return reader;
                }
            }
            // EPRF
            {
                final String MAGIC_ID = "EPRF";
                if (MAGIC_ID.equalsIgnoreCase(key)) {
                    endian = ProfilerReaderUtil.BIG_ENDIAN;
                }
                else if (MAGIC_ID.equalsIgnoreCase(repkey)) {
                    endian = ProfilerReaderUtil.LITTLE_ENDIAN;
                }
                if (endian != -1) {
                    IProfilerReader reader = new EProfReader();
                    reader.setEndian(endian);
                    return reader;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {}
            }
        }

        return null;
    }

    /**
     * ソースファイル一覧を設定する
     * @param files		ソースファイル一覧
     */
    public void setSourceFiles(SourceFile[] files) {
        this.sourceFiles = files;
    }

    /**
     * プロファイラ情報クラス
     * @param info プロファイラ情報クラス
     */
    public void setProfilerInfo(ProfilerInfo info) {
        this.profilerInfo = info;
    }

    /**
     * プロファイラモデルを設定する
     * @param models		プロファイラモデルリスト
     */
    public void setProfilerModels(ProfilerTableBaseModel[] models) {
        this.profilerModels = models;
    }

    /**
     * プロファイラモデルを取得する
     * @param type		プロファイラデータタイプ
     * @return			プロファイラモデル
     */
    private ProfilerTableBaseModel getProfilerModel(PROFILERINFO_TYPE type) {
        if (this.profilerModels == null) return null;
        for (ProfilerTableBaseModel model : this.profilerModels) {
            if (model.getEnumInfo() == type) {
                return model;
            }
        }
        return null;
    }

    /**
     * ファイルタイプ:DPRF, EPRF
     * @return fileType		ファイルタイプ:DPRF, EPRF
     */
    public String getFileType() {
        return fileType;
    }


    /**
     * コード行情報から、それが属するプログラム単位を探索し返す。
     *
     * @param line
     *            コード行情報
     * @return プログラム単位。無ければnullを返す。
     */
    @SuppressWarnings("unused")
    private IBlock searchCodeLine(CodeLine line) {
        if (line == null) return null;
        if (this.fortranDb == null) return null;
        SourceFile file = line.getSourceFile();
        if (file == null) return null;
        LanguageSearcher searcher = new LanguageSearcher(this.fortranDb, line);
        IBlock block = searcher.searchCodeLine();

        return block;
    }

    /**
     * サブルーチン名からブロックを探索し返す。
     *
     * @param groupname           グループ名
     * @param callstart            開始サブルーチン名
     * @param callend            終了サブルーチン名
     * @return サブルーチンCALL文{開始サブルーチン,終了サブルーチン} 。無ければnullを返す。
     */
    private List<IBlock[]> searchProcedureUsage(String groupname, String callstart, String callend) {
        if (groupname == null) return null;
        if (callstart == null) return null;
        if (callend == null) return null;
        if (this.fortranDb == null) return null;

        List<IBlock[]> list = new ArrayList<IBlock[]>();
        // allの場合はプログラム全体
        if ("all".equalsIgnoreCase(groupname)) {
            String mainname = fortranDb.getMainName();
            Procedure proc = fortranDb.search_subroutine(mainname);
            if (proc != null) {
                list.add(new IBlock[]{proc, null});
                return list;
            }
        }
        IBlock[] callblocks = new IBlock[2];
        int index = 0;
        for (String callname : new String[]{callstart, callend}) {
            LanguageSearcher searcher = new LanguageSearcher(this.fortranDb, callname);
            IBlock[] blocks = searcher.searchProcedureUsage();
            if (blocks == null) continue;
            for (IBlock block : blocks) {
                if (block == null) continue;
                if (!(block instanceof ProcedureUsage)) continue;
                // グループ名が実引数に存在するかチェックする
                List<Expression> args = ((ProcedureUsage)block).getArguments();
                if (args == null || args.size() <= 0) continue;
                for (Expression arg : args) {
                    String argText = StringUtils.trimQuote(arg.getLine());
                    if (groupname.equalsIgnoreCase(argText)) {
                        callblocks[index] = block;
                        if (index == 0) {
                            list.add(new IBlock[]{block, null});
                        }
                        else {
                            // callendがnullにセットする
                            for (IBlock[] procs : list) {
                                if (procs[1] == null) {
                                    procs[1] = block;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            index++;
        }
        if (list.size() <= 0) {
            return null;
        }
        return list;
    }

    /**
     * フォートラン構文解析結果格納データベースを設定する
     * @param db		フォートラン構文解析結果格納データベース
     */
    public void setFortranLanguage(Fortran db) {
        this.fortranDb = db;
    }

    /**
     * 詳細プロファイラ測定区間情報モデルを設定する
     * @param model		詳細プロファイラ測定区間情報モデル
     */
    public void setMeasureModel(ProfilerMeasureModel model) {
        this.measureModel = model;
    }

    /**
     * 詳細プロファイラ測定区間を追加する
     * @param code		測定区間行
     * @param name		グループ名
     * @param number	詳細番号
     * @param level		プライオリティレベル
     */
    public void addProfilerMeasureInfo(CodeLine code, String name, String number, String level) {
        ProfilerMeasureInfo info = this.profilerInfo.getMeasureInfo();
        if (info == null) {
            info = new ProfilerMeasureInfo();
            this.profilerInfo.setMeasureInfo(info);
        }
        CodeLine addcode = new CodeLine(code);
        if (this.projectFolder != null) {
            SourceFile file = addcode.getSourceFile();
            String path = FileUtils.getRelativePath(file.getFile(), this.projectFolder);
            if (path == null) {
            	JOptionPane.showMessageDialog(null,
            			Message.getString("profilerservice.profilermeasureinfo.notexists", file.getFile()), //は存在しないか、ファイルではありません。
            			Message.getString("profilerservice.error"), //エラー
            			JOptionPane.ERROR_MESSAGE);
            	return;
            }
            file.setFile(new File(path));
        }
        info.addMeasureData(addcode, name, number, level);
        this.measureModel.setMeasureInfo(info);
    }

    /**
     * 詳細プロファイラ測定区間を追加する
     * @param blocks		測定区間{開始ブロック〜終了ブロック}
     * @param name		グループ名
     * @param number	詳細番号
     * @param level		プライオリティレベル
     */
    public void addProfilerMeasureInfo(IBlock[] blocks, String name, String number, String level) {
        ProfilerMeasureInfo info = this.profilerInfo.getMeasureInfo();
        if (info == null) {
            info = new ProfilerMeasureInfo();
            this.profilerInfo.setMeasureInfo(info);
        }
        info.addMeasureData(blocks, name, number, level);
        this.measureModel.setMeasureInfo(info);
    }

    /**
     * プロジェクトフォルダを設定する
     * @param folder 	プロジェクトフォルダ
     */
    public void setProjectFolder(File folder) {
        this.projectFolder = folder;
    }

    /**
     * PAイベント指定値(EPRFのみ)を取得する.
     *     Cache
     *     Instructions
     *     MEM_access
     *     Performance
     *     Statistics
     * @return 		PAイベント指定値(EPRFのみ)
     */
    public String getPaEventName() {
        return this.paEventName;
    }

    /**
     * プロファイラプロパティを設定する
     * @param properties プロファイラプロパティ
     */
    public void setPropertiesProfiler(ProfilerProperties properties) {
        this.properties = properties;
    }

    /**
     * 測定区間情報を設定する
     * @param info 測定区間情報
     */
    public void setMeasureInfo(ProfilerMeasureInfo info) {
        this.measureInfo = info;
    }

    /**
     * 測定区間を保存する
     * @param saveFolder		保存フォルダ
     * @return true=保存成功
     * @throws Exception        ファイル入出力エラー
     */
    public boolean saveMeasureFile(File saveFolder) throws Exception {
        if (this.measureInfo == null) {
            this.addErrorInfo(Message.getString("profilerservice.measurefile.measureline.empty")); //測定区間が設定されていません
            return false;
        }
        List<MeasureData> list = this.measureInfo.getMeasureList();
        if (list == null || list.size() <= 0) {
            this.addErrorInfo(Message.getString("profilerservice.measurefile.measureline.empty")); //測定区間が設定されていません
            return false;
        }

        // 測定区間のコード行を作成し、ソースファイル毎にまとめる。
        Map<SourceFile, List<CodeLine>> mapLines = createMeasureLine(list);
        if (mapLines == null || mapLines.size() <= 0) {
            this.addErrorInfo(Message.getString("profilerservice.measurefile.measureline.null")); //測定区間を取得できませんでした
            return false;
        }
        Set<SourceFile> keySet = mapLines.keySet();
        for (SourceFile file : keySet) {
            List<CodeLine> lines = mapLines.get(file);
            FileService service = new FileService(this.getErrorInfoModel());

            // ソースコード行の読込
            CodeLine[] codes = service.readSourceFile(file, this.projectFolder);
            if (codes == null) {
                this.addErrorInfo(Message.getString("profilerservice.measurefile.sourcecode.invalidread") + //ソースコードの読込に失敗しました。
            "[file=" + file.getPath() + "]");
                return false;
            }
            // 読込コードリストに測定ステートメントの挿入
            List<CodeLine> sources = new ArrayList<CodeLine>();
            sources.addAll(Arrays.asList(codes));
            // 末尾から挿入
            for (int i=lines.size()-1; i>=0; i--) {
                sources.add(lines.get(i).getStartLine(), lines.get(i));
            }

            // ファイル出力
            File outpath = null;
            if (saveFolder == null) {
                outpath = file.getFile();
            }
            else if (file.getFile().isAbsolute()) {
                String path = FileUtils.getRelativePath(file.getFile(), this.projectFolder);
                if (path != null && (new File(path).isAbsolute())) {
                    outpath = new File(saveFolder, file.getFile().getName());
                }
                else {
                    outpath = new File(saveFolder, path);
                }
            }
            else {
                outpath = new File(saveFolder, file.getFile().getPath());
            }
            service.writeFile(outpath, sources.toArray(new CodeLine[0]));
        }

        return true;
    }

    /**
     * 測定区間のコード行を作成し、ソースファイル毎にまとめる。
     * @param list		測定区間リスト
     * @return			ソース毎の測定コード行
     */
    private Map<SourceFile, List<CodeLine>> createMeasureLine(List<MeasureData> list) {
        if (list == null || list.size() <= 0) return null;

        Map<SourceFile, List<CodeLine>> mapLines = new HashMap<SourceFile, List<CodeLine>>();
        for (MeasureData data : list) {
            // 測定区間
            CodeLine measureLine = data.getMeasureArea();

            // 挿入測定ステートメント
            SourceFile file = measureLine.getSourceFile();
            int start = measureLine.getStartLine();
            int end = measureLine.getEndLine();
            String groupname = data.getGroupname();
            String number = data.getNumber();
            String level = data.getLevel();
            // グループ名、詳細番号、レベルから挿入測定構文の作成
            String startCode = this.properties.createEprofStatementStart(groupname, number, level);
            String endCode = this.properties.createEprofStatementEnd(groupname, number, level);
            String fn = null;
            if(file != null) {
            	fn = file.getPath();
            }
            // 開始コード
            CodeLine startline = new CodeLine(file, startCode, start-1, fn);
            // 終了コード
            CodeLine endline = new CodeLine(file, endCode, end, fn);

            List<CodeLine> lines = null;
            if (mapLines.containsKey(file)) {
                lines = mapLines.get(file);
            }
            else {
                lines = new ArrayList<CodeLine>();
                mapLines.put(file, lines);
            }
            lines.add(startline);
            lines.add(endline);
        }

        // コード行を行番号でソートする
        Set<SourceFile> keySet = mapLines.keySet();
        for (SourceFile key : keySet) {
            List<CodeLine> lines = mapLines.get(key);
            // ソート実行
            Collections.sort(lines, new Comparator<CodeLine>(){
                public int compare(CodeLine code1, CodeLine code2) {
                  return code1.getStartLine() - code2.getStartLine();
                }
              });
        }
        if (mapLines.size() <= 0) return null;
        return mapLines;
    }
}


