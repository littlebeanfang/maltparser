package org.maltparser.parser.algorithm.nivre;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Oracle;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;
/**
 * @author Johan Hall
 *
 */
public class ArcEagerOracle extends Oracle {

	public ArcEagerOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
		super(manager, history);
		setGuideName("ArcEager");
	}
	
	public GuideUserAction predict(DependencyStructure gold, ParserConfiguration config) throws MaltChainedException {
		NivreConfig nivreConfig = (NivreConfig)config;
		DependencyNode stackPeek = nivreConfig.getStack().peek();
		int stackPeekIndex = stackPeek.getIndex();
		int inputPeekIndex = nivreConfig.getInput().peek().getIndex();
		
		if (!stackPeek.isRoot() && gold.getTokenNode(stackPeekIndex).getHead().getIndex() == inputPeekIndex) {
			return updateActionContainers(ArcEager.LEFTARC, gold.getTokenNode(stackPeekIndex).getHeadEdge().getLabelSet(), null);
		} else if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == stackPeekIndex) {
			return updateActionContainers(ArcEager.RIGHTARC, gold.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet(), null);
		} else if (nivreConfig.getRootHandling() == NivreConfig.STRICT && !stackPeek.hasHead()) {
			return updateActionContainers(ArcEager.SHIFT, null, null);
		} else if (gold.getTokenNode(inputPeekIndex).hasLeftDependent() &&
				gold.getTokenNode(inputPeekIndex).getLeftmostDependent().getIndex() < stackPeekIndex) {
			return updateActionContainers(ArcEager.REDUCE, null, null);
		} else if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() < stackPeekIndex && 
				(!gold.getTokenNode(inputPeekIndex).getHead().isRoot() || nivreConfig.getRootHandling() == NivreConfig.NORMAL)) {
			return updateActionContainers(ArcEager.REDUCE, null, null);
		} else {
			return updateActionContainers(ArcEager.SHIFT, null, null);
		}
	}
	
	public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {}
	
	public void terminate() throws MaltChainedException {}
}
