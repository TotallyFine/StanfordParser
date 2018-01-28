package parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class Text {
	public static void main(String[] args) {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		// 文件地址
		String filename = "D:/text.txt";
		// 加载parser
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

		HashMap<String, Tree> wordNodes = new HashMap<>();
		HashMap<Tree, Word> words = new HashMap<>();

		// 根据parser来得到相应语法树的语法包
		TreebankLanguagePack tlp = lp.treebankLanguagePack();
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}

		// 从文件中生成DocumentPreprocessor，从DocumentPreprocessor中得到每句话
		// sentence
		// 是一个List，其中元素的类型为edu.stanford.nlp.ling.CoreLabel，toString方法打印这个单词
		// CoreLabel有setValue(String)
		// 方法可以改变单词，这样可以处理重复的单词，但是在搜索的时候，重复的单词也会被当成同一个吧
		for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {

			// 生成语法树
			Tree parse = lp.apply(sentence);
			// 打印出语法树
			// parse.pennPrint();
			System.out.println();

			// 得到树的每个叶子 即每个单词对应的节点
			List<Tree> leaves = parse.getLeaves();
			// 得到所有的word
			wordNodes.put("ROOT", parse);
			for (Tree node : leaves) {
				wordNodes.put(node.value(), node);
				// 如果是根节点那么初始权重为零
				if (node == parse) {
					words.put(node, new Word(node, 0));
				} else {
					words.put(node, new Word(node, 1));
				}
			}

			if (gsf != null) {
				// 取出这个语法树的语法结构，类型为edu.stanford.nlp.trees.UniversalEnglishGrammaticalStructure
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				// 从语法结构中获得依存关系，每个依存关系的类型是edu.stanford.nlp.trees.TypedDependency
				// 返回一个Collection包含这个语法结构中所有的依存关系
				List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
				int index = 0;
				double weightSum = 0;
				// 当前关系的权重
				double weight = 0;
				// 最大最小权重
				double max = 1;
				double min = 1;
				// 作为包含ROOT节点关系的另一个词语节点的列表
				List<Word> rootlist = new ArrayList<>();
				for (TypedDependency td : tdl) {
					// 从依存关系中得到两个单词对应的节点
					System.out.println(td);
					Tree l1 = wordNodes.get(td.dep().value());
					Tree l2 = wordNodes.get(td.gov().value());
					// 计算每个关系的重要性
					// 如果是case这样的介词关系,对于其中的介词则不增加其重要性，如果是介词关系中的非介词那么就增加
					// 在介词关系中介词处于被支配的地位，是dep
					if (!td.reln().toString().equals("case")) {
						weight = calculate(l1, l2, parse, words, rootlist);
						if (weight > max)
							max = weight;
						if (weight < min)
							min = weight;
						weightSum += weight;
					}else{
						// 如果是case介词，则其关系的重要性只为1，并且在算法中不增加词语的重要性
						// 但是在全部的关系的weight中增加1
						weightSum += 1;						
					}
				}
				//System.out.println("before rootlist weightSum is "+weightSum);
				Random rand = new Random();
				// 给那些包含了ROOT，重要性还没有确定的关系确定重要性
				//System.out.println("in rootlist");
				for (Word w : rootlist) {
					//System.out.println("this word is "+w.t);
					weight = 1 / Math.log(rand.nextDouble() * (max - min) + min);
					weightSum += weight;
					w.weight += weight;
				}
				//System.out.println("weight sum is" + weightSum);
				for (Word w : words.values()) {
					w.weight *= tdl.size();
					w.weight /= weightSum;
					System.out.println(w.t.value() + w.weight);
				}
			}
		}
	}

	public static double calculate(Tree leaf1, Tree leaf2, Tree root, HashMap<Tree, Word> words, List<Word> rootlist){
		int d1 = 0;
		int d2 = 0;
		// 特殊情况1 对于root关系
		// 应该把跟root有语法关系的词的di设置为0
		// root的di设置为它到与之有语法关系的词之间的距离
		if (leaf1 == root || leaf2 == root) {
			if (leaf1 == root)
				rootlist.add(words.get(leaf2));
			else
				rootlist.add(words.get(leaf1));
			return 1;
		} else {
			// 正常情况
			// 从leaf1一路向上寻找 直到找到第一个公共祖先
			// d1 和 d2 即为到公共祖先的距离
			B: for (Tree node1 = leaf1.parent(root); node1 != null; node1 = node1.parent(root)) {
				d1++;
				for (Tree node2 = leaf2.parent(root); node2 != null; node2 = node2.parent(root)) {
					d2++;
					if (node1 == node2) {
						break B;
					}
				}
				d2 = 0;
			}
			// 根据叶节点的值和其到公共祖先的距离构建单词
			int d = d1 + d2;
			Word w1 = words.get(leaf1);
			Word w2 = words.get(leaf2);
			// 这个关系的重要性
			double weight = 1 + 1/Math.log(d);
			// System.out.println(weight);
			// System.out.println(d1+" "+d2);
			// System.out.println(w2.t);

			// 增加关系中的两个节点的重要性
			double weight1 = (double) d2 / d * weight;
			double weight2 = (double) d1 / d * weight;
			// System.out.println(weight1+" "+weight2);
			w1.weight += weight1;
			w2.weight += weight2;
			// System.out.println(w1.t+" "+w1.weight+" "+w2.t+" "+w2.weight);
			//System.out.println("this weight is "+weight);
			return weight;
		}
	}
}
