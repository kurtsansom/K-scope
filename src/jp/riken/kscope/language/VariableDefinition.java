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

package jp.riken.kscope.language;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jp.riken.kscope.data.CodeLine;
import jp.riken.kscope.information.InformationBlock;
import jp.riken.kscope.information.InformationBlocks;
import jp.riken.kscope.information.TextInfo;
/**
 * 変数・構造体の宣言を表現するクラス。
 */
public class VariableDefinition implements Serializable, IInformation, IBlock {
	/** シリアル番号 */
	private static final long serialVersionUID = 8694203337559301954L;
    /** 変数名. */
    private String name;
    /** データ型. */
    private IVariableType type;
    /** 属性. */
    private IVariableAttribute attribute;
    /** 配列要素. */
    private VariableDimension dimension;
    /** 初期値. */
    private String initValue;
    /** 型宣言の開始位置情報. */
    private Statement start;
    /** 型宣言の終了位置情報. */
    private Statement end;
    /** 付加情報. */
    private TextInfo information = null;
    /** USE文によって自身を参照・定義しているプログラム単位の集合. */
    private transient Set<ProgramUnit> referMembers = new HashSet<ProgramUnit>();
    /** 本宣言を保持しているプログラム単位. */
    private ProgramUnit mother;

    /**
     * コンストラクタ。
     *
     * @param line
     *            行情報
     */
    public VariableDefinition(CodeLine line) {
        start = new Statement(line);
        end = new Statement(line);
    }

    /**
     * コンストラクタ
     *
     * VariableDefinition(String nm, VariableType typ, VariableAttribute
     * attrbts)<br>
     * あるいは<br>
     * VariableDefinition(String nm, VariableType typ, VariableAttribute
     * attrbts, VariableDimension dmnsn)<br>
     * を使用するようにしてください。
     *
     * @param varnm
     *            変数名
     */
    public VariableDefinition(String varnm) {
        name = varnm;
    }

    /**
     * コンストラクタ.
     *
     * @param nm
     *            変数名
     * @param typ
     *          型
     * @param attrbts
     *          属性
     */
    public VariableDefinition(String nm, IVariableType typ,
            IVariableAttribute attrbts) {
        this.name = nm;
        this.type = typ;
        this.attribute = attrbts;
    }

    /**
     * コンストラクタ.
     *
     * @param nm
     *          変数名
     * @param typ
     *          型
     * @param attrbts
     *          属性
     * @param dmnsn
     *          配列情報
     */
    public VariableDefinition(String nm, IVariableType typ,
            IVariableAttribute attrbts, VariableDimension dmnsn) {
        this(nm, typ, attrbts);
        this.dimension = dmnsn;
    }
    /**
     * ブロックタイプの取得。
     *
     * @return BlockType.VARIABLEDEFINITION
     */
    @Override
    public BlockType getBlockType() {
        return BlockType.VARIABLEDEFINITION;
    }

    /**
     * データ型を設定する。
     *
     * @param tp
     *            データ型
     */
    public void setVariableType(IVariableType tp) {
        type = tp;
    }

    /**
     * 属性をセットする。
     *
     * @param att
     *            属性
     */
    public void setVariableAttributes(IVariableAttribute att) {
        attribute = att;
    }

    /**
     * 配列要素の下限、上限を設定する。
     *
     * @param i
     *            次元
     * @param startIndex
     *            下限
     * @param endIndex
     *            上限
     */
    public void setDimensionIndex(int i, Expression startIndex, Expression endIndex) {
        setStartIndex(i, startIndex);
        setEndIndex(i, endIndex);
    }

    /**
     * 配列要素の下限を設定する。
     *
     * @param i
     *            次元
     * @param startIndex
     *            下限
     */
    public void setStartIndex(int i, Expression startIndex) {
        dimension.getIndex(i).set_start(startIndex);
    }

    /**
     * 配列要素の上限を設定する。
     *
     * @param i
     *            次元
     * @param indexEnd
     *            上限
     */
    public void setEndIndex(int i, Expression indexEnd) {
        dimension.getIndex(i).set_end(indexEnd);
    }

    /**
     * 自身を参照しているプログラム単位を追加する.<br>
     *
     * @param proc
     *            手続き
     */
    public void addReferMember(ProgramUnit proc) {
        if (proc == null) {
            return;
        }
        this.getReferMember().add(proc);
    }

    /**
     * 自身を参照しているプログラム単位の集合を返す。
     *
     * @return 手続きの集合。存在しない場合は空の集合を返す。
     */
    public Set<ProgramUnit> getReferMember() {
        if (this.referMembers == null) {
            return new HashSet<ProgramUnit>();
        }
        return this.referMembers;
    }

    /**
     * 変数名を取得する。
     *
     * @return 変数名
     */
    public String get_name() {
        return (name);
    }

    /**
     * データ型を取得する。
     *
     * @return データ型
     */
    public IVariableType getType() {
        return type;
    }

    /**
     * 属性を取得する
     *
     * @return 属性
     */
    public IVariableAttribute getAttribute() {
        return attribute;
    }

