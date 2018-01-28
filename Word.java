package parse;

import edu.stanford.nlp.trees.Tree;

public class Word{
	public double weight;
	public Tree t;
	public Word(Tree t, double weight){
		this.t = t;
		this.weight = weight;
	}
}
