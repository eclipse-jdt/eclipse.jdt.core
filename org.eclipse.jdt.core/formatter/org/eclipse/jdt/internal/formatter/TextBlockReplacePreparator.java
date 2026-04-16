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
		if (this.options.put_text_block_quotes_on_new_line && block instanceof TokenTextBlock) {
			if ( needFormat(block)) {
				((TokenTextBlock)block).addNewlineReplace();
			}
		}
		return true;
	}

	public boolean needFormat(Token block) {
		for (int i = block.originalEnd-3; i > block.originalStart; i--) {
			char ch = this.tokenManager.charAt(i);
			if ( ch == ' ' || ch == '\t') continue;
			if ( ch == '\n' ) {
				return false;
			} else {
				break;
			}
		}
		return true;
	}

}