    /**
     * 配列要素の次元数を取得する
     *
     * @return 配列要素次元数
     */
    public int get_dimension_size() {
        if (dimension == null || dimension.getIndex() == null)
            return 0;
        return (dimension.getIndex().length);
    }

    /**
     * 配列要素から指定次元の下限を取得する
     *
     * @param i
     *            次元数
     * @return 次元下限
     */
    public Expression get_index_start(int i) {
        return (dimension.getIndex(i).get_start());
    }

    /**
     * 配列要素から指定次元の上限を取得する
     *
     * @param i
     *            次元数
     * @return 次元上限
     */
    public Expression get_index_end(int i) {
        return (dimension.getIndex(i).get_end());
    }

    /**
     * 配列要素を設定する。
     *
     * @param dimension
     *            配列要素
     */
    public void setDimension(VariableDimension dimension) {
        this.dimension = dimension;
    }

    /**
     * 初期値を設定する。
     *
     * @param value
     *            初期値
     */
    public void setInitValue(String value) {
        this.initValue = value;
    }

    /**
     * 初期値を取得する
     *
     * @return 初期値
     */
    public String getInitValue() {
        return this.initValue;
    }

    @Override
    public String toString() {
        String info = "";
        // delete by @hira at 2013/03/01
//        if (this.getInformation() != null) {
//            if (!(this.getInformation().getContent().equals(""))) {
//                info = "[ ! ] ";
//            }
//        }
        return (info + this.toStringBase());
    }

    /**
     * 変数宣言の文字列表現を返す。
     *
     * @return 変数宣言の文字列表現
     */
    protected String toStringBase() {
        StringBuilder var = new StringBuilder();

        if (type != null) {
            // データ型
            var.append(type.toString());
        }

        if (dimension != null) {
            var.append(",dimension(");
            String dims = "";
            for (int i = 0; i < dimension.get_index_size(); i++) {
                String start = dimension.get_index_start(i).toString();
                String end = dimension.get_index_end(i).toString();
                if (start != null) {
                    dims += start;
                }
                dims += ":";
                if (end != null) {
                    dims += end;
                }
                if (i + 1 < dimension.get_index_size()) {
                    dims += ",";
                }
            }
            var.append(dims);
            var.append(")");
        }

        // 属性
        if (attribute != null) {
          Iterator<String> itr = attribute.getAttributes().iterator();
            while (itr.hasNext()) {
                String attr = itr.next();
                var.append(",");
                var.append(attr);
            }
        }
        var.append(" ");

        // 変数名
        var.append("::");
        var.append(name);

        // 初期値
        if (initValue != null) {
            var.append("=");
            var.append(initValue);
        }
        return var.toString();
    }

    /**
     * 型が適合しているかどうか。<br>
     *
     * 多重定義されている関数群の中から対応する関数を探索する際に、<br>
     * 仮引数と実引数の型チェックをする必要がある。<br>
     * 「適合している」とは、この型チェックで、同一の型と判定される 事を意味している。
     *
     * @param actualArgument
     *
     * @return true : 適合している<br>
     *         false: 適合していない
     *
     */
    public boolean matches(Expression actualArgument) {
        if (actualArgument == null) { return false; }

        // modify by @hira at 2013/02/01
        if (actualArgument.getType() == null) {
        	return false;
        }
        if (this.getType() == null) {
        	return false;
        }
        return actualArgument.getType().matches(this.getType());
    }
    /**
     * 付加情報を設定する
     *
     * @param info
     *            付加情報
     */
    @Override
    public void setInformation(TextInfo info) {
        this.information = info;
    }

    /**
     * 付加情報を取得する
     *
     * @return 付加情報
     */
    @Override
    public TextInfo getInformation() {
        return this.information;
    }


    /**
     * 開始行番号情報を取得する
     * @return      開始行番号情報
     */
    @Override
    public CodeLine getStartCodeLine() {
        if (start == null) return null;
        return start.lineInfo;
    }
    /**
     * 終了行番号情報を取得する
     * @return      終了行番号情報
     */
    @Override
    public CodeLine getEndCodeLine() {
        if (end == null) return null;
        return end.lineInfo;
    }


    /**
     * 行情報を設定する.
     *
     * @param line
     *            行情報
     */
    public void setCodeLine(CodeLine line) {
        start = new Statement(line);
        end = new Statement(line);
    }

    /**
     * データ型を取得する。
     *
     * @return データ型
     */
    public IVariableType getVariableType() {
        return type;
    }

    /**
     * 配列情報を取得する。
     *
     * @return 配列情報
     */
    public VariableDimension getVariableDimension() {
        return this.dimension;
    }

    /**
     * スカラーならば真を返す。
     *
     * @return 真偽値：スカラーならば真
     */
    public boolean isScalar() {
        if (this.dimension == null) {
            return true;
        }
        return false;
    }


    /**
     * 親プログラムをセットする。
     * @param mother 親プログラム
     */
    public void setMother(ProgramUnit mother) {
        this.mother = mother;
    }

