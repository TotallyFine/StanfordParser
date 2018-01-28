package parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/*
 * 将CoreLabel改造一下使其具有一个List，List的元素是包含这个单词的依存关系
 * 如果可以在生成依存关系的时候将该依存关系加入到相应的单词里
 * */
public class MyDemo {
	public static void main(String[] args) {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		// 文件地址
		String filename = "D:/text.txt";
		// 加载parser
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

		List<Word> words = new ArrayList<Word>();
		
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

			if (gsf != null) {
				// 取出这个语法树的语法结构，类型为edu.stanford.nlp.trees.UniversalEnglishGrammaticalStructure
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				// 从语法结构中获得依存关系，每个依存关系的类型是edu.stanford.nlp.trees.TypedDependency
				// 返回一个Collection包含这个语法结构中所有的依存关系
				Collection tdl = gs.typedDependenciesCCprocessed();
				int id = 0;
				// 遍历所有的依存关系，构建类Word的集合words
				for(Iterator it = tdl.iterator();it.hasNext();){
					System.out.println(it.next());
					// 得到这个依存关系
					TypedDependency td = (TypedDependency) it.next();
				}	
			}
		}
	}
}
