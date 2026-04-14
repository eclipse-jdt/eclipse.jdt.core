package org.eclipse.jdt.internal.formatter;

import static org.eclipse.jdt.internal.compiler.parser.TerminalToken.TokenNameTextBlock;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TextBlock;

public class TextBlockReplacePreparator extends ASTVisitor {

	final private TokenManager tokenManager;
	final private DefaultCodeFormatterOptions options;

	public TextBlockReplacePreparator(TokenManager tokenManager, DefaultCodeFormatterOptions options) {
		this.tokenManager = tokenManager;
		this.options = options;
	}

	@Override
	public boolean visit(TextBlock node){
		Token block = this.tokenManager.firstTokenIn(node, TokenNameTextBlock);
		if (this.options.put_new_line_on_text_block && block instanceof TokenTextBlock) {
			if (this.tokenManager.charAt(block.originalEnd-3) != '\n') {
				((TokenTextBlock)block).addNewlineReplace();
			}
		}
		return false;
	}

}