    /**
     * 親プログラム単位を習得する。
     *
     * @return 親プログラム単位
     */
    public ProgramUnit getMother() {
        return this.mother;
    }
    /**
     * 名前空間（モジュール名.ルーチン名）を取得する。
     *
     * @return 名前空間（モジュール名.ルーチン名）
     */
    @Override
    public String getNamespace() {
        String result = "";
        if (this.mother != null) {
            result = mother.getNamespace();
        }
        return result;
    }

    /**
     * 開始位置を取得する。
     *
     * @return 開始位置
     */
    @Override
    public int getStartPos() {
        return this.getStartCodeLine().getStartLine();
    }
    /**
     * 開始位置を設定する。
     *
     * @param pos
     *         開始位置
     */
    @Override
    public void setStartPos(int pos) {
        this.getStartCodeLine().setLine(pos);
    }

    /*
     * TODO: 暫定対応。
     *       本当はプログラムの終了はprogram.getEndCodeLine.getEndLineで
     *       取得するか、programのEndCodeLineを削除し、StartCodeLineを
     *       CodeLineと名称変更すべき。要検討。
     */

    /**
     * 終了位置を取得する。
     *
     * @return 終了位置
     */
    @Override
    public int getEndPos() {
        return this.getStartCodeLine().getEndLine();
    }
    /**
     * 終了位置を設定する。
     *
     * @param pos
     *         終了位置
     */
    @Override
    public void setEndPos(int pos) {
        this.getStartCodeLine().setEndLine(pos);
    }

    /**
     * idにマッチした情報ブロックを検索する。
     * @param id
     *          ID
     * @return 見つかった情報ブロック。見つからなかった場合はnullが返ります。
     */
    public IInformation findInformationBlockBy(String id) {
        IInformation result = null;

        if (this.getID().equals(id)) {
            result = this;
        }

        return result;
    }

    /**
     * 付加情報をすべて削除する。
     */
    @Override
    public void clearInformation() {
        this.setInformation(null);
    }

    /**
     * 付加情報コンテナコレクションを生成する。
     *
     * @return 付加情報コンテナコレクション
     */
    public InformationBlocks createInformationBlocks() {
        InformationBlocks result = new InformationBlocks();

        if (this.information != null) {
            InformationBlock cont = new InformationBlock(this.information, this, this);
            result.add(cont);
        }

        return result;
    }

    /**
     * IDを取得する。
     *
     * @return ID
     */
    @Override
    public String getID() {
        String result = "";
        if (this.mother != null) {
            int offset = this.getStartPos() - this.mother.getStartPos();
            result
              = this.mother.getID() + "$" + offset + ":" + this.toStringBase();
        } else {
            result = this.toStringBase();
        }
        return result;
    }
    /**
     * 親ブロックを取得する
     * @return        親ブロック
     */
    @Override
    public IBlock getMotherBlock() {
        return this.getMother();
    }

	/**
	 * 同一VariableDefinitionであるかチェックする.
	 * 変数宣言の文字列表現にて同一かチェックする.
	 * @param definition		変数・構造体の宣言
	 * @return		true=一致
	 */
	public boolean equalsBlocks(VariableDefinition definition) {
	     // 変数宣言の文字列表現にて同一かチェックする.
		String thisVar = toStringBase();
		String destVar = definition.toStringBase();
		if (thisVar == null && destVar == null) {
			return true;
		}
		else if (thisVar == null) {
			return false;
		}
		return thisVar.equalsIgnoreCase(destVar);
	}


    /**
     * 同一ブロックを検索する
     *
     * @param block    IInformationブロック
     * @return 同一ブロック
     */
    public IInformation[] searchInformationBlocks(IInformation block) {
        List<IInformation> list = new ArrayList<IInformation>();
        if (block instanceof VariableDefinition) {
        	if (this.equalsBlocks((VariableDefinition)block)) {
                list.addAll(Arrays.asList(this));
            }
        }
        if (list.size() <= 0) {
            return null;
        }
        return list.toArray(new IInformation[0]);
    }

    /**
     * 構造IDを取得する.
     * 構造IDは不要であるので、nullを返す.
     * @return 構造ID
     */
    @Override
    public String getLayoutID() {
        return null;
    }


	/**
	 * 行番号のブロックを検索する
	 * @param line			行番号
	 * @return		行番号のブロック
	 */
	public IBlock[] searchCodeLine(CodeLine line) {
		if (line == null) return null;
		if (line.getSourceFile() == null) return null;
		if (this.getStartCodeLine() == null) return null;
		if (!line.getSourceFile().equals(this.getStartCodeLine().getSourceFile())) return null;

		List<IBlock> list = new ArrayList<IBlock>();
		CodeLine thisstart = this.getStartCodeLine();
		CodeLine thisend = this.getEndCodeLine();
		if ( line.isOverlap(thisstart, thisend) ) {
			list.add(this);
		}

        if (list.size() <= 0) {
        	return null;
        }

		return list.toArray(new IBlock[0]);
	}

 	/**
 	 * 変数リストを取得する.
 	 */
 	@Override
 	public Set<Variable> getAllVariables() {
 		return null;
 	}
}
