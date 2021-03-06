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
package jp.riken.kscope.profiler.dprof;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import javax.swing.JOptionPane;

import jp.riken.kscope.Message;
import jp.riken.kscope.data.CodeLine;
import jp.riken.kscope.data.SourceFile;
import jp.riken.kscope.profiler.IProfilerReader;
import jp.riken.kscope.profiler.ProfilerDprofData;
import jp.riken.kscope.profiler.ProfilerEprofData;
import jp.riken.kscope.profiler.common.BaseReader;
import jp.riken.kscope.profiler.common.MagicKey;
import jp.riken.kscope.profiler.common.PaDiscrimInfo;
import jp.riken.kscope.profiler.utils.ProfilerReaderUtil;

/**
 * DProfileファイルを読み込み、情報を保持する
 *
 * @author RIKEN
 *
 */
public class DProfReader extends BaseReader implements IProfilerReader {

    private final String FILE_ID_DPROF = "DPRF"; // DProfファイルを表すファイル識別文字
    private final int FILE_ID_LENGTH = 4; // ファイル識別文字の長さ
    private final int MEASURE_TIME_LENGTH = 32; // 測定時間情報文字列の長さ
    private final short PROFILER_VERSION = 0x412;

    /* PAイベント指定値ごとのPA情報テーブルの大きさ */
    private final Map<String, Integer> MAP_PA_INFO_LENGTH = new HashMap<String, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            put("Cache", 10);
            put("Instructions", 9);
            put("MEM_access", 10);
            put("Performance", 10);
            put("Statistics", 10);
        }
    };

    private MagicKey magicKey = null;
    private CommonInfo commonInfo = null;
    private ArrayList<ThreadInfo> threadInfoList = null;
    private OffSetInfo offSetInfo = null;
    private FileRecord fileInfoList = null;
    private SymbolRecord symbolInfoList = null;
    private ArrayList<ArrayList<LineInfo>> lineInfoList=null;
    private ArrayList<ArrayList<LoopInfo>> loopInfoList=null;
    private ArrayList<CallGraphInfo> callGraphInfoList = null;

    /** 読み込み時のエンディアン設定 */
    private int endian;
    /** 読込プロファイラファイル */
    private File profFile;


    /**
     * 指定されたプロファイラファイルの情報を読み込む
     *
     * @param fDProf
     *            読み込むプロファイラファイル
     * @param endian
     *            エンディアン設定　LITTLE_ENDIAN:0x00 BIG_ENDIAN:0x01;
     * @throws Exception   読込例外
     */
    @Override
    public void readFile(File fDProf, int endian) throws Exception {
        // エンディアンを設定
        this.endian = endian;
        this.profFile = fDProf;

        long fileSize = fDProf.length();
        ByteBuffer byteBuf = ByteBuffer.allocate((int) fileSize);
        FileInputStream fis = new FileInputStream(fDProf);

        while (fis.available() > 0) {
            byteBuf.put((byte) fis.read());
        }
        byteBuf.flip();

        magicKey = readMagicKey(byteBuf);
        commonInfo = readCommonInfo(byteBuf);
        threadInfoList = readThreadInfo(byteBuf);
        offSetInfo = readOffSetInfo(byteBuf);
        fileInfoList = readFileInfo(byteBuf);
        symbolInfoList = readSymbolInfo(byteBuf);
        lineInfoList = readLineInfo(byteBuf);
        loopInfoList = readLoopInfo(byteBuf);
        callGraphInfoList = readCallGraphInfo(byteBuf);

        fis.close();
    }

    /**
     * プロファイラファイルから読み込まれたマジックキー情報のインスタンスを返す。readProfile(File)が実行されていない場合、nullを返す
     *
     * @return
     *         マジックキー情報を格納したMagicKeyクラスのインスタンス。ただし、readProfile(File)が実行されていない場合はnull
     */
    public MagicKey getMagicKey() {
        return magicKey;
    }

    /**
     * プロファイラファイルから読み込まれた共通情報のインスタンスを返す。readProfile(File)が実行されていない場合、nullを返す
     *
     * @return
     *         共通情報を格納したCommonInfoクラスのインスタンス。ただし、readProfile(File)が実行されていない場合はnull
     */
    public CommonInfo getCommonInfo() {
        return commonInfo;
    }

    /**
     * プロファイラファイルから読み込まれたスレッド情報のインスタンスのリストを返す。readProfile(File)が実行されていない場合、
     * nullを返す
     *
     * @return スレッド情報を格納したThreadInfoクラスのインスタンスのリスト。ただし、readProfile(File)
     *         が実行されていない場合はnull
     */
    public List<ThreadInfo> getThreadInfoList() {
        return threadInfoList;
    }

    /**
     * プロファイラファイルから読み込まれたオフセット情報のインスタンスのリストを返す。readProfile(File)が実行されていない場合、
     * nullを返す
     *
     * @return
     *         オフセット情報を格納したOffSetInfoクラスのインスタンス。ただし、readProfile(File)が実行されていない場合はnull
     */
    public OffSetInfo getOffSetInfo() {
        return offSetInfo;
    }

    /**
     * プロファイラファイルから読み込まれたシンボル情報のインスタンスのリストを返す。readProfile(File)が実行されていない場合、
     * nullを返す
     *
     * @return シンボル情報を格納したSymbolInfoクラスのインスタンスのリスト。ただし、readProfile(File)
     *         が実行されていない場合はnull
     */
    public SymbolRecord getSymbolInfoList() {
        return symbolInfoList;
    }

    /**
     * プロファイラファイルから読み込まれたライン情報のインスタンスのリストを返す。readProfile(File)が実行されていない場合、
     * nullを返す
     *
     * @return シンボル情報を格納したLineInfoクラスのインスタンスのリスト。ただし、readProfile(File)
     *         が実行されていない場合はnull
     */
    public ArrayList<ArrayList<LineInfo>> getLineInfoList() {
        return lineInfoList;
    }

    /**
     * プロファイラファイルから読み込まれたループ情報のインスタンスのリストを返す。readProfile(File)が実行されていない場合、
     * nullを返す
     *
     * @return シンボル情報を格納したLoopInfoクラスのインスタンスのリスト。ただし、readProfile(File)
     *         が実行されていない場合はnull
     */
    public ArrayList<ArrayList<LoopInfo>> getLoopInfoList() {
        return loopInfoList;
    }

    /**
     * プロファイラファイルから読み込まれたファイル情報のインスタンスのリストを返す。readProfile(File)が実行されていない場合、
     * nullを返す
     *
     * @return
     *         ファイル情報を格納したFilelInfoクラスのインスタンスのリスト。ただし、readProfile(File)が実行されていない場合はnull
     */
    public FileRecord getFileInfoList() {
        return fileInfoList;
    }

    /**
     * プロファイラファイルから読み込まれたコールグラフ情報のインスタンスのリストを返す。readProfile(File)が実行されていない場合、
     * nullを返す
     *
     * @return コールグラフ情報を格納したCallGraphlInfoクラスのインスタンスのリスト。ただし、readProfile(File)
     *         が実行されていない場合はnull
     */
    public List<CallGraphInfo> getCallGraphInfo() {
        return callGraphInfoList;
    }

    /*マジックキー情報の読み込み*/
    private MagicKey readMagicKey(ByteBuffer byteBuf) throws Exception {
        MagicKey newMagicKey = new MagicKey();
        String fileID = getString(byteBuf, FILE_ID_LENGTH);

        if (!FILE_ID_DPROF.equals(fileID)) {
        	throw new Exception(Message.getString("dialog.common.error") + //エラー
        			": " +
        			Message.getString("dprofreader.exception.notvalid"));//有効なDProfファイルではありません。
        }
        newMagicKey.setId(fileID);
        newMagicKey.setAdd_mode(getShort(byteBuf));
        short version = getShort(byteBuf);
        if (version != PROFILER_VERSION) {
        	throw new Exception(Message.getString("dialog.common.error") + //エラー
        			": " + Message.getString("dprofreader.exception.outside", version, PROFILER_VERSION)); //サポート対象外のDProfバージョンです。 読込=%#04X サポート=%#04X
        }
        newMagicKey.setVer(version);
        return newMagicKey;
    }

    /*共通情報の読み込み*/
    private CommonInfo readCommonInfo(ByteBuffer byteBuf) {
        CommonInfo newCommonInfo = new CommonInfo();
        newCommonInfo.setProcessNum(getInt(byteBuf));
        newCommonInfo.setMeasureOption(getInt(byteBuf));
        newCommonInfo.setRunStyle(getShort(byteBuf));
        newCommonInfo.setThreadNum(getShort(byteBuf));
        newCommonInfo.setCpuClock(getInt(byteBuf));
        newCommonInfo.setMeasureTimeInfo(getString(byteBuf, MEASURE_TIME_LENGTH));
        newCommonInfo.setRecomMemory(getInt(byteBuf));
        newCommonInfo.setSampInterval(getFloat(byteBuf));
        newCommonInfo.setLogicDimention(getInt(byteBuf));
        newCommonInfo.setLogicShapeX(getInt(byteBuf));
        newCommonInfo.setLogicShapeY(getInt(byteBuf));
        newCommonInfo.setLogicShapeZ(getInt(byteBuf));
        newCommonInfo.setLogicCordinateX(getInt(byteBuf));
        newCommonInfo.setLogicCordinateY(getInt(byteBuf));
        newCommonInfo.setLogicCordinateZ(getInt(byteBuf));
        newCommonInfo.setPhisShapeX(getInt(byteBuf));
        newCommonInfo.setPhisShapeY(getInt(byteBuf));
        newCommonInfo.setPhisShapeZ(getInt(byteBuf));
        newCommonInfo.setPhisShapeA(getInt(byteBuf));
        newCommonInfo.setPhisShapeB(getInt(byteBuf));
        newCommonInfo.setPhisShapeC(getInt(byteBuf));
        newCommonInfo.setPhisCordinateX(getInt(byteBuf));
        newCommonInfo.setPhisCordinateY(getInt(byteBuf));
        newCommonInfo.setPhisCordinateZ(getInt(byteBuf));
        newCommonInfo.setPhisCordinateA(getInt(byteBuf));
        newCommonInfo.setPhisCordinateB(getInt(byteBuf));
        newCommonInfo.setPhisCordinateC(getInt(byteBuf));

        if (newCommonInfo.isOptPa()) {
            PaDiscrimInfo paInfo = new PaDiscrimInfo();
            paInfo.setCpu(getShort(byteBuf));
            paInfo.setEvent_nbr(getShort(byteBuf));
            paInfo.setPa_ver(getShort(byteBuf));
            paInfo.setReserve(getShort(byteBuf));
            newCommonInfo.setPaDiscrimInfo(paInfo);

            int paEventLength = getInt(byteBuf);
            newCommonInfo.setPaEventVal(getString(byteBuf, paEventLength));
        }
        return newCommonInfo;
    }

    /*スレッド情報の読み込み*/
    private ArrayList<ThreadInfo> readThreadInfo(ByteBuffer byteBuf) {
        ArrayList<ThreadInfo> newThreadInfoList = new ArrayList<ThreadInfo>();
        int threadNum = this.commonInfo.getThreadNum();

        for (int i = 0; i < threadNum; i++) {
            ThreadInfo newThrInfo = new ThreadInfo();
            newThrInfo.setThreadNo(getInt(byteBuf));
            newThrInfo.setElapsTime(getFloat(byteBuf));
            newThrInfo.setUserTime(getFloat(byteBuf));
            newThrInfo.setSystemTime(getFloat(byteBuf));
            newThrInfo.setTotalSampNum(getFloat(byteBuf));
            newThrInfo.setBarrierWaitSyncNum(getFloat(byteBuf));
            newThrInfo.setMpiLibCostNum(getFloat(byteBuf));
            newThrInfo.setMpiFuncElapsTime(getFloat(byteBuf));

            if (this.commonInfo.isOptPa()) {
                int paEventLength = MAP_PA_INFO_LENGTH.get(this.commonInfo.getPaEventVal());
                double[] paInfo = new double[paEventLength];

                for (int j = 0; j < paEventLength; j++) {
                    paInfo[j] = getDouble(byteBuf);
                }
                newThrInfo.setPaInfo(paInfo);
            }
            newThreadInfoList.add(newThrInfo);
        }

        return newThreadInfoList;
    }

    /*オフセット情報の読み込み*/
    private OffSetInfo readOffSetInfo(ByteBuffer byteBuf) {
        OffSetInfo newOffsetInfo = new OffSetInfo();
        newOffsetInfo.setLineInfo(getInt(byteBuf));
        newOffsetInfo.setLoopInfo(getInt(byteBuf));
        newOffsetInfo.setCallGraphInfo(getInt(byteBuf));
        newOffsetInfo.setMpiFuncElapsTimeInfo(getInt(byteBuf));
        newOffsetInfo.setComInfo(getInt(byteBuf));
        newOffsetInfo.setSymbolInfo(getInt(byteBuf));
        return newOffsetInfo;
    }

    /*ファイル情報の読み込み*/
    private FileRecord readFileInfo(ByteBuffer byteBuf) {
        FileRecord newFileInfoList = new FileRecord();
        int fileNameNum = getInt(byteBuf);

        for (int i = 0; i < fileNameNum; i++) {
            FileInfo fileInfo = new FileInfo();
            int fileNameLength = getInt(byteBuf);
            fileInfo.setFileName(getString(byteBuf, fileNameLength));

            newFileInfoList.addFileInfo(fileInfo);
        }
        return newFileInfoList;
    }

    /*シンボル情報の読み込み*/
    private SymbolRecord readSymbolInfo(ByteBuffer byteBuf) {
        SymbolRecord newSymbolInfoList = new SymbolRecord();
        int threadNum = this.commonInfo.getThreadNum();

        for (int i = 0; i < threadNum; i++) {

            if (byteBuf.position() >= this.offSetInfo.getLineInfo()) {
                break;
            }
            SymbolList threadSymbList = new SymbolList();
            int symbolNum = getInt(byteBuf);

            for (int j = 0; j < symbolNum; j++) {
                SymbolInfo newSymbInfo = new SymbolInfo();
                newSymbInfo.setSampNum(getFloat(byteBuf));
                newSymbInfo.setBarrierSyncWaitNum(getFloat(byteBuf));
                newSymbInfo.setMpiLibCostNum(getFloat(byteBuf));
                newSymbInfo.setLineSymbolStart(getInt(byteBuf));
                newSymbInfo.setLineSymbolEnd(getInt(byteBuf));
                newSymbInfo.setFileIndex(getInt(byteBuf));
                int symbNameLength = getInt(byteBuf);
                newSymbInfo.setSymbolName(getString(byteBuf, symbNameLength));

                threadSymbList.addSymbolInfo(newSymbInfo);
            }
            newSymbolInfoList.addSymbolList(threadSymbList);
        }
        return newSymbolInfoList;
    }

    /*ライン情報の読み込み*/
    private ArrayList<ArrayList<LineInfo>> readLineInfo(ByteBuffer byteBuf) {
        ArrayList<ArrayList<LineInfo>> newLineInfoList = new ArrayList<ArrayList<LineInfo>>();

        if(this.offSetInfo.getLineInfo()>0){
            int threadNum = this.commonInfo.getThreadNum();
            int offset = this.offSetInfo.getLineInfo();
            byteBuf.position(offset);

            for (int i = 0; i < threadNum; i++) {

                if (byteBuf.position() >= this.offSetInfo.getLoopInfo()) {
                    break;
                }
                ArrayList<LineInfo> threadLineList = new ArrayList<LineInfo>();
                int symbolNum = getInt(byteBuf);

                for (int j = 0; j < symbolNum; j++) {
                    LineInfo newLineInfo = new LineInfo();
                    newLineInfo.setSampNum(getFloat(byteBuf));
                    newLineInfo.setLineNo(getInt(byteBuf));
                    newLineInfo.setSymbolIndex(getInt(byteBuf));
                    newLineInfo.setFileIndex(getInt(byteBuf));

                    threadLineList.add(newLineInfo);
                }
                newLineInfoList.add(threadLineList);
            }
        }
        return newLineInfoList;
    }

    /*ループ情報の読み込み*/
    private ArrayList<ArrayList<LoopInfo>> readLoopInfo(ByteBuffer byteBuf) {
        ArrayList<ArrayList<LoopInfo>> newLoopInfoList = new ArrayList<ArrayList<LoopInfo>>();

        if(this.offSetInfo.getLoopInfo()>0){
            int threadNum = this.commonInfo.getThreadNum();
            int offset = this.offSetInfo.getLoopInfo();
            byteBuf.position(offset);

            for (int i = 0; i < threadNum; i++) {

                if (byteBuf.position() >= this.offSetInfo.getCallGraphInfo()) {
                    break;
                }
                ArrayList<LoopInfo> threadLoopList = new ArrayList<LoopInfo>();
                int symbolNum = getInt(byteBuf);

                for (int j = 0; j < symbolNum; j++) {
                    LoopInfo newLoopInfo = new LoopInfo();

                    newLoopInfo.setSampNum(getFloat(byteBuf));
                    newLoopInfo.setBarrierSyncWaitNum(getFloat(byteBuf));
                    newLoopInfo.setMpiLibCostNum(getFloat(byteBuf));
                    newLoopInfo.setLineLoopStart(getInt(byteBuf));
                    newLoopInfo.setLineLoopEnd(getInt(byteBuf));
                    newLoopInfo.setNestLevel(getInt(byteBuf));
                    newLoopInfo.setLoopType(getShort(byteBuf));
                    newLoopInfo.setParallelInfo(getShort(byteBuf));
                    newLoopInfo.setSymbolIndex(getInt(byteBuf));
                    newLoopInfo.setFileIndex(getInt(byteBuf));

                    threadLoopList.add(newLoopInfo);
                }
                newLoopInfoList.add(threadLoopList);
            }
        }
        return newLoopInfoList;
    }

    /*コールグラフ情報の読み込み*/
    private ArrayList<CallGraphInfo> readCallGraphInfo(ByteBuffer byteBuf) {
        ArrayList<CallGraphInfo> newCallGraphInfoList = new ArrayList<CallGraphInfo>();

        if (this.commonInfo.isOptCallGraph() && this.offSetInfo.getCallGraphInfo() > 0) {
            int threadNum = this.commonInfo.getThreadNum();
            int offset = this.offSetInfo.getCallGraphInfo();
            byteBuf.position(offset);

            for (int i = 0; i < threadNum; i++) {
                CallGraphInfo newCallGraphInfo = new CallGraphInfo();

                if (byteBuf.remaining() < ProfilerReaderUtil.SIZEOF_FLOAT) {
                    break;
                }
                newCallGraphInfo.setTotalSumSampNum(getFloat(byteBuf));
                int stackNum = getInt(byteBuf);
                ArrayList<StackInfo> newStackInfoList = new ArrayList<StackInfo>();

                for (int j = 0; j < stackNum; j++) {
                    StackInfo newStackInfo = new StackInfo();
                    newStackInfo.setNestLevel(getInt(byteBuf));
                    newStackInfo.setSampNum(getFloat(byteBuf));
                    newStackInfo.setSumSampNum(getFloat(byteBuf));
                    int symbNameLength = getInt(byteBuf);
                    newStackInfo.setSymbolName(getString(byteBuf, symbNameLength));

                    newStackInfoList.add(newStackInfo);
                }
                newCallGraphInfo.setStackInfo(newStackInfoList);
                newCallGraphInfoList.add(newCallGraphInfo);
            }
        }
        return newCallGraphInfoList;
    }

    /**
     * プロファイラファイルから読み込みを行う
     * @param profilerfile		プロファイラファイル
     * @throws IOException		読込エラー
     */
    @Override
    public void readFile(File profilerfile) throws Exception {
        readFile(profilerfile, this.endian);
    }

    /**
     * エンディアンを設定する
     * @param endian		エンディアン設定
     */
    @Override
    public void setEndian(int endian) {
        this.endian = endian;
    }

    /**
     * エンディアンを取得する
     */
    @Override
    public int getEndian() {
        return this.endian;
    }


    /**
     * コスト情報リスト:ラインを取得する
     * @return		コスト情報リスト:ライン
     */
    @Override
    public ProfilerDprofData[] getCostInfoLine() {
        if (this.lineInfoList == null) return null;

        // スレッドの積算を行う
        Map<CodeLine, ProfilerDprofData> listCost = new LinkedHashMap<CodeLine, ProfilerDprofData>();
        int threadid = 0;
        for (List<LineInfo> list : lineInfoList) {
            for (LineInfo info : list) {
                float sampNum = info.getSampNum();
                int lineNo = info.getLineNo();
                int symbolIndex = info.getSymbolIndex();
                int fileIndex = info.getFileIndex();

                // シンボル名を取得する
                SymbolInfo symbol = this.symbolInfoList.getSymbolInfo(threadid, symbolIndex);
                String symbolname = symbol.getSymbolName();

                // ファイル名を取得する
                String filename = null;
                if (fileIndex >= 0) {
                    FileInfo file = this.fileInfoList.getFileInfo(fileIndex);
                    filename = file.getFileName();
                }

                // コスト情報
                ProfilerDprofData cost = createProfilerCostInfo(sampNum, symbolname, lineNo, lineNo, filename);
                // コスト情報を追加する
                addProfilerCostInfo(listCost, cost);
            }
            threadid++;
        }

        if (listCost.size() <= 0) return null;
        return listCost.values().toArray(new ProfilerDprofData[0]);
    }

    /**
     * コスト情報リスト:ループを取得する
     * @return		コスト情報リスト:ループ
     */
    @Override
    public ProfilerDprofData[] getCostInfoLoop() {

        if (this.loopInfoList == null) return null;

        // スレッドの積算を行う
        Map<CodeLine, ProfilerDprofData> listCost = new LinkedHashMap<CodeLine, ProfilerDprofData>();
        int threadid = 0;
        for (List<LoopInfo> list : loopInfoList) {
            for (LoopInfo info : list) {
                float sampNum = info.getSampNum();
                int linenoStart = info.getLineLoopStart();
                int linenoEnd = info.getLineLoopEnd();
                int symbolIndex = info.getSymbolIndex();
                int fileIndex = info.getFileIndex();
                int nest = info.getNestLevel();

                // シンボル名を取得する
                SymbolInfo symbol = this.symbolInfoList.getSymbolInfo(threadid, symbolIndex);
                String symbolname = symbol.getSymbolName();

                // ファイル名を取得する
                String filename = null;
                if (fileIndex >= 0) {
                    FileInfo file = this.fileInfoList.getFileInfo(fileIndex);
                    filename = file.getFileName();
                }

                // コスト情報
                ProfilerDprofData cost = createProfilerCostInfo(sampNum, symbolname, linenoStart, linenoEnd, filename);
                // コスト情報を追加する
                addProfilerCostInfo(listCost, cost);
            }
            threadid++;
        }

        if (listCost.size() <= 0) return null;
        return listCost.values().toArray(new ProfilerDprofData[0]);
    }


    /**
     * コスト情報を生成する
     * @param sampling		サンプリング回数
     * @param symbolname	シンボル名
     * @param linenostart	開始行番号
     * @param linenoend		終了行番号
     * @param filename		ファイル名
     * @return				生成コスト情報
     */
    private ProfilerDprofData createProfilerCostInfo(float sampling, String symbolname, int linenostart, int linenoend, String filename) {
        // コスト情報
        ProfilerDprofData cost = new ProfilerDprofData();
        cost.setSampling(sampling);
        cost.setSymbol(symbolname);
        // コード行
        CodeLine line = new CodeLine(null, linenostart, linenoend, filename);
        if (filename != null) {
        	line.setSourceFile(new SourceFile(filename));
        }
        cost.setCodeLine(line);

        return cost;
    }

    /**
     * コスト情報を追加する.
     * 追加済みの場合は、サンプリング回数を積算する
     * @param listCost		追加コスト情報マップ
     * @param costinfo		追加コスト情報
     */
    private void addProfilerCostInfo(Map<CodeLine, ProfilerDprofData> listCost, ProfilerDprofData costinfo) {
        if (listCost == null) return;
        if (costinfo == null) return;

        // コスト情報
        CodeLine line = costinfo.getCodeLine();
        String symbolname = costinfo.getSymbol();
        float sampling = costinfo.getSampling();
        if (line == null) return;

        boolean find = false;
        if (listCost.containsKey(line)) {
            ProfilerDprofData srccost = listCost.get(line);
            if (symbolname != null && symbolname.equals(srccost.getSymbol())) {
                // 一致
                find = true;
                // サンプリング回数を積算する
                float srcsampling = srccost.getSampling();
                srcsampling += sampling;
                srccost.setSampling(srcsampling);
            }
        }
        if (!find) {
            // 新規追加
            listCost.put(line, costinfo);
        }

        return;
    }


    /**
     * コスト情報リスト:手続を取得する
     * @return		コスト情報リスト:手続
     */
    @Override
    public ProfilerDprofData[] getCostInfoProcedure() {
        if (this.symbolInfoList == null) return null;

        // スレッドの積算を行う
        Map<CodeLine, ProfilerDprofData> listCost = new LinkedHashMap<CodeLine, ProfilerDprofData>();
        int threadid = 0;
        for (SymbolList list : this.symbolInfoList.getSymbolRecord()) {
            for (SymbolInfo info : list.getSymbolList()) {
                float sampNum = info.getSampNum();
                int linenoStart = info.getLineSymbolStart();
                int linenoEnd = info.getLineSymbolEnd();
                int fileIndex = info.getFileIndex();
                String symbolname = info.getSymbolName();

                // ファイル名を取得する
                String filename = null;
                if (fileIndex >= 0) {
                    FileInfo file = this.fileInfoList.getFileInfo(fileIndex);
                    filename = file.getFileName();
                }

                // コスト情報
                ProfilerDprofData cost = createProfilerCostInfo(sampNum, symbolname, linenoStart, linenoEnd, filename);
                // コスト情報を追加する
                addProfilerCostInfo(listCost, cost);
            }
            threadid++;
        }

        if (listCost.size() <= 0) return null;
        return listCost.values().toArray(new ProfilerDprofData[0]);
    }


    /**
     * コールグラフ情報を取得する
     * @return		コールグラフ情報
     */
    @Override
    public ProfilerDprofData[] getDprofCallGraphInfo() {
        if (this.callGraphInfoList == null) return null;

        // スレッドのすべてのコールグラフを１つにする
        List<ProfilerDprofData> listCall = new ArrayList<ProfilerDprofData>();
        for (CallGraphInfo list : this.callGraphInfoList) {
            List<ProfilerDprofData> listThread = new ArrayList<ProfilerDprofData>();
            // スレッド毎に積算する
            float sum = 0;
            for (StackInfo info : list.getStackInfo()) {
                float sampling = info.getSampNum();
                int nestLevel = info.getNestLevel();
                String symbolname = info.getSymbolName();
                sum += sampling;
                // コールグラフ情報
                ProfilerDprofData callinfo = new ProfilerDprofData();
                callinfo.setSymbol(symbolname);
                callinfo.setSampling(sampling);
                callinfo.setNestLevel(nestLevel);

                // コスト情報を追加する
                listThread.add(callinfo);
            }
            // 積算値をセットする
            for (ProfilerDprofData data : listThread) {
                data.setSumSampling(sum);
                data.setRatio(data.getSampling()/sum);
            }
            listCall.addAll(listThread);
        }

        if (listCall.size() <= 0) return null;
        return listCall.toArray(new ProfilerDprofData[0]);
    }


    /**
     * EProf:イベントカウンタ情報を取得する
     * @return		EProf:イベントカウンタ情報
     */
    @Override
    public ProfilerEprofData[] getEprofEventCounterInfo() {
        return null;
    }

    /**
     * 読込プロファイラファイル
     * @return 読込プロファイラファイル
     */
    @Override
    public File getProfFile() {
        return this.profFile;
    }

    /**
     * プロファイラマジックキーを取得する
     * @return		マジックキー
     */
    @Override
    public String getFileType() {
        return FILE_ID_DPROF;
    }

    /**
     * PAイベント指定値(EPRFのみ)を取得する.
     *     Cache
     *     Instructions
     *     MEM_access
     *     Performance
     *     Statistics
     * @return 	PAイベント指定値(EPRFのみ)
     */
    @Override
    public String getPaEventName() {
        return null;
    }
}


