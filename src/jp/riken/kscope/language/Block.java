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
import java.util.List;
import java.util.Set;

import jp.riken.kscope.data.CodeLine;
import jp.riken.kscope.information.InformationBlock;
import jp.riken.kscope.information.InformationBlocks;
import jp.riken.kscope.information.TextInfo;

/**
 * 処理ブロックを表現する抽象クラス。
 *
 * @author RIKEN
 *
 */
public abstract class Block implements Serializable, IInformation, IBlock {
    /** シリアル番号 */
    private static final long serialVersionUID = 4754517459536460336L;
    /** 親ブロック */
    private Block mother;
    /** 開始行情報 */
    private Statement start;
    /** 終了行情報 */
    private Statement end;
    /** 子ブロック */
    // TODO childrenを常にnewするのはやめるべきか要検討
    private ArrayList<Block> children = new ArrayList<Block>();
    /** 付加情報 */
    // TODO 常にnullで初期化するのはやめるべきか要検討
    private TextInfo information = null;

    /**
     * コンストラクタ。
     */
    protected Block() {
    }

    /**
     * コンストラクタ。
     *
     * @param mama
     *            親ブロック
     */
    public Block(Block mama) {
        this.mother = mama;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++
    /**
     * 開始コード行情報を設定する。
     *
     * @param lineInfo
     *            開始コード行情報
     */
    protected void set_block_start(CodeLine lineInfo) {
        start = new Statement(lineInfo);
    }

    /**
     * 終了コード行情報を設定する。
     *
     * @param lineInfo
     *            終了コード行情報
     */
    protected void set_block_end(CodeLine lineInfo) {
        end = new Statement(lineInfo);
    }

    /**
     * ブロックの文字列表現を取得する.
     */
    @Override
    public String toString() {
        // delete by @hira at 2013/03/01  付加情報登録ブロックは[!]から赤色文字に変更の為削除
//         String info = "";
//        if (this.getInformation() != null) {
//            if (!(this.getInformation().getContent().equals(""))) {
//                info = "[ ! ] ";
//            }
//        }
        //return (info + this.toStringBase());
    	return this.get_start_str();
    }

    /**
     * ブロックの基本文字列表現を取得する.
     * @return  ブロックの文字列表現
     */
    protected String toStringBase() {
        //return this.get_start_str().toLowerCase();
    	return this.get_start_str();
    }

    /**
     * 親ブロックを設定する。
     * @param mama		親ブロック
     */
    protected void set_mother(Block mama) {
        mother = mama;
    }

    /**
     * 親ブロックを返す。
     *
     * @return 親ブロック
     */
    public Block get_mother() {
        return mother;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++
    /**
     * 開始Statementを返す。
     *
     * @return 開始Statement
     */
    public Statement get_start() {
        return start;
    }

    /**
     * 終了Statementを返す。
     *
     * @return 終了Statement
     */
    public Statement get_end() {
        return end;
    }

    // ++++++++++++++++++++++++++++++++++++++++++++
    /**
     * ブロックの開始行の文字列表現を返す。
     *
     * @return 開始行の文字列表現
     */
    public String get_start_str() {
        return start.get_statement();
    }

    /**
     * ブロックの終了行の文字列表現を返す。
     *
     * @return 終了行の文字列表現
     */
    public String get_end_str() {
        return end.get_statement();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++

    protected int get_num_of_child() {
        return children.size();
    }

    // ++++++++++++++++++++++++++++++++++++++++++++

    protected void add_child(Block child) {
        child.set_mother(this);
        children.add(child);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++

    protected Block get_child(int i) {
        return children.get(i);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++
    /**
     * 子要素を返す。
     *
     * @return 子要素。無ければ空のリストを返す
     */
    public ArrayList<Block> getChildren() {
        return children;
    }

    protected void getCalls(List<ProcedureUsage> calls) {
        for (int i = 0; i < get_num_of_child(); i++) {
            Block child = this.get_child(i);
            if (child instanceof ProcedureUsage) {
                calls.add((ProcedureUsage) child);
            } else if (child instanceof Selection) {
                for (int j = 0; j < ((Selection) (child))
                        .getNumOfConditions(); j++) {
                    Block child_block = ((((Selection) child)
                            .getConditions()
                            .get(j)));
                    child_block.getCalls(calls);
                }

            } else if (child instanceof Substitution){
                List<ProcedureUsage> funcCalls = ((Substitution)child).getRightValue().getFuncCalls();
                for (ProcedureUsage call:funcCalls) {
                    calls.add(call);
                }
            } else {
                child.getCalls(calls);
            }
        }
    }
    // ++++++++++++++++++++++++++++++++++++++++++++
    protected void get_blocks(List<Block> blocks) {
        for (int i = 0; i < get_num_of_child(); i++) {
            Block child = get_child(i);
            if (child instanceof ProcedureUsage) {
                blocks.add(child);
            } else {
                blocks.add(child);
            }
        }
    }


    // -----------Informations------------//
    @Override
    public void setInformation(TextInfo info) {
        this.information = info;
    }

    /**
     * 付加情報を取得する
     * @return		付加情報
     */
    @Override
    public TextInfo getInformation() {
        return this.information;
    }

    /**
     * 開始行番号情報を取得する
     *
     * @return		開始行番号情報
     */
    @Override
    public CodeLine getStartCodeLine() {
        if (start == null)
            return null;
        return start.lineInfo;
    }

    /**
     * 終了行番号情報を取得する
     *
     * @return		終了行番号情報
     */
    @Override
    public CodeLine getEndCodeLine() {
        if (end == null)
            return null;
        return end.lineInfo;
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
     * @param id    ID
     * @return 見つかった情報ブロック。見つからなかった場合はnullが返ります。
     */
    public IInformation findInformationBlockBy(String id) {
        IInformation result = null;

        if (this.getID().equals(id)) {
            result = this;
        } else {
            ArrayList<Block> blocks = this.getChildren();
            for (Block block : blocks) {
                result = block.findInformationBlockBy(id);
                if (result != null) { break; }
            }
        }

        return result;
    }

    /**
     * 付加情報をすべて削除する。
     */
    @Override
    public void clearInformation() {
        this.setInformation(null);
        for (Block child : this.children) {
            child.clearInformation();
        }
    }

    /**
     * 付加情報ブロックコレクションを生成する。
     *
     * @return 付加情報ブロックコレクション
     */
    public InformationBlocks createInformationBlocks() {
        InformationBlocks result = new InformationBlocks();

        if (this.information != null) {
            InformationBlock block
              = new InformationBlock(this.information, this, this);
            result.add(block);
        }
        if (this.children != null) {
            for (Block block : this.children) {
                result.addAll(block.createInformationBlocks());
            }
        }
        return result;
    }

    /**
     * 自身の子ブロックのリストを返す。
     * @return 子ブロックのリスト。無ければ空のリストを返す。
     */
    public List<Block> getBlocks() {
        return this.children;
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
        	// modify by @hira at 2013/03/01
            // int offset = this.getStartPos() - this.mother.getStartPos();
        	int offset = this.mother.indexOfChildren(this);
            result = this.mother.getID() + "$" + offset + ":" + this.toStringBase();
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
        return this.get_mother();
    }

    /**
     * 同一ブロックであるかチェックする.
     * childrenが同じサイズ、同じ文字列であること.
     * @param block		ブロック
	 * @return		true=一致
     */
	public boolean equalsBlocks(Block block) {
		if (block == null) return false;
		if (this.children == null && block.children == null) {
			return true;
		}
		else if (this.children == null) {
			return false;
		}
		else if (this.children != null && block.children != null) {
			if (this.children.size() != block.children.size()) {
				return false;
			}
		}
		if (!this.toString().equalsIgnoreCase(block.toString())) {
			return false;
		}

		int count = this.children.size();
		for (int i=0; i<count; i++) {
			Block thisChildren = this.children.get(i);
			Block destChildren = block.get_child(i);
			if (thisChildren == destChildren) continue;
			else if (thisChildren == null) {
				return false;
			}
			if (!thisChildren.equalsBlocks(destChildren)) {
				return false;
			}
		}
		return true;
	}

    /**
     * 子ブロックのインデックスを返す.
     * 存在しない場合は、-1を返す。
     * @param block		ブロック
	 * @return			インデックス
     */
	protected int indexOfChildren(Block block) {
		return this.children.indexOf(block);
	}


	/**
	 * 同一ブロックを検索する
	 * @param block			IInformationブロック
	 * @return		同一ブロック
	 */
	public IInformation[] searchInformationBlocks(IInformation block) {
		if (!(block instanceof Block)) {
			return null;
		}
		List<IInformation> list = new ArrayList<IInformation>();
		if (this.equalsBlocks((Block)block)) {
			list.add(this);
		}

        ArrayList<Block> blocks = this.getChildren();
        for (Block blockChildren : blocks) {
        	IInformation[] infos = blockChildren.searchInformationBlocks(block);
	        if (infos != null) {
	        	list.addAll(Arrays.asList(infos));
	        }
        }
        if (list.size() <= 0) {
        	return null;
        }

		return list.toArray(new IInformation[0]);
	}

	/**
	 * 同一ブロック階層であるかチェックする.
	 * @param block		チェック対象Block
	 * @return   true=一致
	 */
	public boolean equalsLayout(Block block) {
		if (block == null) return false;

		String layoutIdThis = this.getLayoutID();
		String layoutIdBlock = block.getLayoutID();
		if (layoutIdThis == null) return false;
		if (!layoutIdThis.equalsIgnoreCase(layoutIdBlock)) {
			return false;
		}

		if (this.children == null && block.children == null) {
			return true;
		}
		else if (this.children == null) {
			return false;
		}
		else if (block.children == null) {
			return false;
		}

		int idThis = 0;
		int idBlock = 0;
		int countThis = this.children.size();
		int countBlock = block.children.size();
		Block thisChildren = null;
		Block blockChildren = null;
		while (true) {
			thisChildren = null;
			blockChildren = null;
			if (idThis < countThis) {
				thisChildren = this.children.get(idThis);
			}
			if (idBlock < countBlock) {
				blockChildren = block.children.get(idBlock);
			}

			if (thisChildren != null) {
				if (thisChildren.getBlockType() != BlockType.SELECTION
					&& thisChildren.getBlockType() != BlockType.REPETITION) {
					idThis++;
					continue;
				}
			}
			if (blockChildren != null) {
				if (blockChildren.getBlockType() != BlockType.SELECTION
					&& blockChildren.getBlockType() != BlockType.REPETITION) {
					idBlock++;
					continue;
				}
			}

			if (idThis >= countThis || idBlock >= countBlock) {
				break;
			}
			if (thisChildren == null && blockChildren == null) {
				break;
			}
			else if (thisChildren == null) {
				idThis++;
				continue;
			}
			else if (blockChildren == null) {
				idBlock++;
				continue;
			}
			if (!thisChildren.equalsLayout(blockChildren)) {
				return false;
			}
			idThis++;
			idBlock++;
		}

		if (thisChildren != null || blockChildren != null) {
			return false;
		}
		return true;
	}


    /**
     * 構造IDを取得する。
     *
     * @return 構造ID
     */
    @Override
    public String getLayoutID() {
        String result = "";

        BlockType type = this.getBlockType();
        String typeText = null;
        if (type == BlockType.REPETITION) {
        	typeText = "do";
        }
        else if (type == BlockType.SELECTION) {
        	if (this instanceof Selection) {
        		if (((Selection)this).isSelect()) {
        			typeText = "select";
        		}
        		else if (((Selection)this).isIF()) {
        			typeText = "if";
        		}
        		else if (((Selection)this).isWHERE()) {
        			typeText = "where";
        		}
        	}
        }
        else {
			typeText = type.name().toLowerCase();
        }
        if (this.mother != null) {
        	int offset = this.mother.indexOfLayout(this);
            result = this.mother.getLayoutID() + "$" + offset + ":" + typeText;
        } else {
            result = typeText;
        }
        return result;
    }

    /**
     * 子ブロックのインデックスを返す.
     * DO, SELECT, IF文の出現回数とする
     * 存在しない場合は、-1を返す。
     * @param block		ブロック
	 * @return			インデックス
     */
    protected int indexOfLayout(Block block) {
		int index = -1;
        for (Block child : this.children) {
            BlockType type = child.getBlockType();
            if (type == BlockType.REPETITION) {
            	index++;
            }
            else if (type == BlockType.SELECTION) {
            	index++;
            }
            if (child == block) {
            	return index;
            }
        }
        return -1;
	}

    /**
     * layoutIDにマッチした構造ブロックを検索する。
     * @param id    layoutID
     * @return 見つかった構造ブロック
     */
    public IInformation findInformationLayoutID(String id) {
    	if (id == null || id.isEmpty()) return null;
        IInformation result = null;
        String layoutId = this.getLayoutID();
        if (layoutId == null) return null;
        if (layoutId.equalsIgnoreCase(id)) {
            result = this;
        } else {
            ArrayList<Block> blocks = this.getChildren();
            for (Block block : blocks) {
                result = block.findInformationLayoutID(id);
                if (result != null) { break; }
            }
        }

        return result;
    }


	/**
	 * 行番号のブロックを検索する
	 * @param line			行番号
	 * @return		行番号のブロック
	 */
	public IBlock[] searchCodeLine(CodeLine line) {
		if (line == null) return null;

		CodeLine thisstart = this.getStartCodeLine();
		CodeLine thisend = this.getEndCodeLine();
		Block addblock = null;
		if ( line.isOverlap(thisstart, thisend) ) {
			addblock = this;
		}
		List<IBlock> list = new ArrayList<IBlock>();
        ArrayList<Block> blocks = this.getChildren();
        if (blocks == null || blocks.size() <= 0) {
        	if (addblock != null) {
        		list.add(addblock);
        	}
        }
		else {
	        for (Block blockChildren : blocks) {
	        	IBlock[] childlist = blockChildren.searchCodeLine(line);
		        if (childlist != null) {
		        	list.addAll(Arrays.asList(childlist));
		        }
	        }
	        if (list.size() <= 0) {
	        	if (addblock != null) {
	        		list.add(addblock);
	        	}
	        }
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
		Set<Variable> list = new HashSet<Variable>();
        ArrayList<Block> blocks = this.getChildren();
        for (Block block : blocks) {
        	Set<Variable> vars = block.getAllVariables();
	        if (vars != null) {
	        	list.addAll(vars);
	        }
        }
        if (list.size() <= 0) return null;
		return list;
	}


}
